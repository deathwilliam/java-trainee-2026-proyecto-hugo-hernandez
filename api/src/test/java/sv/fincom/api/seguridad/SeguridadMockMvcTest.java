
package sv.fincom.api.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "jwt.clave=FINCOM_CLAVE_DE_PRUEBAS_EXCLUSIVA_2026_LARGA_123",
        "spring.datasource.url=${DB_TEST_URL:jdbc:postgresql://localhost:5432/fincom_test}"
})
@AutoConfigureMockMvc
class SeguridadMockMvcTest {

    @Autowired
    private MockMvc mvc;

    private String iniciarSesion(String correo, String clave)
            throws Exception {

        String json = """
                {
                  "correo": "%s",
                  "clave": "%s"
                }
                """.formatted(correo, clave);

        String respuesta = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = Pattern
                .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(respuesta);

        assertTrue(matcher.find(), "No se recibio un JWT");

        return matcher.group(1);
    }

    @Test
    void sinTokenRetorna401() throws Exception {

        mvc.perform(get("/api/operaciones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void clienteNoPuedeRegistrar403() throws Exception {

        String token = iniciarSesion(
                "cliente@fincom.test",
                "FincomLector2026!"
        );

        mvc.perform(
                        post("/api/operaciones")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminPuedeConsultar200() throws Exception {

        String token = iniciarSesion(
                "admin@fincom.test",
                "FincomAdmin2026!"
        );

        mvc.perform(
                        get("/api/operaciones")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }
}
