# Groupe-11-repo

## Objectif
App Android (Java) reconstruisible : écran d’accueil + authentification, rôles **Admin/Chef/Waiter**, **changement de mot de passe** après login.  
Données **en mémoire** (pas de SQLite au L1).

## Dernière ajout
Avec cette mise à jour, les utilisateurs concernés sont enfin en mesure de voir la composition de recettes d'en créer et/ou d'en suppimer.


## Inclus
- Écran d’accueil + **login** avec validations et messages d’erreur
- **Redirection** selon le rôle (menus visibles, actions non actives)
- **Admin** : gestion des Waiters (**CRUD** basique, identifiant unique)
- **Changement de mot de passe** post-auth

##  Comment lancer
1. Ouvrir le projet dans **Android Studio**
2. Choisir un **téléphone** (émulateur ou réel)
3. Cliquer **Run ‘app’** (bouton triangle en haut)

## Comptes par défaut (scénario d’accès): utilisateur et mot de passe
- **Admin** : `admin / admin-pwd`
- **Chef** : `chef / chef-pwd`
- **Waiter** : `waiter1` **ou** `waiter2` / `waiter-pwd`
> Toute faute de frappe affiche un message d’erreur et bloque l’accès.

##  Structure & livraison
- **README** (description, build, scénario L1, limites)
- **READMESUP** pour les demo et diagrammes trié en fonction des livrables
- **Tag** de livraison : `deliverable-1`

## Limites (L1)
- interface peu ergonomique pour la recherche de recette
- Fonctions **Waiter** : affichées mais **inactives**
