
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(length = 60, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();


    protected Rol() {
    }















}
