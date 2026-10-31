-- ====================================================================================
-- RoyalMedico Migration: V4__pharmacy_onboarding_and_governance.sql
-- Module: Stage 5 — Super Admin Registration, Identity Registry, Owner Governance & Invariants
-- Target DB: PostgreSQL 17.5 (auth_user)
-- Status: PROPOSED (Requires user sign-off prior to execution against live database)
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- STEP 1: CREATE TABLE pharmacy_registrations
-- ------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pharmacy_registrations (
    id BIGSERIAL PRIMARY KEY,
    legal_business_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255) NOT NULL,
    proposed_slug VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    proposed_owner_email VARCHAR(255) NOT NULL,
    proposed_owner_phone VARCHAR(20),
    proposed_owner_name VARCHAR(255) NOT NULL,
    address_line_1 VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Maharashtra',
    postal_code VARCHAR(10) NOT NULL,
    verification_notes TEXT,
    verified_by_user_id BIGINT REFERENCES users(id) ON DELETE RESTRICT,
    verified_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),

    CONSTRAINT chk_registration_status CHECK (
        status IN ('PENDING_VERIFICATION', 'VERIFIED', 'INVITATION_SENT', 'COMPLETED', 'REJECTED', 'CANCELLED')
    )
);

CREATE INDEX IF NOT EXISTS idx_registrations_status ON pharmacy_registrations (status);
CREATE INDEX IF NOT EXISTS idx_registrations_proposed_email ON pharmacy_registrations (proposed_owner_email);

