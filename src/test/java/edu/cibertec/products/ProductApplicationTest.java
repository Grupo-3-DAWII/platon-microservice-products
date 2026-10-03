package edu.cibertec.products;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ProductApplicationTest {

    @Test
    void shouldStartApplication() {
        try (MockedStatic<SpringApplication> application = mockStatic(SpringApplication.class)) {
            ProductApplication.main(new String[0]);
            application.verify(() -> SpringApplication.run(ProductApplication.class, new String[0]));
        }
    }
}
