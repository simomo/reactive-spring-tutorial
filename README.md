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
args -> movieRepository.deleteAll().subscribe(null, null, () ->Stream.of("Hero", "3 idiots", "Huang Fei Hong")
                .map(title -> new Movie(UUID.randomUUID().toString(), title, title))
                .map(movie -> movieRepository.save(movie))
                .forEach(System.out::println);
```
