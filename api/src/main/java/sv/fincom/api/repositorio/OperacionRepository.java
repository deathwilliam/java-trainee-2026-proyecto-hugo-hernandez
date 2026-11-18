
package sv.fincom.api.repositorio;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sv.fincom.api.entidad.Operacion;
import sv.fincom.api.entidad.enums.EstadoOperacion;

import java.time.LocalDateTime;
import java.util.List;

public interface OperacionRepository
        extends JpaRepository<Operacion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT o
        FROM Operacion o
        WHERE o.agente.id = :agenteId
          AND o.estado = :estado
          AND o.fechaOperacion >= :inicio
          AND o.fechaOperacion < :fin
        ORDER BY o.id
        """)
    List<Operacion> buscarPorAgentePeriodoYEstado(
            @Param("agenteId") Long agenteId,
            @Param("estado") EstadoOperacion estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    boolean existsByReferencia(String referencia);


    boolean existsByTarifaComision_Id(Long tarifaComisionId);





    List<Operacion> findByAgente_Usuario_Id(Long usuarioId);

    List<Operacion> findByCliente_Usuario_Id(Long usuarioId);

    java.util.Optional<Operacion> findByIdAndAgente_Usuario_Id(
            Long operacionId,
            Long usuarioId
    );

    java.util.Optional<Operacion> findByIdAndCliente_Usuario_Id(
            Long operacionId,
            Long usuarioId
    );

}
