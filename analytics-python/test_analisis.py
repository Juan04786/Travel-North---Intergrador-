import unittest

import pandas as pd

from analisis import paquetes_mas_reservados, reservas_por_destino, reservas_por_mes


class AnalisisTests(unittest.TestCase):
    def setUp(self):
        self.reservas = pd.DataFrame([
            {"estado": "CONFIRMADA", "cantidadPersonas": 3, "fechaReserva": "2026-10-12", "paqueteNombre": "Andes", "paqueteId": 1},
            {"estado": "PENDIENTE", "cantidadPersonas": 2, "fechaReserva": "2026-10-20", "paqueteNombre": "Andes", "paqueteId": 1},
            {"estado": "CANCELADA", "cantidadPersonas": 4, "fechaReserva": "2026-10-21", "paqueteNombre": "Costa", "paqueteId": 2},
        ])
        self.paquetes = pd.DataFrame([
            {"id": 1, "destinos": [{"nombre": "Medellín"}, {"nombre": "Bogotá"}]},
            {"id": 2, "destinos": [{"nombre": "Cartagena"}]},
        ])

    def test_viajeros_por_destino_suma_personas_y_ignora_canceladas(self):
        self.assertEqual(
            reservas_por_destino(self.reservas, self.paquetes),
            [{"label": "Bogotá", "value": 5}, {"label": "Medellín", "value": 5}],
        )

    def test_reservas_por_mes_ignora_canceladas(self):
        self.assertEqual(
            reservas_por_mes(self.reservas),
            [{"label": "2026-10", "value": 2}],
        )

    def test_top_paquetes_ignora_canceladas_y_respeta_limite(self):
        self.assertEqual(
            paquetes_mas_reservados(self.reservas, top=1),
            [{"label": "Andes", "value": 2}],
        )

    def test_resumenes_sin_datos_devuelven_lista_vacia(self):
        vacio = pd.DataFrame()
        self.assertEqual(reservas_por_destino(vacio, vacio), [])
        self.assertEqual(reservas_por_mes(vacio), [])
        self.assertEqual(paquetes_mas_reservados(vacio), [])


if __name__ == "__main__":
    unittest.main()
