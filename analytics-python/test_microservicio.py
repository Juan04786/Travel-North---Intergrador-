import unittest
from unittest.mock import patch

import pandas as pd

import microservicio


class MicroservicioTests(unittest.TestCase):
    def setUp(self):
        self.reservas = pd.DataFrame([
            {
                "estado": "CONFIRMADA",
                "cantidadPersonas": 2,
                "fechaReserva": "2026-10-12",
                "paqueteNombre": "Andes",
                "paqueteId": 1,
            },
        ])
        self.paquetes = pd.DataFrame([
            {"id": 1, "destinos": [{"nombre": "Medellín"}]},
        ])

    @patch("microservicio.get_paquetes")
    @patch("microservicio.get_reservas")
    def test_endpoint_json_devuelve_resumen_de_viajeros(self, get_reservas, get_paquetes):
        get_reservas.return_value = self.reservas
        get_paquetes.return_value = self.paquetes

        self.assertEqual(
            microservicio.headless_reservas_por_destino(),
            [{"label": "Medellín", "value": 2}],
        )

    @patch("microservicio.get_reservas")
    def test_endpoint_png_devuelve_imagen(self, get_reservas):
        get_reservas.return_value = self.reservas

        response = microservicio.pull_reservas_por_mes()

        self.assertEqual(response.media_type, "image/png")
        self.assertTrue(response.body.startswith(b"\x89PNG\r\n\x1a\n"))


if __name__ == "__main__":
    unittest.main()
