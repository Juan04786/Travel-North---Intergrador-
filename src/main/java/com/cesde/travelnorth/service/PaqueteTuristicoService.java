package com.cesde.travelnorth.service;

import com.cesde.travelnorth.dto.DestinoResponseDTO;
import com.cesde.travelnorth.dto.PaqueteRequestDTO;
import com.cesde.travelnorth.dto.PaqueteResponseDTO;
import com.cesde.travelnorth.exception.RecursoNoEncontradoException;
import com.cesde.travelnorth.exception.ReglaNegocioException;
import com.cesde.travelnorth.model.Destino;
import com.cesde.travelnorth.model.PaqueteTuristico;
import com.cesde.travelnorth.repository.DestinoRepository;
import com.cesde.travelnorth.repository.PaqueteTuristicoRepository;
import com.cesde.travelnorth.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaqueteTuristicoService {

    private final PaqueteTuristicoRepository paqueteRepository;
    private final DestinoRepository destinoRepository;
    private final ReservaRepository reservaRepository;

    public PaqueteTuristicoService(
            PaqueteTuristicoRepository paqueteRepository,
            DestinoRepository destinoRepository,
            ReservaRepository reservaRepository) {
        this.paqueteRepository = paqueteRepository;
        this.destinoRepository = destinoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponseDTO> listar() {
        return paqueteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaqueteResponseDTO obtener(Long id) {
        return toResponse(buscar(id));
    }

    public PaqueteResponseDTO crear(PaqueteRequestDTO request) {
        validar(request, null);
        PaqueteTuristico paquete = new PaqueteTuristico();
        aplicar(paquete, request);
        return toResponse(paqueteRepository.save(paquete));
    }

    public PaqueteResponseDTO actualizar(Long id, PaqueteRequestDTO request) {
        PaqueteTuristico paquete = buscar(id);
        validar(request, id);
        aplicar(paquete, request);
        return toResponse(paqueteRepository.save(paquete));
    }

    public void eliminar(Long id) {
        PaqueteTuristico paquete = buscar(id);
        if (reservaRepository.existsByPaqueteId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un paquete que tiene reservas.");
        }
        paqueteRepository.delete(paquete);
    }

    private void validar(PaqueteRequestDTO request, Long id) {
        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin debe ser igual o posterior a la fecha de inicio.");
        }
        boolean nombreDuplicado = id == null
                ? paqueteRepository.existsByNombreIgnoreCase(request.nombre())
                : paqueteRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id);
        if (nombreDuplicado) {
            throw new ReglaNegocioException("Ya existe un paquete con ese nombre.");
        }
        Set<Long> encontrados = destinoRepository.findAllById(request.destinoIds()).stream()
                .map(Destino::getId)
                .collect(Collectors.toSet());
        if (!encontrados.equals(request.destinoIds())) {
            throw new RecursoNoEncontradoException("Uno o más destinos seleccionados no existen.");
        }
        if (id != null && request.cupoMaximo() < reservasActivas(id)) {
            throw new ReglaNegocioException("El cupo máximo no puede ser menor a las personas ya reservadas.");
        }
    }

    private long reservasActivas(Long paqueteId) {
        return reservaRepository.sumPersonasActivasPorPaquete(
                paqueteId, com.cesde.travelnorth.model.EstadoReserva.CANCELADA);
    }

    private void aplicar(PaqueteTuristico paquete, PaqueteRequestDTO request) {
        paquete.setNombre(request.nombre().trim());
        paquete.setDescripcion(request.descripcion() == null || request.descripcion().isBlank()
                ? null : request.descripcion().trim());
        paquete.setPrecio(request.precio());
        paquete.setDuracionDias(request.duracionDias());
        paquete.setFechaInicio(request.fechaInicio());
        paquete.setFechaFin(request.fechaFin());
        paquete.setCupoMaximo(request.cupoMaximo());
        paquete.setActivo(request.activo() == null || request.activo());
        paquete.setDestinos(destinoRepository.findAllById(request.destinoIds()).stream()
                .collect(Collectors.toSet()));
    }

    private PaqueteTuristico buscar(Long id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paquete " + id + "."));
    }

    private PaqueteResponseDTO toResponse(PaqueteTuristico paquete) {
        List<DestinoResponseDTO> destinos = paquete.getDestinos().stream()
                .map(destino -> new DestinoResponseDTO(
                        destino.getId(), destino.getCodigo(), destino.getNombre(),
                        destino.getPais(), destino.getCiudad(), destino.getDescripcion()))
                .toList();
        return new PaqueteResponseDTO(
                paquete.getId(), paquete.getNombre(), paquete.getDescripcion(),
                paquete.getPrecio(), paquete.getDuracionDias(), paquete.getFechaInicio(),
                paquete.getFechaFin(), paquete.getCupoMaximo(), paquete.getActivo(), destinos);
    }
}
