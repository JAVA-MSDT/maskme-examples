/**
 * Copyright (c) 2025: Ahmed Samy, All rights reserved.
 * LinkedIn: https://www.linkedin.com/in/java-msdt/
 * GitHub: https://github.com/JAVA-MSDT
 * Email: serenitydiver@hotmail.com
 */
package com.javamsdt.javamasking.domain;

import io.github.javamsdt.maskme.api.annotation.ExcludeMaskMe;
import io.github.javamsdt.maskme.api.annotation.MaskMe;
import io.github.javamsdt.maskme.implementation.condition.AlwaysMaskMeCondition;
import io.github.javamsdt.maskme.implementation.condition.MaskMeOnInput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class User {
    private Long id;
    @MaskMe(conditions = {MaskMeOnInput.class}, maskValue = "*****")
    private String name;
    @MaskMe(conditions = {AlwaysMaskMeCondition.class})
    private String email;
    @MaskMe(conditions = {AlwaysMaskMeCondition.class})
    private String password;
    private String phone;
    @ExcludeMaskMe
    private Address address;
    @MaskMe(conditions = {AlwaysMaskMeCondition.class})
    private LocalDate birthDate;
    private Gender gender;
    @MaskMe(conditions = {AlwaysMaskMeCondition.class})
    private BigDecimal balance;
    private Instant createdAt;

    public User() {
    }

    public User(Long id, String name, String email, String password, String phone, Address address, LocalDate birthDate, Gender gender, BigDecimal balance, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", address=" + address +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                '}';
    }
}
