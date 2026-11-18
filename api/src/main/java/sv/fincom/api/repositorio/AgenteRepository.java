
package sv.fincom.api.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sv.fincom.api.entidad.Agente;

public interface AgenteRepository
        extends JpaRepository<Agente, Long> {

    Optional<Agente> findByIdAndActivoTrue(Long id);

    Optional<Agente> findByCodigoAgente(String codigoAgente);

    boolean existsByCodigoAgente(String codigoAgente);

    boolean existsByUsuario_Id(Long usuarioId);

    List<Agente> findByActivoTrue();
}
