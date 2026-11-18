
package sv.fincom.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import sv.fincom.api.entidad.enums.EstadoOperacion;

public record OperacionSolicitud(

        @NotBlank(message = "La referencia es obligatoria")
        @Size(max = 40, message = "Maximo 40 caracteres")
        String referencia,

        @NotNull(message = "El agente es obligatorio")
        @Positive(message = "El ID del agente debe ser positivo")
        Long agenteId,

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El ID del cliente debe ser positivo")
        Long clienteId,

        @NotNull(message = "La tarifa es obligatoria")
        @Positive(message = "El ID de tarifa debe ser positivo")
        Long tarifaComisionId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser positivo")
        @Digits(integer = 13, fraction = 2)
        BigDecimal monto,

        @NotNull(message = "El estado es obligatorio")
        EstadoOperacion estado,

        @NotNull(message = "La fecha es obligatoria")
        LocalDateTime fechaOperacion

) {
}
