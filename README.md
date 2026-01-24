# MaskMe - Field Masking Library

## 📋 Overview

MaskMe is a modern, annotation-based Java library for dynamically masking sensitive data in objects. It supports both
regular Java **classes** and Java **Records**, with conditional masking based on runtime inputs and framework integration.

## 🚀 Key Features

- ✅ **Annotation-based masking** - Simple `@MaskMe` annotation.
- ✅ **Conditional masking** – Mask based on runtime conditions.
- ✅ **Framework-agnostic** - Works with Spring, Quarkus, or pure Java, configure the framework of your choice.
- ✅ **Thread-safe** - Proper handling of concurrent requests.
- ✅ **Supports Classes and Records** – Full Java 17+ support.
- ✅ **Field referencing** – Dynamic field referencing with `{fieldName}` syntax.
- ✅ **Nested Field masking** – Masking nested filed in a nested record or class.
- ✅ **Type-safe** - Comprehensive type conversion system.
- ✅ **Custom converters** - Override default behavior.
- ✅ **No modification of originals** – Returns new masked instances.

## 📦 Installation

### Maven

```xml

<dependency>
    <groupId>io.github.java-msdt</groupId>
    <artifactId>maskme</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.java-msdt:maskme:1.0.0'
```

## 🚀 5-Minute Quick Start

### Step 1: Add Dependency

