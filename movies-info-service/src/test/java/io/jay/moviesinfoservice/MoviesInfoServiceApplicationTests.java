package io.jay.moviesinfoservice;

import io.jay.moviesinfoservice.configuration.TestMongoContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMongoContainerConfiguration.class)
class MoviesInfoServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
