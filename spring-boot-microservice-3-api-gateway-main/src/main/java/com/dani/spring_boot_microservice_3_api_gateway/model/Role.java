package com.dani.spring_boot_microservice_3_api_gateway.model;

/**
 * Enumeración que define los roles de usuario posibles dentro del sistema API Gateway.
 * Estos roles son utilizados por Spring Security para la autorización basada en roles,
 * determinando los permisos y accesos de cada usuario a diferentes funcionalidades y endpoints.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-04 (Fecha de creación inicial o última modificación significativa)
 */
public enum Role {
    /** * Rol de usuario estándar.
     * Generalmente posee permisos para acceder a funcionalidades generales de la aplicación,
     * gestionar sus propios datos y realizar operaciones como compras.
     */
    USER,

    /** * Rol de administrador.
     * Posee permisos elevados para gestionar todos los aspectos del sistema,
     * incluyendo la administración de usuarios, inmuebles de otros usuarios,
     * y acceso a secciones administrativas.
     */
    ADMIN


}