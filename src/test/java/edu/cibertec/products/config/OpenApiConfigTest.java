package edu.cibertec.products.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldBuildProductsOpenApiDefinition() {
        OpenAPI openAPI = new OpenApiConfig().productsOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Platon Products API");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("basicAuth");
    }
}
