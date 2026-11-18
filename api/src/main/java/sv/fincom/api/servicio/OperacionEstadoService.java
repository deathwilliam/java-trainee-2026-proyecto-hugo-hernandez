
package sv.fincom.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.entidad.Operacion;
import sv.fincom.api.entidad.enums.EstadoOperacion;
import sv.fincom.api.excepcion.OperacionRechazadaException;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;
import sv.fincom.api.repositorio.OperacionRepository;
import sv.fincom.api.repositorio.DetalleLiquidacionRepository;

@Service
public class OperacionEstadoService {

    private final OperacionRepository operaciones;
    private final DetalleLiquidacionRepository detalles;

    public OperacionEstadoService(
            OperacionRepository operaciones,
            DetalleLiquidacionRepository detalles) {

        this.operaciones = operaciones;
        this.detalles = detalles;
    }

    private Operacion buscarYVerificar(
            Long id,
            Long usuarioId,
            boolean esAdmin) {

        Operacion operacion = operaciones.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La operacion no existe"
                        )
                );
        if (!esAdmin
                && !operacion.getAgente().getUsuario().getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puede modificar operaciones de otro agente"
            );
        }

        if (operacion.getEstado() == EstadoOperacion.LIQUIDADA
                || detalles.existsByOperacion_Id(id)) {

            throw new OperacionRechazadaException(
                    "La operacion ya fue liquidada y no se puede modificar"
            );
        }

        return operacion;
    }

    @Transactional
    public void cambiarEstado(
            Long id,
            EstadoOperacion nuevoEstado,
            Long usuarioId,
            boolean esAdmin) {

        Operacion operacion = buscarYVerificar(id, usuarioId, esAdmin);

        try {
            operacion.cambiarEstado(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new OperacionRechazadaException(e.getMessage());
        }
    }

    @Transactional
    public void anular(
            Long id,
            Long usuarioId,
            boolean esAdmin) {

        Operacion operacion = buscarYVerificar(id, usuarioId, esAdmin);

        if (operacion.getEstado() != EstadoOperacion.ANULADA) {
            try {
                operacion.cambiarEstado(EstadoOperacion.ANULADA);
            } catch (IllegalArgumentException e) {
                throw new OperacionRechazadaException(e.getMessage());
            }
        }
    }
}
