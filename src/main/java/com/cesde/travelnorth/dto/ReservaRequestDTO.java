package com.cesde.travelnorth.dto;

import com.cesde.travelnorth.model.EstadoReserva;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReservaRequestDTO(
        @NotBlank @Size(max = 120) String clienteNombre,
        @NotBlank @Email @Size(max = 160) String clienteEmail,
        @NotNull @Min(1) Integer cantidadPersonas,
        @NotNull Long paqueteId,
        EstadoReserva estado) {
}
