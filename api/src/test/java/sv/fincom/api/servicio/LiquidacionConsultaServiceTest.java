
package sv.fincom.api.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sv.fincom.api.repositorio.LiquidacionRepository;
import sv.fincom.api.repositorio.OrdenPagoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionConsultaServiceTest {

    @Mock
    private LiquidacionRepository liquidaciones;

    @Mock
    private OrdenPagoRepository ordenes;

    @InjectMocks
    private LiquidacionConsultaService consultas;

    @Test
    void asesorNoPuedeConsultarLiquidacionAjena() {

        Long liquidacionId = 10L;
        Long usuarioId = 99L;

        when(liquidaciones.existsByIdAndAgente_Usuario_Id(
                liquidacionId, usuarioId
        )).thenReturn(false);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> consultas.obtenerParaAsesor(
                        liquidacionId, usuarioId
                )
        );

        assertEquals(404, error.getStatusCode().value());

        verify(liquidaciones, never()).findById(liquidacionId);
        verifyNoInteractions(ordenes);
    }
}
