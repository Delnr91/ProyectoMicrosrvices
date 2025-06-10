# InmoRISM - Plataforma de Microservicios para Inmobiliaria

Este repositorio contiene el código fuente de **InmoRISM**, una aplicación de portafolio que simula un portal inmobiliario completo, desarrollado bajo una arquitectura de microservicios utilizando Java y el ecosistema Spring.

## 📄 Resumen General del Proyecto

**InmoRISM** es una plataforma web diseñada para la compra y venta de propiedades inmobiliarias. El proyecto demuestra la implementación de un sistema distribuido robusto, seguro y resiliente, siguiendo las mejores prácticas de la industria del software.

* **Objetivo**: Servir como una aplicación de demostración de habilidades avanzadas en desarrollo backend, cubriendo desde la autenticación de usuarios y la gestión de datos hasta patrones complejos de comunicación entre servicios.
* **Público Objetivo**: Usuarios que buscan publicar sus propiedades para la venta y usuarios interesados en adquirir nuevas propiedades.
* **Funcionalidad Principal**:
    * **Gestión de Usuarios**: Registro, inicio de sesión y gestión de roles (USER, ADMIN).
    * **Catálogo de Inmuebles**: Publicación, visualización, edición y eliminación de propiedades.
    * **Proceso de Compra**: Funcionalidad para que los usuarios adquieran inmuebles disponibles en el catálogo.
    * **Panel de Control (Dashboard)**: Vistas personalizadas para usuarios y administradores con estadísticas y acciones rápidas.

---

## 🏗️ Arquitectura del Software

El proyecto está construido siguiendo una **arquitectura de microservicios**, lo que garantiza una alta cohesión, bajo acoplamiento, escalabilidad independiente y resiliencia.

El sistema se compone de los siguientes servicios independientes:

1.  **`api-gateway` (Gateway de API y UI)**:
    * **Función**: Es el único punto de entrada para todas las peticiones del cliente. Se encarga de enrutar las peticiones a los servicios internos correspondientes.
    * **Responsabilidades**:
        * **Autenticación y Seguridad**: Gestiona el registro (`sign-up`) y la autenticación (`sign-in`) de usuarios, generando **Tokens JWT** para gestionar las sesiones.
        * **Servir la Interfaz de Usuario (UI)**: Renderiza las vistas del lado del servidor utilizando **Thymeleaf**.
        * **Proxy Inverso y Enrutamiento**: Utiliza un cliente **Feign** para comunicarse de forma declarativa con los microservicios internos (`inmueble-service`, `compra-service`).
        * **Patrón BFF (Backend for Frontend)**: Actúa como un backend específico para la interfaz de usuario web.

2.  **`inmueble-service` (Servicio de Inmuebles)**:
    * **Función**: Es el responsable de toda la lógica de negocio y persistencia de los datos relacionados con los inmuebles.
    * **Responsabilidades**:
        * Operaciones **CRUD** completas sobre los inmuebles.
        * Gestión de la lógica de negocio, como la validación de permisos para asegurar que solo el propietario o un administrador pueda modificar/eliminar un inmueble.
        * Expone una API REST segura para ser consumida por otros servicios.

3.  **`compra-service` (Servicio de Compras)**:
    * **Función**: Gestiona el proceso de compra de inmuebles.
    * **Responsabilidades**:
        * Registrar las transacciones de compra en su propia base de datos.
        * Orquestar la comunicación con el `inmueble-service` para actualizar el estado de una propiedad a "VENDIDO" tras una compra exitosa.

4.  **`eureka-server` (Registro y Descubrimiento de Servicios)**:
    * **Función**: Actúa como el registro de servicios del sistema.
    * **Responsabilidades**:
        * Cada microservicio se registra en Eureka al iniciar, informando su nombre y dirección.
        * Permite que los servicios se descubran entre sí dinámicamente, sin necesidad de codificar URLs fijas. Esto es fundamental para la resiliencia y escalabilidad del sistema.

### Patrones de Diseño y Conceptos Aplicados

