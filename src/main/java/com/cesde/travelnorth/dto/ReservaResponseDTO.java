package com.cesde.travelnorth.dto;

import com.cesde.travelnorth.model.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaResponseDTO(
        Long id,
        String clienteNombre,
        String clienteEmail,
        LocalDate fechaReserva,
        Integer cantidadPersonas,
        BigDecimal total,
        EstadoReserva estado,
        Long paqueteId,
        String paqueteNombre) {
}
