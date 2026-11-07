package com.cesde.travelnorth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.cesde.travelnorth.repository.DestinoRepository;
import com.cesde.travelnorth.repository.PaqueteTuristicoRepository;
import com.cesde.travelnorth.repository.ReservaRepository;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TravelNorthApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PaqueteTuristicoRepository paqueteRepository;

    @Autowired
    private DestinoRepository destinoRepository;

    @BeforeEach
    void limpiarDatos() {
        reservaRepository.deleteAll();
        paqueteRepository.deleteAll();
        destinoRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void publicaOpenApiScalarYRedireccionDesdeRaiz() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Travel North API"));

        mockMvc.perform(get("/scalar"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/scalar"))
                .andExpect(redirectedUrl("/scalar"));
    }

    @Test
    void permitePreflightDesdeVercelYRechazaOrigenNoAutorizado() throws Exception {
        mockMvc.perform(options("/api/destinos")
                        .header("Origin", "https://travel-north.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://travel-north.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")));

        mockMvc.perform(options("/api/destinos")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteCrearDestinoPaqueteYReservaYRespetaElCupo() throws Exception {
        MvcResult destinoResult = mockMvc.perform(post("/api/destinos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"BOG","nombre":"Bogotá","pais":"Colombia","ciudad":"Bogotá","descripcion":"Capital"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long destinoId = idFrom(destinoResult);
        String inicio = LocalDate.now().plusDays(10).toString();
        String fin = LocalDate.now().plusDays(14).toString();
        MvcResult paqueteResult = mockMvc.perform(post("/api/paquetes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Escapada Andina","descripcion":"Viaje de prueba","precio":150000,
                                 "duracionDias":5,"fechaInicio":"%s","fechaFin":"%s","cupoMaximo":3,
                                 "activo":true,"destinoIds":[%d]}
                                """.formatted(inicio, fin, destinoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long paqueteId = idFrom(paqueteResult);
        String reserva = """
                {"clienteNombre":"Ana Torres","clienteEmail":"ana@example.com","cantidadPersonas":2,
                 "paqueteId":%d,"estado":"CONFIRMADA"}
                """.formatted(paqueteId);
        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(reserva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(300000));

        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(reserva))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No hay suficientes cupos disponibles para esa cantidad de personas."));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paqueteNombre").value("Escapada Andina"));

    }

    @Test
    void rechazaDestinoConCamposObligatoriosVacios() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"","nombre":"","pais":"","ciudad":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    private long idFrom(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();
    }
}
