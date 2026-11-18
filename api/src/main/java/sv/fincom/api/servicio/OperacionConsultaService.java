
package sv.fincom.api.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.dto.OperacionRespuesta;
import sv.fincom.api.entidad.Operacion;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;
import sv.fincom.api.repositorio.OperacionRepository;

@Service
public class OperacionConsultaService {

    private final OperacionRepository operaciones;

    public OperacionConsultaService(
            OperacionRepository operaciones) {

        this.operaciones = operaciones;
    }

    private OperacionRespuesta convertir(Operacion operacion) {

        return new OperacionRespuesta(
                operacion.getId(),
                operacion.getReferencia(),
                operacion.getAgente().getId(),
                operacion.getCliente().getId(),
                operacion.getTarifaComision().getId(),
                operacion.getMonto(),
                operacion.getEstado(),
                operacion.getFechaOperacion()
        );
    }

    @Transactional(readOnly = true)
    public List<OperacionRespuesta> listar() {

        return operaciones.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperacionRespuesta obtener(Long id) {

        Operacion operacion = operaciones.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La operacion no existe"
                        )
                );

        return convertir(operacion);
    }


    @Transactional(readOnly = true)
    public List<OperacionRespuesta> listarPorAsesor(Long usuarioId) {

        return operaciones.findByAgente_Usuario_Id(usuarioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperacionRespuesta> listarPorCliente(Long usuarioId) {

        return operaciones.findByCliente_Usuario_Id(usuarioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperacionRespuesta obtenerPorAsesor(
            Long operacionId,
            Long usuarioId) {

        Operacion operacion = operaciones
                .findByIdAndAgente_Usuario_Id(operacionId, usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La operacion no existe"
                        )
                );

        return convertir(operacion);
    }

    @Transactional(readOnly = true)
    public OperacionRespuesta obtenerPorCliente(
            Long operacionId,
            Long usuarioId) {

        Operacion operacion = operaciones
                .findByIdAndCliente_Usuario_Id(operacionId, usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La operacion no existe"
                        )
                );

        return convertir(operacion);
    }

}
