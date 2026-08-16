package com.sindikbackend.syndic.syndicbuilding.services;// services/SyndicBuildingService.java

import com.sindikbackend.admin.building.entities.Building;
import com.sindikbackend.admin.building.repositories.BuildingRepository;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.auth.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sindikbackend.syndic.syndicbuilding.dto.requests.AssignSyndicRequest;
import com.sindikbackend.syndic.syndicbuilding.dto.responses.SyndicBuildingResponse;
import com.sindikbackend.syndic.syndicbuilding.entities.SyndicBuilding;
import com.sindikbackend.syndic.syndicbuilding.repositories.SyndicBuildingRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SyndicBuildingService {

    private final SyndicBuildingRepository syndicBuildingRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;

    private SyndicBuildingResponse mapToResponse(SyndicBuilding sb) {
        return SyndicBuildingResponse.builder()
                .uuid(sb.getUuid())
                .syndicUuid(sb.getSyndic().getUuid())
                .syndicName(sb.getSyndic().getFullName())
                .syndicEmail(sb.getSyndic().getEmail())
                .buildingUuid(sb.getBuilding().getUuid())
                .buildingName(sb.getBuilding().getName())
                .residenceUuid(sb.getBuilding().getResidence().getUuid())
                .residenceName(sb.getBuilding().getResidence().getName())
                .assignedAt(sb.getAssignedAt())
                .assignedBy(sb.getAssignedBy())
                .build();
    }

    // Assigner un syndic à un ou plusieurs bâtiments
    public List<SyndicBuildingResponse> assignSyndicToBuildings(AssignSyndicRequest request) {
        log.info("Assigning syndic {} to {} buildings", request.getSyndicUuid(), request.getBuildingUuids().size());

        // Vérifier que l'utilisateur existe et est bien un SYNDIC
        User syndic = userRepository.findByUuid(request.getSyndicUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + request.getSyndicUuid()));

        if (syndic.getRole() != Role.SYNDIC) {
            throw new BadRequestException("User is not a SYNDIC");
        }

        List<SyndicBuilding> assignments = request.getBuildingUuids().stream()
                .map(buildingUuid -> {
                    Building building = buildingRepository.findByUuid(buildingUuid)
                            .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + buildingUuid));

                    // Vérifier si l'assignation existe déjà
                    if (syndicBuildingRepository.existsBySyndicAndBuilding(request.getSyndicUuid(), buildingUuid)) {
                        log.warn("Syndic {} already assigned to building {}", request.getSyndicUuid(), buildingUuid);
                        return null;
                    }

                    return SyndicBuilding.builder()
                            .syndic(syndic)
                            .building(building)
                            .assignedBy(request.getAssignedBy())
                            .build();
                })
                .filter(sb -> sb != null)
                .collect(Collectors.toList());

        List<SyndicBuilding> saved = syndicBuildingRepository.saveAll(assignments);
        log.info("Assigned {} buildings to syndic {}", saved.size(), request.getSyndicUuid());

        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Récupérer tous les bâtiments d'un syndic
    @Transactional(readOnly = true)
    public List<SyndicBuildingResponse> getBuildingsBySyndic(UUID syndicUuid) {
        log.info("Getting buildings for syndic: {}", syndicUuid);
        return syndicBuildingRepository.findBySyndicUuid(syndicUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Récupérer tous les syndics d'un bâtiment
    @Transactional(readOnly = true)
    public List<SyndicBuildingResponse> getSyndicsByBuilding(UUID buildingUuid) {
        log.info("Getting syndics for building: {}", buildingUuid);
        return syndicBuildingRepository.findByBuildingUuid(buildingUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Retirer un syndic d'un bâtiment
    @Transactional
    public void removeSyndicFromBuilding(UUID syndicUuid, UUID buildingUuid) {
        log.info("Removing syndic {} from building {}", syndicUuid, buildingUuid);
        syndicBuildingRepository.deleteBySyndicUuidAndBuildingUuid(syndicUuid, buildingUuid);
    }

    // Vérifier si un syndic gère un bâtiment
    @Transactional(readOnly = true)
    public boolean canManageBuilding(UUID syndicUuid, UUID buildingUuid) {
        return syndicBuildingRepository.existsBySyndicAndBuilding(syndicUuid, buildingUuid);
    }
}