
package sv.fincom.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.entidad.BitacoraOperacion;
import sv.fincom.api.entidad.Usuario;
import sv.fincom.api.entidad.enums.AccionBitacora;
import sv.fincom.api.entidad.enums.ResultadoOperacion;
import sv.fincom.api.repositorio.BitacoraOperacionRepository;
import sv.fincom.api.repositorio.UsuarioRepository;

@Service
public class AuditoriaFalloService {

    private final BitacoraOperacionRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaFalloService(
            BitacoraOperacionRepository bitacoraRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.bitacoraRepository = bitacoraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFallo(
            Long usuarioId,
            String motivo
    ) {

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "El usuario de auditoria es obligatorio"
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario que intento liquidar"
                ));

        String detalle = (motivo == null || motivo.isBlank())
                ? "Error al generar la liquidacion"
                : motivo.trim();

        if (detalle.length() > 255) {
            detalle = detalle.substring(0, 255);
        }

        BitacoraOperacion registro = new BitacoraOperacion(
                usuario,
                null,
                AccionBitacora.LIQUIDACION_FALLIDA,
                ResultadoOperacion.FALLIDO,
                detalle
        );

        bitacoraRepository.saveAndFlush(registro);
    }
}
