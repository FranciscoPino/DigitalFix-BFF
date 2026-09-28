# ms-digitalfix-bff

Backend For Frontend de DigitalFix. Recibe las llamadas del frontend y las reenvía a los servicios de dominio. Es una fachada/proxy HTTP; no tiene base de datos propia.

## Arquitectura y patrón

```text
React + MSAL -> BFF (8080) -> Workorders (8081)
												 -> Catalog (8082)
												 -> Report (8084)
												 -> Audit (8085)
```

El BFF conserva los paths del contrato del caso y reenvía `Authorization: Bearer ...` al servicio destino. Spring Security Resource Server valida la firma, `issuer` y `audience` del access token de Microsoft Entra ID. El claim `roles` se normaliza a `ROLE_Admin`, `ROLE_Supervisor`, `ROLE_Cliente` y `ROLE_Auditor`.

## Permisos

| Operación | Roles |
|---|---|
| Consultar/crear órdenes | Admin, Supervisor, Cliente |
| Cambiar estado | Admin, Supervisor |
| Leer servicios | Admin, Supervisor, Cliente |
| Administrar servicios | Admin |
| Administrar repuestos | Admin |
| Reportería | Admin |
| Auditoría | Admin, Auditor |

La consulta de órdenes requiere autenticación. La visibilidad del frontend no reemplaza la autorización del BFF.

## Endpoints

| Método | Ruta BFF | Destino |
|---|---|---|
| POST | `/api/workorders` | Workorders |
| GET | `/api/workorders` | Workorders; admite `status`, `customerId` |
| GET | `/api/workorders/{id}` | Workorders |
| PUT | `/api/workorders/{id}/status` | Workorders |
| GET/POST | `/api/catalog/services` | Catalog |
| PUT | `/api/catalog/services/{id}` | Catalog |
| GET/POST | `/api/catalog/spare-parts` | Catalog |
| PUT | `/api/catalog/spare-parts/{id}` | Catalog |
| GET | `/api/report/kpis?range=last24h` | Report |
| GET | `/api/report/top-services?range=last7d` | Report |
| GET | `/api/audit/{workOrderId}` | Audit |
| GET | `/api/audit?userId=...&eventType=...` | Audit |

## Configuración local

El proyecto importa `.env` desde el directorio del repositorio. Completa Tenant/audience y las URLs de los servicios en ese archivo. No subas secretos reales a Git.

```env
SERVER_PORT=8080
SECURITY_ENABLED=true
AZURE_ISSUER_URI=https://sts.windows.net/<TENANT_ID>/
AZURE_API_AUDIENCE=api://<API_CLIENT_ID>
CORS_ALLOWED_ORIGINS=http://localhost:5173
WORKORDERS_SERVICE_URL=http://localhost:8081
CATALOG_SERVICE_URL=http://localhost:8082
REPORT_SERVICE_URL=http://localhost:8084
AUDIT_SERVICE_URL=http://localhost:8085
```

Para desarrollo aislado se puede usar `SECURITY_ENABLED=false`; no uses ese modo en producción.
El BFF admite issuer v2 (`login.microsoftonline.com/<tenant>/v2.0`) e issuer v1 (`sts.windows.net/<tenant>/`). Si los tokens de Entra traen `iss=https://sts.windows.net/.../`, configura el mismo valor exacto en el JWT Authorizer de API Gateway y en el secret `AZURE_ISSUER_URI`; el BFF obtiene las claves v1 desde el endpoint JWKS del tenant.

## Ejecutar y probar

```powershell
mvn clean verify
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

En Swagger pulsa **Authorize** y pega un access token válido cuando la seguridad esté habilitada. También puedes usar Postman con `{{baseUrl}}=http://localhost:8080` y `Authorization: Bearer {{accessToken}}`.

Ejemplo de creación desde Postman (`POST /api/workorders`, JSON):

```json
{
	"customerId": "cliente@empresa.cl",
	"serviceId": "<UUID_DEVUELTO_POR_CATALOG>",
	"title": "Revisión de tablero",
	"description": "Falla en circuito de enchufes",
	"address": "Av. Central 123"
}
```

El ID de la orden lo genera Workorders. En despliegue conjunto consulta [`DEPLOYMENT_DIGITALFIX.md`](../DEPLOYMENT_DIGITALFIX.md) para secrets, GHCR y EC2.
