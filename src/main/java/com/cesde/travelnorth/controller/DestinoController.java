package com.cesde.travelnorth.controller;

import com.cesde.travelnorth.dto.DestinoRequestDTO;
import com.cesde.travelnorth.dto.DestinoResponseDTO;
import com.cesde.travelnorth.service.DestinoService;
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
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;

    public DestinoController(DestinoService destinoService) {
        this.destinoService = destinoService;
    }

    @GetMapping
    public List<DestinoResponseDTO> listar() {
        return destinoService.listar();
    }

    @GetMapping("/{id}")
    public DestinoResponseDTO obtener(@PathVariable Long id) {
        return destinoService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<DestinoResponseDTO> crear(@Valid @RequestBody DestinoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinoService.crear(request));
    }

    @PutMapping("/{id}")
    public DestinoResponseDTO actualizar(
            @PathVariable Long id, @Valid @RequestBody DestinoRequestDTO request) {
        return destinoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        destinoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
