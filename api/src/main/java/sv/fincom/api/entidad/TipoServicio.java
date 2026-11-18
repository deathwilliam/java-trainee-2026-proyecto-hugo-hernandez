
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_servicio")
public class TipoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 24, nullable = false, unique = true)
    private String codigo;

    @Column(length = 120, nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();


    protected TipoServicio() {}




    public Long getId() {
        return id;
    }


    public Boolean getActivo() {
        return activo;
    }








    }
