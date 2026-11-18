
package sv.fincom.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.dto.OperacionSolicitud;
import sv.fincom.api.dto.OperacionRespuesta;
import sv.fincom.api.entidad.Agente;
import sv.fincom.api.entidad.Cliente;
import sv.fincom.api.entidad.Operacion;
import sv.fincom.api.entidad.TarifaComision;
import sv.fincom.api.entidad.enums.EstadoOperacion;
import sv.fincom.api.excepcion.OperacionRechazadaException;
import sv.fincom.api.repositorio.AgenteRepository;
import sv.fincom.api.repositorio.ClienteRepository;
import sv.fincom.api.repositorio.OperacionRepository;
import sv.fincom.api.repositorio.TarifaComisionRepository;

@Service
public class OperacionRegistroService {

    private final OperacionRepository operaciones;
    private final AgenteRepository agentes;
    private final ClienteRepository clientes;
    private final TarifaComisionRepository tarifas;

    public OperacionRegistroService(
            OperacionRepository operaciones,
            AgenteRepository agentes,
            ClienteRepository clientes,
            TarifaComisionRepository tarifas) {

        this.operaciones = operaciones;
        this.agentes = agentes;
        this.clientes = clientes;
        this.tarifas = tarifas;
    }

    @Transactional
    public OperacionRespuesta crear(
            OperacionSolicitud solicitud,
            Long usuarioId,
            boolean esAdmin)  {

        String referencia = solicitud.referencia().trim();

        // Evitar referencias duplicadas.
        if (operaciones.existsByReferencia(referencia)) {
            throw new OperacionRechazadaException(
                    "Ya existe una operacion con esa referencia"
            );
        }

        // Una nueva operacion no puede nacer LIQUIDADA.
        if (solicitud.estado() != EstadoOperacion.PENDIENTE
                && solicitud.estado() != EstadoOperacion.CONFIRMADA) {

            throw new OperacionRechazadaException(
                    "El estado inicial debe ser PENDIENTE o CONFIRMADA"
            );
        }

        // Comprobar el agente y su usuario.
        Agente agente = agentes
                .findByIdAndActivoTrue(solicitud.agenteId())
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "El agente no existe o esta inactivo"
                        )
                );

        if (!agente.getUsuario().estaActivo()) {
            throw new OperacionRechazadaException(
                    "El usuario del agente esta inactivo"
            );
        }

        if (!esAdmin && !agente.getUsuario().getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puede registrar operaciones de otro agente"
            );
        }


        // Comprobar el cliente.
        Cliente cliente = clientes
                .findByIdAndActivoTrue(solicitud.clienteId())
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "El cliente no existe o esta inactivo"
                        )
                );

        // Comprobar la tarifa.
        TarifaComision tarifa = tarifas
                .findById(solicitud.tarifaComisionId())
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "La tarifa no existe"
                        )
                );

        if (!tarifa.estaVigente(
                solicitud.fechaOperacion().toLocalDate())) {

            throw new OperacionRechazadaException(
                    "La tarifa no esta activa o vigente"
            );
        }

        if (!Boolean.TRUE.equals(
                tarifa.getTipoServicio().getActivo())) {

            throw new OperacionRechazadaException(
                    "El tipo de servicio esta inactivo"
            );
        }

        // Crear la operacion utilizando la entidad existente.
        Operacion nueva = new Operacion(
                referencia,
                agente,
                cliente,
                tarifa,
                solicitud.monto(),
                solicitud.estado(),
                solicitud.fechaOperacion()
        );

        Operacion guardada = operaciones.saveAndFlush(nueva);

        return new OperacionRespuesta(
                guardada.getId(),
                guardada.getReferencia(),
                agente.getId(),
                cliente.getId(),
                tarifa.getId(),
                guardada.getMonto(),
                guardada.getEstado(),
                guardada.getFechaOperacion()
        );
    }
}
