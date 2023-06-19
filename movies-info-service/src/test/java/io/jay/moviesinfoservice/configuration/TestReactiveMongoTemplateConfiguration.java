package io.jay.moviesinfoservice.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration
@Import(TestMongoContainerConfiguration.class)
public class TestReactiveMongoTemplateConfiguration {

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoDBContainer container) throws Exception {
        ConnectionString connectionString = new ConnectionString(container.getReplicaSetUrl());
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        MongoClient mongoClient = MongoClients.create(mongoClientSettings);

        return new ReactiveMongoTemplate(mongoClient, "test");
    }
}
