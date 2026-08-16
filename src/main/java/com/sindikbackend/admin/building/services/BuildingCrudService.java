package com.sindikbackend.admin.building.services;

import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.admin.building.dtos.requests.BuildingCreateRequest;
import com.sindikbackend.admin.building.dtos.requests.BuildingUpdateRequest;
import com.sindikbackend.admin.building.dtos.responses.BuildingResponse;
import com.sindikbackend.admin.building.entities.Building;
import com.sindikbackend.admin.building.repositories.BuildingRepository;
import com.sindikbackend.admin.residence.entities.Residence;
import com.sindikbackend.admin.residence.repositories.ResidenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sindikbackend.syndic.syndicbuilding.entities.SyndicBuilding;
import com.sindikbackend.syndic.syndicbuilding.repositories.SyndicBuildingRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BuildingCrudService {

    private final BuildingRepository buildingRepository;
    private final ResidenceRepository residenceRepository;
    private final SyndicBuildingRepository syndicBuildingRepository; // AJOUTER


    private BuildingResponse mapToResponse(Building building) {
        return BuildingResponse.builder()
                .uuid(building.getUuid())
                .name(building.getName())
                .address(building.getAddress())
                .numberOfFloors(building.getNumberOfFloors())
                .numberOfUnits(building.getNumberOfUnits())
                .description(building.getDescription())
                .isActive(building.getIsActive())
                .residenceUuid(building.getResidence().getUuid())
                .residenceName(building.getResidence().getName())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findAllBuildings() {
        log.info("Finding all buildings");
        return buildingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findBuildings(UUID residenceUuid, Boolean active, UUID currentUserUuid, Role currentUserRole) {
        log.info("Finding buildings with filters - residence: {}, active: {}, user: {}, role: {}",
                residenceUuid, active, currentUserUuid, currentUserRole);

        // Si SYNDIC, ne retourner QUE ses bâtiments
        if (currentUserRole == Role.SYNDIC) {
            return findBuildingsManagedBySyndic(currentUserUuid);
        }

        // Si ADMIN, appliquer les filtres normaux
        List<Building> buildings = buildingRepository.findByFilters(residenceUuid, active);

        return buildings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findByResidenceUuid(UUID residenceUuid) {
        log.info("Finding buildings by residence uuid: {}", residenceUuid);

        // Vérifier que la résidence existe
        if (!residenceRepository.existsByUuid(residenceUuid)) {
            throw new ResourceNotFoundException("Residence not found with uuid: " + residenceUuid);
        }

        return buildingRepository.findByResidenceUuid(residenceUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findActiveBuildings() {
        log.info("Finding active buildings");
        return buildingRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> searchBuildings(String keyword) {
        log.info("Searching buildings with keyword: {}", keyword);
        return buildingRepository.searchByName(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findBuildings(UUID residenceUuid, Boolean active) {
        log.info("Finding buildings with filters - residence: {}, active: {}", residenceUuid, active);

        List<Building> buildings = buildingRepository.findByFilters(residenceUuid, active);

        return buildings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BuildingResponse createBuilding(BuildingCreateRequest request) {
        log.info("Creating new building: {} for residence: {}", request.getName(), request.getResidenceUuid());

        // Vérifier que la résidence existe
        Residence residence = residenceRepository.findByUuid(request.getResidenceUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + request.getResidenceUuid()));

        Building building = Building.builder()
                .name(request.getName())
                .address(request.getAddress())
                .numberOfFloors(request.getNumberOfFloors())
                .numberOfUnits(request.getNumberOfUnits())
                .description(request.getDescription())
                .residence(residence)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Building savedBuilding = buildingRepository.save(building);
        log.info("Building created successfully with uuid: {}", savedBuilding.getUuid());

        return mapToResponse(savedBuilding);
    }

    public BuildingResponse updateBuilding(UUID uuid, BuildingUpdateRequest request) {
        log.info("Updating building with uuid: {}", uuid);

        Building building = buildingRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with uuid: " + uuid));

        building.setName(request.getName());
        building.setAddress(request.getAddress());

        // Si la résidence change
        if (request.getResidenceUuid() != null &&
                !request.getResidenceUuid().equals(building.getResidence().getUuid())) {

            Residence newResidence = residenceRepository.findByUuid(request.getResidenceUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Residence not found with uuid: " + request.getResidenceUuid()));

            building.setResidence(newResidence);
        }

        if (request.getNumberOfFloors() != null) {
            building.setNumberOfFloors(request.getNumberOfFloors());
        }

        if (request.getNumberOfUnits() != null) {
            building.setNumberOfUnits(request.getNumberOfUnits());
        }

        building.setDescription(request.getDescription());

        if (request.getIsActive() != null) {
            building.setIsActive(request.getIsActive());
        }

        Building updatedBuilding = buildingRepository.save(building);
        log.info("Building updated successfully with uuid: {}", updatedBuilding.getUuid());

        return mapToResponse(updatedBuilding);
    }

    @Transactional
    public void deleteBuilding(UUID uuid) {
        log.info("Deleting building with uuid: {}", uuid);

        Building building = buildingRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with uuid: " + uuid));

        buildingRepository.delete(building);
        log.info("Building deleted successfully: {}", uuid);
    }

    public BuildingResponse toggleBuildingStatus(UUID uuid) {
        log.info("Toggling status for building: {}", uuid);

        Building building = buildingRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with uuid: " + uuid));

        building.setIsActive(!building.getIsActive());
        Building updatedBuilding = buildingRepository.save(building);

        log.info("Building status toggled to: {} for uuid: {}", building.getIsActive(), uuid);

        return mapToResponse(updatedBuilding);
    }

    @Transactional(readOnly = true)
    public long countBuildingsByResidence(UUID residenceUuid) {
        log.info("Counting buildings for residence: {}", residenceUuid);
        return buildingRepository.countByResidenceUuid(residenceUuid);
    }
    // NOUVELLE MÉTHODE : Récupérer les bâtiments gérés par un syndic
    @Transactional(readOnly = true)
    public List<BuildingResponse> findBuildingsManagedBySyndic(UUID syndicUuid) {
        log.info("Finding buildings managed by syndic: {}", syndicUuid);

        List<SyndicBuilding> syndicBuildings = syndicBuildingRepository.findBySyndicUuid(syndicUuid);

        return syndicBuildings.stream()
                .map(sb -> mapToResponse(sb.getBuilding()))
                .collect(Collectors.toList());
    }


}