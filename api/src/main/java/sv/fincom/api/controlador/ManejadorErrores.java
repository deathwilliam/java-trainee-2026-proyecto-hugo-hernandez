
package sv.fincom.api.controlador;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sv.fincom.api.excepcion.CredencialesInvalidasException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sv.fincom.api.excepcion.RecursoNoEncontradoException;

import sv.fincom.api.excepcion.OperacionRechazadaException;

@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    // Regla de negocio rechazada: HTTP 400
    @ExceptionHandler(OperacionRechazadaException.class)
    public ProblemDetail operacionRechazada(
            OperacionRechazadaException e) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        e.getMessage()
                );

        problema.setTitle("Operacion rechazada");

        return problema;
    }

    // Argumentos incorrectos: HTTP 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(
            IllegalArgumentException e) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        e.getMessage()
                );

        problema.setTitle("Datos no validos");

        return problema;
    }

    // Restriccion de integridad: HTTP 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflicto(
            DataIntegrityViolationException e) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        "La operacion entra en conflicto con datos existentes"
                );

        problema.setTitle("Conflicto con los datos");

        return problema;
    }

    // Errores de @Valid: HTTP 400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errores = new TreeMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Hay datos que no cumplen las validaciones"
                );

        problema.setTitle("Datos no validos");
        problema.setProperty("errores", errores);

        return ResponseEntity.badRequest().body(problema);
    }

    // JSON incorrecto: HTTP 400
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "El JSON es invalido o contiene valores incorrectos"
                );

        problema.setTitle("Cuerpo ilegible");

        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(
            CredencialesInvalidasException e) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        "Correo o clave incorrectos"
                );

        problema.setTitle("No autorizado");

        return problema;}

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(
            RecursoNoEncontradoException error) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        error.getMessage()
                );

        problema.setTitle("Recurso no encontrado");

        return problema;
    }

}
