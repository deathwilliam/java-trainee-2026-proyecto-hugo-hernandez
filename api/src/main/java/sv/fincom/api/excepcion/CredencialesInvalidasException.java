
package sv.fincom.api.excepcion;

public class CredencialesInvalidasException
        extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales invalidas");
    }
}
