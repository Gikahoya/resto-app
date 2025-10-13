# Structure des packages et classes — Projet uTaste

| **Package** | **Classes / Interfaces** | **Notes principales** |
|--------------|---------------------------|------------------------|
| `com.utaste.core` | - Result\<T>  <br> - TimeProvider | Utilitaires génériques (succès/erreur, horodatage). |
| `com.utaste.domain.user` | - User (abstract)  <br> - Admin  <br> - Chef  <br> - Waiter  <br> - Credentials  <br> - UserRepository (interface) | Contient la hiérarchie des utilisateurs et le contrat de persistance. |
| `com.utaste.domain.recipe` | - Recipe  <br> - Ingredient  <br> - NutritionFact  <br> - RecipeIngredient (association) | Prévu pour L2/L3 (gestion recettes, nutrition). |
| `com.utaste.data.memory` | - InMemoryUserRepository | Stockage en mémoire (utilisé pour L1). |
| `com.utaste.data.sqlite` | - SQLiteUserRepository | Stockage SQLite (prévu dès L2). |
| `com.utaste.app.auth` | - AuthService  <br> - PasswordPolicy | Authentification + changement de mot de passe. |
| `com.utaste.app.admin` | - ManageWaitersService  <br> - Dto | Fonctions Admin de création/modif/suppression des Waiters. |
| `com.utaste.app.chef` *(L2+)* | (ex: RecipeService, IngredientService) | Réservé pour la logique du Chef. |
| `com.utaste.app.waiter` *(L4)* | (ex: SalesService) | Réservé pour la logique du Waiter. |
| `com.utaste.ui` | - WelcomeActivity  <br> - LoginActivity  <br> - AdminMenuActivity  <br> - ChefMenuActivity  <br> - WaiterMenuActivity  <br> - ChangePasswordActivity  <br> - AdminManageWaitersActivity | Activités Android (interfaces utilisateur par rôle). |
