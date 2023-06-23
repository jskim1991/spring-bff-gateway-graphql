package io.jay.gateway;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

interface MovieInfoHttpClient {
    @GetExchange("/v1/movieinfos")
    Flux<MovieInfo> getMovies();
}

interface MovieReviewsHttpClient {
    @PostExchange("/v1/reviews/list")
    Flux<MovieReview> getReviews(@RequestBody List<Long> movieInfoIds);
}

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayApplication.class, args);
    }

//    @Bean
//    public RouteLocator routes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route(p -> p.path("/products-java")
//                        .filters(f -> f.setPath("/products"))
//                        .uri("https://dummyjson.com"))
//                .build();
//    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public MovieInfoHttpClient movieInfoHttpProxy(WebClient.Builder builder) {
        var wca = WebClientAdapter.forClient(builder.baseUrl("http://localhost:8080").build());
        return HttpServiceProxyFactory.builder()
                .clientAdapter(wca)
                .build()
                .createClient(MovieInfoHttpClient.class);
    }

    @Bean
    public MovieReviewsHttpClient movieReviewsHttpProxy(WebClient.Builder builder) {
        var wca = WebClientAdapter.forClient(builder.baseUrl("http://localhost:8081").build());
        return HttpServiceProxyFactory.builder()
                .clientAdapter(wca)
                .build()
                .createClient(MovieReviewsHttpClient.class);
    }
}

@Controller
class GatewayController {

    private final MovieInfoHttpClient movieInfoHttpClient;
    private final MovieReviewsHttpClient movieReviewsHttpClient;
    private final ObservationRegistry registry;
    Logger log = LoggerFactory.getLogger(GatewayController.class);

    public GatewayController(MovieInfoHttpClient movieInfoHttpClient, MovieReviewsHttpClient movieReviewsHttpClient, ObservationRegistry registry) {
        this.movieInfoHttpClient = movieInfoHttpClient;
        this.movieReviewsHttpClient = movieReviewsHttpClient;
        this.registry = registry;
    }

    @BatchMapping(typeName = "MovieInfo")
    public Mono<Map<MovieInfo, List<MovieReview>>> reviews(List<MovieInfo> movieInfos) {
        var movieInfoIds = movieInfos.stream()
                .map(movieInfo -> Long.parseLong(movieInfo.movieInfoId()))
                .collect(Collectors.toList());

        var reviews = movieReviewsHttpClient.getReviews(movieInfoIds);

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
        log.info("Starting to fetch movie infos");
        return Observation.createNotStarted("movieInfos", this.registry)
                .observe(movieInfoHttpClient::getMovies);
    }
}

record MovieInfo(String movieInfoId, String name, int year, List<String> cast, String release_date) {
}

record MovieReview(String reviewId, Long movieInfoId, String comment, Double rating) {
}