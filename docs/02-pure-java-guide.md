# Pure Java Integration Guide

- [Full Pure Java guide project](https://github.com/JAVA-MSDT/MaskMe-Guide/tree/main/Pure-java-maskme)

## 📋 Overview

- This guide demonstrates how to integrate the MaskMe library with pure Java applications.
- No framework dependencies – simple, lightweight, and portable.
- Manual dependency injection using a Map-based registry.

## 🚀 Quick Setup

### Step 1: Framework Configuration

Configure MaskMe without any framework:

```java
public class MaskMeConfiguration {

    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static void setupMaskMe(UserService userService) {
        // Register framework provider for dependency injection
        registerFrameworkProvider();

        // Configure a custom field regex pattern (optional)
        configureFieldPattern();

        // Clear and register custom converters
        setupCustomConverters();

        // Register condition instances
        registerConditionInstances(userService);
    }

    private static void registerFrameworkProvider() {
        MaskMeConditionFactory.setFrameworkProvider(new MaskMeFrameworkProvider() {
            @Override
            public <T> T getInstance(Class<T> type) {
                return (T) instances.get(type);
            }
        });
    }

    private static void configureFieldPattern() {
        // Optional: Configure a custom field reference pattern 
        // Default is {fieldName}, you can change to [fieldName] or others
        MaskMeFieldAccessUtil.setUserPattern(Pattern.compile("\\{([^}]+)\\}"));
    }

    private static void setupCustomConverters() {
        MaskMeConverterRegistry.clearGlobal();
        MaskMeConverterRegistry.registerGlobal(new CustomEmailConverter());
        MaskMeConverterRegistry.registerGlobal(new CustomPhoneConverter());
    }

    private static void registerConditionInstances(UserService userService) {
        // Declare built-in conditions as singletons to avoid reflection overhead
        instances.put(AlwaysMaskMeCondition.class, new AlwaysMaskMeCondition());
        instances.put(MaskMeOnInput.class, new MaskMeOnInput());

        // Register custom conditions with dependencies
        instances.put(PhoneMaskingCondition.class, new PhoneMaskingCondition(userService));
    }

    public static void destroy() {
        MaskMeConverterRegistry.clearGlobal();
        instances.clear();
    }
}
```

### ⚠️ Important: Why Register Built-in Conditions?

**MaskMe creates a NEW instance every time** it encounters a condition annotation unless you register them in your Map
as singletons.

**Quick Summary:**

- ✅ **Memory efficient** – One instance instead of many
- ✅ **Better performance** – No reflection overhead
- ✅ **Consistent with best practices** – Singletons by default
- ✅ **Required for custom conditions** – Enables manual dependency injection

**📖 See [Design Philosophy](../readME.md#-design-philosophy) for a detailed explanation.**

### Step 2: Custom Conditions with Manual DI

Create conditions with constructor-based dependency injection:

```java
public class PhoneMaskingCondition implements MaskMeCondition {

    private final UserService userService;
    private String input;

    public PhoneMaskingCondition(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setInput(Object input) {
        if (input instanceof String inputValue) {
            this.input = inputValue;
        }
    }

    @Override
    public boolean shouldMask(Object maskedFieldValue, Object objectContainingMaskedField) {
        boolean anyUserPhoneMatched = userService.findUsers().stream()
                .anyMatch(user -> user.getPhone().equals(maskedFieldValue));

        return input != null && input.equals(maskedFieldValue) && anyUserPhoneMatched;
    }
}
```

### Step 3: Default Conditions Usage

Use built-in conditions in your DTOs:

```java
public record UserDto(
        @MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "****")
        String password,

        @MaskMe(conditions = {MaskMeOnInput.class}, maskValue = "{name}@masked.com")
        String email,

        @MaskMe(conditions = {PhoneMaskingCondition.class}, maskValue = "***-**-****")
        String phone,

        String name
) {
}
```

### Step 4: Field Reference Configuration

Configure custom field reference patterns at startup:

```java
public class FieldPatternConfiguration {

    public static void configureFieldPattern() {
        // Use square brackets instead of curly braces
        MaskMeFieldAccessUtil.setUserPattern(Pattern.compile("\\[([^]]+)]"));
    }
}

// Now use in DTOs:
public record ProductDto(
        @MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "[name]-MASKED")
        String description,

        String name
) {
}
```

### Step 5: Custom Converters

Implement custom converters:

```java
public class CustomEmailConverter implements MaskMeConverter {

    @Override
    public int getPriority() {
        return 10; // Higher than defaults
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return String.class.equals(type);
    }

    @Override
    public Object convert(String maskValue, Class<?> targetType, Object originalValue,
                          Object objectContainingMaskedField, String maskedFieldName) {

        String processedValue = MaskMeFieldAccessUtil
                .getMaskedValueFromAnotherFieldOrMaskedValue(maskValue, objectContainingMaskedField);

        if (maskedFieldName.toLowerCase().contains("email")) {
            return processedValue + "@custom-domain.com";
        }

        return processedValue;
    }
}
```

## 📚 Practical Examples

### Example 1: @ExcludeMaskMe Annotation

Use `@ExcludeMaskMe` to prevent masking of entire objects or fields:

```java
public record UserDto(
        String name,
        String email,

        // This entire address object will NOT be processed for masking
        @ExcludeMaskMe
        AddressDto address,

        @MaskMe(conditions = {AlwaysMaskMeCondition.class})
        String ssn
) {
}

public record AddressDto(
        String street,
        @MaskMe(conditions = {AlwaysMaskMeCondition.class})
        String city  // This will NOT be masked because the parent has @ExcludeMaskMe
) {
}
```

**Use Cases:**

- Exclude complex nested objects from masking processing
- Improve performance by skipping unnecessary fields
- Preserve original data for non-sensitive fields

### Example 2: Multiple Conditions on Same Field

```java
public record SensitiveDto(
        // Masked if ANY condition returns true (OR logic)
        @MaskMe(
                conditions = {RoleBasedCondition.class, EnvironmentCondition.class, TimeBasedCondition.class},
                maskValue = "***"
        )
        String sensitiveData
) {
}

// Usage
public void processData() {
    SensitiveDto dto = service.getData();

    SensitiveDto masked = MaskMeInitializer.mask(dto,
            RoleBasedCondition.class, "ADMIN",
            EnvironmentCondition.class, "production",
            TimeBasedCondition.class, LocalTime.now()
    );
}
```

**Behavior:** Field is masked if **ANY** condition evaluates to `true`.

### Example 3: Error Handling

```java
public class UserService {

    private final UserRepository repository;

    public UserDto getUser(Long id, String maskInput) {
        try {
            User user = repository.findById(id);
            UserDto dto = mapper.toDto(user);

            // Apply masking if the input is present
            if (maskInput != null && !maskInput.isBlank()) {
                dto = MaskMeInitializer.mask(dto, MaskMeOnInput.class, maskInput);
            }

            return dto;

        } catch (MaskMeException e) {
            log.error("Masking failed for user {}: {}", id, e.getMessage());
            throw new ServiceException("Failed to mask sensitive data", e);
        }
    }
}
```

### Example 4: Logging Output

#### Enable Logging in Configuration

```java
public class MaskMeConfiguration {

    public static void setupMaskMe(UserService userService) {
        // Enable DEBUG level logging
        MaskMeLogger.enable(Level.FINE);

        // Rest of configuration...
    }
}
```

#### Expected Log Output

```log
2025-01-10T10:15:30.123+02:00  INFO 12345 --- [main] c.j.m.m.c.MaskMeConfiguration : MaskMe library initialized

2025-01-10T10:15:31.456+02:00  INFO 12345 --- [main] c.j.m.a.c.MaskMeConditionFactory : [DEBUGGING] Registering framework provider

2025-01-10T10:15:32.789+02:00  INFO 12345 --- [main] c.j.m.i.c.MaskMeOnInput : [DEBUGGING] Input set to: maskMe

2025-01-10T10:15:32.890+02:00  INFO 12345 --- [main] c.j.m.i.c.AlwaysMaskMeCondition : [DEBUGGING] Condition evaluated: shouldMask=true
```

#### Disable Logging for Production

```java
public class MaskMeConfiguration {

    public static void setupMaskMe(UserService userService) {
        // Disable logging for zero overhead
        MaskMeLogger.disable();

        // Rest of configuration...
    }
}
```

## 🎯 Application Integration

### Using MaskMeInitializer (Recommended)

```java
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDto getUser(Long id, String maskLevel) {
        User user = repository.findById(id);
        UserDto dto = mapper.toDto(user);

        return MaskMeInitializer.mask(dto,
                MaskMeOnInput.class, maskLevel,
                PhoneMaskingCondition.class, "ADMIN"
        );
    }

    public List<UserDto> getUsers(String env) {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .map(dto -> MaskMeInitializer.mask(dto, EnvironmentCondition.class, env))
                .toList();
    }
}
```

### Using MaskMeProcessor with Manual Cleanup

```java
public class UserService {

    private final UserRepository repository;
    private final MaskMeProcessor maskProcessor;

    public UserDto getDetailedUser(Long id, boolean maskSensitive) {
        try {
            maskProcessor.setConditionInput(MaskMeOnInput.class, maskSensitive ? "maskMe" : "none");

            User user = repository.findById(id);
            UserDto dto = mapper.toDto(user);

            return maskProcessor.process(dto);
        } finally {
            maskProcessor.clearInputs();
        }
    }
}
```

## 🔧 Advanced Pure Java Integration

### Request-Scoped Converters

```java
public class AdvancedMaskingService {

    public UserDto getUserWithCustomMask(Long id, String pattern) {
        // Register request-scoped converter
        MaskMeConverterRegistry.registerRequestScoped(new CustomPatternConverter(pattern));

        try {
            User user = userService.findById(id);
            return MaskMeInitializer.mask(userMapper.toDto(user));
        } finally {
            MaskMeConverterRegistry.clearRequestScoped();
        }
    }
}

public class CustomPatternConverter implements MaskMeConverter {
    private final String pattern;

    public CustomPatternConverter(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public int getPriority() {
        return 15; // Highest priority
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return String.class.equals(type);
    }

    @Override
    public Object convert(String maskValue, Class<?> targetType, Object originalValue,
                          Object objectContainingMaskedField, String maskedFieldName) {
        return pattern.replace("{original}", String.valueOf(originalValue));
    }
}
```

### Configuration Properties

Manage configuration with properties:

```java
public class MaskMeProperties {

    private boolean enabled = true;
    private String defaultMaskValue = "***";
    private String fieldPattern = "CURLY_BRACES";
    private final Map<String, String> customMasks = new HashMap<>();

    // Getters and setters

    public Pattern getCompiledPattern() {
        return switch (fieldPattern) {
            case "SQUARE_BRACKETS" -> Pattern.compile("\\[([^]]+)]");
            case "PARENTHESES" -> Pattern.compile("\\(([^)]+)\\)");
            default -> Pattern.compile("\\{([^}]+)\\}");
        };
    }
}

public class MaskMeConfiguration {

    public static void setupMaskMe(UserService userService, MaskMeProperties properties) {
        if (properties.isEnabled()) {
            MaskMeFieldAccessUtil.setUserPattern(properties.getCompiledPattern());
        }
        // Rest of configuration...
    }
}
```

## 🧪 Testing with Pure Java

### Writing Your Own Tests

```java

@DisplayName("My Custom Masking Test")
class MyMaskingTest {

    private static UserService userService;

    @BeforeAll
    static void setup() {
        userService = new UserService();
        MaskMeConfiguration.setupMaskMe(userService);
    }

    @Test
    void testCustomMasking() {
        UserDto dto = new UserDto();
        UserDto masked = MaskMeInitializer.mask(dto, MyCondition.class, "input");

        assertThat(masked.field()).isEqualTo("expected-value");
    }
}
```

### Unit Testing

```java
class MaskingTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();

        // Clear and register test-specific converters
        MaskMeConverterRegistry.clearThreadLocal();
        MaskMeConverterRegistry.registerThreadLocal(new TestEmailConverter());
        MaskMeConverterRegistry.registerThreadLocal(new TestPhoneConverter());
    }

    @AfterEach
    void tearDown() {
        MaskMeConverterRegistry.clearThreadLocal();
    }

    @Test
    void testUserMasking() {
        UserDto user = new UserDto("test@example.com", "123-456-7890");
        UserDto masked = MaskMeInitializer.mask(user);

        assertThat(masked.email()).isEqualTo("[TEST-MASKED]");
        assertThat(masked.phone()).isEqualTo("[TEST-PHONE]");
    }
}
```

## 📝 Best Practices

### 1. Startup Configuration

- Configure a framework provider once at application startup
- Clear global converters to prevent memory leaks
- Register custom converters after clearing globals

### 2. Instance Management

- Use Map-based registry for condition instances
- Register all conditions with dependencies at startup
- Keep the registry simple and focused

### 3. Service Design

- Use MaskMeInitializer for cleaner code
- Handle inputs for dynamic masking
- Implement proper error handling

### 4. Memory Management

- Always call `destroy()` when shutting down
- Clear global converters to prevent memory leaks
- Clear the instance registry

### 5. Testing

- Set up MaskMe configuration in `@BeforeAll`
- Use thread-local converters for test isolation
- Clean up resources after tests

## ⚠️ Common Issues & Solutions

### Issue 1: Condition Not Found

```java
// Problem: Custom condition returns null from the framework provider
// Solution: Register the condition instance in the Map
private static void registerConditionInstances(UserService userService) {
    instances.put(PhoneMaskingCondition.class, new PhoneMaskingCondition(userService));
}
```

### Issue 2: NullPointerException in Condition

```java
// Problem: Injected dependency is null
// Solution: Ensure the condition is created with dependencies
private static void registerConditionInstances(UserService userService) {
    // Wrong
    instances.put(PhoneMaskingCondition.class, new PhoneMaskingCondition());

    // Correct
    instances.put(PhoneMaskingCondition.class, new PhoneMaskingCondition(userService));
}
```

### Issue 3: Memory Leaks

```java
// Problem: Converters or instances remain in memory after shutdown
// Solution: Always call destroy()
public static void destroy() {
    MaskMeConverterRegistry.clearGlobal();
    instances.clear();
}
```

## 🔑 Key Differences: Pure Java vs. Framework

| Aspect                     | Pure Java                 | Spring/Quarkus                    |
|----------------------------|---------------------------|-----------------------------------|
| **Dependency Injection**   | Manual Map-based registry | Automatic via framework           |
| **Bean Management**        | Manual instance creation  | Framework-managed beans           |
| **Configuration**          | Static methods            | @Configuration/@ApplicationScoped |
| **Condition Registration** | Manual Map.put()          | @Component/@Produces              |
| **Complexity**             | Simple, explicit          | More abstraction                  |
| **Dependencies**           | Zero framework deps       | Framework required                |

**When to use Pure Java:**

- Lightweight applications
- No framework dependencies desired
- Simple use cases
- Learning/prototyping
- Embedded systems

**When to use Framework:**

- Large applications
- Complex dependency graphs
- Need framework features (web, security, etc.)
- Team familiar with a framework

---

[← Back to Main Documentation](../readME.md)
