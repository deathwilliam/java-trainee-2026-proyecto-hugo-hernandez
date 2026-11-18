
package sv.fincom.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaSolicitud(

        @NotNull(message = "El tipo de servicio es obligatorio")
        @Positive(message = "El ID debe ser positivo")
        Long tipoServicioId,

        @NotNull(message = "El monto de la tarifa es obligatorio")
        @DecimalMin(value = "0.01",
                message = "La comision debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2,
                message = "El monto debe tener maximo 10 enteros y 2 decimales")
        BigDecimal montoFijo,

        @NotNull(message = "La fecha inicial es obligatoria")
        LocalDate vigenteDesde,

        LocalDate vigenteHasta

) {
}
