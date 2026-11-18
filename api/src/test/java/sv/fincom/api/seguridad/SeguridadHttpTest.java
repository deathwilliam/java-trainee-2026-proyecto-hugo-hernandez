
package sv.fincom.api.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.clave=FINCOM_CLAVE_DE_PRUEBAS_EXCLUSIVA_2026_LARGA_123",
                "spring.datasource.url=${DB_TEST_URL:jdbc:postgresql://localhost:5432/fincom_test}"
        }
)

class SeguridadHttpTest {

    @LocalServerPort
    private int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();

    private HttpResponse<String> enviar(
            String metodo,
            String ruta,
            String token,
            String json) throws Exception {

        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Accept", "application/json");

        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }

        if ("POST".equals(metodo)) {
            solicitud.header("Content-Type", "application/json");
            solicitud.POST(HttpRequest.BodyPublishers.ofString(json));
        } else {
            solicitud.GET();
        }

        return cliente.send(
                solicitud.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private String iniciarSesion(String correo, String clave)
            throws Exception {

        String json = """
                {
                  "correo": "%s",
                  "clave": "%s"
                }
                """.formatted(correo, clave);

        HttpResponse<String> respuesta =
                enviar("POST", "/api/auth/login", null, json);

        assertEquals(200, respuesta.statusCode(), respuesta.body());

        Matcher matcher = Pattern
                .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(respuesta.body());

        assertTrue(matcher.find(), "El login no devolvio token JWT");

        return matcher.group(1);
    }

    @Test
    void sinTokenRetorna401() throws Exception {

        HttpResponse<String> respuesta =
                enviar("GET", "/api/liquidaciones", null, null);

        assertEquals(401, respuesta.statusCode(), respuesta.body());

        assertTrue(
                respuesta.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .startsWith("application/problem+json")
        );

        assertTrue(respuesta.body().contains("\"status\":401"));
        assertTrue(respuesta.body().contains("\"title\":\"No autorizado\""));
        assertTrue(respuesta.body().contains("\"detail\":\"Se requiere un token JWT valido\""));
        assertTrue(respuesta.body().contains("\"instance\":\"/api/liquidaciones\""));

    }

    @Test
    void clienteNoPuedeLiquidar403() throws Exception {
        String token = iniciarSesion(
                "cliente@fincom.test",
                "FincomLector2026!"
        );

        String json = """
                {
                  "agenteId": 1,
                  "anio": 2026,
                  "mes": 10
                }
                """;

        HttpResponse<String> respuesta =
                enviar("POST", "/api/liquidaciones", token, json);

        assertEquals(403, respuesta.statusCode(), respuesta.body());

        assertTrue(
                respuesta.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .startsWith("application/problem+json")
        );

        assertTrue(respuesta.body().contains("\"status\":403"));
        assertTrue(respuesta.body().contains("\"title\":\"Acceso prohibido\""));
        assertTrue(respuesta.body().contains("\"detail\":\"No tiene permisos para esta operacion\""));
        assertTrue(respuesta.body().contains("\"instance\":\"/api/liquidaciones\""));

    }


    @Test
    void adminLiquida201YDuplicado400() throws Exception {

        String token = iniciarSesion(
                "admin@fincom.test",
                "FincomAdmin2026!"
        );


        String json = """
                {
                  "agenteId": 1,
                  "anio": 2026,
                  "mes": 10
                }
                """;

        HttpResponse<String> primera =
                enviar("POST", "/api/liquidaciones", token, json);

        assertEquals(201, primera.statusCode(), primera.body());

        HttpResponse<String> duplicada =
                enviar("POST", "/api/liquidaciones", token, json);

        assertEquals(400, duplicada.statusCode(), duplicada.body());
    }
}
