# **MaskMe-Guide** and use cases for **MaskMe** field masking library

## 📋 Overview

MaskMe is a modern, annotation-based Java library for dynamically masking sensitive data in objects returned when
calling HTTP GET. It supports both regular Java classes and Java Records, with conditional masking based on runtime
inputs and framework integration.

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
    <groupId>com.javamsdt</groupId>
    <artifactId>maskme</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.javamsdt:maskme:1.0.0'
```

## 🚀 Quick Start

### 1. Annotate Your Data

```java
public record UserDto(
    @MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "****")
    String password,
    
    @MaskMe(conditions = {MaskMeOnInput.class}, maskValue = "{name}@masked.com")
    String email,
    
    String name // Used in the field reference above
) {}
```

### 2. Use in Your Application

```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public UserDto getUser(@PathVariable Long id,
                          @RequestHeader("X-Mask-Level") String maskLevel) {
        
        UserDto dto = userService.getUserDto(id);
        return MaskMeInitializer.mask(dto, MaskMeOnInput.class, maskLevel);
    }
}
```

## 📚 Documentation

### 🔧 **Logging Configuration**

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
class ApplicationStartup{
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

### 🔧 **Core Concepts**

- **Conditions**: Control when fields should be masked.
- **Converters**: Handle type conversion for mask values.
- **Field References**: Use `{fieldName}` to reference other field values.
- **Framework Integration**: Leverage dependency injection in Spring/Quarkus.
- **Thread Safety**: Built-in ThreadLocal management.

### 📖 **Detailed Guides**

- [Library Sourcecode](https://github.com/JAVA-MSDT/maskme)
- [Spring Framework Integration Guide](/Spring-maskme)
- [Quarkus Framework Integration Guide](/Quarkus-maskme)
- [Pure Java Integration Guide](/Pure-java-maskme)

| Guide                                                                                         | Description                                                         |
|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| **01. [Library Architecture](docs/01-library-internal-architecture.md)**                      | Technical details about internal structure and design patterns.     |
| **02. [Spring Framework Integration](docs/02-spring-framework-guide.md)**                     | Complete Spring Boot setup, configuration, and usage examples.      |
| **03. [Quarkus Framework Integration](docs/03-quarkus-framework-guide.md)**                   | CDI integration, native compilation, and Quarkus-specific features. |
| **04. [Custom Conditions & Field Patterns](docs/04-custom-conditions-and-field-patterns.md)** | Creating custom masking conditions and configuring field patterns.  |
| **05. [Custom Converters](docs/05-converter.md)**                                             | Creating and using custom type converters for advanced masking.     |

**Note:** Code duplication across Spring, Quarkus, and Pure Java projects is intentional. Each project is a self-contained, fully working example that can be used independently without requiring other guides or modules. This design allows developers to focus on their specific framework of interest.

### ⚡ **Quick Examples**

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

## 🏗️ Design Philosophy

### Why Register Conditions as Singletons?

MaskMe is **framework-agnostic** by design. It doesn't cache condition instances internally, giving you full control over lifecycle management.

#### How It Works

```java
// MaskMe asks your framework: "Do you have an instance?"
MaskMeConditionFactory.setFrameworkProvider(type -> {
    return yourFramework.getInstance(type);  // You control lifecycle
});

// If no framework provider, falls back to reflection:
new AlwaysMaskMeCondition()  // Creates new instance each time
```

#### Without Singleton Registration (Reflection)
```java
@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field1;  // New instance #1
@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field2;  // New instance #2
@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field3;  // New instance #3
// Result: 3 separate instances via reflection
```

#### With Singleton Registration
```java
// Spring: @Bean, Quarkus: @Produces, Pure Java: Map
@Bean
public AlwaysMaskMeCondition alwaysMaskMeCondition() {
    return new AlwaysMaskMeCondition();  // Created once
}

@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field1;  // Same instance
@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field2;  // Same instance
@MaskMe(conditions = {AlwaysMaskMeCondition.class}) String field3;  // Same instance
// Result: 1 singleton reused 3 times
```

#### Why This Design?

**Benefits:**
- ✅ Works with ANY framework (Spring, Quarkus, Guice, Pure Java)
- ✅ Framework manages lifecycle (creation, destruction, scope)
- ✅ No memory leaks (framework handles cleanup)
- ✅ Thread-safe (framework handles synchronization)
- ✅ You control singleton behavior

**Alternative Would Be Worse:**
If MaskMe cached internally, it would need to:
- Manage lifecycle (when to create/destroy?)
- Handle thread safety (synchronization overhead)
- Deal with memory leaks (when to clear cache?)
- Lose framework benefits (no DI, no AOP, no lifecycle hooks)

**Conclusion:** Not a limitation—it's a design decision that keeps the library lightweight, framework-agnostic, and delegates lifecycle management to frameworks (their job).

## 📂 Project Structure

This repository contains three complete, self-contained example projects:

- **Spring-maskme**: Spring Boot integration with dependency injection
- **Quarkus-maskme**: Quarkus CDI integration with native compilation support
- **Pure-java-maskme**: Framework-free implementation with manual dependency management

Each project includes identical domain models, DTOs, and custom converters to demonstrate the same masking scenarios across different frameworks. This intentional duplication ensures each example is complete and runnable without external dependencies.

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

**Happy Masking! 🔒**
