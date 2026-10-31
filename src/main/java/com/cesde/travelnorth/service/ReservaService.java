package com.cesde.travelnorth.service;

import com.cesde.travelnorth.dto.ReservaRequestDTO;
import com.cesde.travelnorth.dto.ReservaResponseDTO;
import com.cesde.travelnorth.exception.RecursoNoEncontradoException;
import com.cesde.travelnorth.exception.ReglaNegocioException;
import com.cesde.travelnorth.model.EstadoReserva;
import com.cesde.travelnorth.model.PaqueteTuristico;
import com.cesde.travelnorth.model.Reserva;
import com.cesde.travelnorth.repository.PaqueteTuristicoRepository;
import com.cesde.travelnorth.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final PaqueteTuristicoRepository paqueteRepository;

    public ReservaService(ReservaRepository reservaRepository, PaqueteTuristicoRepository paqueteRepository) {
        this.reservaRepository = reservaRepository;
        this.paqueteRepository = paqueteRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listar() {
        return reservaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponseDTO obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorPaquete(Long paqueteId) {
        if (!paqueteRepository.existsById(paqueteId)) {
            throw new RecursoNoEncontradoException("No se encontró el paquete " + paqueteId + ".");
        }
        return reservaRepository.findByPaqueteId(paqueteId).stream().map(this::toResponse).toList();
    }

    public ReservaResponseDTO crear(ReservaRequestDTO request) {
        PaqueteTuristico paquete = buscarPaquete(request.paqueteId());
        EstadoReserva estado = request.estado() == null ? EstadoReserva.PENDIENTE : request.estado();
        validarDisponibilidad(paquete, request.cantidadPersonas(), estado, null);
        Reserva reserva = Reserva.builder()
                .clienteNombre(request.clienteNombre().trim())
                .clienteEmail(request.clienteEmail().trim().toLowerCase(Locale.ROOT))
                .fechaReserva(LocalDate.now())
                .cantidadPersonas(request.cantidadPersonas())
                .total(calcularTotal(paquete, request.cantidadPersonas()))
                .estado(estado)
                .paquete(paquete)
                .build();
        return toResponse(reservaRepository.save(reserva));
    }

    public ReservaResponseDTO actualizar(Long id, ReservaRequestDTO request) {
        Reserva reserva = buscar(id);
        PaqueteTuristico paquete = buscarPaquete(request.paqueteId());
        EstadoReserva estado = request.estado() == null ? reserva.getEstado() : request.estado();
        validarDisponibilidad(paquete, request.cantidadPersonas(), estado, reserva);
        reserva.setClienteNombre(request.clienteNombre().trim());
        reserva.setClienteEmail(request.clienteEmail().trim().toLowerCase(Locale.ROOT));
        reserva.setCantidadPersonas(request.cantidadPersonas());
        reserva.setEstado(estado);
        reserva.setPaquete(paquete);
        reserva.setTotal(calcularTotal(paquete, request.cantidadPersonas()));
        return toResponse(reservaRepository.save(reserva));
    }

    public void eliminar(Long id) {
        reservaRepository.delete(buscar(id));
    }

    private void validarDisponibilidad(
            PaqueteTuristico paquete, int cantidad, EstadoReserva estado, Reserva actual) {
        if (estado == EstadoReserva.CANCELADA) {
            return;
        }
        if (!Boolean.TRUE.equals(paquete.getActivo())) {
            throw new ReglaNegocioException("El paquete no está disponible para nuevas reservas.");
        }
        if (paquete.getFechaInicio().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No se pueden crear reservas para un paquete que ya inició.");
        }
        long ocupados = reservaRepository.sumPersonasActivasPorPaquete(
                paquete.getId(), EstadoReserva.CANCELADA);
        if (actual != null
                && actual.getPaquete().getId().equals(paquete.getId())
                && actual.getEstado() != EstadoReserva.CANCELADA) {
            ocupados -= actual.getCantidadPersonas();
        }
        if (ocupados + cantidad > paquete.getCupoMaximo()) {
            throw new ReglaNegocioException("No hay suficientes cupos disponibles para esa cantidad de personas.");
        }
    }

    private BigDecimal calcularTotal(PaqueteTuristico paquete, int personas) {
        return paquete.getPrecio().multiply(BigDecimal.valueOf(personas));
    }

    private Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la reserva " + id + "."));
    }

    private PaqueteTuristico buscarPaquete(Long id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paquete " + id + "."));
    }

    private ReservaResponseDTO toResponse(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(), reserva.getClienteNombre(), reserva.getClienteEmail(),
                reserva.getFechaReserva(), reserva.getCantidadPersonas(), reserva.getTotal(),
                reserva.getEstado(), reserva.getPaquete().getId(), reserva.getPaquete().getNombre());
    }
}
