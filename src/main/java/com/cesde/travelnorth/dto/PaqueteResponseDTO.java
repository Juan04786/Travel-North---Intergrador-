package com.cesde.travelnorth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PaqueteResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer duracionDias,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer cupoMaximo,
        Boolean activo,
        List<DestinoResponseDTO> destinos) {
}
