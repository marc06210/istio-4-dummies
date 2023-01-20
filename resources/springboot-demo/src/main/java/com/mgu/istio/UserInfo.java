package com.mgu.istio;

import java.util.List;

public class UserInfo {
    private String sub;
    private String logoutUrl;
    private List<String> profile;
    private String name;
    private String given_name;
    private String family_name;
    private String email;

    public UserInfo() {
    }
    public UserInfo(String sub, String logoutUrl, List<String> profile, String family_name, String given_name,
                    String email) {
        super();
        this.sub = sub;
        this.logoutUrl = logoutUrl;
        this.profile = profile;
        this.given_name = given_name;
        this.family_name = family_name;
        this.email = email;
        this.name = family_name + " " + given_name;
    }

    public String getSub() {
        return sub;
    }
    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getLogoutUrl() {
        return logoutUrl;
    }
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
    public List<String> getProfile() {
        return profile;
    }
    public void setProfile(List<String> profile) {
        this.profile = profile;
    }
    public String getName() {
        return name;
    }
    public String getGiven_name() {
        return given_name;
    }
    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }
    public String getFamily_name() {
        return family_name;
    }
    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
