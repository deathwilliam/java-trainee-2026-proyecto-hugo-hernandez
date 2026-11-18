
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(name = "codigo_cliente",length = 24, nullable = false,unique = true)
    private String codigoCliente;


    @Column(length = 90, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();


    protected Cliente() {}


    public Long getId() {
        return id;
    }

}