* **Patrón DTO (Data Transfer Object)**: Se utilizan DTOs (`InmuebleDto`, `CompraDto`) para la comunicación entre el API Gateway y los servicios internos. Esto desacopla la capa de presentación de la capa de persistencia, permitiendo que la API evolucione sin exponer detalles de la base de datos.
* **Inyección de Dependencias (DI)**: Utilizada extensivamente en todo el proyecto a través de la inyección por constructor (`@RequiredArgsConstructor` de Lombok), siguiendo las mejores prácticas de Spring para lograr un bajo acoplamiento.
* **Patrón Repositorio**: La capa de acceso a datos se abstrae mediante interfaces que extienden `JpaRepository` de Spring Data JPA (ej. `InmuebleRepository`, `UserRepository`), simplificando enormemente las operaciones CRUD.
* **Circuit Breaker (Patrón de Cortocircuito)**: El API Gateway implementa un cortocircuito con **Resilience4j** en la llamada para obtener todos los inmuebles. Si el `inmueble-service` falla, en lugar de propagar el error, el gateway devuelve una respuesta de contingencia (fallback), mejorando la resiliencia del sistema.

---

## 🧠 Explicación Técnica Detallada

El flujo de datos y la lógica de negocio del sistema están cuidadosamente orquestados entre los diferentes microservicios.

### Flujo de Autenticación y Seguridad

1.  **Registro y Login**: El usuario interactúa con la UI servida por el `api-gateway`. Las peticiones de registro y login son manejadas por `AuthenticationController`.
2.  **Validación**: `UserService` y `AuthenticationService` validan las credenciales. Para nuevos usuarios, la contraseña se codifica usando `BCryptPasswordEncoder`.
3.  **Generación de JWT**: Tras una autenticación exitosa, `JwtProvider` genera un JSON Web Token (JWT) que contiene la identidad del usuario y sus roles.
4.  **Autorización por Petición**: En las peticiones subsecuentes, el cliente envía el JWT. El `JwtAuthorizationFilter` en el `api-gateway` intercepta este token, lo valida y establece el contexto de seguridad de Spring (`SecurityContextHolder`), autenticando al usuario para esa petición.

### Flujo de Comunicación Inter-Servicios

La comunicación entre los microservicios es un pilar fundamental de esta arquitectura:

1.  **Propagación de Contexto de Usuario**: Cuando un usuario autenticado realiza una acción (ej. crear un inmueble), el `api-gateway` no solo enruta la petición, sino que también propaga la identidad del usuario. El interceptor `UserContextRequestInterceptor` añade cabeceras HTTP (`X-User-ID`, `X-User-Roles`) a la petición Feign saliente.
2.  **Autorización en Servicios Internos**: Los servicios como `inmueble-service` leen estas cabeceras para aplicar su lógica de negocio. Por ejemplo, al eliminar un inmueble, `InmuebleServiceImpl` verifica si el `userId` de la petición coincide con el `userId` del inmueble o si el usuario tiene el rol de `ADMIN`.
3.  **Seguridad de Servicio a Servicio**: Para prevenir que los servicios internos sean llamados por clientes no autorizados, la comunicación está protegida por **Autenticación Básica**. Los clientes Feign (`InmuebleServiceRequest`, `CompraServiceRequest`) están configurados para incluir un encabezado de autorización básico en cada llamada.

---

## ✅ Buenas Prácticas y Calidad de Código

El proyecto se adhiere a principios de código limpio y buenas prácticas de ingeniería de software.

* **Separación de Responsabilidades (SoC)**: Se respeta estrictamente la arquitectura multicapa (Controlador → Servicio → Repositorio) dentro de cada microservicio. Los controladores manejan las peticiones HTTP, los servicios contienen la lógica de negocio y los repositorios se encargan de la persistencia.
* **Principio de Responsabilidad Única (SRP)**: Cada microservicio tiene una responsabilidad de negocio clara y definida (gestionar inmuebles, gestionar compras, etc.). A su vez, cada clase dentro de ellos tiene un propósito bien definido.
* **Principio de Inversión de Dependencias (DIP)**: Se depende de abstracciones (interfaces como `UserService`) en lugar de implementaciones concretas, facilitado por la inyección de dependencias de Spring.
* **Código Limpio**: Se utilizan nombres de variables y métodos descriptivos y se evita la duplicación de código (DRY).

