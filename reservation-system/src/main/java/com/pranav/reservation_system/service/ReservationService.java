
package com.pranav.reservation_system.service;

import com.pranav.reservation_system.entity.AppUser;
import com.pranav.reservation_system.entity.Reservation;
import com.pranav.reservation_system.entity.Resource;
import com.pranav.reservation_system.entity.Role;
import com.pranav.reservation_system.repository.ReservationRepository;
import com.pranav.reservation_system.repository.ResourceRepository;
import com.pranav.reservation_system.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // Shared booking validation and creation.
    // Private: cannot be called directly by controllers.
    private Reservation buildReservation(
            Long resourceId,
            String reservedBy,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (resourceId == null ||
                reservedBy == null ||
                reservedBy.isBlank() ||
                startTime == null ||
                endTime == null) {

            throw new IllegalArgumentException(
                    "All reservation fields are required.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time.");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Start time cannot be in the past.");
        }

        // Lock resource to prevent simultaneous double-booking.
        Resource resource = resourceRepository
                .findByIdForUpdate(resourceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Resource not found."));

        if (!resource.isAvailable()) {
            throw new IllegalArgumentException(
                    "Resource is unavailable.");
        }

        List<Reservation> overlappingReservations =
                reservationRepository
                        .findByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                                resourceId,
                                endTime,
                                startTime
                        );

        if (!overlappingReservations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resource is already booked for this time.");
        }

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setReservedBy(reservedBy);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        return reservation;
    }

    // Create reservation for authenticated USER or ADMIN.
    @Transactional
    public Reservation createReservationForUser(
            Long resourceId,
            String email,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        AppUser user = findAuthenticatedUser(email);

        Reservation reservation = buildReservation(
                resourceId,
                user.getName(),
                startTime,
                endTime
        );

        reservation.setUser(user);

        return reservationRepository.save(reservation);
    }

    // USER sees own reservations; ADMIN sees all.
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForUser(String email) {

        AppUser user = findAuthenticatedUser(email);

        if (user.getRole() == Role.ADMIN) {
            return reservationRepository.findAll();
        }

        return reservationRepository.findByUserId(user.getId());
    }

    // USER can cancel own booking; ADMIN can cancel any.
    @Transactional
    public boolean cancelReservationForUser(
            Long reservationId,
            String email) {

        AppUser user = findAuthenticatedUser(email);

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            return false;
        }

        boolean isAdmin = user.getRole() == Role.ADMIN;

        boolean isOwner =
                reservation.getUser() != null
                        && user.getId().equals(
                        reservation.getUser().getId()
                );

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You cannot cancel another user's reservation.");
        }

        reservationRepository.delete(reservation);
        return true;
    }

    // Resolve the authenticated account.
    private AppUser findAuthenticatedUser(String email) {

        if (email == null || email.isBlank()) {
            throw new AccessDeniedException(
                    "Authenticated user is required.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "User account not found."));
    }
}
