/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 * Email: serenitydiver@hotmail.com
 */
package com.javamsdt.javamasking.mapper;

import com.javamsdt.javamasking.domain.User;
import com.javamsdt.javamasking.dto.AddressDto;
import com.javamsdt.javamasking.dto.GeoLocationDto;
import com.javamsdt.javamasking.dto.UserDto;

public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        AddressDto addressDto = user.getAddress() != null ? new AddressDto(
                user.getAddress().getId(),
                user.getAddress().getStreet(),
                user.getAddress().getBuilding(),
                user.getAddress().getCity(),
                user.getAddress().getState(),
                user.getAddress().getZipCode(),
                user.getAddress().getCountry(),
                getGeoLocation(user)
        ) : null;

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                addressDto,
                user.getBirthDate(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getGender() != null ? user.getGender().getValue() : null,
                user.getBalance(),
                user.getCreatedAt()
        );
    }

    private static GeoLocationDto getGeoLocation(User user) {
        return user.getAddress().getGeoLocation() != null ? getGeoLocationDto(user) : null;
    }

    private static GeoLocationDto getGeoLocationDto(User user) {
        return new GeoLocationDto(
                user.getAddress().getGeoLocation().getId(),
                user.getAddress().getGeoLocation().getLongitude(),
                user.getAddress().getGeoLocation().getLatitude()
        );
    }
}
