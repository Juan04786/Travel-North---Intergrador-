package com.cesde.travelnorth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DestinoRequestDTO(
        @NotBlank @Size(max = 10) String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 60) String pais,
        @NotBlank @Size(max = 60) String ciudad,
        @Size(max = 500) String descripcion) {
}
