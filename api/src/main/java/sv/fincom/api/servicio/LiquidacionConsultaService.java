
package sv.fincom.api.servicio;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sv.fincom.api.dto.LiquidacionRespuesta;
import sv.fincom.api.entidad.Liquidacion;
import sv.fincom.api.entidad.OrdenPago;
import sv.fincom.api.repositorio.LiquidacionRepository;
import sv.fincom.api.repositorio.OrdenPagoRepository;
import java.util.List;


@Service
public class LiquidacionConsultaService {

    private final LiquidacionRepository liquidaciones;
    private final OrdenPagoRepository ordenes;

    public LiquidacionConsultaService(
            LiquidacionRepository liquidaciones,
            OrdenPagoRepository ordenes) {

        this.liquidaciones = liquidaciones;
        this.ordenes = ordenes;
    }

    @Transactional(readOnly = true)
    public List<LiquidacionRespuesta> listar() {

        return liquidaciones.findAll()
                .stream()
                .map(liquidacion -> obtener(liquidacion.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public LiquidacionRespuesta obtener(Long id) {

        Liquidacion liquidacion = liquidaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La liquidacion no existe"
                ));

        OrdenPago orden = ordenes.findByLiquidacion_Id(id)
                .orElseThrow(() -> new IllegalStateException(
                        "La liquidacion no tiene orden de pago"
                ));

        return new LiquidacionRespuesta(
                liquidacion.getId(),
                orden.getNumeroOrden(),
                liquidacion.getAnio(),
                liquidacion.getMes(),
                liquidacion.getTotalComisiones(),
                orden.getEstado().name()
        );
    }

    @Transactional(readOnly = true)
    public LiquidacionRespuesta obtenerParaAsesor(
            Long liquidacionId,
            Long usuarioId) {

        boolean perteneceAlAsesor =
                liquidaciones.existsByIdAndAgente_Usuario_Id(
                        liquidacionId,
                        usuarioId
                );

        if (!perteneceAlAsesor) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La liquidacion no existe"
            );
        }

        return obtener(liquidacionId);
    }

}
