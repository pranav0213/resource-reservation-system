
package com.pranav.reservation_system.service;

import com.pranav.reservation_system.entity.Resource;
import com.pranav.reservation_system.repository.ResourceRepository;
import com.pranav.reservation_system.repository.ReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            ReservationRepository reservationRepository) {

        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public Resource updateResource(Long id, Resource updatedResource) {
        Optional<Resource> existingResource = resourceRepository.findById(id);

        if (existingResource.isEmpty()) {
            return null;
        }

        Resource resource = existingResource.get();

        resource.setName(updatedResource.getName());
        resource.setDescription(updatedResource.getDescription());
        resource.setType(updatedResource.getType());
        resource.setAvailable(updatedResource.isAvailable());

        return resourceRepository.save(resource);
    }

    @Transactional
    public boolean deleteResource(Long id) {

        if (!resourceRepository.existsById(id)) {
            return false;
        }

        if (reservationRepository.existsByResourceId(id)) {
            throw new IllegalStateException(
                    "Cannot delete resource because it has existing reservations."
            );
        }

        resourceRepository.deleteById(id);
        return true;
    }
}