## 📚 Documentación del Código

Una documentación clara es fundamental para la mantenibilidad y colaboración.

* **Javadoc**: El código fuente está ampliamente documentado utilizando Javadoc. Cada clase, método y sus parámetros tienen comentarios que explican su propósito y funcionamiento, facilitando la comprensión del código.
* **Swagger (OpenAPI 3)**: Los microservicios `inmueble-service` y `compra-service` utilizan la librería `springdoc-openapi` para generar automáticamente documentación interactiva de sus APIs. Esta documentación está disponible en la ruta `/swagger-ui.html` de cada servicio y permite a los desarrolladores explorar y probar los endpoints fácilmente.

---

## 🔧 Tecnologías Utilizadas

* **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA.
* **Arquitectura de Microservicios**:
    * **Service Discovery**: Netflix Eureka.
    * **Comunicación**: Spring Cloud OpenFeign.
    * **Resiliencia**: Resilience4j (Circuit Breaker).
* **Base de Datos**: PostgreSQL (para `inmueble-service` y `compra-service`), MySQL (para `api-gateway`).
* **Frontend (Renderizado en Servidor)**: Thymeleaf, HTML5, CSS3.
* **Build y Dependencias**: Apache Maven.
* **Pruebas**: JUnit 5, Mockito.
* **Documentación de API**: Springdoc (OpenAPI 3).

## 🧪 Pruebas

El proyecto incluye pruebas unitarias para garantizar la calidad y el correcto funcionamiento de la lógica de negocio.

* **Herramientas**: Se utiliza **JUnit 5** como framework de pruebas y **Mockito** para crear mocks y aislar las unidades bajo prueba.
* **Estructura**: Las pruebas se encuentran en el directorio estándar `src/test/java`. El fichero `UserServiceImplTest.java` es un ejemplo de cómo se testea la capa de servicio, mockeando las dependencias del repositorio y del proveedor JWT.
* **Ejecución**: Para ejecutar todas las pruebas del proyecto, sitúese en el directorio raíz de cada módulo y ejecute el siguiente comando Maven:
    ```bash
    mvn test
    ```
* **Reportes**: Maven Surefire genera reportes de las pruebas en formato `.txt` y `.xml` dentro del directorio `target/surefire-reports` de cada módulo después de la ejecución.

---

## 🚀 Instrucciones de Despliegue Local

Para ejecutar el proyecto completo en un entorno local, sigue estos pasos:

### Prerrequisitos

1.  **Java JDK 21** o superior instalado.
2.  **Apache Maven** instalado.
3.  **Bases de Datos**:
    * Una instancia de **MySQL** en ejecución.
    * Una instancia de **PostgreSQL** en ejecución.
4.  **Variables de Entorno**: Configura las siguientes variables de entorno con tus credenciales. Son necesarias para que los microservicios se conecten a las bases de datos y para la seguridad del JWT.
    * `DB_GATEWAY_PASSWORD`: Contraseña para el usuario de la base de datos de `api-gateway`.
    * `DB_INMUEBLE_PASSWORD`: Contraseña para el usuario de la base de datos de `inmueble-service`.
    * `DB_COMPRA_PASSWORD`: Contraseña para el usuario de la base de datos de `compra-service`.
    * `JWT_SECRET`: Una cadena de texto larga y segura para firmar los tokens JWT.

### Pasos de Ejecución

1.  **Crear las Bases de Datos**:
    * En MySQL, crea una base de datos llamada `db_gateway`.
    * En PostgreSQL, crea una base de datos `db_inmueble` con un schema `sc_inmueble`, y una base de datos `db_compra` con un schema `sc_compra`.
    * La primera vez que ejecutes los servicios, la propiedad `spring.jpa.hibernate.ddl-auto=update` creará las tablas necesarias. Para ejecuciones posteriores, se recomienda cambiarla a `validate`.

