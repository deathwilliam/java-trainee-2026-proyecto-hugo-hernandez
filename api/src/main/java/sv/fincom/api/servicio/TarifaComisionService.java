
package sv.fincom.api.servicio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.entidad.TarifaComision;
import sv.fincom.api.entidad.TipoServicio;
import sv.fincom.api.repositorio.TarifaComisionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import sv.fincom.api.dto.TarifaRespuesta;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;

import sv.fincom.api.dto.TarifaSolicitud;
import sv.fincom.api.excepcion.OperacionRechazadaException;

@Service
public class TarifaComisionService {

    private final TarifaComisionRepository tarifaRepository;
    private final EntityManager entityManager;

    public TarifaComisionService(
            TarifaComisionRepository tarifaRepository,
            EntityManager entityManager
    ) {
        this.tarifaRepository = tarifaRepository;
        this.entityManager = entityManager;
    }


    private TarifaRespuesta convertir(TarifaComision tarifa) {

        return new TarifaRespuesta(
                tarifa.getId(),
                tarifa.getTipoServicio().getId(),
                tarifa.getMontoFijo(),
                tarifa.getVigenteDesde(),
                tarifa.getVigenteHasta(),
                tarifa.getActivo()
        );
    }


    @Transactional(readOnly = true)
    public List<TarifaRespuesta> listar() {

        return tarifaRepository.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }


    @Transactional(readOnly = true)
    public TarifaRespuesta obtener(Long id) {

        TarifaComision tarifa = tarifaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La tarifa no existe"
                        )
                );

        return convertir(tarifa);
    }


    @Transactional
    public TarifaRespuesta crear(TarifaSolicitud solicitud) {

        try {

            TarifaComision nueva = crearTarifa(
                    solicitud.tipoServicioId(),
                    solicitud.montoFijo(),
                    solicitud.vigenteDesde(),
                    solicitud.vigenteHasta()
            );

            return convertir(nueva);

        } catch (IllegalArgumentException | IllegalStateException e) {

            throw new OperacionRechazadaException(
                    e.getMessage()
            );
        }
    }


    @Transactional
    public TarifaComision crearTarifa(
            Long tipoServicioId,
            BigDecimal montoFijo,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta
    ) {

        if (tipoServicioId == null || tipoServicioId <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de servicio es obligatorio"
            );
        }

        // Construir la entidad validara monto y fechas.
        // Buscamos el servicio y bloqueamos su fila
        // para evitar registros simultaneos de
        // tarifas para el mismo servicio.

        TipoServicio tipoServicio = entityManager.find(
                TipoServicio.class,
                tipoServicioId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (tipoServicio == null) {
            throw new IllegalArgumentException(
                    "El tipo de servicio no existe"
            );
        }

        TarifaComision nuevaTarifa = new TarifaComision(
                tipoServicio,
                montoFijo,
                vigenteDesde,
                vigenteHasta
        );

        // R14: ninguna tarifa activa debe tener
        // un periodo superpuesto para este servicio.

        long solapamientos;

        if (vigenteHasta == null) {

            solapamientos =
                    tarifaRepository.contarSolapamientosSinFin(
                            tipoServicioId,
                            vigenteDesde
                    );

        } else {

            solapamientos =
                    tarifaRepository.contarSolapamientosConFin(
                            tipoServicioId,
                            vigenteDesde,
                            vigenteHasta
                    );
        }

        if (solapamientos > 0) {
            throw new IllegalStateException(
                    "Ya existe una tarifa activa con "
                            + "un periodo de vigencia superpuesto"
            );
        }

        return tarifaRepository.saveAndFlush(nuevaTarifa);
    }
}
