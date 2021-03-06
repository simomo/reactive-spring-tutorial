# An practice of the sharing "Reactive Spring" by josh Long

The sharing link: https://www.youtube.com/watch?v=zVNIZXf4BG8

# Notes

## The lib Lombok

```java
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
class Movie {}
```

The annotations will tell the Lombok lib to generate a constructor with all fields as parameters, generate a constructor without any parameters, generate a nice `toString` method and generate getter and setter for each field.

## ReactiveMongoRepository

It's a "reactive" version of `MongoRepository`, all return values of methods are kind of `Flux<T>` or `Mono<T>`.
For example, `MongoRepository.deleteAll` has no return values(void), then it would be impossible to write codes like `movieRepository.deleteAll().subscribe`.

A result is we must careful about that the code is async now, some instincts may be not applicable anymore. Here is an example, the code below will print 3 `MonoOnErrorResume` exceptions instead of the `Movie` objects, it's because the return of `.map(movie -> movieRepository.save(movie))` is `Stream<Mono<Movie>>`, which is a async structure, the `forEach` will be called immediately without waiting for the completion of async operations. To fix this, we must let `System.out::println` wait for the aysnc operations are completed, the solution is what our codes look like now :)
```java
// Error Code!!!
args -> movieRepository.deleteAll().subscribe(null, null, () ->Stream.of("Hero", "3 idiots", "Huang Fei Hong")
                .map(title -> new Movie(UUID.randomUUID().toString(), title, title))
                .map(movie -> movieRepository.save(movie))
                .forEach(System.out::println);
```

## Service is upper the DAO layer

and it's the major part of business logic implementation.

## How to create a stream which generates events/items intervally?

`Flux.zip` two Flux, one is for the payloads/events/items, the other is the timeline.
```java
Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
Flux<MovieEvent> events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUserId(), randomEventType())));

return Flux.zip(interval, events).map(Tuple2::getT2);
```

Please note that, the return value of `zip` is a Flux of tuple, `Flux<Tuple<Long, MovieEvent>>`, so we have to use `Tuple2::getT2` to extract the 2nd value, the event/payload, in the each tuple.

## How to random pick from a array or a enum?

The trick is `new Random().nextInt(length of the array or enum)`.

Examples:
```java
private String randomUserId() {
    String[] choices = "zduo,fangchiw,lijieg,chuanweig".split(",");
    return choices[new Random().nextInt(choices.length)];
}

private String randomEventType() {
    return EventType.values()[new Random().nextInt(EventType.values().length)].toString();
}
```

## How to generate a stream

### `Stream.of(T ...)`

Create a static stream.
```java
Stream.of("Hero", "3 idiots", "Huang Fei Hong")
```

### `Stream.generate(() -> {})`

Create a dynamic stream, every time we get one value from a stream, the lambda will be called.
```java
Stream.generate(() -> new MovieEvent(movie, new Date(), randomUserId(), randomEventType()))
```

## How to generate a Flux

### `Flux.interval(Duration period)`

```java
Flux.interval(Duration.ofSeconds(1));
```

### `Flux.fromStream(Stream<? extends T> s)`

```java
Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUserId(), randomEventType())));
```

### More...

[[Reactor Java #1] How to create Mono and Flux ?](https://medium.com/@cheron.antoine/reactor-java-1-how-to-create-mono-and-flux-471c505fa158)
And its brother articles are also worth to read.

## URL mapping

```java
@RestController
@RequestMapping("/movie")
class FlixRestController {
    
    @GetMapping("/{id}")
    public Mono<Movie> byId(@PathVariable String id) {}
}
```

## `flixService.byId(id).flatMapMany(flixService::events)`

### Async Dependency

It makes `flixService.events` is called after `byId` returns a event.

### `flatMapMany` vs `flatMap`

In latest spring, `flatmap` can only return `Mono`, if you want to map one item to multiple items, you have use `flatMapMany` instead.