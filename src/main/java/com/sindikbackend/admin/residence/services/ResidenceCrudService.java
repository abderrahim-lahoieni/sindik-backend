package com.sindikbackend.admin.residence.services;

import com.sindikbackend.auth.dtos.requests.UserCreateRequest;
import com.sindikbackend.auth.dtos.responses.UserResponse;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.admin.residence.dtos.requests.ResidenceCreateRequest;
import com.sindikbackend.admin.residence.dtos.requests.ResidenceUpdateRequest;
import com.sindikbackend.admin.residence.dtos.responses.ResidenceResponse;
import com.sindikbackend.admin.residence.entities.Residence;
import com.sindikbackend.admin.residence.entities.ResidenceType;
import com.sindikbackend.admin.residence.repositories.ResidenceRepository;
import com.sindikbackend.auth.repositories.UserRepository;
import com.sindikbackend.auth.services.UserCrudService;
import com.sindikbackend.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResidenceCrudService {

    private final ResidenceRepository residenceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCrudService userService;

    private ResidenceResponse mapToResponse(Residence residence) {
        return ResidenceResponse.builder()
                .uuid(residence.getUuid())
                .name(residence.getName())
                .address(residence.getAddress())
                .city(residence.getCity())
                .postalCode(residence.getPostalCode())
                .type(residence.getType())
                .numberOfBuildings(residence.getNumberOfBuildings())
                .numberOfUnits(residence.getNumberOfUnits())
                .description(residence.getDescription())
                .isActive(residence.getIsActive())
                .createdAt(residence.getCreatedAt())
                .updatedAt(residence.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ResidenceResponse> findAllResidences() {
        log.info("Finding all residences");
        return residenceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResidenceResponse findResidenceByUuid(UUID uuid) {
        log.info("Finding residence by uuid: {}", uuid);
        Residence residence = residenceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + uuid));
        return mapToResponse(residence);
    }

    @Transactional(readOnly = true)
    public List<ResidenceResponse> findActiveResidences() {
        log.info("Finding active residences");
        return residenceRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidenceResponse> findResidencesByType(String type) {
        try {
            ResidenceType typeEnum = ResidenceType.valueOf(type.toUpperCase());
            log.info("Finding residences by type: {}", typeEnum);
            return residenceRepository.findByTypeActive(typeEnum).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid residence type: " + type);
        }
    }

    @Transactional(readOnly = true)
    public List<ResidenceResponse> searchResidences(String keyword) {
        log.info("Searching residences with keyword: {}", keyword);
        return residenceRepository.searchByNameOrCity(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidenceResponse> findResidences(String type, Boolean active) {
        ResidenceType typeEnum = null;

        if (type != null && !type.isBlank()) {
            try {
                typeEnum = ResidenceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid residence type: " + type);
            }
        }

        List<Residence> residences = residenceRepository.findByFilters(typeEnum, active);

        return residences.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResidenceResponse createResidence(ResidenceCreateRequest request) {
        log.info("Creating new residence: {}", request.getName());


        Residence residence = Residence.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .type(request.getType())
                .numberOfBuildings(request.getNumberOfBuildings())
                .numberOfUnits(request.getNumberOfUnits())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();


        Residence savedResidence = residenceRepository.save(residence);
        log.info("Residence created successfully with uuid: {}", savedResidence.getUuid());

        return mapToResponse(savedResidence);
    }

    public ResidenceResponse updateResidence(UUID uuid, ResidenceUpdateRequest request) {
        log.info("Updating residence with uuid: {}", uuid);

        Residence residence = residenceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + uuid));

        residence.setName(request.getName());
        residence.setAddress(request.getAddress());
        residence.setCity(request.getCity());
        residence.setPostalCode(request.getPostalCode());

        if (request.getType() != null) {
            residence.setType(request.getType());
        }

        if (request.getNumberOfBuildings() != null) {
            residence.setNumberOfBuildings(request.getNumberOfBuildings());
        }

        if (request.getNumberOfUnits() != null) {
            residence.setNumberOfUnits(request.getNumberOfUnits());
        }

        residence.setDescription(request.getDescription());

        if (request.getIsActive() != null) {
            residence.setIsActive(request.getIsActive());
        }

        Residence updatedResidence = residenceRepository.save(residence);
        log.info("Residence updated successfully with uuid: {}", updatedResidence.getUuid());

        return mapToResponse(updatedResidence);
    }

    @Transactional
    public void deleteResidence(UUID uuid) {
        log.info("Deleting residence with uuid: {}", uuid);

        Residence residence = residenceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + uuid));

        residenceRepository.delete(residence);
        log.info("Residence deleted successfully: {}", uuid);
    }

    public ResidenceResponse toggleResidenceStatus(UUID uuid) {
        log.info("Toggling status for residence: {}", uuid);

        Residence residence = residenceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + uuid));

        residence.setIsActive(!residence.getIsActive());
        Residence updatedResidence = residenceRepository.save(residence);

        log.info("Residence status toggled to: {} for uuid: {}", residence.getIsActive(), uuid);

        return mapToResponse(updatedResidence);
    }
}