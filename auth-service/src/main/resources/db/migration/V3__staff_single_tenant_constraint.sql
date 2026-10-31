-- V3__staff_single_tenant_constraint.sql
-- Enforce that a user can have at most one ACTIVE membership with role 'PHARMACY_STAFF'.
-- Pharmacy Owners can have active memberships across multiple tenants.
-- Inactive or suspended staff memberships do not violate this constraint.

CREATE UNIQUE INDEX uk_user_active_staff_membership
ON tenant_memberships (user_id)
WHERE role = 'PHARMACY_STAFF' AND status = 'ACTIVE';

