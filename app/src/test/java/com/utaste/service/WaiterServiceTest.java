package com.utaste.service;


import com.utaste.domain.user.User;
import com.utaste.domain.user.UserRepository;

import com.utaste.domain.user.Credentials; // si jamais ça n'existe pas ou que le nom est différent, adapte ou supprime l'import

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests unitaires pour WaiterService (gestion des vendeurs / waiters).
 */
public class WaiterServiceTest {

    private WaiterService service;
    private InMemoryUserRepository repo;

    @Before
    public void setUp() {
        repo = new InMemoryUserRepository();
        service = new WaiterService(repo);
    }

    // 1) create() avec des données valides
    @Test
    public void create_withValidData_shouldCreateWaiter() {
        User u = service.create("John", "Doe", "john@example.com", "secret123");

        assertNotNull(u);
        assertNotNull(u.id);
        assertEquals("john@example.com", u.email);
        assertEquals("John", u.firstName);
        assertEquals("Doe", u.lastName);

        // Vérifier que l'utilisateur a bien été ajouté dans le repo
        User fromRepo = repo.findByEmail("john@example.com");
        assertNotNull(fromRepo);
        assertEquals(u.id, fromRepo.id);
    }

    // 2) create() avec email null → exception
    @Test(expected = IllegalArgumentException.class)
    public void create_withNullEmail_shouldThrowException() {
        service.create("John", "Doe", null, "pwd");
    }

    // 3) create() avec email vide → exception
    @Test(expected = IllegalArgumentException.class)
    public void create_withBlankEmail_shouldThrowException() {
        service.create("John", "Doe", "   ", "pwd");
    }

    // 4) create() avec email déjà utilisé → exception
    @Test(expected = IllegalArgumentException.class)
    public void create_withDuplicateEmail_shouldThrowException() {
        service.create("John", "Doe", "john@example.com", "pwd1");
        // Deuxième création avec le même email → doit échouer
        service.create("Jane", "Smith", "john@example.com", "pwd2");
    }

    // 5) findByEmail() quand l'utilisateur existe
    @Test
    public void findByEmail_existingUser_shouldReturnUser() {
        service.create("John", "Doe", "john@example.com", "pwd");
        User u = service.findByEmail("john@example.com");

        assertNotNull(u);
        assertEquals("john@example.com", u.email);
    }

    // 6) findByEmail() quand l'utilisateur n'existe pas → null
    @Test
    public void findByEmail_nonExistingUser_shouldReturnNull() {
        User u = service.findByEmail("unknown@example.com");
        assertNull(u);
    }

    // 7) list() doit retourner tous les waiters créés par le service
    @Test
    public void list_shouldReturnAllWaiters() {
        service.create("John", "Doe", "john@example.com", "pwd");
        service.create("Jane", "Smith", "jane@example.com", "pwd");

        List<User> waiters = service.list();

        assertEquals(2, waiters.size());

        List<String> emails = Arrays.asList(
                waiters.get(0).email,
                waiters.get(1).email
        );

        assertTrue(emails.contains("john@example.com"));
        assertTrue(emails.contains("jane@example.com"));
    }

    // 8) update() sur un utilisateur inexistant → exception
    @Test(expected = IllegalArgumentException.class)
    public void update_nonExistingUser_shouldThrowException() {
        service.update("unknown@example.com", "New", "Name", "new@example.com", "pwd");
    }

    // 9) update() avec changement d'email vers un email déjà utilisé → exception
    @Test(expected = IllegalArgumentException.class)
    public void update_withAlreadyUsedNewEmail_shouldThrowException() {
        // Utilisateur qui possède déjà newEmail
        service.create("Existing", "User", "existing@example.com", "pwd1");

        // Utilisateur qu'on veut modifier
        service.create("John", "Doe", "john@example.com", "pwd2");

        // On essaye de donner à John l'email "existing@example.com"
        service.update(
                "john@example.com",
                "John", "Doe",
                "existing@example.com",
                "newpwd"
        );
    }

    // 10) update() avec un nouveau mot de passe → le mot de passe est modifié
    @Test
    public void update_withNewPassword_shouldChangePassword() {
        service.create("John", "Doe", "john@example.com", "oldpwd");
        User before = repo.findByEmail("john@example.com");
        String oldPwd = before.password;
        long oldUpdatedAt = before.updatedAt;

        User updated = service.update(
                "john@example.com",
                "John", "Doe",
                "john@example.com",
                "newpwd"
        );

        assertEquals("john@example.com", updated.email);
        assertEquals("newpwd", updated.password);
        assertNotEquals(oldPwd, updated.password);
        assertTrue(updated.updatedAt >= oldUpdatedAt);
    }

    // 11) update() avec mot de passe vide → ne change pas le mot de passe
    @Test
    public void update_withBlankPassword_shouldKeepOldPassword() {
        service.create("John", "Doe", "john@example.com", "oldpwd");
        User before = repo.findByEmail("john@example.com");
        String oldPwd = before.password;

        User updated = service.update(
                "john@example.com",
                "Johnny", "Doey",
                "john@example.com",
                "   " // mot de passe vide/blanc
        );

        assertEquals("Johnny", updated.firstName);
        assertEquals("Doey", updated.lastName);
        // Le mot de passe ne doit PAS avoir changé
        assertEquals(oldPwd, updated.password);
    }

    // 12) delete() avec utilisateur inexistant → exception
    @Test(expected = IllegalArgumentException.class)
    public void delete_nonExistingUser_shouldThrowException() {
        service.delete("unknown@example.com");
    }

    // 13) delete() avec utilisateur existant → supprimé du repo
    @Test
    public void delete_existingUser_shouldRemoveUser() {
        service.create("John", "Doe", "john@example.com", "pwd");
        assertNotNull(repo.findByEmail("john@example.com"));

        service.delete("john@example.com");

        assertNull(repo.findByEmail("john@example.com"));
    }

    /**
     * Implémentation simple en mémoire de UserRepository
     * pour les tests (pas de vraie base de données).
     */
    private static class InMemoryUserRepository implements UserRepository {

        private final Map<String, User> usersById = new HashMap<>();

        @Override
        public User findByCredentials(Credentials creds) {
            if (creds == null) return null;
            // ⬇️ Adapte si ta classe Credentials n'a pas ces champs
            for (User u : usersById.values()) {
                if (creds.id.equals(u.email) && creds.password.equals(u.password)) {
                    return u;
                }
            }
            return null;
        }

        @Override
        public User findByEmail(String email) {
            if (email == null) return null;
            for (User u : usersById.values()) {
                if (email.equals(u.email)) {
                    return u;
                }
            }
            return null;
        }

        @Override
        public User findById(String id) {
            return usersById.get(id);
        }

        @Override
        public void addUser(User user) {
            usersById.put(user.id, user);
        }

        @Override
        public void updateUser(User user) {
            usersById.put(user.id, user);
        }

        @Override
        public void deleteUser(String emailOrId) {
            // Dans WaiterService, on appelle deleteUser(userToDelete.id)
            usersById.remove(emailOrId);
        }

        @Override
        public List<User> getAllUsers() {
            return new ArrayList<>(usersById.values());
        }
    }
}
