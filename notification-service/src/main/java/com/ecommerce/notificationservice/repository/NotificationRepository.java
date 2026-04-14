package com.ecommerce.notificationservice.repository;
import com.ecommerce.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {}
