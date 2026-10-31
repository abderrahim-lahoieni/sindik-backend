package com.sindikbackend.syndic.contract.services;
// src/main/java/com/sindikbackend/contract/services/ContractCrudService.java


import com.sindikbackend.admin.resident.entities.Resident;
import com.sindikbackend.admin.resident.repositories.ResidentRepository;
import com.sindikbackend.admin.unit.entities.Unit;
import com.sindikbackend.admin.unit.repositories.UnitRepository;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.auth.entities.Role;

import com.sindikbackend.syndic.contract.dtos.requests.ContractCreateRequest;
import com.sindikbackend.syndic.contract.dtos.requests.ContractUpdateRequest;
import com.sindikbackend.syndic.contract.dtos.responses.ContractResponse;
import com.sindikbackend.syndic.contract.entities.Contract;
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.contract.repositories.ContractRepository;
import com.sindikbackend.syndic.syndicbuilding.repositories.SyndicBuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractCrudService {

    private final ContractRepository contractRepository;
    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;
    private final SyndicBuildingRepository syndicBuildingRepository;

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .uuid(contract.getUuid())
                .residentUuid(contract.getResident().getUuid())
                .residentName(contract.getResident().getFullName())
                .unitUuid(contract.getUnit().getUuid())
                .unitNumber(contract.getUnit().getUnitNumber())
                .buildingUuid(contract.getUnit().getBuilding().getUuid())
                .buildingName(contract.getUnit().getBuilding().getName())
                .residenceUuid(contract.getUnit().getBuilding().getResidence().getUuid())
                .residenceName(contract.getUnit().getBuilding().getResidence().getName())
                .type(contract.getType())
                .status(contract.getStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .monthlyRent(contract.getMonthlyRent())
                .depositAmount(contract.getDepositAmount())
                .depositPaid(contract.getDepositPaid())
                .paymentFrequency(contract.getPaymentFrequency())
                .paymentDay(contract.getPaymentDay())
                .contractNumber(contract.getContractNumber())
                .terms(contract.getTerms())
                .notes(contract.getNotes())
                .renewalNoticeDays(contract.getRenewalNoticeDays())
                .autoRenewable(contract.getAutoRenewable())
                .isActive(contract.getIsActive())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }

    @Transactional
    public ContractResponse createContract(ContractCreateRequest request, UUID userUuid, Role role) {
        log.info("Creating new contract for resident: {} and unit: {}",
                request.getResidentUuid(), request.getUnitUuid());

        // Vérifier que le résident existe
        Resident resident = residentRepository.findByUuid(request.getResidentUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resident not found with uuid: " + request.getResidentUuid()));

        // Vérifier que l'unité existe
        Unit unit = unitRepository.findByUuid(request.getUnitUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unit not found with uuid: " + request.getUnitUuid()));
        requireBuildingAccess(userUuid, role, unit.getBuilding().getUuid());

        // Vérifier les dates
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Contract contract = Contract.builder()
                .resident(resident)
                .unit(unit)
                .type(request.getType())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .monthlyRent(request.getMonthlyRent())
                .depositAmount(request.getDepositAmount())
                .depositPaid(request.getDepositPaid() != null ? request.getDepositPaid() : false)
                .paymentFrequency(request.getPaymentFrequency())
                .paymentDay(request.getPaymentDay())
                .terms(request.getTerms())
                .notes(request.getNotes())
                .renewalNoticeDays(request.getRenewalNoticeDays())
                .autoRenewable(request.getAutoRenewable() != null ? request.getAutoRenewable() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created successfully with number: {}", savedContract.getContractNumber());

        return mapToResponse(savedContract);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findAllContracts() {
        log.info("Finding all contracts");
        return contractRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findAllContracts(UUID userUuid, Role role) {
        return restrictToManagedBuildings(findAllContracts(), userUuid, role);
    }

    @Transactional(readOnly = true)
    public ContractResponse findByUuid(UUID uuid) {
        log.info("Finding contract by uuid: {}", uuid);
        Contract contract = contractRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with uuid: " + uuid));
        return mapToResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse findByUuid(UUID uuid, UUID userUuid, Role role) {
        ContractResponse contract = findByUuid(uuid);
        requireBuildingAccess(userUuid, role, contract.getBuildingUuid());
        return contract;
    }

    @Transactional
    public ContractResponse updateContract(UUID uuid, ContractUpdateRequest request, UUID userUuid, Role role) {
        log.info("Updating contract: {}", uuid);

        Contract contract = contractRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with uuid: " + uuid));
        requireBuildingAccess(userUuid, role, contract.getUnit().getBuilding().getUuid());

        if (request.getResidentUuid() != null) {
            Resident resident = residentRepository.findByUuid(request.getResidentUuid())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resident not found with uuid: " + request.getResidentUuid()));
            contract.setResident(resident);
        }

        if (request.getUnitUuid() != null) {
            Unit unit = unitRepository.findByUuid(request.getUnitUuid())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unit not found with uuid: " + request.getUnitUuid()));
            requireBuildingAccess(userUuid, role, unit.getBuilding().getUuid());
            contract.setUnit(unit);
        }

        if (request.getType() != null) contract.setType(request.getType());
        if (request.getStatus() != null) contract.setStatus(request.getStatus());
        if (request.getStartDate() != null) contract.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());
        if (request.getMonthlyRent() != null) contract.setMonthlyRent(request.getMonthlyRent());
        if (request.getDepositAmount() != null) contract.setDepositAmount(request.getDepositAmount());
        if (request.getDepositPaid() != null) contract.setDepositPaid(request.getDepositPaid());
        if (request.getPaymentFrequency() != null) contract.setPaymentFrequency(request.getPaymentFrequency());
        if (request.getPaymentDay() != null) contract.setPaymentDay(request.getPaymentDay());
        if (request.getTerms() != null) contract.setTerms(request.getTerms());
        if (request.getNotes() != null) contract.setNotes(request.getNotes());
        if (request.getRenewalNoticeDays() != null) contract.setRenewalNoticeDays(request.getRenewalNoticeDays());
        if (request.getAutoRenewable() != null) contract.setAutoRenewable(request.getAutoRenewable());
        if (request.getIsActive() != null) contract.setIsActive(request.getIsActive());

        Contract updatedContract = contractRepository.save(contract);
        log.info("Contract updated successfully: {}", uuid);

        return mapToResponse(updatedContract);
    }

    @Transactional
    public void deleteContract(UUID uuid, UUID userUuid, Role role) {
        log.info("Deleting contract: {}", uuid);
        Contract contract = contractRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with uuid: " + uuid));
        requireBuildingAccess(userUuid, role, contract.getUnit().getBuilding().getUuid());
        contractRepository.delete(contract);
        log.info("Contract deleted successfully: {}", uuid);
    }

    // Méthodes spécifiques

    @Transactional(readOnly = true)
    public List<ContractResponse> findByBuildingUuid(UUID buildingUuid) {
        log.info("Finding contracts by building: {}", buildingUuid);
        return contractRepository.findByBuildingUuid(buildingUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findByBuildingUuid(UUID buildingUuid, UUID userUuid, Role role) {
        if (role == Role.SYNDIC && !hasBuildingAccess(userUuid, buildingUuid)) return List.of();
        return restrictToManagedBuildings(findByBuildingUuid(buildingUuid), userUuid, role);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findByResidentUuid(UUID residentUuid) {
        log.info("Finding contracts by resident: {}", residentUuid);
        return contractRepository.findByResidentUuid(residentUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findByResidentUuid(UUID residentUuid, UUID userUuid, Role role) {
        return restrictToManagedBuildings(findByResidentUuid(residentUuid), userUuid, role);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findExpiringContracts(int daysAhead) {
        log.info("Finding contracts expiring in {} days", daysAhead);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        return contractRepository.findExpiringBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findExpiringContracts(int daysAhead, UUID userUuid, Role role) {
        return restrictToManagedBuildings(findExpiringContracts(daysAhead), userUuid, role);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findByFilters(UUID residentUuid, UUID unitUuid, UUID buildingUuid,
                                                ContractType type, ContractStatus status, Boolean isActive) {
        log.info("Finding contracts with filters");
        return contractRepository.findByFilters(residentUuid, unitUuid, buildingUuid, type, status, isActive).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findByFilters(UUID residentUuid, UUID unitUuid, UUID buildingUuid,
                                                ContractType type, ContractStatus status, Boolean isActive,
                                                UUID userUuid, Role role) {
        return restrictToManagedBuildings(
                findByFilters(residentUuid, unitUuid, buildingUuid, type, status, isActive), userUuid, role);
    }

    private List<ContractResponse> restrictToManagedBuildings(List<ContractResponse> contracts,
                                                                UUID userUuid, Role role) {
        if (role == Role.ADMIN) return contracts;
        if (role != Role.SYNDIC) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins and syndics can view contracts");
        }
        Set<UUID> managedBuildings = syndicBuildingRepository.findBySyndicUuid(userUuid).stream()
                .map(assignment -> assignment.getBuilding().getUuid())
                .collect(Collectors.toSet());
        return contracts.stream()
                .filter(contract -> managedBuildings.contains(contract.getBuildingUuid()))
                .collect(Collectors.toList());
    }

    private void requireBuildingAccess(UUID userUuid, Role role, UUID buildingUuid) {
        if (role == Role.ADMIN) return;
        if (role != Role.SYNDIC || !hasBuildingAccess(userUuid, buildingUuid)) {
            throw new org.springframework.security.access.AccessDeniedException("Contract is outside the syndic's assigned buildings");
        }
    }

    private boolean hasBuildingAccess(UUID userUuid, UUID buildingUuid) {
        return syndicBuildingRepository.existsBySyndicAndBuilding(userUuid, buildingUuid);
    }
}
