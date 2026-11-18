
package sv.fincom.api.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sv.fincom.api.entidad.Liquidacion;

@Service
public class EjecutorLiquidacionService {

    private static final Logger log =
            LoggerFactory.getLogger(EjecutorLiquidacionService.class);

    private final LiquidacionService liquidacionService;
    private final AuditoriaFalloService auditoriaFalloService;

    public EjecutorLiquidacionService(
            LiquidacionService liquidacionService,
            AuditoriaFalloService auditoriaFalloService
    ) {
        this.liquidacionService = liquidacionService;
        this.auditoriaFalloService = auditoriaFalloService;
    }

    public Liquidacion ejecutarLiquidacion(
            Long agenteId,
            Integer anio,
            Integer mes,
            Long usuarioId
    ) {
        try {
            // La liquidacion principal utiliza @Transactional.
            Liquidacion liquidacion = liquidacionService.liquidarComisiones(
                    agenteId,
                    anio,
                    mes,
                    usuarioId
            );

            // Solo se ejecuta cuando la liquidacion termina correctamente.
            log.info(
                    "Liquidacion realizada: agente={}, periodo={}-{}",
                    agenteId, anio, mes
            );

            return liquidacion;

        } catch (RuntimeException error) {

            // Registrar el rechazo sin revelar credenciales ni tokens.
            log.warn(
                    "Liquidacion rechazada: agente={}, periodo={}-{}",
                    agenteId, anio, mes
            );

            String motivo = String.format(
                    "Fallo al liquidar agente %s, periodo %s-%s: %s",
                    agenteId,
                    anio,
                    mes,
                    error.getMessage()
            );

            try {
                // La auditoria fallida utiliza REQUIRES_NEW.
                auditoriaFalloService.registrarFallo(
                        usuarioId,
                        motivo
                );

            } catch (RuntimeException errorAuditoria) {
                // Una falla de auditoria no debe ocultar el error original.
                log.error(
                        "No se pudo registrar la auditoria del fallo",
                        errorAuditoria
                );

                error.addSuppressed(errorAuditoria);
            }

            // Mantener el comportamiento original ante un fallo.
            throw error;
        }
    }
}
