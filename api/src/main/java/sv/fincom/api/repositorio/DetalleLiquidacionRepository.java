
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.fincom.api.entidad.DetalleLiquidacion;

import java.util.List;
import java.util.Optional;

public interface DetalleLiquidacionRepository
        extends JpaRepository<DetalleLiquidacion, Long> {

    boolean existsByOperacion_Id(Long operacionId);

    Optional<DetalleLiquidacion> findByOperacion_Id(
            Long operacionId
    );

    List<DetalleLiquidacion> findByLiquidacion_Id(
            Long liquidacionId
    );
}
