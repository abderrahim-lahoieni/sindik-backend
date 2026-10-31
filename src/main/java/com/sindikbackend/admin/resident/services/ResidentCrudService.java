package com.sindikbackend.admin.resident.services;

import com.sindikbackend.auth.dtos.requests.UserCreateRequest;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.auth.repositories.UserRepository;
import com.sindikbackend.admin.resident.dtos.requests.ResidentCreateRequest;
import com.sindikbackend.admin.resident.dtos.requests.ResidentUpdateRequest;
import com.sindikbackend.admin.resident.dtos.responses.ResidentResponse;
import com.sindikbackend.admin.resident.entities.Resident;
import com.sindikbackend.admin.resident.repositories.ResidentRepository;
import com.sindikbackend.admin.unit.entities.Unit;
import com.sindikbackend.admin.unit.repositories.UnitRepository;
import com.sindikbackend.auth.services.UserCrudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sindikbackend.syndic.syndicbuilding.entities.SyndicBuilding;
import com.sindikbackend.syndic.syndicbuilding.repositories.SyndicBuildingRepository;

import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResidentCrudService {

    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final SyndicBuildingRepository syndicBuildingRepository; // AJOUTER
    private final UserCrudService userService;


    private ResidentResponse mapToResponse(Resident resident) {
        return ResidentResponse.builder()
                .uuid(resident.getUuid())
                .fullName(resident.getFullName())
                .phoneNumber(resident.getPhoneNumber())
                .emergencyContact(resident.getEmergencyContact())
                .emergencyPhone(resident.getEmergencyPhone())
                .moveInDate(resident.getMoveInDate())
                .moveOutDate(resident.getMoveOutDate())
                .isOwner(resident.getIsOwner())
                .isPrimaryResident(resident.getIsPrimaryResident())
                .notes(resident.getNotes())
                .isActive(resident.getIsActive())
                .userUuid(resident.getUser() != null ? resident.getUser().getUuid() : null)
                .userEmail(resident.getUser() != null ? resident.getUser().getEmail() : null)
                .unitUuid(resident.getUnit().getUuid())
                .unitNumber(resident.getUnit().getUnitNumber())
                .buildingUuid(resident.getUnit().getBuilding().getUuid())
                .buildingName(resident.getUnit().getBuilding().getName())
                .residenceUuid(resident.getUnit().getBuilding().getResidence().getUuid())
                .residenceName(resident.getUnit().getBuilding().getResidence().getName())
                .createdAt(resident.getCreatedAt())
                .updatedAt(resident.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findAllResidents() {
        log.info("Finding all residents");
        return residentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResidentResponse findResidentByUuid(UUID uuid) {
        log.info("Finding resident by uuid: {}", uuid);
        Resident resident = residentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with uuid: " + uuid));
        return mapToResponse(resident);
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findByUnitUuid(UUID unitUuid) {
        log.info("Finding residents by unit uuid: {}", unitUuid);

        if (!unitRepository.existsByUuid(unitUuid)) {
            throw new ResourceNotFoundException("Unit not found with uuid: " + unitUuid);
        }

        return residentRepository.findByUnitUuid(unitUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findByBuildingUuid(UUID buildingUuid) {
        log.info("Finding residents by building uuid: {}", buildingUuid);

        return residentRepository.findByBuildingUuid(buildingUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findByResidenceUuid(UUID residenceUuid) {
        log.info("Finding residents by residence uuid: {}", residenceUuid);

        return residentRepository.findByResidenceUuid(residenceUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResidentResponse findByUserUuid(UUID userUuid) {
        log.info("Finding resident by user uuid: {}", userUuid);

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userUuid));
        if (user.getRole() != Role.RESIDENT) {
            throw new ResourceNotFoundException("Resident not found for user uuid: " + userUuid);
        }

        Resident resident = residentRepository.findByUserUuid(userUuid).orElse(null);
        if (resident == null) {
            List<Resident> matchingResidents = residentRepository.findAllByFullNameIgnoreCaseAndUserIsNull(user.getFullName()).stream()
                    .filter(candidate -> generatedResidentEmail(candidate.getFullName()).equalsIgnoreCase(user.getEmail()))
                    .limit(2)
                    .toList();
            if (matchingResidents.size() != 1) {
                throw new ResourceNotFoundException("Resident record could not be matched to user: " + userUuid);
            }

            resident = matchingResidents.get(0);
            resident.setUser(user);
            resident = residentRepository.save(resident);
        }
        return mapToResponse(resident);
    }

    private String generatedResidentEmail(String fullName) {
        return fullName.toLowerCase().replaceAll("\\s+", "") + "@resident.com";
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findActiveResidents() {
        log.info("Finding active residents");
        return residentRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findOwners() {
        log.info("Finding owner residents");
        return residentRepository.findOwners().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findPrimaryResidents() {
        log.info("Finding primary residents");
        return residentRepository.findPrimaryResidents().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> searchResidents(String keyword) {
        log.info("Searching residents with keyword: {}", keyword);
        return residentRepository.searchByNameOrPhone(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> findResidents(
            UUID unitUuid,
            UUID buildingUuid,
            UUID residenceUuid,
            Boolean isOwner,
            Boolean isPrimaryResident,
            Boolean active) {

        log.info("Finding residents with filters - unit: {}, building: {}, residence: {}, owner: {}, primary: {}, active: {}",
                unitUuid, buildingUuid, residenceUuid, isOwner, isPrimaryResident, active);

        List<Resident> residents = residentRepository.findByFilters(
                unitUuid, buildingUuid, residenceUuid, isOwner, isPrimaryResident, active);

        return residents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResidentResponse createResident(ResidentCreateRequest request) {
        log.info("Creating new resident: {} for unit: {}", request.getFullName(), request.getUnitUuid());

        Unit unit = unitRepository.findByUuid(request.getUnitUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + request.getUnitUuid()));

        User user = null;
        if (request.getUserUuid() != null) {
            user = userRepository.findByUuid(request.getUserUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + request.getUserUuid()));
        }

        if (user == null) {
            UserCreateRequest userRequest = UserCreateRequest.builder()
                    .fullName(request.getFullName())
                    .email(generatedResidentEmail(request.getFullName()))
                    .password("resident@" + Year.now().getValue())
                    .role(Role.RESIDENT)
                    .isActive(true)
                    .build();

            var createdUser = userService.createUser(userRequest);
            user = userRepository.findByUuid(createdUser.getUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Created resident user could not be loaded"));
        } else if (user.getRole() != Role.RESIDENT) {
            throw new BadRequestException("A resident profile can only be linked to a RESIDENT account");
        }

        Resident resident = Resident.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .moveInDate(request.getMoveInDate())
                .moveOutDate(request.getMoveOutDate())
                .isOwner(request.getIsOwner() != null ? request.getIsOwner() : false)
                .isPrimaryResident(request.getIsPrimaryResident() != null ? request.getIsPrimaryResident() : true)
                .notes(request.getNotes())
                .unit(unit)
                .user(user)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Resident savedResident = residentRepository.save(resident);
        log.info("Resident created successfully with uuid: {}", savedResident.getUuid());

        return mapToResponse(savedResident);
    }

    public ResidentResponse updateResident(UUID uuid, ResidentUpdateRequest request) {
        log.info("Updating resident with uuid: {}", uuid);

        Resident resident = residentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with uuid: " + uuid));

        resident.setFullName(request.getFullName());
        resident.setPhoneNumber(request.getPhoneNumber());
        resident.setEmergencyContact(request.getEmergencyContact());
        resident.setEmergencyPhone(request.getEmergencyPhone());

        if (request.getUnitUuid() != null &&
                !request.getUnitUuid().equals(resident.getUnit().getUuid())) {

            Unit newUnit = unitRepository.findByUuid(request.getUnitUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with uuid: " + request.getUnitUuid()));

            resident.setUnit(newUnit);
        }

        if (request.getUserUuid() != null) {
            if (resident.getUser() == null || !request.getUserUuid().equals(resident.getUser().getUuid())) {
                User user = userRepository.findByUuid(request.getUserUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + request.getUserUuid()));
                resident.setUser(user);
            }
        }

        resident.setMoveInDate(request.getMoveInDate());
        resident.setMoveOutDate(request.getMoveOutDate());

        if (request.getIsOwner() != null) {
            resident.setIsOwner(request.getIsOwner());
        }

        if (request.getIsPrimaryResident() != null) {
            resident.setIsPrimaryResident(request.getIsPrimaryResident());
        }

        resident.setNotes(request.getNotes());

        if (request.getIsActive() != null) {
            resident.setIsActive(request.getIsActive());
        }

        Resident updatedResident = residentRepository.save(resident);
        log.info("Resident updated successfully with uuid: {}", updatedResident.getUuid());

        return mapToResponse(updatedResident);
    }

    @Transactional
    public void deleteResident(UUID uuid) {
        log.info("Deleting resident with uuid: {}", uuid);

        Resident resident = residentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with uuid: " + uuid));

        residentRepository.delete(resident);
        log.info("Resident deleted successfully: {}", uuid);
    }

    public ResidentResponse toggleResidentStatus(UUID uuid) {
        log.info("Toggling status for resident: {}", uuid);

        Resident resident = residentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with uuid: " + uuid));

        resident.setIsActive(!resident.getIsActive());
        Resident updatedResident = residentRepository.save(resident);

        log.info("Resident status toggled to: {} for uuid: {}", resident.getIsActive(), uuid);

        return mapToResponse(updatedResident);
    }

    @Transactional(readOnly = true)
    public long countResidentsByUnit(UUID unitUuid) {
        log.info("Counting residents for unit: {}", unitUuid);
        return residentRepository.countByUnitUuid(unitUuid);
    }

    @Transactional(readOnly = true)
    public long countResidentsByBuilding(UUID buildingUuid) {
        log.info("Counting residents for building: {}", buildingUuid);
        return residentRepository.countByBuildingUuid(buildingUuid);
    }

    @Transactional(readOnly = true)
    public long countResidentsByResidence(UUID residenceUuid) {
        log.info("Counting residents for residence: {}", residenceUuid);
        return residentRepository.countByResidenceUuid(residenceUuid);
    }

    @Transactional(readOnly = true)
    public long countOwners() {
        log.info("Counting owner residents");
        return residentRepository.countOwners();
    }

    // NOUVELLE MÉTHODE : Récupérer les résidents gérés par un syndic
    @Transactional(readOnly = true)
    public List<ResidentResponse> findResidentsManagedBySyndic(UUID syndicUuid) {
        log.info("Finding residents managed by syndic: {}", syndicUuid);

        List<SyndicBuilding> syndicBuildings = syndicBuildingRepository.findBySyndicUuid(syndicUuid);

        List<UUID> buildingUuids = syndicBuildings.stream()
                .map(sb -> sb.getBuilding().getUuid())
                .collect(Collectors.toList());

        if (buildingUuids.isEmpty()) {
            return List.of();
        }

        return buildingUuids.stream()
                .flatMap(buildingUuid -> residentRepository.findByBuildingUuid(buildingUuid).stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MODIFIER LA MÉTHODE findResidents pour tenir compte du role
    @Transactional(readOnly = true)
    public List<ResidentResponse> findResidents(
            UUID unitUuid,
            UUID buildingUuid,
            UUID residenceUuid,
            Boolean isOwner,
            Boolean isPrimaryResident,
            Boolean active,
            UUID currentUserUuid,
            Role currentUserRole) {

        log.info("Finding residents with filters - unit: {}, building: {}, residence: {}, owner: {}, primary: {}, active: {}, user: {}, role: {}",
                unitUuid, buildingUuid, residenceUuid, isOwner, isPrimaryResident, active, currentUserUuid, currentUserRole);

        // Si SYNDIC, filtrer par ses bâtiments
        if (currentUserRole == Role.SYNDIC) {
            List<ResidentResponse> syndicResidents = findResidentsManagedBySyndic(currentUserUuid);

            // Appliquer les filtres additionnels
            return syndicResidents.stream()
                    .filter(resident -> unitUuid == null || resident.getUnitUuid().equals(unitUuid))
                    .filter(resident -> buildingUuid == null || resident.getBuildingUuid().equals(buildingUuid))
                    .filter(resident -> isOwner == null || resident.getIsOwner().equals(isOwner))
                    .filter(resident -> isPrimaryResident == null || resident.getIsPrimaryResident().equals(isPrimaryResident))
                    .filter(resident -> active == null || resident.getIsActive().equals(active))
                    .collect(Collectors.toList());
        }

        // Si ADMIN, appliquer les filtres normaux
        List<Resident> residents = residentRepository.findByFilters(
                unitUuid, buildingUuid, residenceUuid, isOwner, isPrimaryResident, active);

        return residents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE : Vérifier si un syndic peut gérer un résident
    @Transactional(readOnly = true)
    public boolean canManageResident(UUID syndicUuid, UUID residentUuid) {
        Resident resident = residentRepository.findByUuid(residentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found: " + residentUuid));

        return syndicBuildingRepository.existsBySyndicAndBuilding(
                syndicUuid,
                resident.getUnit().getBuilding().getUuid()
        );
    }
}
