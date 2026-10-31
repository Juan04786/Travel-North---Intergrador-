package com.cesde.travelnorth.service;

import com.cesde.travelnorth.dto.GraficoDataDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class AnalyticsServiceTests {

    @Test
    void obtieneResumenesJsonDelServicioPython() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AnalyticsService service = new AnalyticsService(builder, "http://analytics.test");

        server.expect(requestTo("http://analytics.test/api/graficos/headless/reservas-por-destino"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        [{"label":"Medellín","value":12}]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://analytics.test/api/graficos/headless/reservas-por-mes"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        [{"label":"2026-10","value":4}]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://analytics.test/api/graficos/headless/paquetes-mas-reservados?top=2"))
                .andExpect(method(GET))
                .andExpect(queryParam("top", "2"))
                .andRespond(withSuccess("""
                        [{"label":"Andes","value":3}]
                        """, MediaType.APPLICATION_JSON));

        List<GraficoDataDTO> destinos = service.reservasPorDestino();
        List<GraficoDataDTO> meses = service.reservasPorMes();
        List<GraficoDataDTO> paquetes = service.paquetesMasReservados(2);

        assertEquals(List.of(new GraficoDataDTO("Medellín", 12)), destinos);
        assertEquals(List.of(new GraficoDataDTO("2026-10", 4)), meses);
        assertEquals(List.of(new GraficoDataDTO("Andes", 3)), paquetes);
        server.verify();
    }

    @Test
    void obtieneGraficosPngDelServicioPython() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AnalyticsService service = new AnalyticsService(builder, "http://analytics.test");
        byte[] image = new byte[] {(byte) 0x89, 'P', 'N', 'G'};

        server.expect(requestTo("http://analytics.test/api/graficos/pull/reservas-por-destino"))
                .andExpect(method(GET))
                .andRespond(withSuccess(image, MediaType.IMAGE_PNG));

        assertArrayEquals(image, service.graficoPng("reservas-por-destino"));
        server.verify();
    }
}
