package com.utaste;

// Importez InMemoryUserRepository pour accéder à la source de données principale
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.service.WaiterService;

public final class ServiceLocator {
    // Supprimez la référence à InMemoryUserStore, nous n'en avons plus besoin ici
    // private static InMemoryUserStore store;

    private static WaiterService waiters;

    private ServiceLocator() {}

    // Supprimez la méthode store(), elle est la cause du problème.
    // public static InMemoryUserStore store() { ... }

    public static WaiterService waiters() {
        if (waiters == null) {
            // ✨ CORRECTION :
            // On passe maintenant le "vrai" repository (celui de la connexion) au service.
            waiters = new WaiterService(InMemoryUserRepository.getInstance());
        }
        return waiters;
    }
}
