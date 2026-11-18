
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.fincom.api.entidad.BitacoraOperacion;
import sv.fincom.api.entidad.enums.ResultadoOperacion;

import java.util.List;

public interface BitacoraOperacionRepository
        extends JpaRepository<BitacoraOperacion, Long> {

    // Historial de acciones realizadas por un usuario
    List<BitacoraOperacion> findByUsuario_IdOrderByCreadoEnDesc(
            Long usuarioId
    );

    // Registros asociados a una liquidacion
    List<BitacoraOperacion> findByLiquidacion_IdOrderByCreadoEnDesc(
            Long liquidacionId
    );

    // Filtrar operaciones exitosas o fallidas
    List<BitacoraOperacion> findByResultadoOrderByCreadoEnDesc(
            ResultadoOperacion resultado
    );
}
