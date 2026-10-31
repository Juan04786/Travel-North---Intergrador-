package com.cesde.travelnorth.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record PaqueteRequestDTO(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 1000) String descripcion,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precio,
        @NotNull @Min(1) Integer duracionDias,
        @NotNull @FutureOrPresent LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        @NotNull @Min(1) Integer cupoMaximo,
        Boolean activo,
        @NotEmpty Set<Long> destinoIds) {
}
