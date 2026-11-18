
package sv.fincom.api.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sv.fincom.api.entidad.UsuarioRol;

public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, Long> {

    @Query("""
           SELECT ur.rol.codigo
           FROM UsuarioRol ur
           WHERE ur.usuario.id = :usuarioId
           AND ur.rol.activo = true
           """)
    List<String> buscarRolesActivos(
            @Param("usuarioId") Long usuarioId
    );
}
