package com.utaste.service;

import java.util.List;                          // pour list()
import java.util.UUID;                          // pour générer un id
import com.utaste.data.memory.InMemoryUserStore;// store RAM
import com.utaste.data.memory.WaiterRepository; // dépôt waiters
import com.utaste.domain.user.Role;             // rôle
import com.utaste.domain.user.User;             // modèle


public class WaiterService {
    private final InMemoryUserStore store;        // accès brut à la Map
    private final WaiterRepository repo;          // accès filtré à WAITER

    public WaiterService(InMemoryUserStore store) { // DI simple
        this.store = store;                         // garder réf
        this.repo = new WaiterRepository(store);    // créer le repo
    }

    public List<User> list() {                    // lister tous les serveurs
        return repo.list();                         // déléguer
    }

    public User create(String first, String last, String email, String pwd) { // créer
        if (email == null || email.isBlank())  throw new IllegalArgumentException("Email requis");            // email vide
        if (!email.contains("@"))              throw new IllegalArgumentException("Format d’email invalide"); // check rapide
        if (pwd == null || pwd.isBlank())      throw new IllegalArgumentException("Mot de passe requis");     // mot de passe vide
        if (store.data().containsKey(email))   throw new IllegalArgumentException("Email déjà utilisé");      // unique

        User u = new User(UUID.randomUUID().toString(), email.trim(), pwd, Role.WAITER); // fabriquer user
        u.firstName = first;                   // set prénom
        u.lastName  = last;                    // set nom
        store.data().put(u.email, u);          // insérer dans la Map (clé=email)
        return u;                              // renvoyer l’objet
    }

    public User update(String oldEmail, String first, String last, String newEmail, String pwd) { // modifier
        User u = repo.findByEmail(oldEmail);   // retrouver existant
        if (u == null) throw new IllegalArgumentException("Utilisateur introuvable");               // sécurité

        if (newEmail == null || newEmail.isBlank()) throw new IllegalArgumentException("Email requis");       // email vide
        if (!newEmail.contains("@"))                throw new IllegalArgumentException("Format d’email invalide"); // format


        if (!newEmail.equals(oldEmail))
            throw new IllegalArgumentException("Modification de l’email non autorisée");

        u.firstName = first;                    // maj prénom
        u.lastName  = last;                     // maj nom
        if (pwd != null && !pwd.isBlank())     // mot de passe saisi ?
            u.password = pwd;                    // alors maj mot de passe

        u.updatedAt = System.currentTimeMillis(); // timestamp maj
        return u;                              // renvoie l’objet modifié
    }

    public void delete(String email) {        // supprimer
        if (repo.findByEmail(email) == null)    // existe ?
            throw new IllegalArgumentException("Utilisateur introuvable");
        store.data().remove(email);             // remove par clé
    }
}