2.  **Ejecutar los Microservicios**:
    Es crucial ejecutar los servicios en el orden correcto debido a sus dependencias. Abre una terminal para cada servicio.

    * **1. Eureka Server**:
        ```bash
        cd spring-boot-microservice-4-eureka-main/
        mvn spring-boot:run
        ```
        Espera a que inicie. Puedes verificarlo en `http://localhost:6666`.

    * **2. Inmueble Service**:
        ```bash
        cd spring-boot-microservice-1-inmueble-main/
        mvn spring-boot:run
        ```

    * **3. Compra Service**:
        ```bash
        cd spring-boot-microservice-2-compra-main/
        mvn spring-boot:run
        ```

    * **4. API Gateway**:
        ```bash
        cd spring-boot-microservice-3-api-gateway-main/
        mvn spring-boot:run
        ```

3.  **Acceder a la Aplicación**:
    Una vez que todos los servicios estén en funcionamiento, puedes acceder a la aplicación web en `http://localhost:5555`.

---

## 🗺️ Guía de Contribución y Mejoras Futuras

Este proyecto es una excelente base para explorar conceptos más avanzados.

### Cómo Contribuir

1.  **Fork** el repositorio.
2.  Crea una nueva rama para tu funcionalidad (`git checkout -b feature/nombre-feature`).
3.  Realiza tus cambios y haz **commits** descriptivos.
4.  Haz **push** de tu rama a tu fork (`git push origin feature/nombre-feature`).
5.  Abre un **Pull Request** hacia la rama principal del repositorio original.

### Posibles Mejoras

* **Seguridad Avanzada**: Reemplazar la autenticación básica entre servicios por la **propagación del token JWT**. Esto centralizaría la seguridad en el token y eliminaría la necesidad de credenciales estáticas.
* **Contenerización con Docker**: Crear un `Dockerfile` para cada microservicio y un archivo `docker-compose.yml` para orquestar el levantamiento de todo el stack (incluidas las bases de datos) con un solo comando.
* **Integración Continua / Despliegue Continuo (CI/CD)**: Configurar un pipeline con **GitHub Actions** que compile, pruebe y empaquete los artefactos de cada microservicio automáticamente con cada push.
* **Configuración Centralizada**: Implementar **Spring Cloud Config Server** para gestionar las configuraciones de todos los microservicios desde un único repositorio Git, eliminando la duplicación y facilitando la gestión de diferentes entornos (dev, prod).
* **Frontend Moderno**: Migrar la interfaz de usuario de Thymeleaf a un framework de JavaScript moderno como **React, Angular o Vue**. Esto convertiría al `api-gateway` en una API REST pura, optimizada para servir a un cliente SPA (Single-Page Application).

---

## 📚 Anexo: Roadmap de Aprendizaje Sugerido

1.  **Fundamentos de Programación**
    * **Lógica de Programación**: Comprender algoritmos, estructuras de control (condicionales, bucles) y la descomposición de problemas.
    * **IDE**: Familiarizarse con IntelliJ IDEA o VSCode para escribir, compilar y depurar código.

2.  **Java Standard Edition (SE)**
    * **Sintaxis Básica**: Variables, tipos de datos (primitivos vs. referencia), operadores.
    * **Estructuras de Datos**: Manejo de Arrays y Matrices.
    * **Programación Orientada a Objetos (POO)**: Dominar los 4 pilares:
        * **Abstracción**: Clases abstractas e interfaces.
        * **Encapsulamiento**: Modificadores de acceso, getters/setters.
        * **Herencia**: `extends`, reutilización de código, `@Override`.
        * **Polimorfismo**: Tratar objetos de diferentes clases de manera uniforme.
    * **Conceptos Avanzados de Java**:
        * **Collections Framework**: List, Set, Map.
        * **Manejo de Excepciones**: `try-catch-finally`, checked vs. unchecked exceptions.
        * **Java 8+ Features**: Expresiones Lambda y la API de Streams para un código más funcional y conciso.

