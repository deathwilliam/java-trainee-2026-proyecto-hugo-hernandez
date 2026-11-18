
package sv.fincom.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LiquidacionSolicitud(

        @NotNull(message = "El agente es obligatorio")
        @Positive(message = "El agente debe ser mayor que cero")
        Long agenteId,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2020, message = "El año mínimo es 2020")
        @Max(value = 2100, message = "El año máximo es 2100")
        Integer anio,

        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1, message = "El mes mínimo es 1")
        @Max(value = 12, message = "El mes máximo es 12")
        Integer mes

) {
}
