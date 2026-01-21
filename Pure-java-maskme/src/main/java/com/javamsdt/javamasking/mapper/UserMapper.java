/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 * Email: serenitydiver@hotmail.com
 */
package com.javamsdt.javamasking.mapper;

import com.javamsdt.javamasking.domain.User;
import com.javamsdt.javamasking.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface UserMapper {

    @Mapping(target = "genderId", source = "user.gender")
    @Mapping(target = "genderName", source = "user.gender.value")
    UserDto toDto(User user);

}
