
package sv.fincom.api.dto;

import java.math.BigDecimal;

public record LiquidacionRespuesta(

        Long liquidacionId,

        String numeroOrden,

        Integer anio,

        Integer mes,

        BigDecimal totalComisiones,

        String estadoOrden

) {
}
