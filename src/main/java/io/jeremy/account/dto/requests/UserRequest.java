package io.jeremy.account.dto.requests;

import io.jeremy.account.validation.ValidEmail;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class UserRequest {

    @NotNull
    @NotBlank
    private final String name;

    @NotNull
    @NotBlank
    private final String lastname;

    @ValidEmail
    @NotNull
    @NotBlank
    private final String email;

    @NotNull
    @NotBlank
    private final String password;

    private List<String> roles;

    public UserRequest(final String name, final String lastname, final String email, final String password) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public UserRequest(final String name, final String lastname, final String email, final String password, List<String> roles) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
