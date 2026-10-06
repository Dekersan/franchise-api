# Franchise API

API REST reactiva para gestionar franquicias, sus sucursales y los productos ofertados en cada sucursal.

Construida con **Spring Boot 4 + WebFlux**, **MongoDB**, **arquitectura hexagonal** y programación funcional. La infraestructura de datos se aprovisiona con **Terraform** en **MongoDB Atlas** y la aplicación se despliega con **Docker** en un servidor en la nube.

**Demo en producción**

- Swagger UI: http://72.61.79.27:8081/swagger-ui.html
- Health check: http://72.61.79.27:8081/actuator/health

---

## Tabla de contenido

1. [Tecnologías](#tecnologías)
2. [Arquitectura](#arquitectura)
3. [Modelo de datos](#modelo-de-datos)
4. [Endpoints](#endpoints)
5. [Ejecución en local](#ejecución-en-local)
6. [Pruebas](#pruebas)
7. [Ejemplo de uso](#ejemplo-de-uso)
8. [Infraestructura con Terraform](#infraestructura-con-terraform)
9. [Despliegue en producción](#despliegue-en-producción)
10. [Decisiones de diseño](#decisiones-de-diseño)
11. [Flujo de trabajo con Git](#flujo-de-trabajo-con-git)
12. [Mejoras futuras](#mejoras-futuras)

---

## Tecnologías

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1, Spring WebFlux (router functions) |
| Persistencia | MongoDB 8 con Spring Data Reactive MongoDB |
| Base de datos en la nube | MongoDB Atlas (clúster M0) |
| Infraestructura como código | Terraform (provider `mongodb/mongodbatlas` 2.x) |
| Contenedores | Docker (imagen multi-etapa) y Docker Compose |
| Documentación de la API | springdoc-openapi 3 (Swagger UI) |
| Pruebas | JUnit 5, AssertJ, Mockito, Reactor Test (`StepVerifier`), `WebTestClient` |
| Cobertura | JaCoCo |
| Build | Gradle |
| Otros | Lombok, Bean Validation, Spring Boot Actuator |

---

## Arquitectura

El proyecto sigue una **arquitectura hexagonal** (puertos y adaptadores). La regla principal es que **las dependencias solo apuntan hacia adentro**: el dominio no conoce Spring, ni MongoDB, ni HTTP.

```mermaid
flowchart LR
    Client([Cliente HTTP]) --> Router

    subgraph Infrastructure["infrastructure"]
        Router[FranchiseRouter + FranchiseHandler]
        Adapter[FranchiseMongoAdapter]
        Config[UseCaseConfig / OpenApiConfig]
    end

    subgraph Application["application"]
        UseCase[FranchiseUseCase]
    end

    subgraph Domain["domain"]
        Model[Franchise / Branch / Product]
        Port[[FranchiseRepositoryPort]]
    end

    Router --> UseCase
    UseCase --> Model
    UseCase --> Port
    Adapter -. implementa .-> Port
    Adapter --> Mongo[(MongoDB)]
```

```
src/main/java/com/dekersan/franchise_api
├── domain
│   ├── model          → Franchise, Branch, Product, TopStockProduct (records inmutables con reglas de negocio)
│   ├── exception      → ResourceNotFoundException, DuplicateResourceException
│   └── port           → FranchiseRepositoryPort
├── application
│   └── usecase        → FranchiseUseCase (orquesta: buscar → transformar → guardar)
└── infrastructure
    ├── config         → registro de beans y configuración de OpenAPI
    ├── entrypoint/web → router functions, handler, DTOs, validación y manejo de errores
    └── persistence/mongo → documentos, repositorio reactivo, mapper y adaptador
```

- **Dominio:** los modelos son `records` inmutables. Las operaciones (agregar sucursal, renombrar producto, calcular el producto con más stock, etc.) devuelven una copia nueva en lugar de modificar el objeto.
- **Aplicación:** `FranchiseUseCase` no tiene anotaciones de Spring; se registra como bean desde `infrastructure/config`, lo que mantiene la capa independiente del framework.
- **Infraestructura:** los endpoints usan **router functions** de WebFlux (estilo funcional) y el adaptador de Mongo implementa el puerto del dominio.

---

## Modelo de datos

Cada franquicia se guarda como **un solo documento** en la colección `franchises`, con sus sucursales y productos anidados:

```json
{
  "_id": "2f2aaa7b-828f-49fc-8bda-b764551e7e01",
  "name": "Burger House",
  "branches": [
    {
      "id": "a1a96da8-eec1-4fbc-8e97-2d95a0433e20",
      "name": "Sucursal Centro",
      "products": [
        { "id": "c5a94bee-3edd-4a6a-afdd-ae6ce5924571", "name": "Hamburguesa Doble", "stock": 50 }
      ]
    }
  ]
}
```

---

## Endpoints

Base path: `/api/franchises`

| Método | Ruta | Descripción | Respuesta exitosa |
|---|---|---|---|
| POST | `/api/franchises` | Crear una franquicia | `201` |
| PATCH | `/api/franchises/{franchiseId}/name` | Actualizar el nombre de una franquicia | `200` |
| GET | `/api/franchises/{franchiseId}/top-stock-products` | Producto con más stock por sucursal | `200` |
| POST | `/api/franchises/{franchiseId}/branches` | Agregar una sucursal | `201` |
| PATCH | `/api/franchises/{franchiseId}/branches/{branchId}/name` | Actualizar el nombre de una sucursal | `200` |
| POST | `/api/franchises/{franchiseId}/branches/{branchId}/products` | Agregar un producto | `201` |
| DELETE | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar un producto | `204` |
| PATCH | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar el stock | `200` |
| PATCH | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar el nombre de un producto | `200` |

**Cuerpos de las peticiones**

| Petición | Cuerpo |
|---|---|
| Crear franquicia, agregar sucursal, actualizar cualquier nombre | `{ "name": "Burger House" }` |
| Agregar producto | `{ "name": "Hamburguesa Doble", "stock": 50 }` |
| Modificar stock | `{ "stock": 120 }` |

**Errores**

Todos los errores responden con el mismo formato:

```json
{ "status": 404, "error": "Not Found", "message": "Franquicia no encontrada: abc-123" }
```

| Código | Cuándo ocurre |
|---|---|
| `400` | Cuerpo vacío, JSON inválido, nombre vacío, stock faltante o negativo |
| `404` | La franquicia, sucursal o producto no existe |
| `409` | Ya existe una franquicia con ese nombre, una sucursal con ese nombre en la franquicia, o un producto con ese nombre en la sucursal |

La documentación interactiva completa está en Swagger UI: `/swagger-ui.html`.

---

## Ejecución en local

### Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose)
- [JDK 21](https://adoptium.net/) — solo para la opción B o para ejecutar las pruebas
- Git

### Clonar el repositorio

```bash
git clone https://github.com/Dekersan/franchise-api.git
cd franchise-api
```

### Opción A: todo con Docker (recomendada)

Levanta MongoDB y la aplicación en contenedores, sin necesidad de instalar Java:

```bash
docker compose up -d --build
```

La primera vez tarda unos minutos mientras descarga las imágenes y compila. Cuando termine:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

Comandos útiles:

```bash
docker compose ps              # estado de los contenedores
docker compose logs -f app     # logs de la aplicación
docker compose down            # detener todo
```

### Opción B: MongoDB en Docker y la aplicación con Gradle

Útil para desarrollar, porque permite reiniciar la aplicación rápidamente:

```bash
docker compose up -d mongodb
```

```bash
# Linux / macOS
./gradlew bootRun

# Windows (PowerShell)
.\gradlew.bat bootRun
```

### Configuración

La conexión a MongoDB se define con la variable de entorno `MONGODB_URI`. Si no existe, la aplicación usa el MongoDB local de Docker Compose:

```
mongodb://admin:admin123@localhost:27017/franchises?authSource=admin
```

Para conectarse a otra base de datos (por ejemplo, Atlas), basta con definir la variable antes de arrancar:

```bash
export MONGODB_URI="mongodb+srv://usuario:contrasena@cluster.mongodb.net/franchises?retryWrites=true&w=majority"
```

---

## Pruebas

```bash
# Linux / macOS
./gradlew test

# Windows (PowerShell)
.\gradlew.bat test
```

Las pruebas no necesitan MongoDB ni Docker: el repositorio y el caso de uso se simulan con Mockito.

| Capa | Clase de prueba | Qué valida |
|---|---|---|
| Dominio | `BranchTest`, `FranchiseTest` | Reglas de negocio, inmutabilidad, nombres duplicados, cálculo del producto con más stock |
| Aplicación | `FranchiseUseCaseTest` | Flujos reactivos con `StepVerifier` y repositorio simulado |
| Web | `FranchiseRouterTest` | Rutas, códigos HTTP, validaciones y formato de errores con `WebTestClient` |

Reportes generados:

- Resultados de pruebas: `build/reports/tests/test/index.html`
- Cobertura (JaCoCo): `build/reports/jacoco/test/html/index.html`

---

## Ejemplo de uso

Flujo completo con `curl` (reemplaza los IDs con los que devuelve cada respuesta):

```bash
BASE=http://localhost:8080/api/franchises

# 1. Crear franquicia
curl -X POST $BASE -H "Content-Type: application/json" -d '{"name": "Franquicia 2"}'

# 2. Agregar sucursal
curl -X POST $BASE/{franchiseId}/branches -H "Content-Type: application/json" -d '{"name": "Sucursal Centro"}'

# 3. Agregar productos
curl -X POST $BASE/{franchiseId}/branches/{branchId}/products -H "Content-Type: application/json" -d '{"name": "Hamburguesa Doble", "stock": 50}'
curl -X POST $BASE/{franchiseId}/branches/{branchId}/products -H "Content-Type: application/json" -d '{"name": "Papas Fritas", "stock": 80}'

# 4. Modificar stock
curl -X PATCH $BASE/{franchiseId}/branches/{branchId}/products/{productId}/stock -H "Content-Type: application/json" -d '{"stock": 120}'

# 5. Producto con más stock por sucursal
curl $BASE/{franchiseId}/top-stock-products

# 6. Eliminar producto
curl -X DELETE $BASE/{franchiseId}/branches/{branchId}/products/{productId}
```

Respuesta del paso 5:

```json
[
  {
    "branchId": "a1a96da8-...",
    "branchName": "Sucursal Centro",
    "productId": "c5a94bee-...",
    "productName": "Hamburguesa Doble",
    "stock": 120
  }
]
```

---

## Infraestructura con Terraform

La base de datos de producción se aprovisiona en **MongoDB Atlas** con Terraform. Los archivos están en `infra/terraform` y crean:

| Recurso | Descripción |
|---|---|
| `mongodbatlas_project` | Proyecto `franchise-api` |
| `mongodbatlas_advanced_cluster` | Clúster gratuito M0 en AWS |
| `random_password` | Contraseña generada automáticamente para el usuario de la aplicación |
| `mongodbatlas_database_user` | Usuario `franchise_app` con rol `readWrite` solo sobre la base `franchises` y solo en este clúster |
| `mongodbatlas_project_ip_access_list` | IPs autorizadas para conectarse (servidor de producción y equipo de desarrollo) |

### Requisitos

- [Terraform](https://developer.hashicorp.com/terraform/install) 1.9 o superior
- Una cuenta de MongoDB Atlas con una **cuenta de servicio** a nivel de organización con el rol *Organization Project Creator* (Atlas → Identity & Access → Applications → Service Accounts)

### Pasos

```bash
cd infra/terraform

# 1. Credenciales de la cuenta de servicio (nunca se guardan en archivos)
export MONGODB_ATLAS_CLIENT_ID="..."
export MONGODB_ATLAS_CLIENT_SECRET="..."

# 2. Variables propias a partir de la plantilla
cp terraform.tfvars.example terraform.tfvars
# editar terraform.tfvars: atlas_org_id y allowed_ip_addresses

# 3. Crear la infraestructura
terraform init
terraform plan
terraform apply

# 4. Obtener la URI de conexión para la aplicación
terraform output -raw mongodb_uri
```

En PowerShell, las variables de entorno se definen con `$env:MONGODB_ATLAS_CLIENT_ID = "..."`.

Para rotar la contraseña de la base de datos:

```bash
terraform apply -replace="random_password.db"
```

> **Seguridad:** `terraform.tfvars`, `terraform.tfstate` y la carpeta `.terraform` están excluidos en `.gitignore`. El archivo de estado contiene la contraseña generada, por lo que se mantiene solo en local.

---

## Despliegue en producción

La aplicación corre en un VPS con Docker y se conecta a MongoDB Atlas.

```
Cliente  →  VPS (Docker: franchise-api, puerto 8081)  →  MongoDB Atlas
```

El archivo `docker-compose.prod.yml` levanta solo la aplicación (sin MongoDB local), con reinicio automático y health check.

### Pasos en el servidor

```bash
# 1. Clonar el repositorio
git clone https://github.com/Dekersan/franchise-api.git /opt/franchise-api
cd /opt/franchise-api

# 2. Crear el archivo .env a partir de la plantilla
cp .env.example .env
nano .env   # pegar la URI obtenida con `terraform output -raw mongodb_uri`

# 3. Construir y levantar
docker compose -f docker-compose.prod.yml up -d --build

# 4. Verificar
docker compose -f docker-compose.prod.yml logs -f
curl http://localhost:8081/actuator/health
```

Variables del archivo `.env`:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `MONGODB_URI` | URI de conexión a MongoDB Atlas | — (obligatoria) |
| `APP_PORT` | Puerto expuesto en el servidor | `8081` |

### Actualizar el despliegue

```bash
cd /opt/franchise-api
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

---

## Decisiones de diseño

**MongoDB con documentos anidados.** El dominio es jerárquico (franquicia → sucursales → productos), así que cada franquicia es un solo documento. Una lectura trae la franquicia completa, cada modificación es atómica a nivel de documento, y el cálculo del producto con más stock se hace en memoria sobre un único documento.

**Programación reactiva y funcional.** Todo el flujo, desde el router hasta el repositorio, es no bloqueante con `Mono` y `Flux`. Los modelos son `records` inmutables y las modificaciones se expresan como funciones (`UnaryOperator<Branch>`, `UnaryOperator<Franchise>`), de modo que el caso de uso concentra el patrón *buscar → transformar → guardar* en dos métodos reutilizables.

**Router functions en lugar de controllers.** Los endpoints se declaran como funciones (`RouterFunctions.route()`), y los errores de negocio se traducen a códigos HTTP en un solo lugar mediante `onError`. Ni el dominio ni el caso de uso conocen HTTP.

**Identificadores UUID.** Las sucursales y productos no tienen colección propia, así que la aplicación genera sus IDs. Se usa `UUID` (estándar de Java) para no acoplar la capa de aplicación a tipos de MongoDB como `ObjectId`; además, los UUID no son adivinables y no requieren consultar la base de datos.

**Reglas de nombres únicos.** No puede haber dos franquicias con el mismo nombre, dos sucursales con el mismo nombre en una franquicia, ni dos productos con el mismo nombre en una sucursal. En sucursales y productos la comparación ignora mayúsculas y minúsculas. Al renombrar, se permite cambiar solo las mayúsculas del propio nombre.

**Producto con más stock por sucursal.** Las sucursales sin productos no aparecen en el resultado. Si dos productos de una sucursal empatan en stock, se devuelve uno de ellos.

**Validación.** Los DTOs usan Bean Validation (`@NotBlank`, `@NotNull`, `@PositiveOrZero`). El stock se recibe como `Integer` para distinguir entre un valor ausente y un cero.

**Configuración por variables de entorno.** La URI de MongoDB y el puerto de producción se inyectan por variables de entorno, así la misma imagen funciona en local y en producción, y ningún secreto se versiona.

**Imagen Docker multi-etapa.** Se compila con el JDK y se ejecuta sobre una imagen JRE Alpine, con un usuario sin privilegios. Las dependencias de Gradle se copian en una capa separada para aprovechar la caché.

---

## Flujo de trabajo con Git

Se usó un **GitFlow simplificado**:

- `main`: versiones estables y desplegables.
- `develop`: rama de integración.
- `feature/*`: una rama por funcionalidad, integrada a `develop` mediante Pull Request.

Los commits siguen **Conventional Commits** (`feat:`, `fix:`, `test:`, `docs:`, `build:`, `chore:`).

---

## Mejoras futuras

- **Bloqueo optimista** (`@Version`) para evitar que dos peticiones concurrentes sobre la misma franquicia se sobrescriban.
- **Estado remoto de Terraform** (por ejemplo, S3 o Terraform Cloud) en lugar de un archivo local.
- **HTTPS** mediante un proxy inverso con certificado TLS.
- **Integración continua** con GitHub Actions para ejecutar las pruebas en cada Pull Request.
- **UUID v7** para identificadores ordenados en el tiempo, más eficientes en índices con grandes volúmenes.
- Unicidad del nombre de franquicia sin distinguir mayúsculas, respaldada por un índice único en MongoDB.
- Implementar Token.
