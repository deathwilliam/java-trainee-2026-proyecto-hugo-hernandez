
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "usuario_rol",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"usuario_id", "rol_id"}
        )
)
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;


    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();


    protected UsuarioRol() {}
    }





