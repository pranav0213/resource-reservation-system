
package com.pranav.reservation_system.service;

import com.pranav.reservation_system.entity.AppUser;
import com.pranav.reservation_system.entity.Reservation;
import com.pranav.reservation_system.entity.Resource;
import com.pranav.reservation_system.entity.Role;
import com.pranav.reservation_system.repository.ReservationRepository;
import com.pranav.reservation_system.repository.ResourceRepository;
import com.pranav.reservation_system.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPreventSimultaneousDoubleBooking() throws Exception {

        // Create a unique test user.
        AppUser user = new AppUser();
        user.setName("Concurrency Test User");
        user.setEmail(
                "concurrency-" + UUID.randomUUID() + "@example.com"
        );
        user.setPassword("test-only-password");
        user.setRole(Role.USER);

        user = userRepository.save(user);

        Long userId = user.getId();
        String userEmail = user.getEmail();

        // Create a resource for this test.
        Resource resource = new Resource();
        resource.setName("Concurrency Test Room");
        resource.setType("Room");
        resource.setAvailable(true);

        resource = resourceRepository.save(resource);

        Long resourceId = resource.getId();

        LocalDateTime start = LocalDateTime.now().plusDays(30);
        LocalDateTime end = start.plusHours(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {

            Callable<Boolean> bookingTask = () -> {

                ready.countDown();
                startSignal.await();

                try {
                    reservationService.createReservationForUser(
                            resourceId,
                            userEmail,
                            start,
                            end
                    );

                    return true;

                } catch (IllegalArgumentException ex) {

                    if ("Resource is already booked for this time."
                            .equals(ex.getMessage())) {
                        return false;
                    }

                    throw ex;
                }
            };

            Future<Boolean> first = executor.submit(bookingTask);
            Future<Boolean> second = executor.submit(bookingTask);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both booking threads should be ready."
            );

            // Start both booking attempts together.
            startSignal.countDown();

            int successes =
                    (first.get(20, TimeUnit.SECONDS) ? 1 : 0)
                            + (second.get(20, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(
                    1,
                    successes,
                    "Only one booking should succeed."
            );

            // Verify only one reservation exists for the resource.
            List<Reservation> bookings =
                    reservationRepository.findAll()
                            .stream()
                            .filter(r ->
                                    r.getResource() != null
                                            && resourceId.equals(
                                            r.getResource().getId()
                                    )
                            )
                            .toList();

            assertEquals(
                    1,
                    bookings.size(),
                    "Only one reservation should be saved."
            );

            assertEquals(
                    userId,
                    bookings.get(0).getUser().getId()
            );

        } finally {

            startSignal.countDown();
            executor.shutdownNow();

            // Remove test reservations.
            reservationRepository.findAll()
                    .stream()
                    .filter(r ->
                            r.getResource() != null
                                    && resourceId.equals(
                                    r.getResource().getId()
                            )
                    )
                    .forEach(reservationRepository::delete);

            // Remove the test resource and account.
            resourceRepository.deleteById(resourceId);
            userRepository.deleteById(userId);
        }
    }
}
