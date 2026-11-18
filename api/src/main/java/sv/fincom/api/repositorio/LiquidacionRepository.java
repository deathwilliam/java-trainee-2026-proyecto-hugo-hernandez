
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.fincom.api.entidad.Liquidacion;

import java.util.List;
import java.util.Optional;

public interface LiquidacionRepository
        extends JpaRepository<Liquidacion, Long> {

    boolean existsByAgente_IdAndAnioAndMes(
            Long agenteId,
            Integer anio,
            Integer mes
    );

    Optional<Liquidacion> findByAgente_IdAndAnioAndMes(
            Long agenteId,
            Integer anio,
            Integer mes
    );

    List<Liquidacion> findByAgente_Id(
            Long agenteId
    );


    boolean existsByIdAndAgente_Usuario_Id(
            Long liquidacionId,
            Long usuarioId
    );

}
