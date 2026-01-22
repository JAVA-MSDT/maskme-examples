/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 * Email: serenitydiver@hotmail.com
 */
package com.javamsdt.javamasking.domain;

import com.javamsdt.maskme.api.annotation.MaskMe;
import com.javamsdt.maskme.implementation.condition.AlwaysMaskMeCondition;


public class Address {
    private Long id;
    @MaskMe(conditions = {AlwaysMaskMeCondition.class}, maskValue = "*****")
    private String street;
    private String building;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private GeoLocation geoLocation;

    public Address() {
    }

    public Address(Long id, String street, String building, String city, String state, String zipCode, String country, GeoLocation geoLocation) {
        this.id = id;
        this.street = street;
        this.building = building;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.geoLocation = geoLocation;
    }

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getBuilding() {
        return building;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", building='" + building + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", country='" + country + '\'' +
                ", geoLocation=" + geoLocation +
                '}';
    }
}
