
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "correo",
            length = 120,
            nullable = false,
            unique = true
    )
    private String correo;

    @Column(
            name = "clave_hash",
            length = 255,
            nullable = false
    )
    private String claveHash;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Usuario() {
    }

    public Usuario(String correo, String claveHash) {

        if (correo == null
                || correo.isBlank()
                || correo.length() > 120
                || !correo.contains("@")) {
            throw new IllegalArgumentException(
                    "El correo electronico es invalido"
            );
        }

        if (claveHash == null
                || claveHash.isBlank()
                || claveHash.length() > 255) {
            throw new IllegalArgumentException(
                    "El hash de la clave es obligatorio"
            );
        }

        this.correo = correo.trim().toLowerCase();
        this.claveHash = claveHash;
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

    public String getCorreo() {
        return correo;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
