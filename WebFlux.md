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

### WebClient
- Functional, fluent API for Java 8+
- Async, non-blocking by design
- Reactive, declarative
- Streaming
