
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.fincom.api.entidad.OrdenPago;

import java.util.Optional;

public interface OrdenPagoRepository
        extends JpaRepository<OrdenPago, Long> {

    // Verifica si una liquidacion ya tiene orden de pago
    boolean existsByLiquidacion_Id(Long liquidacionId);

    // Busca la orden de una liquidacion especifica
    Optional<OrdenPago> findByLiquidacion_Id(
            Long liquidacionId
    );

    // Comprueba si el numero de orden ya existe
    boolean existsByNumeroOrden(String numeroOrden);

    // Busca una orden mediante su numero
    Optional<OrdenPago> findByNumeroOrden(
            String numeroOrden
    );
}
