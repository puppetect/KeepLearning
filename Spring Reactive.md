# WebFlux

## 演变

#### RestTemplate
- Created 10 years ago, for java 1.5
- Template methods, 24 to start, 40+ today
- Synchronous API
- No streaming
```java
public class ClientApp {
    private static final Logger logger = LoggerFactory.getLogger(Stepla.class);
    private static RestTemplate restTempalte = new RestTemplate();

    static {
        String baseUrl = "http://localhost:8081?delay=2";
        restTempalte.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }

    public static void main(Stringp[] args){
        Instant start = Instant.now();
        for(int i=0; i<3; i++){
            restTempalte.getForObject("/person/{id}", Person.class, i);
        }
        logTime(start);
    }

    private static void logTime(Instant start){
        logger.debug("Elapsed time: " + Durable.between(start, Instant.now()).toMillis() + "ms");
    }
}
```

#### Ten Years Later...
- New language features in Java 8
- Moore's law is no more
- We can no longer pretend like network are synchronous

#### WebClient
- Functional, fluent API for Java 8+
- Async, non-blocking by design
- Reactive, declarative
- Streaming

```java
public class ClientApp{
    private static final Logger logger = LoggerFactory.getLogger(Stepla.class);
    private static WebClient client = WebClient.create("http://localhost:8081?delay=2");

    public static void main(String[] args){
        Instant start = Instant.now();

        // Method 1
        List<Mono<Person>> personMonos = Stream.of(1,2,3).map(i -> client.get().uri("/person/{id}", i).retrieve().bodyToMono(Person.class)).collect(Collectors.toList());
        Mono.when(personMonos).block();

        // Method 2
        Flux<Person> personFlux = Flux.range(1,3).flatMap(i -> client.get().uri("/person/{id}", i).retrieve().bodyToMono(Person.class));
        Mono.when(personFlux).block();

        // Method 3
        Flux.range(1,3).flatMap(i -> client.get().uri("/person/{id}", i).retrieve().bodyToMono(Person.class)).doOnNext(person -> ...).block();

        // Nested Http Calls
        Flux.range(1,3).flatMap(i -> client.get().uri("/person/{id}", i).retrieve().bodyToMono(Person.class)).flatMap(person -> client.get().uri("/person/{id}/hobby", i).retrieve().bodyToMono(Hobby.class)));

        logTime(start);
    }

    private static void logTime(Instant start){
        logger.debug("Elapsed time: " + Durable.between(start, Instant.now()).toMillis() + "ms");
    }
}
```

#### Spring MVC Reactive Support
- Controller can return Flux, Mono, Observable, etc.
- Decoupled from container thread
- Built on Servlet 3.0 async

#### Reactive Type Handling
Return Value | Adapted To
--- | ---
Mono | DefferedResult
Flux / non-streaming | DefferedResult<List<T>>
Flux / streaming | ResponseBodyEmitter with back pressure

## 使用WebClient
*pom.xml*
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
    </dependency>
</dependencies>
```
*MainApp.java*
```java
@SpringBootApplication
public class MainApp {
    public static void main(String[] args){
        SpringApplication.run(MainApp.class, args);
    }

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository){
        return args -> {
            Flux<Account> accounts = Flux.just(
                new Account(1L, 680, 9L),
                new Account(2L, 755, 7L),
                new Account(3L, 798, 2L),
                new Account(4L, 691, 6L),
                new Account(5L, 723, 8L),
                new Account(6L, 755, 3L),
                new Account(7L, 820, 1L),
                new Account(8L, 789, 4L),
                new Account(9L, 529, 10L));
            repository.deleteAll().thenMany(repository.saveAll(accounts)).blockLast();
        };
    }
}
```
*MainController.java**
```java
@RestController
public class MainController {
    public static final Sort BY_SCORE_SORT = Sort.by(Sort.Direction.DESC, "score");

    private WebClient client;

    private AccountRepository accountRepository;

    public MainController(WebClient.Builder builder, AccountRepository repository){
        this.client = builder.baseUrl("http://localhost:8081").build();
        this.accountRepository = repository;
    }

    @GetMapping("person/{id}")
    Mono<Person> getPerson(@PathVariable Long id){
        return client.get().uri("/person/{id}?delay=2", id).retrieve().bodyToMono(Person.class);
    }

    @GetMapping("/persons")
    Flux<Person> getPersons(){
        return client.get().uri("/persons?delay=2").retrieve().bodyToFlux(Person.class);
    }

    @GetMapping(path="/persons/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Person> getPersonStream(){
        return client.get().uri("/person/events").accept(MediaType.TEXT_EVENT_STREAM).retrieve().bodyToFlux(Person.class);
    }

    @GetMapping("/accounts/hobbies")
    Flux<Map<String, String>> getTopAccountHobbies() {
        return accountRepository.findAll(BY_SCORE_SORT)
            .take(5)
            .flatMapSequential(account -> {
                Long personId = account.getPersonId();
                Mono<String> nameMono = client.get().uri("/person/{id}?delay=2", personId)
                    .retrieve()
                    .bodyToMono(Person.class)
                    .map(Person::getName);
                Mono<String> hobbyMono = client.get().uri("/person/{id}/hobby?dealy=1", personId)
                    .retrieve()
                    .bodyToMono(Hobby.class)
                    .map(Hobby::getHobby);
                return Mono.zip(nameMono, hobbyMono, (personName, hobby) -> {
                    Map<String, String> data = new LinkedHashMap<>();
                    data.put("person", personName);
                    data.put("hobby", hobby);
                    return data;
                })
            })
    }
}
```

*MainAppTests.java*
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainAppTests {
    @Autowired
    WebTestClient client;

    @Test
    public void person() {
        client.get().uri("/person/1").exchange().expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8).expectBody(Person.class).isEqualTo(new Person(1L, "Amanda"));
    }

    @Test
    public void persons(){
        client.get().uri("/persons").exhcange().expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8).expectBody().jsonPath("$.length()").isEqualTo(10).jsonPath("[0].name").isEqualTo("Amanda").jsonPath("$[1].name").isEqualTo("Brittany")
    }

    @Test
    public void personsEvents(){
        Flux<Person> body = client.get().uri("/persons/events").exchange().expectStatus().isOk().expectHeader().contentType("text/event-stream;charset=UTF-8").returnResult(Person.class).getResponseBody().take(3);
        StepVerifier.create(body).expectNext(new Person(1L, "Amanda")).expectNext(new Person(2L, "Brittany")).verifyComplete();
    }

}
```

## 最佳实践
- Don't mix blocking and non-blocking APIs
- Vertical non-blocking slices
- Don't put non-blocking code behind synchronous API
- Compose single, deferred, request handle chain
- Don't use block(), subscribe() and the like
- Let Spring MVC handle it
