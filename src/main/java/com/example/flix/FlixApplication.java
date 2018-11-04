package com.example.flix;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class FlixApplication {

    @Autowired
    MovieRepository movieRepository;

    @Bean
    CommandLineRunner demo() {

        return args -> movieRepository.deleteAll().subscribe(null, null, () ->Stream.of("Hero", "3 idiots", "Huang Fei Hong")
                .map(title -> new Movie(UUID.randomUUID().toString(), title, title))
                .forEach(movie -> movieRepository.save(movie).subscribe(System.out::println)));
    }

    public static void main(String[] args) {
        SpringApplication.run(FlixApplication.class, args);
    }
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {}

@Document
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
class Movie {

    @Id
    private String id;

    private String title;

    private String desc;
}
