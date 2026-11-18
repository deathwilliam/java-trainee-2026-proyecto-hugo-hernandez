
package sv.fincom.api.controlador;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {

    @Bean
    public OpenAPI documentacion() {

        return new OpenAPI()

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                )

                .info(
                        new Info()
                                .title("FINCOM - API de Liquidaciones")
                                .version("1.0")
                                .description(
                                        "Sistema de gestion y liquidacion " +
                                                "de comisiones financieras"
                                )
                );
    }
}
