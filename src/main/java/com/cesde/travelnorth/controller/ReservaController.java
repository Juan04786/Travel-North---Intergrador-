package com.cesde.travelnorth.controller;

import com.cesde.travelnorth.dto.ReservaRequestDTO;
import com.cesde.travelnorth.dto.ReservaResponseDTO;
import com.cesde.travelnorth.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponseDTO> listar(@RequestParam(required = false) Long paqueteId) {
        return paqueteId == null ? reservaService.listar() : reservaService.listarPorPaquete(paqueteId);
    }

    @GetMapping("/{id}")
    public ReservaResponseDTO obtener(@PathVariable Long id) {
        return reservaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(request));
    }

    @PutMapping("/{id}")
    public ReservaResponseDTO actualizar(
            @PathVariable Long id, @Valid @RequestBody ReservaRequestDTO request) {
        return reservaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
