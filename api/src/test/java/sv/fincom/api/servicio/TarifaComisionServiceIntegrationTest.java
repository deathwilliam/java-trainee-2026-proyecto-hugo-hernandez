
package sv.fincom.api.servicio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.entidad.TarifaComision;
import sv.fincom.api.repositorio.TarifaComisionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {
        "jwt.clave=FINCOM_CLAVE_DE_PRUEBAS_EXCLUSIVA_2026_LARGA_123",
        "spring.datasource.url=${DB_TEST_URL:jdbc:postgresql://localhost:5432/fincom_test}"
})

class TarifaComisionServiceIntegrationTest {

    @Autowired
    private TarifaComisionService tarifaService;

    @Autowired
    private TarifaComisionRepository tarifaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long obtenerTipoServicioId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM tipo_servicio WHERE codigo = ?",
                Long.class,
                "PAGO"
        );
    }

    @Test
    void debeRechazarTarifaConPeriodoSuperpuesto() {

        Long tipoServicioId = obtenerTipoServicioId();

        int cantidadInicial = tarifaRepository
                .findByTipoServicio_IdAndActivoTrue(
                        tipoServicioId
                ).size();

        // La tarifa inicial ya esta vigente desde
        // el 1 de enero de 2026 y no tiene fecha final.
        // Octubre de 2026 se superpone con ella.

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> tarifaService.crearTarifa(
                        tipoServicioId,
                        new BigDecimal("0.75"),
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                )
        );

        assertTrue(
                error.getMessage().contains("superpuesto")
        );

        int cantidadFinal = tarifaRepository
                .findByTipoServicio_IdAndActivoTrue(
                        tipoServicioId
                ).size();

        assertEquals(cantidadInicial, cantidadFinal);
    }

    @Test
    void debeRechazarOtraTarifaSinFechaFinal() {

        Long tipoServicioId = obtenerTipoServicioId();

        // Aunque la nueva tarifa no tenga fecha final,
        // tambien se superpone con la tarifa existente.

        assertThrows(
                IllegalStateException.class,
                () -> tarifaService.crearTarifa(
                        tipoServicioId,
                        new BigDecimal("0.80"),
                        LocalDate.of(2027, 1, 1),
                        null
                )
        );
    }

    @Test
    @Transactional
    void debePermitirPeriodoSinSolapamiento() {

        Long tipoServicioId = obtenerTipoServicioId();

        // Este periodo termina antes de que comience
        // la tarifa existente del 1 de enero de 2026.

        TarifaComision nuevaTarifa =
                tarifaService.crearTarifa(
                        tipoServicioId,
                        new BigDecimal("0.30"),
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 12, 31)
                );

        assertNotNull(nuevaTarifa.getId());

        assertEquals(
                0,
                new BigDecimal("0.30")
                        .compareTo(nuevaTarifa.getMontoFijo())
        );

        assertTrue(
                nuevaTarifa.estaVigente(
                        LocalDate.of(2025, 6, 15)
                )
        );

        assertFalse(
                nuevaTarifa.estaVigente(
                        LocalDate.of(2026, 2, 1)
                )
        );

        // La transaccion de esta prueba se revierte
        // al finalizar para no dejar datos adicionales.
    }
}
