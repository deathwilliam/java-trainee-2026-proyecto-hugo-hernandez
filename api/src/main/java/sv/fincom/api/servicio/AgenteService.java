
package sv.fincom.api.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.dto.AgenteSolicitud;
import sv.fincom.api.dto.AgenteEditarSolicitud;
import sv.fincom.api.dto.AgenteRespuesta;
import sv.fincom.api.entidad.Agente;
import sv.fincom.api.entidad.Usuario;
import sv.fincom.api.excepcion.OperacionRechazadaException;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;
import sv.fincom.api.repositorio.AgenteRepository;
import sv.fincom.api.repositorio.UsuarioRepository;

@Service
public class AgenteService {

    private final AgenteRepository agentes;
    private final UsuarioRepository usuarios;

    public AgenteService(
            AgenteRepository agentes,
            UsuarioRepository usuarios) {

        this.agentes = agentes;
        this.usuarios = usuarios;
    }

    private AgenteRespuesta convertir(Agente agente) {
        return new AgenteRespuesta(
                agente.getId(),
                agente.getCodigoAgente(),
                agente.getNombre(),
                agente.getActivo()
        );
    }

    @Transactional(readOnly = true)
    public List<AgenteRespuesta> listar() {
        return agentes.findByActivoTrue()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgenteRespuesta obtener(Long id) {

        Agente agente = agentes.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El agente no existe"
                        )
                );

        return convertir(agente);
    }

    @Transactional
    public AgenteRespuesta crear(AgenteSolicitud solicitud) {

        String codigo = solicitud.codigoAgente().trim();
        String nombre = solicitud.nombre().trim();

        if (agentes.existsByCodigoAgente(codigo)) {
            throw new OperacionRechazadaException(
                    "Ya existe un agente con ese codigo"
            );
        }

        Usuario usuario = usuarios
                .findByIdAndActivoTrue(solicitud.usuarioId())
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "El usuario no existe o esta inactivo"
                        )
                );

        if (agentes.existsByUsuario_Id(usuario.getId())) {
            throw new OperacionRechazadaException(
                    "El usuario ya esta asociado a otro agente"
            );
        }

        Agente nuevo = new Agente(
                usuario,
                codigo,
                nombre
        );

        Agente guardado = agentes.save(nuevo);

        return convertir(guardado);
    }

    @Transactional
    public AgenteRespuesta actualizar(
            Long id,
            AgenteEditarSolicitud solicitud) {

        Agente agente = agentes.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El agente no existe"
                        )
                );

        if (!agente.estaActivo()) {
            throw new OperacionRechazadaException(
                    "No se puede modificar un agente inactivo"
            );
        }

        String codigo = solicitud.codigoAgente().trim();
        String nombre = solicitud.nombre().trim();

        agentes.findByCodigoAgente(codigo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new OperacionRechazadaException(
                            "El codigo ya pertenece a otro agente"
                    );
                });

        agente.actualizarDatos(codigo, nombre);

        return convertir(agente);
    }

    @Transactional
    public void desactivar(Long id) {

        Agente agente = agentes.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El agente no existe"
                        )
                );

        agente.desactivar();
    }
}
