/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 */
package com.javamsdt.masking.config;

import com.javamsdt.masking.maskconverter.CustomStringConverter;
import com.javamsdt.maskme.api.condition.MaskMeConditionFactory;
import com.javamsdt.maskme.api.condition.MaskMeFrameworkProvider;
import com.javamsdt.maskme.api.converter.MaskMeConverterRegistry;
import com.javamsdt.maskme.api.exception.MaskMeException;
import com.javamsdt.maskme.implementation.condition.AlwaysMaskMeCondition;
import com.javamsdt.maskme.implementation.condition.MaskMeOnInput;
import com.javamsdt.maskme.logging.MaskMeLogger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MaskingConfiguration {

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void registerCustomConverters() {
        MaskMeLogger.enable(Level.FINE);

        registerMaskConditionProvider();
        // Clear Global
        MaskMeConverterRegistry.clearGlobal();
        // Register user's custom converters
        MaskMeConverterRegistry.registerGlobal(new CustomStringConverter());

       // MaskMeFieldAccessUtil.setUserPattern(Pattern.compile("\\{([^}]+)}"));
    }

    @Bean
    public AlwaysMaskMeCondition alwaysMaskMeCondition() {
        return new AlwaysMaskMeCondition();
    }

    @Bean
    public MaskMeOnInput maskMeOnInput() {
        return new MaskMeOnInput();
    }

    public void registerMaskConditionProvider() {
        // One-time registration at startup
        MaskMeConditionFactory.setFrameworkProvider(new MaskMeFrameworkProvider() {
            @Override
            public <T> T getInstance(Class<T> type) {
                try {
                    return applicationContext.getBean(type);
                } catch (Exception e) {
                    log.warn("Failed to get bean of type {} from the application context", type.getName(), e);
                    throw new MaskMeException("Failed to get bean of type " + type.getName() + " from the application context", e);
                }
            }
        });
    }

    @PreDestroy
    public void destroy() {
        // to avoid any memory leak from the current application run.
        // that way you will avoid any custom register to live in memory
        // due to the live reference from this run
        MaskMeConverterRegistry.clearGlobal();
    }
}