-- ------------------------------------------------------------------------------------
-- STEP 2: CREATE TABLE business_identities (Common Statutory Identity Registry)
-- ------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS business_identities (
    id BIGSERIAL PRIMARY KEY,
    identifier_type VARCHAR(50) NOT NULL,
    identifier_value VARCHAR(100) NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    registration_id BIGINT REFERENCES pharmacy_registrations(id) ON DELETE RESTRICT,
    tenant_id BIGINT REFERENCES tenants(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    metadata TEXT,
    verified_at TIMESTAMP,
    verified_by_user_id BIGINT REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_business_identity_entity CHECK (
        (entity_type = 'REGISTRATION' AND registration_id IS NOT NULL AND tenant_id IS NULL) OR
        (entity_type = 'TENANT' AND tenant_id IS NOT NULL AND registration_id IS NULL)
    ),

    CONSTRAINT chk_business_identity_status CHECK (
        status IN ('RESERVED', 'ACTIVE', 'REVOKED', 'RELEASED')
    ),

    CONSTRAINT chk_business_identity_normalized CHECK (
        identifier_value = UPPER(TRIM(identifier_value)) AND LENGTH(identifier_value) > 0
    ),

    CONSTRAINT chk_business_identity_type CHECK (
        identifier_type IN (
            'GSTIN',
            'BUSINESS_PAN',
            'DRUG_LICENCE_FORM_20',
            'DRUG_LICENCE_FORM_21',
            'DRUG_LICENCE_FORM_20B',
            'DRUG_LICENCE_FORM_21B',
            'DRUG_LICENCE_FORM_20C',
            'FSSAI'
        )
    ),

    CONSTRAINT chk_business_identity_format_gstin CHECK (
        identifier_type <> 'GSTIN' OR
        identifier_value ~ '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$'
    ),

    CONSTRAINT chk_business_identity_format_pan CHECK (
        identifier_type <> 'BUSINESS_PAN' OR
        identifier_value ~ '^[A-Z]{5}[0-9]{4}[A-Z]{1}$'
    ),

    CONSTRAINT chk_business_identity_format_drug_license CHECK (
        identifier_type NOT IN ('DRUG_LICENCE_FORM_20', 'DRUG_LICENCE_FORM_21', 'DRUG_LICENCE_FORM_20B', 'DRUG_LICENCE_FORM_21B', 'DRUG_LICENCE_FORM_20C') OR
        identifier_value ~ '^[A-Z0-9/_-]{5,50}$'
    ),

    CONSTRAINT chk_business_identity_format_fssai CHECK (
        identifier_type <> 'FSSAI' OR
        identifier_value ~ '^[0-9]{14}$'
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_active_business_identity 
ON business_identities (identifier_type, identifier_value) 
WHERE status IN ('RESERVED', 'ACTIVE');

CREATE INDEX IF NOT EXISTS idx_business_identities_tenant ON business_identities (tenant_id);
CREATE INDEX IF NOT EXISTS idx_business_identities_registration ON business_identities (registration_id);
CREATE INDEX IF NOT EXISTS idx_business_identities_status ON business_identities (status);

-- ------------------------------------------------------------------------------------
-- STEP 3: ALTER TABLE tenants (Add Registration & Ownership Columns)
-- ------------------------------------------------------------------------------------
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS registration_id BIGINT UNIQUE REFERENCES pharmacy_registrations(id) ON DELETE RESTRICT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS previous_tenant_id BIGINT REFERENCES tenants(id) ON DELETE RESTRICT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS primary_owner_id BIGINT REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS primary_owner_membership_id BIGINT REFERENCES tenant_memberships(id) ON DELETE RESTRICT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS gstin VARCHAR(15);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS pan VARCHAR(10);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS drug_license_number VARCHAR(100);

-- Pointer consistency constraint: both pointers must be populated together or both NULL (interim vacancy)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_tenant_primary_owner_pointers_consistent') THEN
        ALTER TABLE tenants ADD CONSTRAINT chk_tenant_primary_owner_pointers_consistent
        CHECK (
            (primary_owner_id IS NOT NULL AND primary_owner_membership_id IS NOT NULL) OR
            (primary_owner_id IS NULL AND primary_owner_membership_id IS NULL)
        );
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 4: ALTER TABLE tenant_memberships (Add Ownership & Security Version Columns)
-- ------------------------------------------------------------------------------------
ALTER TABLE tenant_memberships ADD COLUMN IF NOT EXISTS is_primary_owner BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tenant_memberships ADD COLUMN IF NOT EXISTS security_version BIGINT NOT NULL DEFAULT 1;

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_single_primary_owner
ON tenant_memberships (tenant_id)
WHERE is_primary_owner = TRUE;

-- ------------------------------------------------------------------------------------
-- STEP 5: COMPOSITE UNIQUE & FOREIGN KEY CONSTRAINTS
-- ------------------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_membership_id_tenant_user') THEN
        ALTER TABLE tenant_memberships ADD CONSTRAINT uk_membership_id_tenant_user 
        UNIQUE (id, tenant_id, user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_tenant_primary_owner_membership') THEN
        ALTER TABLE tenants ADD CONSTRAINT fk_tenant_primary_owner_membership
        FOREIGN KEY (primary_owner_membership_id, id, primary_owner_id) 
        REFERENCES tenant_memberships (id, tenant_id, user_id) 
        ON DELETE RESTRICT;
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 6: PRE-MIGRATION COLLISION DETECTION ASSERTION (Zero ON CONFLICT DO NOTHING)
-- ------------------------------------------------------------------------------------
DO $$
DECLARE
    v_collision_count INT;
    v_collision_details TEXT;
BEGIN
    SELECT COUNT(*), string_agg(format('Type: %s, Value: %s (Tenant IDs: %s)', identifier_type, identifier_value, tenant_ids), E'\n')
    INTO v_collision_count, v_collision_details
    FROM (
        SELECT identifier_type, identifier_value, string_agg(tenant_id::text, ', ') AS tenant_ids
        FROM (
            SELECT 'GSTIN' AS identifier_type, UPPER(TRIM(gstin)) AS identifier_value, id AS tenant_id 
            FROM tenants WHERE gstin IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
            UNION ALL
            SELECT 'BUSINESS_PAN' AS identifier_type, UPPER(TRIM(pan)) AS identifier_value, id AS tenant_id 
            FROM tenants WHERE pan IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
            UNION ALL
            SELECT 'DRUG_LICENCE_FORM_20' AS identifier_type, UPPER(TRIM(drug_license_number)) AS identifier_value, id AS tenant_id 
            FROM tenants WHERE drug_license_number IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
        ) all_ids
        GROUP BY identifier_type, identifier_value
        HAVING COUNT(*) > 1
    ) collisions;

    IF v_collision_count > 0 THEN
        RAISE EXCEPTION 'V4 Migration Aborted: Duplicate business identifiers detected in existing tenant baseline! Details:\n%', v_collision_details
            USING ERRCODE = 'data_exception'; -- SQLSTATE 22000
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 7: BACKFILL STATUTORY IDENTITIES FOR EXISTING ACTIVE TENANTS
-- ------------------------------------------------------------------------------------
INSERT INTO business_identities (identifier_type, identifier_value, entity_type, tenant_id, status, verified_at, created_at, updated_at)
SELECT 'GSTIN', UPPER(TRIM(gstin)), 'TENANT', id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants
WHERE gstin IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
  AND NOT EXISTS (
      SELECT 1 FROM business_identities bi 
      WHERE bi.tenant_id = tenants.id AND bi.identifier_type = 'GSTIN'
  );

INSERT INTO business_identities (identifier_type, identifier_value, entity_type, tenant_id, status, verified_at, created_at, updated_at)
SELECT 'BUSINESS_PAN', UPPER(TRIM(pan)), 'TENANT', id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants
WHERE pan IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
  AND NOT EXISTS (
      SELECT 1 FROM business_identities bi 
      WHERE bi.tenant_id = tenants.id AND bi.identifier_type = 'BUSINESS_PAN'
  );

INSERT INTO business_identities (identifier_type, identifier_value, entity_type, tenant_id, status, verified_at, created_at, updated_at)
SELECT 'DRUG_LICENCE_FORM_20', UPPER(TRIM(drug_license_number)), 'TENANT', id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants
WHERE drug_license_number IS NOT NULL AND status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
  AND NOT EXISTS (
      SELECT 1 FROM business_identities bi 
      WHERE bi.tenant_id = tenants.id AND bi.identifier_type = 'DRUG_LICENCE_FORM_20'
  );

-- Backfill existing Primary Owners
UPDATE tenant_memberships tm
SET is_primary_owner = TRUE
FROM tenants t
WHERE tm.tenant_id = t.id 
  AND tm.role = 'OWNER' 
  AND tm.status = 'ACTIVE'
  AND t.primary_owner_membership_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM tenant_memberships other
      WHERE other.tenant_id = tm.tenant_id AND other.is_primary_owner = TRUE
  );

UPDATE tenants t
SET primary_owner_membership_id = tm.id,
    primary_owner_id = tm.user_id
FROM tenant_memberships tm
WHERE tm.tenant_id = t.id 
  AND tm.is_primary_owner = TRUE
  AND t.primary_owner_membership_id IS NULL;

-- ------------------------------------------------------------------------------------
-- STEP 8: POST-BACKFILL VERIFICATION ASSERTION
-- ------------------------------------------------------------------------------------
DO $$
DECLARE
    v_missing_count INT;
BEGIN
    SELECT COUNT(*) INTO v_missing_count
    FROM tenants t
    WHERE t.status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED')
      AND (
          (t.gstin IS NOT NULL AND NOT EXISTS (
              SELECT 1 FROM business_identities bi 
              WHERE bi.tenant_id = t.id AND bi.identifier_type = 'GSTIN' AND bi.identifier_value = UPPER(TRIM(t.gstin)) AND bi.status = 'ACTIVE'
          )) OR
          (t.pan IS NOT NULL AND NOT EXISTS (
              SELECT 1 FROM business_identities bi 
              WHERE bi.tenant_id = t.id AND bi.identifier_type = 'BUSINESS_PAN' AND bi.identifier_value = UPPER(TRIM(t.pan)) AND bi.status = 'ACTIVE'
          ))
      );

    IF v_missing_count > 0 THEN
        RAISE EXCEPTION 'V4 Migration Post-Check Failed: Found % active tenants with missing or mismatched business identities!', v_missing_count
            USING ERRCODE = 'data_exception';
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 9: DATABASE-ENFORCED TENANT LOCK TRIGGER (fn_membership_lock_tenants_before)
-- ------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_membership_lock_tenants_before()
RETURNS TRIGGER AS $$
DECLARE
    v_first_tenant_id BIGINT;
    v_second_tenant_id BIGINT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.tenant_id IS NOT NULL THEN
            PERFORM id FROM tenants WHERE id = OLD.tenant_id FOR UPDATE;
        END IF;
        RETURN OLD;

    ELSIF TG_OP = 'INSERT' THEN
        IF NEW.tenant_id IS NOT NULL THEN
            PERFORM id FROM tenants WHERE id = NEW.tenant_id FOR UPDATE;
        END IF;
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.tenant_id IS DISTINCT FROM NEW.tenant_id THEN
            v_first_tenant_id  := LEAST(OLD.tenant_id, NEW.tenant_id);
            v_second_tenant_id := GREATEST(OLD.tenant_id, NEW.tenant_id);

            IF v_first_tenant_id IS NOT NULL THEN
                PERFORM id FROM tenants WHERE id = v_first_tenant_id FOR UPDATE;
            END IF;
            IF v_second_tenant_id IS NOT NULL AND v_second_tenant_id <> v_first_tenant_id THEN
                PERFORM id FROM tenants WHERE id = v_second_tenant_id FOR UPDATE;
            END IF;
        ELSE
            IF NEW.tenant_id IS NOT NULL THEN
                PERFORM id FROM tenants WHERE id = NEW.tenant_id FOR UPDATE;
            END IF;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_membership_lock_tenants_before ON tenant_memberships;
CREATE TRIGGER trg_membership_lock_tenants_before
BEFORE INSERT OR UPDATE OR DELETE ON tenant_memberships
FOR EACH ROW EXECUTE FUNCTION fn_membership_lock_tenants_before();

-- ------------------------------------------------------------------------------------
-- STEP 10: DEFERRED PRIMARY OWNER INVARIANT VALIDATION TRIGGERS
-- ------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_validate_single_tenant_primary_owner(p_tenant_id BIGINT)
RETURNS VOID AS $$
DECLARE
    v_tenant RECORD;
    v_primary_membership RECORD;
    v_total_primary_count INT;
BEGIN
    IF p_tenant_id IS NULL THEN
        RETURN;
    END IF;

    SELECT id, status, primary_owner_id, primary_owner_membership_id 
    INTO v_tenant 
    FROM tenants 
    WHERE id = p_tenant_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Invariant violation: Referenced tenant % does not exist', p_tenant_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_tenant.status IN ('ACTIVE', 'SUBSCRIPTION_SUSPENDED') THEN
        IF v_tenant.primary_owner_membership_id IS NULL THEN
            SELECT COUNT(*) INTO v_total_primary_count
            FROM tenant_memberships
            WHERE tenant_id = p_tenant_id
              AND is_primary_owner = TRUE;

            IF v_total_primary_count > 0 THEN
                RAISE EXCEPTION 'Invariant violation: Tenant % has null primary_owner_membership_id but has membership with is_primary_owner = TRUE', 
                    p_tenant_id
                    USING ERRCODE = 'check_violation';
            END IF;
            RETURN;
        END IF;

        SELECT COUNT(*) INTO v_total_primary_count
        FROM tenant_memberships
        WHERE tenant_id = p_tenant_id
          AND is_primary_owner = TRUE;

        IF v_total_primary_count <> 1 THEN
            RAISE EXCEPTION 'Invariant violation: Active tenant % must have exactly one membership with is_primary_owner = TRUE (found %)', 
                p_tenant_id, v_total_primary_count
                USING ERRCODE = 'check_violation';
        END IF;

        SELECT id, user_id, role, status, is_primary_owner
        INTO v_primary_membership
        FROM tenant_memberships
        WHERE tenant_id = p_tenant_id
          AND is_primary_owner = TRUE;

        IF v_primary_membership.role <> 'OWNER' THEN
            RAISE EXCEPTION 'Invariant violation: Active tenant % primary owner membership % must have role OWNER (found %)', 
                p_tenant_id, v_primary_membership.id, v_primary_membership.role
                USING ERRCODE = 'check_violation';
        END IF;

        IF v_primary_membership.status <> 'ACTIVE' THEN
            RAISE EXCEPTION 'Invariant violation: Active tenant % primary owner membership % must have status ACTIVE (found %)', 
                p_tenant_id, v_primary_membership.id, v_primary_membership.status
                USING ERRCODE = 'check_violation';
        END IF;

        IF v_tenant.primary_owner_membership_id IS NULL OR v_tenant.primary_owner_membership_id <> v_primary_membership.id THEN
            RAISE EXCEPTION 'Invariant violation: Tenant % primary_owner_membership_id (%) does not match membership % with is_primary_owner = TRUE', 
                p_tenant_id, v_tenant.primary_owner_membership_id, v_primary_membership.id
                USING ERRCODE = 'check_violation';
        END IF;

        IF v_tenant.primary_owner_id IS NULL OR v_tenant.primary_owner_id <> v_primary_membership.user_id THEN
            RAISE EXCEPTION 'Invariant violation: Tenant % primary_owner_id (%) does not match membership user_id %', 
                p_tenant_id, v_tenant.primary_owner_id, v_primary_membership.user_id
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_enforce_tenant_primary_owner_integrity()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSIF TG_OP IN ('INSERT', 'UPDATE') THEN
        PERFORM fn_validate_single_tenant_primary_owner(NEW.id);
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_enforce_membership_primary_owner_integrity()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.tenant_id IS NOT NULL THEN
            PERFORM fn_validate_single_tenant_primary_owner(OLD.tenant_id);
        END IF;
        RETURN OLD;

    ELSIF TG_OP = 'INSERT' THEN
        IF NEW.tenant_id IS NOT NULL THEN
            PERFORM fn_validate_single_tenant_primary_owner(NEW.tenant_id);
        END IF;
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.tenant_id IS DISTINCT FROM NEW.tenant_id THEN
            IF OLD.tenant_id IS NOT NULL THEN
                PERFORM fn_validate_single_tenant_primary_owner(OLD.tenant_id);
            END IF;
            IF NEW.tenant_id IS NOT NULL THEN
                PERFORM fn_validate_single_tenant_primary_owner(NEW.tenant_id);
            END IF;
        ELSE
            IF NEW.tenant_id IS NOT NULL THEN
                PERFORM fn_validate_single_tenant_primary_owner(NEW.tenant_id);
            END IF;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_enforce_tenant_primary_owner_deferred ON tenants;
CREATE CONSTRAINT TRIGGER trg_enforce_tenant_primary_owner_deferred
AFTER INSERT OR UPDATE ON tenants
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION fn_enforce_tenant_primary_owner_integrity();

DROP TRIGGER IF EXISTS trg_enforce_membership_primary_owner_deferred ON tenant_memberships;
CREATE CONSTRAINT TRIGGER trg_enforce_membership_primary_owner_deferred
AFTER INSERT OR UPDATE OR DELETE ON tenant_memberships
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION fn_enforce_membership_primary_owner_integrity();

