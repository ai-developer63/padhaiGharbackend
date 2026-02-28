package app.nepaliapp.padhaighar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import app.nepaliapp.padhaighar.model.UserDevice;

import java.util.Optional;

public interface UserNotificationDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByUserId(Long userId);
    
    // This allows Spring to handle the "LIMIT" and "OFFSET" automatically for 5000+ rows
    Page<UserDevice> findAll(Pageable pageable);
}