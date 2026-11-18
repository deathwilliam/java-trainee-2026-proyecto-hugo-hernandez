
package sv.fincom.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import sv.fincom.api.entidad.enums.EstadoOperacion;

public record OperacionRespuesta(
        Long id,
        String referencia,
        Long agenteId,
        Long clienteId,
        Long tarifaComisionId,
        BigDecimal monto,
        EstadoOperacion estado,
        LocalDateTime fechaOperacion
) {
}
