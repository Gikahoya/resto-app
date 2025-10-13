package com.utaste.domain.user;

public class Credentials {
    public final String id;        // username
    public final String password;  // mot de passe

    public Credentials(String id, String password) {
        this.id = id;
        this.password = password;
    }
}