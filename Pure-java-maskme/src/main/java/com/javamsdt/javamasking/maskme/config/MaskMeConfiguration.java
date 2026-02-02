package com.javamsdt.javamasking.maskme.config;

import com.javamsdt.javamasking.maskme.condition.PhoneMaskingCondition;
import com.javamsdt.javamasking.maskme.converter.CustomStringConverter;
import com.javamsdt.javamasking.service.UserService;
import io.github.javamsdt.maskme.api.condition.MaskMeConditionFactory;
import io.github.javamsdt.maskme.api.condition.MaskMeFrameworkProvider;
import io.github.javamsdt.maskme.api.converter.MaskMeConverterRegistry;
import io.github.javamsdt.maskme.logging.MaskMeLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Configuration class for MaskMe library in pure Java applications.
 * <p>
 * This class provides a simple Map-based dependency injection mechanism
 * for registering custom conditions with their dependencies without requiring
 * any framework (Spring, Quarkus, etc.).
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * UserService userService = new UserService();
 * MaskMeConfiguration.setupMaskMe(userService);
 * }</pre>
 *
 * @author Ahmed Samy
 * @since 1.0.0
 */
public class MaskMeConfiguration {

    private MaskMeConfiguration() {

    }
    /**
     * Registry for storing condition instances with their dependencies.
     * Key: Condition class type
     * Value: Condition instance
     */
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Initializes and configures the MaskMe library for pure Java usage.
     * <p>
     * This method performs the following setup:
     * <ul>
     *   <li>Enables MaskMe logging at INFO level</li>
     *   <li>Registers custom condition instances with their dependencies</li>
     *   <li>Configures the framework provider for dependency lookup</li>
     *   <li>Registers custom converters for type conversion</li>
     * </ul>
     * </p>
     *
     * @param userService the user service instance to inject into conditions
     */
    public static void setupMaskMe(UserService userService) {
        // Logger configuration
        MaskMeLogger.enable(Level.INFO);

        // Register condition instances for dependency injection
        registerConditionInstances(userService);

        // Register framework provider for pure Java
        registerFrameworkProvider();

        // Setup custom converters
        setupCustomConverters();
    }

    /**
     * Registers custom condition instances with their required dependencies.
     * <p>
     * Add your custom conditions here with their dependencies injected via constructor.
     * </p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * instances.put(MyCustomCondition.class, new MyCustomCondition(myService));
     * }</pre>
     *
     * @param userService the user service to inject into PhoneMaskingCondition
     */
    private static void registerConditionInstances(UserService userService) {
        // Register PhoneMaskingCondition with UserService dependency
        instances.put(PhoneMaskingCondition.class, new PhoneMaskingCondition(userService));
    }

    /**
     * Registers a framework provider that resolves condition instances from the registry.
     * <p>
     * This provider is used by the MaskMe library to get condition instances
     * when processing masked fields. It performs a simple Map lookup.
     * </p>
     */
    private static void registerFrameworkProvider() {
        MaskMeConditionFactory.setFrameworkProvider(new MaskMeFrameworkProvider() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getInstance(Class<T> type) {
                return (T) instances.get(type);
            }
        });
    }

    /**
     * Configures custom converters for type conversion during masking.
     * <p>
     * Clears any existing global converters and registers custom ones.
     * Custom converters can override default conversion behavior.
     * </p>
     */
    private static void setupCustomConverters() {
        MaskMeConverterRegistry.clearGlobal();
        MaskMeConverterRegistry.registerGlobal(new CustomStringConverter());
    }

    /**
     * Cleans up resources and clears all registered instances and converters.
     * <p>
     * Call this method when shutting down the application to prevent memory leaks.
     * </p>
     *
     * <p><b>Usage:</b></p>
     * <pre>{@code
     * // At application shutdown
     * MaskMeConfiguration.destroy();
     * }</pre>
     */
    public static void destroy() {
        MaskMeConverterRegistry.clearGlobal();
        instances.clear();
    }
}