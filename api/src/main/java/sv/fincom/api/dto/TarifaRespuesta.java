
package sv.fincom.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRespuesta(
        Long id,
        Long tipoServicioId,
        BigDecimal montoFijo,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta,
        Boolean activo
) {
}
