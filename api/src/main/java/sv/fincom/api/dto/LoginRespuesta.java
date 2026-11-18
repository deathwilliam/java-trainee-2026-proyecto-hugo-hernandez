
package sv.fincom.api.dto;

import java.util.List;

public record LoginRespuesta(
        String token,
        String tipo,
        Long usuarioId,
        List<String> roles
) {
}