3.  **Bases de Datos y Persistencia**
    * **Bases de Datos Relacionales**: Entender tablas, relaciones, claves primarias (PK) y foráneas (FK).
    * **SQL**: Aprender a realizar consultas (SELECT, JOIN, subconsultas) y manipular datos (INSERT, UPDATE, DELETE).
    * **JPA y Hibernate**: Comprender el mapeo objeto-relacional (ORM) y cómo usar anotaciones JPA (`@Entity`, `@Id`, `@OneToMany`) para persistir objetos Java.

4.  **Desarrollo Web y Spring Boot**
    * **Fundamentos Web**: Protocolo HTTP (métodos GET/POST), arquitectura cliente-servidor.
    * **Spring Boot**:
        * **Core**: Inversión de Control (IoC) y la Inyección de Dependencias (DI).
        * **Patrón MVC**: Desarrollar controladores (`@RestController`), servicios (`@Service`) y repositorios (`@Repository`).
        * **Spring Data JPA**: Simplificar la capa de datos extendiendo `JpaRepository`.
        * **API REST**: Construir APIs RESTful utilizando las anotaciones de Spring MVC.
    * **Herramientas del Ecosistema**:
        * **Maven**: Para la gestión de dependencias y el ciclo de vida del build.
        * **Postman**: Para probar y depurar los endpoints de la API.

5.  **Arquitectura de Microservicios**
    * **Conceptos**: Entender las ventajas y desafíos de los microservicios frente a los monolitos.
    * **Spring Cloud**:
        * **Service Discovery**: `Eureka Server` para el registro y descubrimiento de servicios.
        * **Comunicación Declarativa**: `Feign Client` para simplificar las llamadas REST entre servicios.
        * **API Gateway**: Implementar un gateway para centralizar el acceso y la seguridad.
        * **Resiliencia**: Aplicar patrones como el **Circuit Breaker** con `Resilience4j`.

6.  **Seguridad**
    * **Spring Security**: Proteger los endpoints de la aplicación.
    * **Autenticación y Autorización**: Implementar seguridad basada en tokens usando **JWT**.

7.  **Pruebas y Herramientas Complementarias**
    * **Testing**: Escribir pruebas unitarias con **JUnit** y **Mockito**.
    * **Control de Versiones**: Usar **Git** y **GitHub** para la colaboración y el control del código fuente.
    * **Contenerización**: Aprender **Docker** para empaquetar y desplegar las aplicaciones de forma consistente.
    * **Metodologías Ágiles**: Familiarizarse con **Scrum** para la gestión de proyectos.

  
## 🤝 Cómo Contribuir

¡Las contribuciones son bienvenidas! Si deseas mejorar este proyecto, sigue estos pasos:

1.  **Haz un Fork** del repositorio: [https://github.com/Delnr91/ProyectoMicrosrvices](https://github.com/Delnr91/ProyectoMicrosrvices)
2.  **Crea una nueva Rama** para tu funcionalidad o corrección: `git checkout -b feature/nueva-funcionalidad` o `git checkout -b fix/correccion-bug`.
3.  **Realiza tus Cambios** y haz commits descriptivos.
4.  **Asegúrate** de que el código sigue las guías de estilo del proyecto (si existen) y que las pruebas (si existen) pasan.
5.  **Haz Push** a tu rama: `git push origin feature/nueva-funcionalidad`.
6.  **Abre un Pull Request** en el repositorio original, describiendo claramente tus cambios.

---

## 👨‍💻 Autor

* **Nombre:** Daniel Núñez Rojas
* **GitHub:** [@Delnr91](https://github.com/Delnr91)
* **LinkedIn:** [Daniel Núñez Rojas](https://www.linkedin.com/in/delnr91)
* **Correo:** [danidev33@gmail.com](mailto:danidev33@gmail.com)

---

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

```text
MIT License

Copyright (c) 2025 Daniel Núñez Rojas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR
