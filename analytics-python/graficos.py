from io import BytesIO

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd


def crear_grafico(
    datos: list[dict],
    titulo: str,
    etiqueta_x: str,
    horizontal: bool = False,
) -> bytes:
    figura, eje = plt.subplots(figsize=(9, 5))
    if datos:
        resumen = pd.DataFrame(datos)
        if horizontal:
            eje.barh(resumen["label"], resumen["value"], color="#0f766e")
            eje.invert_yaxis()
            eje.set_xlabel(etiqueta_x)
        else:
            eje.bar(resumen["label"], resumen["value"], color="#0891b2")
            eje.set_ylabel(etiqueta_x)
            eje.tick_params(axis="x", rotation=35)
    else:
        eje.text(0.5, 0.5, "Sin datos disponibles", ha="center", va="center")
        eje.set_axis_off()

    eje.set_title(titulo)
    figura.tight_layout()
    salida = BytesIO()
    figura.savefig(salida, format="png", bbox_inches="tight")
    plt.close(figura)
    return salida.getvalue()
