package com.utaste.data.memory;

import java.util.ArrayList;
import java.util.List;
import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;


public class WaiterRepository {
    private final InMemoryUserStore store;       // référence au store RAM

    public WaiterRepository(InMemoryUserStore store) { // DI très simple
        this.store = store;                        // garde la réf
    }

    public List<User> list() {                   // retourne seulement les WAITER
        List<User> out = new ArrayList<>();        // liste résultat
        for (User u : store.data().values()) {     // parcourir tous les users
            if (u.role == Role.WAITER) out.add(u);   // filtrer par rôle
        }
        // tri  par nom de famille (optionnel)
        out.sort((a,b) -> String.valueOf(a.lastName)
                .compareToIgnoreCase(String.valueOf(b.lastName)));
        return out;                                // renvoie la liste
    }

    public User findByEmail(String email) {      // récupérer par clé
        return store.data().get(email);            // clé = email
    }
}
