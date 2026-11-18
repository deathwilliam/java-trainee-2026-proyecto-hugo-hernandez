
package sv.fincom.api.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sv.fincom.api.entidad.Agente;
import sv.fincom.api.entidad.Usuario;
import sv.fincom.api.entidad.Operacion;
import sv.fincom.api.entidad.Liquidacion;
import sv.fincom.api.entidad.TarifaComision;

import sv.fincom.api.entidad.enums.EstadoOperacion;
import sv.fincom.api.excepcion.OperacionRechazadaException;

import sv.fincom.api.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionReglasTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private AgenteRepository agentes;

    @Mock
    private OperacionRepository operaciones;

    @Mock
    private LiquidacionRepository liquidaciones;

    @Mock
    private DetalleLiquidacionRepository detalles;

    @Mock
    private OrdenPagoRepository ordenes;

    @Mock
    private BitacoraOperacionRepository bitacora;

    @InjectMocks
    private LiquidacionService servicio;

    private void prepararAgenteYOperador() {

        when(agentes.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mock(Agente.class)));

        when(usuarios.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mock(Usuario.class)));
    }

    private void prepararOperacionConfirmada(Operacion operacion) {

        when(operaciones.buscarPorAgentePeriodoYEstado(
                1L,
                EstadoOperacion.CONFIRMADA,
                LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 11, 1, 0, 0)
        )).thenReturn(List.of(operacion));
    }

    @Test
    void liquidacionValidaCalculaComisionYRegistraDatos() {

        prepararAgenteYOperador();

        Operacion operacion = mock(Operacion.class);
        TarifaComision tarifa = mock(TarifaComision.class);

        prepararOperacionConfirmada(operacion);

        when(operacion.getId()).thenReturn(10L);

        when(operacion.getMonto())
                .thenReturn(new BigDecimal("100.00"));

        when(operacion.getTarifaComision())
                .thenReturn(tarifa);

        when(operacion.getFechaOperacion())
                .thenReturn(LocalDateTime.of(2026, 10, 5, 12, 0));

        when(tarifa.estaVigente(
                LocalDate.of(2026, 10, 5)
        )).thenReturn(true);

        when(tarifa.getMontoFijo())
                .thenReturn(new BigDecimal("0.50"));

        when(liquidaciones.save(any(Liquidacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Liquidacion resultado = servicio.liquidarComisiones(
                1L, 2026, 10, 1L
        );

        assertNotNull(resultado);

        assertEquals(
                new BigDecimal("0.50"),
                resultado.getTotalComisiones()
        );

        verify(detalles).save(any());
        verify(operacion).marcarLiquidada();
        verify(ordenes).save(any());
        verify(bitacora).save(any());
        verify(liquidaciones).flush();
    }

    @Test
    void rechazaMesInvalidoSinConsultarRepositorios() {

        OperacionRechazadaException error = assertThrows(
                OperacionRechazadaException.class,
                () -> servicio.liquidarComisiones(
                        1L, 2026, 13, 1L
                )
        );


        assertEquals(
                "Agente, operador o periodo invalido",
                error.getMessage()
        );


        verifyNoInteractions(
                usuarios,
                agentes,
                operaciones,
                liquidaciones,
                detalles,
                ordenes,
                bitacora
        );
    }

    @Test
    void rechazaAgenteInexistente() {

        when(agentes.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.empty());

        OperacionRechazadaException error = assertThrows(
                OperacionRechazadaException.class,
                () -> servicio.liquidarComisiones(
                        1L, 2026, 10, 1L
                )
        );

        assertEquals(
                "El agente no existe o esta inactivo",
                error.getMessage()
        );

        verifyNoInteractions(
                usuarios,
                liquidaciones,
                detalles,
                ordenes,
                bitacora
        );
    }

    @Test
    void rechazaOperadorInexistente() {

        when(agentes.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mock(Agente.class)));

        when(usuarios.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.empty());

        OperacionRechazadaException error = assertThrows(
                OperacionRechazadaException.class,
                () -> servicio.liquidarComisiones(
                        1L, 2026, 10, 1L
                )
        );

        assertEquals(
                "El operador no existe o esta inactivo",
                error.getMessage()
        );

        verifyNoInteractions(
                operaciones,
                liquidaciones,
                detalles,
                ordenes,
                bitacora
        );
    }

    @Test
    void rechazaPeriodoYaLiquidado() {

        prepararAgenteYOperador();

        when(liquidaciones.existsByAgente_IdAndAnioAndMes(
                1L, 2026, 10
        )).thenReturn(true);

        OperacionRechazadaException error = assertThrows(
                OperacionRechazadaException.class,
                () -> servicio.liquidarComisiones(
                        1L, 2026, 10, 1L
                )
        );

        assertEquals(
                "El agente ya tiene una liquidacion para este periodo",
                error.getMessage()
        );

        verifyNoInteractions(
                operaciones,
                detalles,
                ordenes,
                bitacora
        );
    }

    @Test
    void rechazaLiquidacionSinOperacionesConfirmadas() {

        prepararAgenteYOperador();

        when(operaciones.buscarPorAgentePeriodoYEstado(
                1L,
                EstadoOperacion.CONFIRMADA,
                LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 11, 1, 0, 0)
        )).thenReturn(List.of());

        OperacionRechazadaException error = assertThrows(
                OperacionRechazadaException.class,
                () -> servicio.liquidarComisiones(
                        1L, 2026, 10, 1L
                )
        );

        assertEquals(
                "No hay operaciones confirmadas en el periodo",
                error.getMessage()
        );

        verifyNoInteractions(
                detalles,
                ordenes,
                bitacora
        );

        verify(liquidaciones, never())
                .save(any(Liquidacion.class));
    }
}
