package com.cesde.travelnorth.repository;

import com.cesde.travelnorth.model.PaqueteTuristico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaqueteTuristicoRepository extends JpaRepository<PaqueteTuristico, Long> {
    @Override
    @EntityGraph(attributePaths = "destinos")
    List<PaqueteTuristico> findAll();

    @Override
    @EntityGraph(attributePaths = "destinos")
    Optional<PaqueteTuristico> findById(Long id);

    List<PaqueteTuristico> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
