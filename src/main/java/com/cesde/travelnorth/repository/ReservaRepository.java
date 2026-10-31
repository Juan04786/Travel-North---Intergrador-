package com.cesde.travelnorth.repository;

import com.cesde.travelnorth.model.Reserva;
import com.cesde.travelnorth.model.EstadoReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @Override
    @EntityGraph(attributePaths = "paquete")
    List<Reserva> findAll();

    @Override
    @EntityGraph(attributePaths = "paquete")
    Optional<Reserva> findById(Long id);

    @EntityGraph(attributePaths = "paquete")
    List<Reserva> findByPaqueteId(Long paqueteId);

    boolean existsByPaqueteId(Long paqueteId);

    @Query("select coalesce(sum(r.cantidadPersonas), 0) from Reserva r "
            + "where r.paquete.id = :paqueteId and r.estado <> :estado")
    long sumPersonasActivasPorPaquete(
            @Param("paqueteId") Long paqueteId,
            @Param("estado") EstadoReserva estado);
}
