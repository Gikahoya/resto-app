package com.utaste.domain.user;                 // dossier logique


public class User {
    public final String id;                       // identifiant technique
    public String firstName;                      // prénom (optionnel)
    public String lastName;                       // nom (optionnel)
    public String email;                          // email = clé principale
    public String password;                       // L1: pas de hash ici
    public Role role;                             // ADMIN ou WAITER
    public long createdAt;                        // timestamp création
    public long updatedAt;                        // timestamp mise à jour

    public User(String id, String email, String password, Role role) { // champs obligatoires
        this.id = id;                               // set id
        this.email = email;                         // set email
        this.password = password;                   // set mdp
        this.role = role;                           // set rôle
        long now = System.currentTimeMillis();      // maintenant en ms
        this.createdAt = now;                       // created = now
        this.updatedAt = now;                       // updated = now (au début pareil)
    }
}
