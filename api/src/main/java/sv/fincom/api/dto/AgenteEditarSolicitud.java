
package sv.fincom.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgenteEditarSolicitud(

        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 24, message = "Maximo 24 caracteres")
        String codigoAgente,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 90, message = "Maximo 90 caracteres")
        String nombre

) {
}
