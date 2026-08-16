package com.sindikbackend.syndic.syndicbuilding.dto.responses;// dtos/responses/SyndicBuildingResponse.java

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SyndicBuildingResponse {
    private UUID uuid;
    private UUID syndicUuid;
    private String syndicName;
    private String syndicEmail;
    private UUID buildingUuid;
    private String buildingName;
    private UUID residenceUuid;
    private String residenceName;
    private LocalDateTime assignedAt;
    private UUID assignedBy;
}