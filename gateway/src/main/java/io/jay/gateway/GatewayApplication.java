package io.jay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/products-java")
                        .filters(f -> f.setPath("/products"))
                        .uri("https://dummyjson.com"))
                .build();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}

@Controller
class GatewayController {

    private final WebClient webClient;

    public GatewayController(WebClient webClient) {
        this.webClient = webClient;
    }

    @BatchMapping(typeName = "MovieInfo")
    public Mono<Map<MovieInfo, List<MovieReview>>> reviews(List<MovieInfo> movieInfos) {
        var movieInfoIds = movieInfos.stream()
                .map(movieInfo -> Long.parseLong(movieInfo.movieInfoId()))
                .collect(Collectors.toList());

        var reviews = fetchReviewList(movieInfoIds);

        return reviews.collectList()
                .map(movieReviews -> {
                    Map<Long, List<MovieReview>> collect = movieReviews.stream()
                            .collect(Collectors.groupingBy(MovieReview::movieInfoId));

                    return movieInfos.stream()
                            .collect(Collectors.toMap(movieInfo -> movieInfo,
                                    movieInfo -> collect.get(Long.parseLong(movieInfo.movieInfoId()))));
                });
    }

    @QueryMapping
    public Flux<MovieInfo> movieInfos() {
        return webClient.get()
                .uri("http://localhost:8080/v1/movieinfos")
                .retrieve()
                .bodyToFlux(MovieInfo.class);
    }

    private Flux<MovieReview> fetchReviews(String movieInfoId) {
        return webClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl("http://localhost:8081/v1/reviews")
                        .queryParam("movieInfoId", Long.parseLong(movieInfoId))
                        .buildAndExpand()
                        .toUri())
                .retrieve()
                .bodyToFlux(MovieReview.class);
    }

    private Flux<MovieReview> fetchReviewList(List<Long> movieInfoIds) {
        return webClient.post()
                .uri("http://localhost:8081/v1/reviews/list")
                .bodyValue(movieInfoIds)
                .retrieve()
                .bodyToFlux(MovieReview.class);
    }
}

record MovieInfo(String movieInfoId, String name, int year, List<String> cast, String release_date) {
}

record MovieReview(String reviewId, Long movieInfoId, String comment, Double rating) {
}