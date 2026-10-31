package com.cesde.travelnorth.dto;

public record DestinoResponseDTO(
        Long id,
        String codigo,
        String nombre,
        String pais,
        String ciudad,
        String descripcion) {
}
