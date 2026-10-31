import pandas as pd


def _reservas_activas(reservas: pd.DataFrame) -> pd.DataFrame:
    if reservas.empty:
        return reservas.copy()
    return reservas.loc[reservas["estado"].ne("CANCELADA")].copy()


def reservas_por_destino(reservas: pd.DataFrame, paquetes: pd.DataFrame) -> list[dict]:
    if reservas.empty or paquetes.empty:
        return []

    reservas_activas = _reservas_activas(reservas)
    paquetes_destinos = paquetes[["id", "destinos"]].explode("destinos").dropna(subset=["destinos"])
    if reservas_activas.empty or paquetes_destinos.empty:
        return []

    paquetes_destinos["destino"] = paquetes_destinos["destinos"].map(
        lambda destino: destino.get("nombre") if isinstance(destino, dict) else None
    )
    unidos = reservas_activas.merge(
        paquetes_destinos[["id", "destino"]],
        left_on="paqueteId",
        right_on="id",
        how="inner",
    ).dropna(subset=["destino"])
    if unidos.empty:
        return []

    resumen = (
        unidos.groupby("destino", as_index=False)["cantidadPersonas"]
        .sum()
        .rename(columns={"destino": "label", "cantidadPersonas": "value"})
        .sort_values(["value", "label"], ascending=[False, True])
    )
    resumen["value"] = resumen["value"].astype(int)
    return resumen.to_dict(orient="records")


def reservas_por_mes(reservas: pd.DataFrame) -> list[dict]:
    if reservas.empty:
        return []
    activas = _reservas_activas(reservas)
    if activas.empty:
        return []

    activas["mes"] = pd.to_datetime(activas["fechaReserva"], errors="coerce").dt.to_period("M")
    activas = activas.dropna(subset=["mes"])
    resumen = (
        activas.groupby("mes", as_index=False)
        .size()
        .rename(columns={"mes": "label", "size": "value"})
        .sort_values("label")
    )
    resumen["label"] = resumen["label"].astype(str)
    resumen["value"] = resumen["value"].astype(int)
    return resumen.to_dict(orient="records")


def paquetes_mas_reservados(reservas: pd.DataFrame, top: int = 10) -> list[dict]:
    if reservas.empty:
        return []
    activas = _reservas_activas(reservas).dropna(subset=["paqueteNombre"])
    if activas.empty:
        return []

    resumen = (
        activas.groupby("paqueteNombre", as_index=False)
        .size()
        .rename(columns={"paqueteNombre": "label", "size": "value"})
        .sort_values(["value", "label"], ascending=[False, True])
        .head(top)
    )
    resumen["value"] = resumen["value"].astype(int)
    return resumen.to_dict(orient="records")
