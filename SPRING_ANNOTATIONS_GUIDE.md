# 🎯 Spring Boot Annotations Complete Guide

This guide explains all the key Spring Boot annotations with practical examples from our crypto backend project.

## 🚀 Application Setup Annotations

### `@SpringBootApplication`
**Location**: Main application class  
**Purpose**: Combines three annotations into one convenient annotation
```java
@SpringBootApplication  // Combines @Configuration + @EnableAutoConfiguration + @ComponentScan
public class CryptoBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoBackendApplication.class, args);
    }
}
```

**What it does**:
- `@Configuration`: Marks class as source of bean definitions
- `@EnableAutoConfiguration`: Spring Boot auto-configures based on dependencies
- `@ComponentScan`: Scans for components in this package and sub-packages

---

## 🌐 Web Layer Annotations

### `@RestController`
**Purpose**: Creates REST API endpoints that return JSON
```java
@RestController
@RequestMapping("/api/crypto")
public class CryptoController {
    // Methods here handle HTTP requests
}
```

**What it does**:
- Combines `@Controller` + `@ResponseBody`
- Automatically converts return values to JSON
- Handles HTTP requests and responses

### `@RequestMapping`
**Purpose**: Maps URLs to controller classes or methods
```java
@RequestMapping("/api/crypto")  // Base URL for all endpoints in this controller
```

### `@GetMapping`, `@PostMapping`, etc.
**Purpose**: Maps specific HTTP methods to handler methods
```java
@GetMapping("/health")           // GET /api/crypto/health
@GetMapping("/price/{symbol}")   // GET /api/crypto/price/BTC
public Object getCryptoPrice(@PathVariable String symbol) { ... }
```

### `@PathVariable`
**Purpose**: Extracts values from URL path
```java
@GetMapping("/price/{symbol}")
public Object getPrice(@PathVariable String symbol) {
    // symbol = "BTC" when URL is /price/BTC
}
```

---

## 🔧 Dependency Injection Annotations

### `@Autowired` ⭐ **MOST IMPORTANT**
**Purpose**: Automatically injects dependencies
```java
@Service
public class CryptoService {
    @Autowired
    private CryptoValidator validator;  // Spring provides this automatically!
}
```

**How it works**:
1. Spring sees `@Autowired`
2. Looks for a bean of the required type (CryptoValidator)
3. Finds our `@Component` annotated CryptoValidator
4. Injects that instance automatically
5. No need for `new CryptoValidator()` - Spring handles it!

**Alternative: Constructor Injection (Recommended)**
```java
@Service
public class CryptoService {
    private final CryptoValidator validator;
    
    public CryptoService(CryptoValidator validator) {  // @Autowired optional here
        this.validator = validator;
    }
}
```

---

## 🏗️ Bean Definition Annotations

### `@Component`
**Purpose**: Generic Spring-managed bean
```java
@Component
public class CryptoValidator {
    // Spring creates and manages this instance
}
```

### `@Service`
**Purpose**: Business logic layer (specialized `@Component`)
```java
@Service  // Semantic meaning: this contains business logic
public class CryptoService {
    // Business methods here
}
```

### `@Repository`
**Purpose**: Data access layer (specialized `@Component`)
```java
@Repository  // Semantic meaning: this accesses data
public class CryptoRepository {
    // Database operations here
}
```

### `@Controller`
**Purpose**: Web layer (specialized `@Component`)
```java
@Controller  // For traditional MVC (returns views)
public class WebController {
    // Returns HTML templates
}
```

**Bean Hierarchy**:
```
@Component (generic)
├── @Service (business logic)
├── @Repository (data access)
└── @Controller (web layer)
    └── @RestController (REST APIs)
```

---

## ⚙️ Configuration Annotations

### `@Configuration`
**Purpose**: Defines custom beans manually
```java
@Configuration
public class CryptoConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  // Spring manages this instance
    }
}
```

### `@Bean`
**Purpose**: Creates Spring-managed objects from methods
```java
@Bean
public DateTimeFormatter cryptoDateFormatter() {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}

@Bean(name = "customName")  // Custom bean name
public ThreadPoolExecutor customExecutor() { ... }
```

**Bean Dependencies**:
```java
@Bean
public CryptoApiClient apiClient(RestTemplate restTemplate, DateTimeFormatter formatter) {
    // Spring automatically injects these beans as parameters!
    return new CryptoApiClient(restTemplate, formatter);
}
```

---

## 📄 Property Injection

### `@Value`
**Purpose**: Injects properties from `application.properties`
```java
@Service
public class CryptoService {
    @Value("${app.crypto.max-results:5}")  // Default value = 5
    private int maxResults;
    
    @Value("${app.crypto.api-timeout}")
    private long apiTimeout;
}
```

**Property file** (`application.properties`):
```properties
app.crypto.max-results=10
app.crypto.api-timeout=5000
```

---

## 🧪 Testing Annotations

### `@SpringBootTest`
**Purpose**: Loads complete application context for testing
```java
@SpringBootTest
class AnnotationDemoTest {
    @Autowired
    private CryptoService cryptoService;  // All beans available!
}
```

### `@WebMvcTest`
**Purpose**: Tests only web layer (faster)
```java
@WebMvcTest(CryptoController.class)
class CryptoControllerTest {
    @Autowired
    private MockMvc mockMvc;  // For testing HTTP requests
}
```

---

## 🎯 Key Concepts Summary

### **Inversion of Control (IoC)**
Instead of:
```java
// Manual object creation
CryptoValidator validator = new CryptoValidator();
CryptoService service = new CryptoService(validator);
```

Spring does:
```java
// Spring creates and injects automatically
@Service
public class CryptoService {
    @Autowired
    private CryptoValidator validator;  // Spring provides this!
}
```

### **Bean Lifecycle**
1. **Startup**: Spring scans for `@Component`, `@Service`, etc.
2. **Creation**: Spring creates instances (singletons by default)
3. **Injection**: Spring injects dependencies via `@Autowired`
4. **Usage**: Your code uses the injected beans
5. **Shutdown**: Spring cleans up resources

### **Why This is Powerful**
- **No manual object creation**: Spring handles instantiation
- **Dependency management**: Spring wires objects together
- **Loose coupling**: Classes depend on interfaces, not implementations
- **Easy testing**: Mock dependencies easily
- **Configuration**: Change behavior via properties

---

## 🚀 Try It Out!

Run our application and test these endpoints:

1. **Basic endpoints**:
   - http://localhost:8080/api/crypto/health
   - http://localhost:8080/api/crypto/info

2. **Annotation demonstrations**:
   - http://localhost:8080/api/crypto/popular (uses @Service + @Autowired)
   - http://localhost:8080/api/crypto/price/BTC (uses @PathVariable)
   - http://localhost:8080/api/crypto/config (shows @Value injection)

3. **Run tests**:
   ```bash
   mvn test
   ```
   Watch the console output to see all annotations working together!

---

## 🎓 Next Steps

Once you understand these core annotations, you're ready for:
- External API integration with RestTemplate
- Database integration with Spring Data JPA
- Security with Spring Security
- Caching with `@Cacheable`
- Async processing with `@Async`

**You now understand the foundation of Spring Boot! 🎉** 