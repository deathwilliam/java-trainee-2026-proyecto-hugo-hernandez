
package sv.fincom.api.dto;

public record AgenteRespuesta(
        Long id,
        String codigoAgente,
        String nombre,
        Boolean activo
) {
}
