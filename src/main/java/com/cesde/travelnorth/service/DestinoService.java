package com.cesde.travelnorth.service;

import com.cesde.travelnorth.dto.DestinoRequestDTO;
import com.cesde.travelnorth.dto.DestinoResponseDTO;
import com.cesde.travelnorth.exception.RecursoNoEncontradoException;
import com.cesde.travelnorth.exception.ReglaNegocioException;
import com.cesde.travelnorth.model.Destino;
import com.cesde.travelnorth.repository.DestinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DestinoService {

    private final DestinoRepository destinoRepository;

    public DestinoService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }

    @Transactional(readOnly = true)
    public List<DestinoResponseDTO> listar() {
        return destinoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DestinoResponseDTO obtener(Long id) {
        return toResponse(buscar(id));
    }

    public DestinoResponseDTO crear(DestinoRequestDTO request) {
        if (destinoRepository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new ReglaNegocioException("Ya existe un destino con ese código.");
        }
        return toResponse(destinoRepository.save(toEntity(request)));
    }

    public DestinoResponseDTO actualizar(Long id, DestinoRequestDTO request) {
        Destino destino = buscar(id);
        if (destinoRepository.existsByCodigoIgnoreCaseAndIdNot(request.codigo(), id)) {
            throw new ReglaNegocioException("Ya existe un destino con ese código.");
        }
        destino.setCodigo(request.codigo().trim());
        destino.setNombre(request.nombre().trim());
        destino.setPais(request.pais().trim());
        destino.setCiudad(request.ciudad().trim());
        destino.setDescripcion(normalizar(request.descripcion()));
        return toResponse(destinoRepository.save(destino));
    }

    public void eliminar(Long id) {
        destinoRepository.delete(buscar(id));
    }

    private Destino buscar(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el destino " + id + "."));
    }

    private Destino toEntity(DestinoRequestDTO request) {
        return Destino.builder()
                .codigo(request.codigo().trim())
                .nombre(request.nombre().trim())
                .pais(request.pais().trim())
                .ciudad(request.ciudad().trim())
                .descripcion(normalizar(request.descripcion()))
                .build();
    }

    private DestinoResponseDTO toResponse(Destino destino) {
        return new DestinoResponseDTO(
                destino.getId(), destino.getCodigo(), destino.getNombre(),
                destino.getPais(), destino.getCiudad(), destino.getDescripcion());
    }

    private String normalizar(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