See [Installation](#-installation) above.

### Step 2: Annotate Your Data

```java
public record UserDto(
        @MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "****")
        String password,

        @MaskMe(conditions = {MaskMeOnInput.class}, maskValue = "{name}@masked.com")
        String email,

        String name // Used in the field reference above
) {
}
```

### Step 3: Use in Your Application

```java

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public UserDto getUser(@PathVariable Long id,
                           @RequestHeader("X-Mask-Level") String maskLevel) { // header should be maskMe to trigger masking.

        UserDto dto = userService.getUserDto(id);
        return MaskMeInitializer.mask(dto, MaskMeOnInput.class, maskLevel);
    }
}
```

### Step 4: Configure Your Framework (Optional)

For production use with dependency injection, see:

- [Pure java Setup](docs/02-pure-java-guide.md#step-1-framework-configuration)
- [Spring Setup](docs/03-spring-framework-guide.md#step-1-framework-configuration)
- [Quarkus Setup](docs/04-quarkus-framework-guide.md#step-1-framework-configuration)

✅ **Done!** You're now masking sensitive data.

---

## 🤔 Which Guide Do I Need?

```
                    Start Here
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    Using Spring?   Using Quarkus?   Pure Java?
        │               │               │
        ▼               ▼               ▼
   Spring Guide    Quarkus Guide    Pure Java Guide
        │               │               │
        └───────────────┴───────────────┘
                        │
            Need Custom Logic?
                        │
        ┌───────────────┼───────────────┐
        │                               │
   Custom Conditions              Custom Converters
        │                               │
   Conditions Guide               Converter Guide
```

## 📚 Documentation

### 🚀 Getting Started

- **[Quick Start](#-5-minute-quick-start)** – Get running in 5 minutes
- **[Pure Java Setup](docs/02-pure-java-guide.md#step-1-framework-configuration)** - Manual configuration
- **[Spring Setup](docs/03-spring-framework-guide.md)** – @Bean registration, Security integration
- **[Quarkus Setup](docs/04-quarkus-framework-guide.md)** - @Produces + @Unremovable, Native compilation

### 📖 Core Concepts

- **[Custom Conditions](docs/05-custom-conditions-and-field-patterns.md)** - Role-based, time-based, business logic
- **[Custom Converters](docs/06-converter.md)** – Type conversion, scoped converters
- **[Field Patterns](docs/05-custom-conditions-and-field-patterns.md#-field-reference-patterns)** – Dynamic field referencing
- **[Logging Configuration](#-logging-configuration)** - Debug and monitor masking operations

### 🏗️ Advanced Topics

- **[Library Architecture](docs/01-library-internal-architecture.md)** – Internal design, extension points
- **[Design Philosophy](#-design-philosophy)** – Why conditions aren't cached
- **[Testing Strategies](docs/03-spring-framework-guide.md#-testing-with-spring)** - Unit and integration tests

### 📦 Example Projects

- [Pure Java Examples](Pure-java-maskme)
- [Spring Framework Examples](Spring-maskme)
- [Quarkus Framework Examples](Quarkus-maskme)
- **[Library Source Code](https://github.com/JAVA-MSDT/maskme)**

---

## 🔧 Logging Configuration

MaskMe includes zero-overhead logging disabled by default for production performance. Enable it for debugging and
monitoring.

MaskMe includes zero-overhead logging disabled by default for production performance. Enable it for debugging and
monitoring.

#### **Configuration Options**

**Via System Properties:**

```bash
-Dmaskme.logging.enabled=true -Dmaskme.logging.level=DEBUG
```

**Via Environment Variables:**

```bash
export MASKME_LOGGING_ENABLED=true
export MASKME_LOGGING_LEVEL=DEBUG
```

**Programmatically:**

```java
class ApplicationStartup {
    public void runOnlyOnce() {
        // Enable with a specific level
        MaskMeLogger.enable(Level.FINE);  // DEBUG level
        MaskMeLogger.enable(Level.INFO);  // INFO level

        // Disable completely
        MaskMeLogger.disable();
    }
}
```

#### **Log Levels**

- `DEBUG`: Detailed operation traces (condition creation, field processing).
- `INFO`: High-level operations (processor initialization, framework setup).
- `WARN`: Recoverable issues (fallback conversions, missing fields).
- `ERROR`: Critical failures (condition creation errors, invalid inputs).

#### **Level Mapping**

- `DEBUG` → `Level.FINE` (Java logging).
- `INFO` → `Level.INFO`
- `WARN` → `Level.WARNING`
- `ERROR` → `Level.SEVERE`

#### **What Gets Logged**

- Framework provider registration.
- Condition creation and evaluation.
- Field type conversions.
- Placeholder replacements.
- Processing errors and fallbacks.

#### **Performance Notes**

- **Zero cost when disabled** – No performance impact in production.
- Uses lazy evaluation with suppliers for expensive operations.
- Thread-safe logging with minimal overhead.
- Debug messages are prefixed with `[DEBUGGING]` for easy identification,
    - Due to the formating limitation between java standard logging and framework logging.
    - `FINE` level doesn't log in the same format as others.
    - eg,
      `2026-01-10T16:57:01.222+02:00  INFO 29836 --- [masking] [nio-9090-exec-3] c.j.m.i.condition.MaskMeOnInput: [DEBUGGING] Input set to: maskMe`

---

## ⚡ Quick Examples

#### Basic Masking

```java
// Always mask
@MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "***")
String sensitiveData;

// Conditional masking
@MaskMe(conditions = {MaskMeOnInput.class}, maskValue = "HIDDEN")
String conditionalData;
```

#### Field References

```java

@MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "{firstName}@company.com")
String email; // Results in "Ahmed@company.com" if the firstName is "Ahmed"
```

#### Multiple Conditions

```java

@MaskMe(conditions = {RoleBasedCondition.class, EnvironmentCondition.class})
String data; // Masked if ANY condition returns true
```

---

## 🏗️ Design Philosophy

### Why Register Conditions as Singletons?

MaskMe is **framework-agnostic** by design. It doesn't cache condition instances internally, giving you full control
over lifecycle management.

#### How It Works

```java
// MaskMe asks your framework: "Do you have an instance?"
private void registerMaskConditionProvider() {
    MaskMeConditionFactory.setFrameworkProvider(type -> {
        return yourFramework.getInstance(type);  // You control the lifecycle
    });

// If no framework provider, falls back to reflection:
    new AlwaysMaskMeCondition();  // Creates a new instance each time
}
```

#### Without Singleton Registration (Reflection)

```java

@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field1;  // New instance #1
@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field2;  // New instance #2
@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field3;  // New instance #3
// Result: 3 separate instances via reflection
```

#### With Singleton Registration

```java
// Spring: @Bean, Quarkus: @Produces, Pure Java: Map
@Bean
public AlwaysMaskMeCondition alwaysMaskMeCondition() {
    return new AlwaysMaskMeCondition();  // Created once
}

@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field1;  // Same instance
@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field2;  // Same instance
@MaskMe(conditions = {AlwaysMaskMeCondition.class})
String field3;  // Same instance
// Result: 1 singleton reused 3 times
```

#### Why This Design?

**Benefits:**

- ✅ Works with ANY framework (Spring, Quarkus, Guice, Pure Java)
- ✅ A framework manages lifecycle (creation, destruction, scope)
- ✅ No memory leaks (a framework handles cleanup)
- ✅ Thread-safe (a framework handles synchronization)
- ✅ You control singleton behavior

**Alternative Would Be Worse:**
If MaskMe cached internally, it would need to:

- Manage lifecycle (when to create/destroy?)
- Handle thread safety (synchronization overhead)
- Deal with memory leaks (when to clear cache?)
- Lose framework benefits (no DI, no AOP, no lifecycle hooks)

**Conclusion:** Not a limitation—it's a design decision that keeps the library lightweight, framework-agnostic, and
delegates lifecycle management to frameworks (their job).

---

## 📝 Best Practices

### 1. Startup Configuration

- Configure a framework provider once at application startup
- Clear global converters to prevent memory leaks
- Register custom converters after clearing globals

### 2. Bean Management

- Declare built-in conditions as framework beans (Spring @Bean, Quarkus @Produces)
- Use dependency injection for custom conditions
- Leverage framework configuration properties

### 3. Controller Design

- Use `MaskMeInitializer` for automatic cleanup
- Handle request headers for dynamic masking
- Implement proper error handling

### 4. Memory Management

- Use framework lifecycle hooks (@PreDestroy) to clear global converters
- Clear request-scoped converters in finally blocks
- Avoid memory leaks with proper ThreadLocal cleanup

### 5. Testing

- Use thread-local converters for test isolation
- Clear converters in @BeforeEach/@AfterEach
- Test with different condition inputs

---

## 🔍 Troubleshooting

### Common Issues

1. **NoSuchBeanDefinitionException** (Spring/Quarkus).
    - Declare built-in conditions as beans in your configuration.
    - See framework-specific guides for details.

2. **Memory leaks**
    - Use `MaskMeInitializer` for automatic cleanup (recommended).
    - Or always use `clearInputs()` in finally blocks if you will use `MaskMeProcessor` directly.

3. **Type conversion errors**
    - Provide valid mask values for the field type.
    - Check supported types in converter documentation.

---
## Support

If you find this library useful, consider supporting me:

[Buy me a coffee](https://revolut.me/ahmedsamy85) ☕

## About Me

- I am Ahmed Samy Bakry Mahmoud, a dedicated Java Backend Developer with over 5 years of hands-on experience architecting, building, and maintaining scalable enterprise applications.
- I am passionate about solving complex technical challenges, mentoring fellow developers, and delivering maintainable, high-quality code. I thrive on adopting emerging technologies and championing accessibility and best practices in software development.

### Connect with Me

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?logo=linkedin)](https://www.linkedin.com/in/java-msdt/)
[![YouTube: Status Code - Technology](https://img.shields.io/badge/YouTube-Status%20Code%20Tech-red?logo=youtube)](https://www.youtube.com/@Status-Code)
[![YouTube: Exploration Echoes](https://img.shields.io/badge/YouTube-Exploration%20Echoes-red?logo=youtube)](https://www.youtube.com/@Exploration-Echoes)
[![Instagram](https://img.shields.io/badge/Instagram-Follow-pink?logo=instagram)](https://www.instagram.com/serenitydiver)
[![Facebook](https://img.shields.io/badge/Facebook-Follow-blue?logo=facebook)](https://www.facebook.com/AhmedSamySerenity)

**Happy Masking! 🔒**
