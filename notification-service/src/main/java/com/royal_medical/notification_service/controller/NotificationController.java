package com.royal_medical.notification_service.controller;

import com.royal_medical.notification_service.dto.NotificationDTO;
import com.royal_medical.notification_service.model.Notification;
import com.royal_medical.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public NotificationDTO sendNotification(@RequestBody NotificationDTO dto) {
        return notificationService.sendNotification(dto);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationDTO>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotificationsByCustomer(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.deleteNotificationById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationDTO>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    @GetMapping("/filter-by-date")
    public ResponseEntity<List<NotificationDTO>> getByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        return ResponseEntity.ok(notificationService.getNotificationsByDateRange(startDate, endDate));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationDTO>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        return ResponseEntity.ok(notificationService.getAllPaginated(pageable));
    }
}
