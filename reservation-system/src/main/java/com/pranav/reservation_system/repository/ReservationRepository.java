
package com.pranav.reservation_system.repository;

import com.pranav.reservation_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Find overlapping reservations
    List<Reservation>
    findByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long resourceId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    // Check whether a resource has reservations
    boolean existsByResourceId(Long resourceId);

    // Find reservations belonging to a registered user
    List<Reservation> findByUserId(Long userId);
}
