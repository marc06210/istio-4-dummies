package com.mgu.istio.oidclight.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing data returned by the {@link com.mgu.istio.oidclight.OidcController}.
 *
 * @author Marc Guerrini
 */
public class UserInformation {

    private String userId;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private List<String> profiles = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}
