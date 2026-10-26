
package com.pranav.reservation_system.controller;

import com.pranav.reservation_system.dto.ReservationRequest;
import com.pranav.reservation_system.entity.Reservation;
import com.pranav.reservation_system.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {

        this.reservationService = reservationService;
    }

    // USER sees own reservations.
    // ADMIN sees all reservations.
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        List<Reservation> reservations =
                reservationService.getReservationsForUser(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(reservations);
    }

    // Create reservation for the logged-in user.
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Reservation reservation =
                reservationService.createReservationForUser(
                        request.resourceId(),
                        jwt.getSubject(),
                        request.startTime(),
                        request.endTime()
                );

        return ResponseEntity.status(201).body(reservation);
    }

    // USER can cancel only own reservations.
    // ADMIN can cancel any reservation.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted =
                reservationService.cancelReservationForUser(
                        id,
                        jwt.getSubject()
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
