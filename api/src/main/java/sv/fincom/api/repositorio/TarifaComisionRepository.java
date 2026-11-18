
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.fincom.api.entidad.TarifaComision;

import java.time.LocalDate;
import java.util.List;

public interface TarifaComisionRepository
        extends JpaRepository<TarifaComision, Long> {

    // Obtener las tarifas activas de un servicio
    List<TarifaComision>
    findByTipoServicio_IdAndActivoTrue(
            Long tipoServicioId
    );

    // Buscar tarifas vigentes en una fecha concreta
    @Query("""
        SELECT t
        FROM TarifaComision t
        WHERE t.tipoServicio.id = :tipoServicioId
          AND t.activo = true
          AND t.vigenteDesde <= :fecha
          AND (
              t.vigenteHasta IS NULL
              OR t.vigenteHasta >= :fecha
          )
        """)
    List<TarifaComision> buscarTarifasVigentes(
            @Param("tipoServicioId") Long tipoServicioId,
            @Param("fecha") LocalDate fecha
    );

    // R14: Detectar solapamientos con fecha final
    @Query("""
        SELECT COUNT(t)
        FROM TarifaComision t
        WHERE t.tipoServicio.id = :tipoServicioId
          AND t.activo = true
          AND t.vigenteDesde <= :hasta
          AND (
              t.vigenteHasta IS NULL
              OR t.vigenteHasta >= :desde
          )
        """)
    long contarSolapamientosConFin(
            @Param("tipoServicioId") Long tipoServicioId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    // R14: Detectar solapamientos sin fecha final
    @Query("""
        SELECT COUNT(t)
        FROM TarifaComision t
        WHERE t.tipoServicio.id = :tipoServicioId
          AND t.activo = true
          AND (
              t.vigenteHasta IS NULL
              OR t.vigenteHasta >= :desde
          )
        """)
    long contarSolapamientosSinFin(
            @Param("tipoServicioId") Long tipoServicioId,
            @Param("desde") LocalDate desde
    );
}
