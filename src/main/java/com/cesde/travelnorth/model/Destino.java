package com.cesde.travelnorth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "destinos", indexes = @Index(name = "idx_destino_codigo", columnList = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "pais", nullable = false, length = 60)
    private String pais;

    @Column(name = "ciudad", nullable = false, length = 60)
    private String ciudad;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    @ManyToMany(mappedBy = "destinos")
    private Set<PaqueteTuristico> paquetesTuristicos = new HashSet<>();
}
