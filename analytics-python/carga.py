import os

import pandas as pd
import requests
from dotenv import load_dotenv

load_dotenv()
_API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api").rstrip("/")
BASE_URL = _API_BASE_URL if _API_BASE_URL.endswith("/api") else f"{_API_BASE_URL}/api"


def _get_dataframe(endpoint: str) -> pd.DataFrame:
    response = requests.get(f"{BASE_URL}/{endpoint}", timeout=10)
    response.raise_for_status()
    payload = response.json()
    if not isinstance(payload, list):
        raise ValueError(f"La respuesta de /{endpoint} no es una lista.")
    return pd.DataFrame(payload)


def get_reservas() -> pd.DataFrame:
    return _get_dataframe("reservas")


def get_paquetes() -> pd.DataFrame:
    return _get_dataframe("paquetes")
