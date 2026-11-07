# Travel North

Plataforma web para la gestión de una agencia de viajes. Permite administrar destinos turísticos, paquetes y reservas, y consultar estadísticas de viajeros y reservas.

## Arquitectura

- `src/` (backend de este directorio): API REST en Java 17 y Spring Boot, organizada en controladores, servicios, repositorios, modelos y DTOs. PostgreSQL almacena la información; Swagger documenta los endpoints.
- `Front/`: panel React + Vite para consultar, crear, editar y eliminar datos, además de mostrar gráficos.
- `analytics-python/`: microservicio FastAPI que obtiene datos del backend, calcula indicadores con Pandas y produce gráficos PNG con Matplotlib.

## Requisitos

- Java 17 o superior
- PostgreSQL
- Node.js y npm
- Python 3.10 o superior

## Preparar PostgreSQL y el backend

1. Crea una base de datos llamada `travelnorth` en PostgreSQL.
2. Copia `.env.template` como `.env` y configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Si no creas el archivo, la aplicación usa los valores locales de `application.yml`.
3. Desde esta carpeta, inicia Spring Boot:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

La API queda disponible en `http://localhost:8080`. La documentación interactiva Scalar está en `http://localhost:8080/scalar` (la raíz redirige allí), Swagger UI en `http://localhost:8080/swagger-ui.html` y el documento OpenAPI JSON en `http://localhost:8080/api-docs`.

## Iniciar analítica Python

En una terminal aparte, inicia Spring Boot primero y luego ejecuta:

```powershell
cd analytics-python
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:API_BASE_URL = "http://localhost:8080/api"
uvicorn microservicio:app --reload --port 8000
```

La API Spring reenvía las solicitudes analíticas a este servicio mediante `ANALYTICS_BASE_URL` (por defecto `http://localhost:8000`). El microservicio toma los datos de `/api/reservas` y `/api/paquetes`. Para cambiar la dirección del backend, copia `analytics-python/.env.example` a `analytics-python/.env` y cambia `API_BASE_URL`.

## Iniciar el frontend

```powershell
cd Front
npm install
npm run dev
```

Vite muestra la dirección local del frontend (normalmente `http://localhost:5173`). Para otro backend, define `VITE_API_BASE_URL`, incluyendo el prefijo `/api`.

## Despliegue

El Blueprint `render.yaml` despliega el backend Spring Boot y el microservicio FastAPI en Render, y conecta ambos a la misma API. El backend usa la base de datos Neon ya configurada; así no se crea una segunda base vacía y se conservan los datos de demostración. El `Dockerfile` de la raíz construye Spring Boot.

### Publicar el backend y la analítica en Render

1. Sube los cambios del proyecto a la rama `main` de GitHub.
2. En Render, crea un **Blueprint** desde el repositorio y acepta los servicios `travel-north-api` y `travel-north-analytics`.
3. En los valores solicitados para el backend Java, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con las credenciales de Neon. La URL debe ser JDBC y contener el host y la base de datos, pero **no** el usuario ni la contraseña: `jdbc:postgresql://<host>/<base>?sslmode=require`. No subas esos valores a Git.
4. Espera a que ambos servicios terminen de desplegarse. Comprueba `https://<URL-API-RENDER>/api/health/status` y `https://<URL-ANALITICA-RENDER>/api/health`; cada uno debe responder con `"status":"UP"`.
5. El servicio de analítica usa la URL pública del backend y agrega `/api` automáticamente. El backend usa la URL pública del servicio FastAPI. Render configura estas referencias entre servicios desde el Blueprint.

Los servicios están en plan gratuito para la prueba: pueden suspenderse tras 15 minutos sin tráfico y tardar alrededor de un minuto en despertar. No son recomendables para producción con disponibilidad continua.

### Conectar el frontend en Vercel

Después de desplegar el frontend, configura en Vercel `VITE_API_BASE_URL=https://<URL-API-RENDER>/api` para el servicio `front` y vuelve a desplegarlo. En Render, `CORS_ALLOWED_ORIGINS` permite el entorno local y los despliegues `*.vercel.app`. Si usas un dominio personalizado, agrega su origen exacto a esa variable, separado por comas y sin ruta ni `/` final. `VITE_API_BASE_URL` se incorpora al compilar el frontend.

## API principal

En Render, abre `https://<URL-DEL-BACKEND>/scalar` para explorar y probar los endpoints. El documento OpenAPI JSON está disponible en `https://<URL-DEL-BACKEND>/api-docs`.

| Recurso | Rutas |
|---|---|
| Destinos | `GET/POST /api/destinos`, `GET/PUT/DELETE /api/destinos/{id}` |
| Paquetes | `GET/POST /api/paquetes`, `GET/PUT/DELETE /api/paquetes/{id}` |
| Reservas | `GET/POST /api/reservas`, `GET/PUT/DELETE /api/reservas/{id}` |
| Salud | `GET /api/health`, `GET /api/health/status` |
| Análisis JSON | `GET /api/graficos/headless/{reservas-por-destino,reservas-por-mes,paquetes-mas-reservados}` |
| Gráficos PNG | `GET /api/graficos/pull/{reservas-por-destino,reservas-por-mes,paquetes-mas-reservados}` |

Las respuestas JSON de analítica usan registros `{ "label": "...", "value": 0 }` para alimentar los gráficos de React. El endpoint de paquetes admite `?top=10` (de 1 a 100). Las estadísticas excluyen reservas canceladas: viajeros por destino, reservas registradas por mes y paquetes con más reservas. Una reserva valida que el paquete esté activo, que la fecha del viaje no haya comenzado y que haya cupos suficientes.

## Validación

Desde la carpeta raíz `travel-north-api` puedes ejecutar las pruebas de integración del backend:

```powershell
.\mvnw.cmd test
```

El perfil de pruebas utiliza H2 en memoria, por lo que las pruebas no necesitan una instancia PostgreSQL. Para validar el frontend, ejecuta `npm run build` y `npm run lint` desde `Front`. Para comprobar los cálculos Python, ejecuta `py -m unittest -v test_analisis` desde `analytics-python`.
