package io.jay.moviesreviewservice;

import io.jay.moviesreviewservice.configuration.TestMongoContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMongoContainerConfiguration.class)
class MoviesReviewServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
