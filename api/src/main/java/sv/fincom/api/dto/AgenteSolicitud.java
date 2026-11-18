
package sv.fincom.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AgenteSolicitud(

        @NotNull(message = "El usuario es obligatorio")
        @Positive(message = "El ID de usuario debe ser positivo")
        Long usuarioId,

        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 24, message = "Maximo 24 caracteres")
        String codigoAgente,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 90, message = "Maximo 90 caracteres")
        String nombre

) {
}
