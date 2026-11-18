
package sv.fincom.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.entidad.*;
import sv.fincom.api.entidad.enums.*;
import sv.fincom.api.excepcion.OperacionRechazadaException;
import sv.fincom.api.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LiquidacionService {

    private final UsuarioRepository usuarioRepository;
    private final AgenteRepository agenteRepository;
    private final OperacionRepository operacionRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final DetalleLiquidacionRepository detalleRepository;
    private final OrdenPagoRepository ordenPagoRepository;
    private final BitacoraOperacionRepository bitacoraRepository;

    public LiquidacionService(
            UsuarioRepository usuarioRepository,
            AgenteRepository agenteRepository,
            OperacionRepository operacionRepository,
            LiquidacionRepository liquidacionRepository,
            DetalleLiquidacionRepository detalleRepository,
            OrdenPagoRepository ordenPagoRepository,
            BitacoraOperacionRepository bitacoraRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.agenteRepository = agenteRepository;
        this.operacionRepository = operacionRepository;
        this.liquidacionRepository = liquidacionRepository;
        this.detalleRepository = detalleRepository;
        this.ordenPagoRepository = ordenPagoRepository;
        this.bitacoraRepository = bitacoraRepository;
    }

    @Transactional
    public Liquidacion liquidarComisiones(
            Long agenteId,
            Integer anio,
            Integer mes,
            Long usuarioId
    ) {

        // 1. Validar los parametros.
        if (agenteId == null || agenteId <= 0
                || usuarioId == null || usuarioId <= 0
                || anio == null || mes == null
                || anio < 2020 || anio > 2100
                || mes < 1 || mes > 12) {

            throw new OperacionRechazadaException(
                    "Agente, operador o periodo invalido"
            );
        }

        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate inicioMesSiguiente = inicioMes.plusMonths(1);

        // 2. Buscar el agente activo.
        Agente agente = agenteRepository
                .findByIdAndActivoTrue(agenteId)
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "El agente no existe o esta inactivo"
                        )
                );

        // 3. Obtener el operador que solicito liquidar.
        // No usamos agente.getUsuario().
        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(usuarioId)
                .orElseThrow(() ->
                        new OperacionRechazadaException(
                                "El operador no existe o esta inactivo"
                        )
                );

        // 4. Evitar liquidaciones duplicadas.
        if (liquidacionRepository
                .existsByAgente_IdAndAnioAndMes(
                        agenteId,
                        anio,
                        mes
                )) {

            throw new OperacionRechazadaException(
                    "El agente ya tiene una liquidacion para este periodo"
            );
        }

        // 5. Buscar operaciones confirmadas.
        List<Operacion> operaciones = operacionRepository
                .buscarPorAgentePeriodoYEstado(
                        agenteId,
                        EstadoOperacion.CONFIRMADA,
                        inicioMes.atStartOfDay(),
                        inicioMesSiguiente.atStartOfDay()
                );

        if (operaciones.isEmpty()) {
            throw new OperacionRechazadaException(
                    "No hay operaciones confirmadas en el periodo"
            );
        }

        // 6. Validar operaciones y calcular comisiones.
        BigDecimal totalComisiones = BigDecimal.ZERO;

        for (Operacion operacion : operaciones) {

            if (detalleRepository.existsByOperacion_Id(
                    operacion.getId()
            )) {
                throw new OperacionRechazadaException(
                        "La operacion ya fue liquidada: "
                                + operacion.getReferencia()
                );
            }

            if (operacion.getMonto() == null
                    || operacion.getMonto().compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                throw new OperacionRechazadaException(
                        "Operacion con monto invalido"
                );
            }

            TarifaComision tarifa =
                    operacion.getTarifaComision();

            if (tarifa == null
                    || !tarifa.estaVigente(
                    operacion.getFechaOperacion()
                            .toLocalDate()
            )) {

                throw new OperacionRechazadaException(
                        "Tarifa no vigente para la operacion: "
                                + operacion.getReferencia()
                );
            }

            totalComisiones = totalComisiones.add(
                    tarifa.getMontoFijo()
            );
        }

        // 7. Crear la liquidacion.
        Liquidacion liquidacion = liquidacionRepository.save(
                new Liquidacion(
                        agente,
                        anio,
                        mes,
                        totalComisiones
                )
        );

        // 8. Crear detalles y actualizar operaciones.
        for (Operacion operacion : operaciones) {

            BigDecimal comision =
                    operacion.getTarifaComision()
                            .getMontoFijo();

            detalleRepository.save(
                    new DetalleLiquidacion(
                            liquidacion,
                            operacion,
                            comision
                    )
            );

            operacion.marcarLiquidada();
        }

        // 9. Crear la orden interna de pago.
        String numeroOrden = String.format(
                "FIN-%04d%02d-%d",
                anio,
                mes,
                agenteId
        );

        ordenPagoRepository.save(
                new OrdenPago(
                        liquidacion,
                        numeroOrden
                )
        );

        // 10. Registrar el exito en la bitacora
        // usando el usuario del operador.
        bitacoraRepository.save(
                new BitacoraOperacion(
                        usuario,
                        liquidacion,
                        AccionBitacora.LIQUIDACION_GENERADA,
                        ResultadoOperacion.EXITOSO,
                        "Liquidacion generada para "
                                + anio + "-" + mes
                )
        );

        // 11. Sincronizar cambios con PostgreSQL.
        liquidacionRepository.flush();

        return liquidacion;
    }
}
