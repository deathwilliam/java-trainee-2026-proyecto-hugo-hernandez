
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agente")
public class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @Column(
            name = "codigo_agente",
            length = 24,
            nullable = false,
            unique = true
    )
    private String codigoAgente;

    @Column(
            name = "nombre",
            length = 90,
            nullable = false
    )
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Agente() {
    }

    public Agente(
            Usuario usuario,
            String codigoAgente,
            String nombre
    ) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario del agente es obligatorio"
            );
        }
        if (codigoAgente == null
                || codigoAgente.isBlank()
                || codigoAgente.length() > 24) {
            throw new IllegalArgumentException(
                    "El codigo del agente es invalido"
            );
        }

        if (nombre == null
                || nombre.isBlank()
                || nombre.length() > 90) {
            throw new IllegalArgumentException(
                    "El nombre del agente es invalido"
            );
        }

        this.usuario = usuario;
        this.codigoAgente = codigoAgente;
        this.nombre = nombre;
    }

    public Agente(
            String codigoAgente,
            String nombre
    ) {
        this(null, codigoAgente, nombre);
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getCodigoAgente() {
        return codigoAgente;
    }

    public String getNombre() {
        return nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }



    public void actualizarDatos(
            String codigoAgente,
            String nombre) {

        if (codigoAgente == null
                || codigoAgente.isBlank()
                || codigoAgente.length() > 24) {

            throw new IllegalArgumentException(
                    "El codigo del agente es invalido"
            );
        }

        if (nombre == null
                || nombre.isBlank()
                || nombre.length() > 90) {

            throw new IllegalArgumentException(
                    "El nombre del agente es invalido"
            );
        }

        this.codigoAgente = codigoAgente;
        this.nombre = nombre;
    }

}
