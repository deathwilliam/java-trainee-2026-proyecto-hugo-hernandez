
package sv.fincom.api.dto;

import jakarta.validation.constraints.NotNull;
import sv.fincom.api.entidad.enums.EstadoOperacion;

public record OperacionEstadoSolicitud(

        @NotNull(message = "El estado es obligatorio")
        EstadoOperacion estado

) {
}
