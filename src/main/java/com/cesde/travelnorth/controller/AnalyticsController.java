package com.cesde.travelnorth.controller;

import com.cesde.travelnorth.dto.GraficoDataDTO;
import com.cesde.travelnorth.service.AnalyticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/graficos/headless")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/reservas-por-destino")
    public List<GraficoDataDTO> reservasPorDestino() {
        return analyticsService.reservasPorDestino();
    }

    @GetMapping("/reservas-por-mes")
    public List<GraficoDataDTO> reservasPorMes() {
        return analyticsService.reservasPorMes();
    }

    @GetMapping("/paquetes-mas-reservados")
    public List<GraficoDataDTO> paquetesMasReservados(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int top) {
        return analyticsService.paquetesMasReservados(top);
    }

    @GetMapping(value = "/pull/reservas-por-destino", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> graficoReservasPorDestino() {
        return graficoPng("reservas-por-destino");
    }

    @GetMapping(value = "/pull/reservas-por-mes", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> graficoReservasPorMes() {
        return graficoPng("reservas-por-mes");
    }

    @GetMapping(value = "/pull/paquetes-mas-reservados", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> graficoPaquetesMasReservados() {
        return graficoPng("paquetes-mas-reservados");
    }

    private ResponseEntity<byte[]> graficoPng(String nombre) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(analyticsService.graficoPng(nombre));
    }
}
