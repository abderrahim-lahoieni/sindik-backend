package com.sindikbackend.admin.unit.services;

import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.admin.building.entities.Building;
import com.sindikbackend.admin.building.repositories.BuildingRepository;
import com.sindikbackend.admin.unit.dtos.requests.UnitCreateRequest;
import com.sindikbackend.admin.unit.dtos.requests.UnitUpdateRequest;
import com.sindikbackend.admin.unit.dtos.responses.UnitResponse;
import com.sindikbackend.admin.unit.entities.Unit;
import com.sindikbackend.admin.unit.entities.UnitType;
import com.sindikbackend.admin.unit.repositories.UnitRepository;
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
public class UnitCrudService {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final SyndicBuildingRepository syndicBuildingRepository; // AJOUTER


    private UnitResponse mapToResponse(Unit unit) {
        return UnitResponse.builder()
                .uuid(unit.getUuid())
                .unitNumber(unit.getUnitNumber())
                .type(unit.getType())
                .floorNumber(unit.getFloorNumber())
                .area(unit.getArea())
                .numberOfRooms(unit.getNumberOfRooms())
                .numberOfBathrooms(unit.getNumberOfBathrooms())
                .hasBalcony(unit.getHasBalcony())
                .hasParking(unit.getHasParking())
                .description(unit.getDescription())
                .isOccupied(unit.getIsOccupied())
                .isActive(unit.getIsActive())
                .buildingUuid(unit.getBuilding().getUuid())
                .buildingName(unit.getBuilding().getName())
                .residenceUuid(unit.getBuilding().getResidence().getUuid())
                .residenceName(unit.getBuilding().getResidence().getName())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findAllUnits() {
        log.info("Finding all units");
        return unitRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MODIFIER LA MÉTHODE findUnits pour tenir compte du role
    @Transactional(readOnly = true)
    public List<UnitResponse> findUnits(
            UUID buildingUuid,
            UUID residenceUuid,
            String type,
            Boolean occupied,
            Boolean active,
            UUID currentUserUuid,
            Role currentUserRole) {

        log.info("Finding units with filters - building: {}, residence: {}, type: {}, occupied: {}, active: {}, user: {}, role: {}",
                buildingUuid, residenceUuid, type, occupied, active, currentUserUuid, currentUserRole);

        // Si SYNDIC, filtrer par ses bâtiments
        if (currentUserRole == Role.SYNDIC) {
            List<UnitResponse> syndicUnits = findUnitsManagedBySyndic(currentUserUuid);

            // Appliquer les filtres additionnels
            return syndicUnits.stream()
                    .filter(unit -> buildingUuid == null || unit.getBuildingUuid().equals(buildingUuid))
                    .filter(unit -> type == null || unit.getType().name().equalsIgnoreCase(type))
                    .filter(unit -> occupied == null || unit.getIsOccupied().equals(occupied))
                    .filter(unit -> active == null || unit.getIsActive().equals(active))
                    .collect(Collectors.toList());
        }

        // Si ADMIN, appliquer les filtres normaux
        UnitType typeEnum = null;
        if (type != null && !type.isBlank()) {
            try {
                typeEnum = UnitType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid unit type: " + type);
            }
        }

        List<Unit> units = unitRepository.findByFilters(buildingUuid, residenceUuid, typeEnum, occupied, active);

        return units.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE : Vérifier si un syndic peut gérer une unité
    @Transactional(readOnly = true)
    public boolean canManageUnit(UUID syndicUuid, UUID unitUuid) {
        Unit unit = unitRepository.findByUuid(unitUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitUuid));

        return syndicBuildingRepository.existsBySyndicAndBuilding(syndicUuid, unit.getBuilding().getUuid());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findByBuildingUuid(UUID buildingUuid) {
        log.info("Finding units by building uuid: {}", buildingUuid);

        if (!buildingRepository.existsByUuid(buildingUuid)) {
            throw new ResourceNotFoundException("Building not found with uuid: " + buildingUuid);
        }

        return unitRepository.findByBuildingUuid(buildingUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findByResidenceUuid(UUID residenceUuid) {
        log.info("Finding units by residence uuid: {}", residenceUuid);

        return unitRepository.findByResidenceUuid(residenceUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findActiveUnits() {
        log.info("Finding active units");
        return unitRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findAvailableUnits() {
        log.info("Finding available units");
        return unitRepository.findAvailableUnits().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findOccupiedUnits() {
        log.info("Finding occupied units");
        return unitRepository.findOccupiedUnits().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findUnitsByType(String type) {
        try {
            UnitType typeEnum = UnitType.valueOf(type.toUpperCase());
            log.info("Finding units by type: {}", typeEnum);
            return unitRepository.findByTypeActive(typeEnum).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid unit type: " + type);
        }
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> searchUnits(String keyword) {
        log.info("Searching units with keyword: {}", keyword);
        return unitRepository.searchByUnitNumber(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findUnits(UUID buildingUuid, UUID residenceUuid, String type, Boolean occupied, Boolean active) {
        log.info("Finding units with filters - building: {}, residence: {}, type: {}, occupied: {}, active: {}",
                buildingUuid, residenceUuid, type, occupied, active);

        UnitType typeEnum = null;
        if (type != null && !type.isBlank()) {
            try {
                typeEnum = UnitType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid unit type: " + type);
            }
        }

        List<Unit> units = unitRepository.findByFilters(buildingUuid, residenceUuid, typeEnum, occupied, active);

        return units.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UnitResponse createUnit(UnitCreateRequest request, UUID syndicUuid, Role syndicRole) {
        log.info("Creating new unit: {} for building: {}", request.getUnitNumber(), request.getBuildingUuid());

        requireSyndicBuildingAccess(syndicUuid, syndicRole, request.getBuildingUuid());

        Building building = buildingRepository.findByUuid(request.getBuildingUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with uuid: " + request.getBuildingUuid()));

        Unit unit = Unit.builder()
                .unitNumber(request.getUnitNumber())
                .type(request.getType())
                .floorNumber(request.getFloorNumber())
                .area(request.getArea())
                .numberOfRooms(request.getNumberOfRooms())
                .numberOfBathrooms(request.getNumberOfBathrooms())
                .hasBalcony(request.getHasBalcony() != null ? request.getHasBalcony() : false)
                .hasParking(request.getHasParking() != null ? request.getHasParking() : false)
                .description(request.getDescription())
                .isOccupied(request.getIsOccupied() != null ? request.getIsOccupied() : false)
                .building(building)
                .isActive(true)
                .build();

        Unit savedUnit = unitRepository.save(unit);
        log.info("Unit created successfully with uuid: {}", savedUnit.getUuid());

        return mapToResponse(savedUnit);
    }

    public UnitResponse updateUnit(UUID uuid, UnitUpdateRequest request, UUID syndicUuid, Role syndicRole) {
        log.info("Updating unit with uuid: {}", uuid);

        Unit unit = unitRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + uuid));

        requireSyndicBuildingAccess(syndicUuid, syndicRole, unit.getBuilding().getUuid());

        unit.setUnitNumber(request.getUnitNumber());

        if (request.getBuildingUuid() != null &&
                !request.getBuildingUuid().equals(unit.getBuilding().getUuid())) {

            Building newBuilding = buildingRepository.findByUuid(request.getBuildingUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found with uuid: " + request.getBuildingUuid()));

            requireSyndicBuildingAccess(syndicUuid, syndicRole, newBuilding.getUuid());

            unit.setBuilding(newBuilding);
        }

        if (request.getType() != null) {
            unit.setType(request.getType());
        }

        if (request.getFloorNumber() != null) {
            unit.setFloorNumber(request.getFloorNumber());
        }

        unit.setArea(request.getArea());
        unit.setNumberOfRooms(request.getNumberOfRooms());
        unit.setNumberOfBathrooms(request.getNumberOfBathrooms());

        if (request.getHasBalcony() != null) {
            unit.setHasBalcony(request.getHasBalcony());
        }

        if (request.getHasParking() != null) {
            unit.setHasParking(request.getHasParking());
        }

        unit.setDescription(request.getDescription());

        if (request.getIsOccupied() != null) {
            unit.setIsOccupied(request.getIsOccupied());
        }

        Unit updatedUnit = unitRepository.save(unit);
        log.info("Unit updated successfully with uuid: {}", updatedUnit.getUuid());

        return mapToResponse(updatedUnit);
    }

    @Transactional
    public void deleteUnit(UUID uuid, UUID syndicUuid, Role syndicRole) {
        log.info("Deleting unit with uuid: {}", uuid);

        Unit unit = unitRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + uuid));

        requireSyndicBuildingAccess(syndicUuid, syndicRole, unit.getBuilding().getUuid());

        unitRepository.delete(unit);
        log.info("Unit deleted successfully: {}", uuid);
    }

    public UnitResponse toggleUnitStatus(UUID uuid, UUID syndicUuid, Role syndicRole) {
        log.info("Toggling status for unit: {}", uuid);

        Unit unit = unitRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + uuid));

        requireSyndicBuildingAccess(syndicUuid, syndicRole, unit.getBuilding().getUuid());

        unit.setIsActive(!unit.getIsActive());
        Unit updatedUnit = unitRepository.save(unit);

        log.info("Unit status toggled to: {} for uuid: {}", unit.getIsActive(), uuid);

        return mapToResponse(updatedUnit);
    }

    public UnitResponse toggleUnitOccupancy(UUID uuid, UUID syndicUuid, Role syndicRole) {
        log.info("Toggling occupancy for unit: {}", uuid);

        Unit unit = unitRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + uuid));

        requireSyndicBuildingAccess(syndicUuid, syndicRole, unit.getBuilding().getUuid());

        unit.setIsOccupied(!unit.getIsOccupied());
        Unit updatedUnit = unitRepository.save(unit);

        log.info("Unit occupancy toggled to: {} for uuid: {}", unit.getIsOccupied(), uuid);

        return mapToResponse(updatedUnit);
    }

    private void requireSyndicBuildingAccess(UUID syndicUuid, Role syndicRole, UUID buildingUuid) {
        if (syndicRole != Role.SYNDIC) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only a syndic can manage units");
        }
        if (!syndicBuildingRepository.existsBySyndicAndBuilding(syndicUuid, buildingUuid)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not assigned to manage this building");
        }
    }

    @Transactional(readOnly = true)
    public long countUnitsByBuilding(UUID buildingUuid) {
        log.info("Counting units for building: {}", buildingUuid);
        return unitRepository.countByBuildingUuid(buildingUuid);
    }

    @Transactional(readOnly = true)
    public long countAvailableUnitsByBuilding(UUID buildingUuid) {
        log.info("Counting available units for building: {}", buildingUuid);
        return unitRepository.countAvailableByBuildingUuid(buildingUuid);
    }

    // NOUVELLE MÉTHODE : Récupérer les unités gérées par un syndic
    @Transactional(readOnly = true)
    public List<UnitResponse> findUnitsManagedBySyndic(UUID syndicUuid) {
        log.info("Finding units managed by syndic: {}", syndicUuid);

        List<SyndicBuilding> syndicBuildings = syndicBuildingRepository.findBySyndicUuid(syndicUuid);

        List<UUID> buildingUuids = syndicBuildings.stream()
                .map(sb -> sb.getBuilding().getUuid())
                .collect(Collectors.toList());

        if (buildingUuids.isEmpty()) {
            return List.of();
        }

        return buildingUuids.stream()
                .flatMap(buildingUuid -> unitRepository.findByBuildingUuid(buildingUuid).stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

}
