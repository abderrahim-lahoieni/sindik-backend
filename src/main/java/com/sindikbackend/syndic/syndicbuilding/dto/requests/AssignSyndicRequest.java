package com.sindikbackend.syndic.syndicbuilding.dto.requests;// dtos/requests/AssignSyndicRequest.java

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AssignSyndicRequest {

    @NotNull(message = "Syndic UUID is required")
    private UUID syndicUuid;

    @NotNull(message = "Building UUIDs are required")
    private List<UUID> buildingUuids;

    private UUID assignedBy; // UUID de l'admin qui fait l'assignation
}