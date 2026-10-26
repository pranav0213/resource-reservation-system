
package com.pranav.reservation_system.service;

import com.pranav.reservation_system.entity.AppUser;
import com.pranav.reservation_system.entity.Reservation;
import com.pranav.reservation_system.entity.Resource;
import com.pranav.reservation_system.entity.Role;

import com.pranav.reservation_system.repository.ReservationRepository;
import com.pranav.reservation_system.repository.ResourceRepository;
import com.pranav.reservation_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reservationService = new ReservationService(
                reservationRepository,
                resourceRepository,
                userRepository
        );
    }

    // TEST 1: Reject invalid reservation times
    @Test
    void shouldRejectReservationWhenStartTimeIsAfterEndTime() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservationForUser(
                        1L,
                        user.getEmail(),
                        startTime,
                        endTime
                )
        );

        assertEquals(
                "Start time must be before end time.",
                exception.getMessage()
        );
    }

    // TEST 2: Reject overlapping reservations
    @Test
    void shouldRejectOverlappingReservation() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Long resourceId = 1L;

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(2);

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setAvailable(true);

        when(resourceRepository.findByIdForUpdate(resourceId))
                .thenReturn(Optional.of(resource));

        Reservation existingReservation = new Reservation();

        when(reservationRepository
                .findByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        resourceId, endTime, startTime))
                .thenReturn(List.of(existingReservation));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservationForUser(
                        resourceId,
                        user.getEmail(),
                        startTime,
                        endTime
                )
        );

        assertEquals(
                "Resource is already booked for this time.",
                exception.getMessage()
        );
    }

    // TEST 3: Reject unavailable resources
    @Test
    void shouldRejectUnavailableResource() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Long resourceId = 1L;

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(2);

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setAvailable(false);

        when(resourceRepository.findByIdForUpdate(resourceId))
                .thenReturn(Optional.of(resource));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservationForUser(
                        resourceId,
                        user.getEmail(),
                        startTime,
                        endTime
                )
        );

        assertEquals(
                "Resource is unavailable.",
                exception.getMessage()
        );
    }

    // TEST 4: Create a valid reservation linked to USER
    @Test
    void shouldCreateValidReservation() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Long resourceId = 1L;

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(2);

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setAvailable(true);

        when(resourceRepository.findByIdForUpdate(resourceId))
                .thenReturn(Optional.of(resource));

        when(reservationRepository
                .findByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        resourceId, endTime, startTime))
                .thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservationForUser(
                resourceId,
                user.getEmail(),
                startTime,
                endTime
        );

        assertNotNull(result);
        assertEquals(user.getName(), result.getReservedBy());
        assertEquals(resourceId, result.getResource().getId());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertSame(user, result.getUser());

        verify(reservationRepository, times(1))
                .save(any(Reservation.class));
    }

    // TEST 5: USER sees only their own reservations
    @Test
    void shouldReturnOnlyOwnReservationsForUser() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Reservation ownReservation = new Reservation();

        when(reservationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(ownReservation));

        List<Reservation> result =
                reservationService.getReservationsForUser(user.getEmail());

        assertEquals(1, result.size());
        assertSame(ownReservation, result.get(0));

        verify(reservationRepository, never()).findAll();
    }

    // TEST 6: ADMIN sees all reservations
    @Test
    void shouldReturnAllReservationsForAdmin() {

        AppUser admin = createUser("admin@example.com", Role.ADMIN);
        mockUserLookup(admin);

        Reservation first = new Reservation();
        Reservation second = new Reservation();

        when(reservationRepository.findAll())
                .thenReturn(List.of(first, second));

        List<Reservation> result =
                reservationService.getReservationsForUser(admin.getEmail());

        assertEquals(2, result.size());

        verify(reservationRepository).findAll();
        verify(reservationRepository, never()).findByUserId(any());
    }

    // TEST 7: USER cannot cancel another user's reservation
    @Test
    void shouldRejectCancellationByNonOwner() {

        AppUser user = createUser("user@example.com", Role.USER);
        AppUser owner = createUser("owner@example.com", Role.USER);

        mockUserLookup(user);

        Reservation reservation = new Reservation();
        reservation.setUser(owner);

        when(reservationRepository.findById(21L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                AccessDeniedException.class,
                () -> reservationService.cancelReservationForUser(
                        21L, user.getEmail()
                )
        );

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    // TEST 8: USER can cancel own reservation
    @Test
    void shouldAllowOwnerToCancelReservation() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Reservation reservation = new Reservation();
        reservation.setUser(user);

        when(reservationRepository.findById(21L))
                .thenReturn(Optional.of(reservation));

        boolean deleted =
                reservationService.cancelReservationForUser(
                        21L, user.getEmail()
                );

        assertTrue(deleted);
        verify(reservationRepository).delete(reservation);
    }

    // TEST 9: ADMIN can cancel another user's reservation
    @Test
    void shouldAllowAdminToCancelAnyReservation() {

        AppUser admin = createUser("admin@example.com", Role.ADMIN);
        AppUser owner = createUser("owner@example.com", Role.USER);

        mockUserLookup(admin);

        Reservation reservation = new Reservation();
        reservation.setUser(owner);

        when(reservationRepository.findById(21L))
                .thenReturn(Optional.of(reservation));

        boolean deleted =
                reservationService.cancelReservationForUser(
                        21L, admin.getEmail()
                );

        assertTrue(deleted);
        verify(reservationRepository).delete(reservation);
    }

    // TEST 10: USER cannot cancel an unowned legacy reservation
    @Test
    void shouldRejectCancellationOfUnownedReservationForUser() {

        AppUser user = createUser("user@example.com", Role.USER);
        mockUserLookup(user);

        Reservation reservation = new Reservation();

        when(reservationRepository.findById(21L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                AccessDeniedException.class,
                () -> reservationService.cancelReservationForUser(
                        21L, user.getEmail()
                )
        );

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    // Helper: create test users

    private AppUser createUser(String email, Role role) {

        AppUser user = mock(AppUser.class);

        Long id = switch (email) {
            case "admin@example.com" -> 1L;
            case "user@example.com" -> 2L;
            case "owner@example.com" -> 3L;
            default -> 4L;
        };

        when(user.getEmail()).thenReturn(email);
        when(user.getId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);

        // Required for reservation creation.
        when(user.getName()).thenReturn("Test User");

        return user;
    }

    // Helper: configure authenticated user lookup
    private void mockUserLookup(AppUser user) {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }
}
