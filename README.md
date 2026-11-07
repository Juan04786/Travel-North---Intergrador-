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

La API queda disponible en `http://localhost:8080`. Swagger UI está en `http://localhost:8080/swagger-ui.html`; los documentos OpenAPI están en `/api-docs`.

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

Vercel aloja el frontend y el microservicio FastAPI definidos en `vercel.json`. El backend Java y PostgreSQL se despliegan con el Blueprint `render.yaml`; el `Dockerfile` de la raíz construye Spring Boot. La configuración enlaza automáticamente el backend con la base de datos.

### Pasos manuales para publicar

1. Sube estos cambios a la rama principal del repositorio en GitHub.
2. En Vercel, importa el repositorio con la raíz del proyecto seleccionada y despliega los servicios `front` y `analytics-python` definidos en `vercel.json`. Anota la URL pública del frontend y la URL pública del servicio FastAPI.
3. En Render, crea un **Blueprint** desde el mismo repositorio y acepta los recursos de `render.yaml`. Cuando solicite variables:
   - `ANALYTICS_BASE_URL`: URL pública completa de FastAPI, por ejemplo `https://analytics-python.example.vercel.app` (sin `/api`).
   - `CORS_ALLOWED_ORIGIN`: origen del frontend Vercel, por ejemplo `https://travel-north.example.vercel.app` (sin ruta final ni `/`).
4. Espera que Render termine de desplegar `travel-north-api` y la base de datos. Comprueba `https://<URL-DEL-BACKEND>/api/health/status`; debe responder JSON con `"status":"UP"`.
5. En la configuración de producción de Vercel, añade estas variables y vuelve a desplegar los servicios afectados:

   | Servicio | Variable | Valor |
   |---|---|---|
   | `front` | `VITE_API_BASE_URL` | `https://<URL-DEL-BACKEND>/api` |
   | `analytics-python` | `API_BASE_URL` | `https://<URL-DEL-BACKEND>/api` |

6. Prueba la aplicación publicada: consulta destinos/paquetes y abre los gráficos. Si aparece un error de CORS, verifica que `CORS_ALLOWED_ORIGIN` sea exactamente el origen del frontend y actualiza esa variable en Render.

No escribas contraseñas en el repositorio. Render genera y conecta las credenciales de la base de datos automáticamente. El Blueprint usa el plan gratuito de PostgreSQL, que Render ofrece solo durante 30 días; **la base gratuita caduca y no es apropiada para conservar datos**. Render permite 14 días adicionales para actualizarla antes de eliminar los datos. Para guardar datos a largo plazo, antes de crear el Blueprint cambia `plan: free` de `travel-north-db` en `render.yaml` por un plan persistente de pago disponible en Render, o configura un PostgreSQL externo y define en Render `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD`.

`VITE_API_BASE_URL` se incorpora al compilar el frontend, por eso es necesario volver a desplegar después de añadirla. `CORS_ALLOWED_ORIGIN` conserva por defecto `http://localhost:5173` para desarrollo local.

## API principal

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
