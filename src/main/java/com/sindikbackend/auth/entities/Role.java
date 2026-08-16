package com.sindikbackend.auth.entities;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Administrateur Système", new String[]{"MANAGE_USERS", "MANAGE_ROLES", "VIEW_ANALYTICS"}),
    SYNDIC("Syndic de Copropriété", new String[]{"MANAGE_RESIDENTS", "MANAGE_FINANCES", "VIEW_REPORTS"}),
    RESIDENT("Résident/Copropriétaire", new String[]{"VIEW_BALANCE", "PAY_BILLS", "VIEW_DOCUMENTS"}),
    FINANCE_MANAGER("Gestionnaire Financier", new String[]{"MANAGE_FINANCES", "VIEW_REPORTS", "EXPORT_DATA"});

    private final String description;
    private final String[] defaultPermissions;

    Role(String description, String[] defaultPermissions) {
        this.description = description;
        this.defaultPermissions = defaultPermissions;
    }
}
