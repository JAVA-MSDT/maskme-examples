/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 * Email: serenitydiver@hotmail.com
 */
package com.javamsdt.javamasking.masking;

import com.javamsdt.javamasking.domain.User;
import com.javamsdt.javamasking.dto.UserDto;
import com.javamsdt.javamasking.mapper.UserMapper;
import com.javamsdt.javamasking.mapper.UserMapperImpl;
import com.javamsdt.javamasking.maskme.condition.PhoneMaskingCondition;
import com.javamsdt.javamasking.maskme.config.MaskMeConfiguration;
import com.javamsdt.javamasking.service.UserService;
import com.javamsdt.maskme.MaskMeInitializer;
import com.javamsdt.maskme.implementation.condition.MaskMeOnInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMasking Integration Tests - MaskMe Pure Java")
class UserMaskingTest {

    private static UserService userService;
    private static UserMapper userMapper;

    @BeforeAll
    static void setup() {
        userService = new UserService();
        userMapper = new UserMapperImpl();
        MaskMeConfiguration.setupMaskMe(userService);
    }

    @Nested
    @DisplayName("Unmasked Operations")
    class UnmaskedOperations {

        @Test
        @DisplayName("Should return original user data without any masking")
        void shouldReturnOriginalUserData() {
            // Given
            Long userId = 1L;

            // When
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // Then
            assertThat(userDto.id()).isEqualTo(1L);
            assertThat(userDto.name()).isEqualTo("Ahmed Samy");
            assertThat(userDto.email()).isEqualTo("one@mail.com");
            assertThat(userDto.phone()).isEqualTo("01000000000");
            assertThat(userDto.address().city()).isEqualTo("City One");
        }
    }

    @Nested
    @DisplayName("Conditional Masking with MaskMeOnInput")
    class ConditionalMasking {

        @Test
        @DisplayName("Should mask sensitive fields when condition matches")
        void shouldMaskSensitiveFieldsWhenConditionMatches() {
            // Given
            Long userId = 1L;
            String maskInput = "maskMe";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.id()).isEqualTo(1000L);
            assertThat(masked.password()).isEqualTo("************");
            assertThat(masked.email()).isEqualTo("[EMAIL PROTECTED]");
            assertThat(masked.birthDate()).isEqualTo(LocalDate.of(1800, 1, 1));
            assertThat(masked.balance()).isEqualTo(BigDecimal.ZERO);
            assertThat(masked.name()).contains("one@mail.com").contains("-M");
            assertThat(masked.address().city()).isEqualTo("****");
            assertThat(masked.address().zipCode()).isEqualTo("[ZIP_MASKED]");
            assertThat(masked.address().geoLocation().latitude()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should not mask fields when condition does not match")
        void shouldNotMaskWhenConditionDoesNotMatch() {
            // Given
            Long userId = 1L;
            String maskInput = "doNotMask";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.id()).isEqualTo(1000L);
            assertThat(masked.name()).isEqualTo("Ahmed Samy");
            assertThat(masked.address().zipCode()).isEqualTo("Zip One");
        }
    }

    @Nested
    @DisplayName("Custom Condition with Dependency Injection")
    class CustomConditionTests {

        @Test
        @DisplayName("Should mask only matching phone number using PhoneMaskingCondition")
        void shouldMaskOnlyMatchingPhoneNumber() {
            // Given
            String maskInput = "maskMe";
            String targetPhone = "01000000000";

            // When
            List<UserDto> users = userService.findUsers().stream()
                    .map(user -> MaskMeInitializer.mask(userMapper.toDto(user),
                            MaskMeOnInput.class, maskInput,
                            PhoneMaskingCondition.class, targetPhone))
                    .toList();

            // Then
            assertThat(users).hasSize(4);
            assertThat(users.get(0).phone()).isEqualTo("****");
            assertThat(users.get(1).phone()).isEqualTo("01000000011");
            assertThat(users.get(2).phone()).isEqualTo("01000000022");
            assertThat(users.get(3).phone()).isEqualTo("01000000033");
        }
    }

    @Nested
    @DisplayName("AlwaysMaskMeCondition Tests")
    class AlwaysMaskConditionTests {

        @Test
        @DisplayName("Should always mask fields on domain entity")
        void shouldAlwaysMaskFieldsOnDomainEntity() {
            // Given
            Long userId = 1L;
            User user = userService.findUserById(userId);

            // When
            User masked = MaskMeInitializer.mask(user);

            // Then
            assertThat(masked.getEmail()).isEqualTo("****");
            assertThat(masked.getPassword()).isEqualTo("************");
            assertThat(masked.getBalance()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("@ExcludeMaskMe Annotation Tests")
    class ExcludeMaskMeTests {

        @Test
        @DisplayName("Should not process field marked with @ExcludeMaskMe")
        void shouldNotProcessExcludedField() {
            // Given
            Long userId = 1L;
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto);

            // Then
            assertThat(masked.genderName()).isNotNull();
            assertThat(masked.genderName()).isEqualTo("Male");
        }
    }

    @Nested
    @DisplayName("Field Referencing Tests")
    class FieldReferencingTests {

        @Test
        @DisplayName("Should replace {fieldName} placeholders with actual field values")
        void shouldReplacePlaceholdersWithFieldValues() {
            // Given
            Long userId = 1L;
            String maskInput = "maskMe";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.name()).contains("one@mail.com");
            assertThat(masked.name()).contains("-M");
        }
    }

    @Nested
    @DisplayName("Custom Converter Tests")
    class CustomConverterTests {

        @Test
        @DisplayName("Should use custom converter with higher priority")
        void shouldUseCustomConverterWithHigherPriority() {
            // Given
            Long userId = 1L;
            String maskInput = "maskMe";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.password()).isEqualTo("************");
            assertThat(masked.email()).isEqualTo("[EMAIL PROTECTED]");
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests")
    class TypeConversionTests {

        @Test
        @DisplayName("Should correctly convert mask values to different field types")
        void shouldConvertMaskValuesToCorrectTypes() {
            // Given
            Long userId = 1L;
            String maskInput = "maskMe";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.id()).isEqualTo(1000L);
            assertThat(masked.birthDate()).isEqualTo(LocalDate.of(1800, 1, 1));
            assertThat(masked.balance()).isEqualTo(BigDecimal.ZERO);
            assertThat(masked.createdAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Nested Object Masking Tests")
    class NestedObjectMaskingTests {

        @Test
        @DisplayName("Should recursively mask fields in nested objects")
        void shouldRecursivelyMaskNestedObjectFields() {
            // Given
            Long userId = 1L;
            String maskInput = "maskMe";
            UserDto userDto = userMapper.toDto(userService.findUserById(userId));

            // When
            UserDto masked = MaskMeInitializer.mask(userDto, MaskMeOnInput.class, maskInput);

            // Then
            assertThat(masked.address().city()).isEqualTo("****");
            assertThat(masked.address().zipCode()).isEqualTo("[ZIP_MASKED]");
            assertThat(masked.address().geoLocation()).isNotNull();
            assertThat(masked.address().geoLocation().latitude()).isEqualTo(0.0);
        }
    }
}
