package com.cesde.travelnorth.controller;

import com.cesde.travelnorth.dto.PaqueteRequestDTO;
import com.cesde.travelnorth.dto.PaqueteResponseDTO;
import com.cesde.travelnorth.service.PaqueteTuristicoService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
public class PaqueteTuristicoController {

    private final PaqueteTuristicoService paqueteService;

    public PaqueteTuristicoController(PaqueteTuristicoService paqueteService) {
        this.paqueteService = paqueteService;
    }

    @GetMapping
    public List<PaqueteResponseDTO> listar() {
        return paqueteService.listar();
    }

    @GetMapping("/{id}")
    public PaqueteResponseDTO obtener(@PathVariable Long id) {
        return paqueteService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<PaqueteResponseDTO> crear(@Valid @RequestBody PaqueteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paqueteService.crear(request));
    }

    @PutMapping("/{id}")
    public PaqueteResponseDTO actualizar(
            @PathVariable Long id, @Valid @RequestBody PaqueteRequestDTO request) {
        return paqueteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
