
package sv.fincom.api.servicio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sv.fincom.api.entidad.*;
import sv.fincom.api.entidad.enums.*;
import sv.fincom.api.repositorio.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.annotation.DirtiesContext;
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(properties = {
        "jwt.clave=FINCOM_CLAVE_DE_PRUEBAS_EXCLUSIVA_2026_LARGA_123",
        "spring.datasource.url=${DB_TEST_URL:jdbc:postgresql://localhost:5432/fincom_test}"
})

class LiquidacionServiceIntegrationTest {

    @Autowired
    private EjecutorLiquidacionService ejecutor;

    @Autowired
    private AgenteRepository agenteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OperacionRepository operacionRepository;

    @Autowired
    private LiquidacionRepository liquidacionRepository;

    @Autowired
    private DetalleLiquidacionRepository detalleRepository;

    @Autowired
    private OrdenPagoRepository ordenPagoRepository;

    @Autowired
    private BitacoraOperacionRepository bitacoraRepository;

    @Test
    void debeLiquidarYAplicarRollbackAnteUnFallo() {

        // Obtener los datos cargados mediante import.sql.
        Agente agente = agenteRepository
                .findByCodigoAgente("AG-001")
                .orElseThrow();

        Usuario usuario = usuarioRepository
                .findByCorreo("asesor@fincom.test")
                .orElseThrow();

        Long agenteId = agente.getId();
        Long usuarioId = usuario.getId();

        // PARTE 1: LIQUIDACION EXITOSA DE OCTUBRE

        Liquidacion octubre = ejecutor.ejecutarLiquidacion(
                agenteId,
                2026,
                10,
                usuarioId
        );

        assertNotNull(octubre.getId());

        assertEquals(
                0,
                new BigDecimal("1.00")
                        .compareTo(octubre.getTotalComisiones())
        );

        assertEquals(
                2,
                detalleRepository
                        .findByLiquidacion_Id(octubre.getId())
                        .size()
        );

        assertTrue(
                ordenPagoRepository
                        .existsByLiquidacion_Id(octubre.getId())
        );

        List<Operacion> operacionesOctubre =
                operacionRepository.findAll()
                        .stream()
                        .filter(o ->
                                o.getReferencia().startsWith("OP-OCT-"))
                        .toList();

        long liquidadas = operacionesOctubre
                .stream()
                .filter(o ->
                        o.getEstado()
                                == EstadoOperacion.LIQUIDADA)
                .count();

        assertEquals(2, liquidadas);

        // PARTE 2: PREPARAR UN FALLO REAL DE BD

        // Creamos una liquidacion de agosto que
        // utilizara intencionalmente el numero
        // de orden reservado para septiembre.

        Liquidacion agosto = liquidacionRepository.saveAndFlush(
                new Liquidacion(
                        agente,
                        2026,
                        8,
                        new BigDecimal("0.50")
                )
        );

        String numeroDuplicado = String.format(
                "FIN-%04d%02d-%d",
                2026,
                9,
                agenteId
        );

        ordenPagoRepository.saveAndFlush(
                new OrdenPago(
                        agosto,
                        numeroDuplicado
                )
        );

        // PARTE 3: VERIFICAR EL ROLLBACK

        // Septiembre tiene una operacion confirmada.
        // El servicio intentara crear su liquidacion,
        // detalles y orden de pago.
        //
        // La orden fallara porque su numero ya existe.
        // La transaccion completa debe revertirse.

        assertThrows(
                RuntimeException.class,
                () -> ejecutor.ejecutarLiquidacion(
                        agenteId,
                        2026,
                        9,
                        usuarioId
                )
        );

        // No debe existir liquidacion de septiembre.
        assertFalse(
                liquidacionRepository
                        .existsByAgente_IdAndAnioAndMes(
                                agenteId, 2026, 9
                        )
        );

        // La operacion debe seguir CONFIRMADA.
        Operacion septiembre = operacionRepository.findAll()
                .stream()
                .filter(o ->
                        o.getReferencia().equals("OP-SEP-001"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                EstadoOperacion.CONFIRMADA,
                septiembre.getEstado()
        );

        // No debe existir detalle para esa operacion.
        assertFalse(
                detalleRepository.existsByOperacion_Id(
                        septiembre.getId()
                )
        );

        // PARTE 4: VERIFICAR AUDITORIA DEL FALLO

        List<BitacoraOperacion> fallos =
                bitacoraRepository
                        .findByResultadoOrderByCreadoEnDesc(
                                ResultadoOperacion.FALLIDO
                        );

        assertTrue(
                fallos.stream().anyMatch(
                        b -> b.getAccion()
                                == AccionBitacora.LIQUIDACION_FALLIDA
                                && b.getLiquidacion() == null
                ),
                "Debe conservarse la auditoria del fallo"
        );

        // La liquidacion exitosa de octubre
        // debe permanecer intacta.
        assertTrue(
                liquidacionRepository
                        .existsByAgente_IdAndAnioAndMes(
                                agenteId, 2026, 10
                        )
        );
    }
}

