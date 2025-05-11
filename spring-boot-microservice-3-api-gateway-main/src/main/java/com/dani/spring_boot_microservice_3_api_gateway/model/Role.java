package com.dani.spring_boot_microservice_3_api_gateway.model;

/**
 * Enumeración que define los roles de usuario posibles en el sistema.
 * Utilizado para la autorización basada en roles con Spring Security.
 */
public enum Role {
    /** Rol de usuario estándar con permisos básicos. */
    USER,
    /** Rol de administrador con permisos elevados. */
    ADMIN
    // Se podrían añadir más roles si fuera necesario (ej. DEV, MANAGER, etc.)
}