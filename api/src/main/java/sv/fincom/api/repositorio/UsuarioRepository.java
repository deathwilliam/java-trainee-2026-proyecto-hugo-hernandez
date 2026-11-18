
package sv.fincom.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.fincom.api.entidad.Usuario;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByIdAndActivoTrue(Long id);
}
