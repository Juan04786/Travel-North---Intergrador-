package com.cesde.travelnorth.service;

import com.cesde.travelnorth.dto.GraficoDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AnalyticsService {

    private static final ParameterizedTypeReference<List<GraficoDataDTO>> DATA_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient analyticsClient;

    public AnalyticsService(
            RestClient.Builder restClientBuilder,
            @Value("${analytics.base-url:http://localhost:8000}") String baseUrl) {
        this.analyticsClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public List<GraficoDataDTO> reservasPorDestino() {
        return analyticsClient.get()
                .uri("/api/graficos/headless/reservas-por-destino")
                .retrieve()
                .body(DATA_LIST);
    }

    public List<GraficoDataDTO> reservasPorMes() {
        return analyticsClient.get()
                .uri("/api/graficos/headless/reservas-por-mes")
                .retrieve()
                .body(DATA_LIST);
    }

    public List<GraficoDataDTO> paquetesMasReservados(int top) {
        return analyticsClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/graficos/headless/paquetes-mas-reservados")
                        .queryParam("top", top)
                        .build())
                .retrieve()
                .body(DATA_LIST);
    }

    public byte[] graficoPng(String nombre) {
        return analyticsClient.get()
                .uri("/api/graficos/pull/{nombre}", nombre)
                .accept(MediaType.IMAGE_PNG)
                .retrieve()
                .body(byte[].class);
    }
}
