package sv.fincom.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = {
		"jwt.clave=FINCOM_CLAVE_DE_PRUEBAS_EXCLUSIVA_2026_LARGA_123",
		"spring.datasource.url=${DB_TEST_URL:jdbc:postgresql://localhost:5432/fincom_test}"
})

class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
