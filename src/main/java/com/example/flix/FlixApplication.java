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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
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

@RestController
@RequestMapping("/movie")
class FlixRestController {

    @Autowired
    private FlixService flixService;

    @GetMapping
    public Flux<Movie> all() {
        return flixService.all();
    }

    @GetMapping("/{id}")
    public Mono<Movie> byId(@PathVariable String id) {
        return flixService.byId(id);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MovieEvent> events(@PathVariable String id) {
        return flixService.byId(id)
                .flatMapMany(flixService::events);
    }
}


@Service
class FlixService {

    @Autowired
    private MovieRepository movieRepository;

    public Flux<Movie> all() {
        return movieRepository.findAll();
    }

    public Mono<Movie> byId(String id) {
        return movieRepository.findById(id);
    }

    public Flux<MovieEvent> events(Movie movie) {
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
        Flux<MovieEvent> events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUserId(), randomEventType())));

        return Flux.zip(interval, events).map(Tuple2::getT2);
    }

    private String randomUserId() {
        String[] choices = "zduo,fangchiw,lijieg,chuanweig".split(",");
        return choices[new Random().nextInt(choices.length)];
    }

    private String randomEventType() {
        return EventType.values()[new Random().nextInt(EventType.values().length)].toString();
    }
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {}

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
class MovieEvent {

    Movie movie;

    Date date;

    String userId;

    String eventType;
}

enum EventType {
    LIKE, DISLIKE, WATCH, WANT;
}

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
