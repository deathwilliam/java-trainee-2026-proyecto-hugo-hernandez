
package sv.fincom.api.servicio;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sv.fincom.api.entidad.TarifaComision;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;
import sv.fincom.api.repositorio.OperacionRepository;
import sv.fincom.api.repositorio.TarifaComisionRepository;

@Service
public class TarifaEstadoService {

    private final TarifaComisionRepository tarifas;
    private final OperacionRepository operaciones;

    public TarifaEstadoService(
            TarifaComisionRepository tarifas,
            OperacionRepository operaciones) {

        this.tarifas = tarifas;
        this.operaciones = operaciones;
    }

    @Transactional
    public void desactivar(Long id) {

        TarifaComision tarifa = tarifas.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La tarifa no existe"
                        )
                );

        if (!Boolean.TRUE.equals(tarifa.getActivo())) {
            return;
        }

        if (operaciones.existsByTarifaComision_Id(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La tarifa tiene operaciones asociadas y no puede desactivarse"
            );
        }

        tarifa.desactivar();
    }
}
