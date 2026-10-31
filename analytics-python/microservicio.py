from fastapi import FastAPI, Query
from fastapi.responses import Response

from analisis import paquetes_mas_reservados, reservas_por_destino, reservas_por_mes
from carga import get_paquetes, get_reservas
from graficos import crear_grafico

app = FastAPI(title="Travel North Analytics", version="1.0.0")


@app.get("/api/health")
def health():
    return {"status": "UP", "application": "Travel North Analytics"}


@app.get("/api/graficos/headless/reservas-por-destino")
def headless_reservas_por_destino():
    return reservas_por_destino(get_reservas(), get_paquetes())


@app.get("/api/graficos/headless/reservas-por-mes")
def headless_reservas_por_mes():
    return reservas_por_mes(get_reservas())


@app.get("/api/graficos/headless/paquetes-mas-reservados")
def headless_paquetes_mas_reservados(top: int = Query(default=10, ge=1, le=100)):
    return paquetes_mas_reservados(get_reservas(), top)


@app.get("/api/graficos/pull/reservas-por-destino")
def pull_reservas_por_destino():
    data = reservas_por_destino(get_reservas(), get_paquetes())
    return Response(
        content=crear_grafico(data, "Viajeros por destino", "Viajeros", horizontal=True),
        media_type="image/png",
    )


@app.get("/api/graficos/pull/reservas-por-mes")
def pull_reservas_por_mes():
    data = reservas_por_mes(get_reservas())
    return Response(
        content=crear_grafico(data, "Reservas por mes", "Reservas"),
        media_type="image/png",
    )


@app.get("/api/graficos/pull/paquetes-mas-reservados")
def pull_paquetes_mas_reservados(top: int = Query(default=10, ge=1, le=100)):
    data = paquetes_mas_reservados(get_reservas(), top)
    return Response(
        content=crear_grafico(data, "Paquetes más reservados", "Reservas", horizontal=True),
        media_type="image/png",
    )
