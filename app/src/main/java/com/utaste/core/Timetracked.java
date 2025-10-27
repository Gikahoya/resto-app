package com.utaste.core;
public abstract class Timetracked {
    /**
     * Cette classe sert à identifier le temps à laquelle un objet a été créé et
     * à la dernière modification.
     */
    private long createdAtMs;
    private long updatedAtMs;

    public Timetracked() {
        long now = System.currentTimeMillis();
        this.createdAtMs = now;
        this.updatedAtMs = now;
    }

    // Pour les objets déjà chargés depuis la BD
    public Timetracked(long createdAtMs, long updatedAtMs) {
        this.createdAtMs = createdAtMs;
        this.updatedAtMs = updatedAtMs;
    }

    // Appelé à chaque modification
    public void markUpdated() {
        this.updatedAtMs = System.currentTimeMillis();
    }

    // Getters
    public long getCreatedAtMs() { return createdAtMs; }
    public long getUpdatedAtMs() { return updatedAtMs; }

    // Méthode utilitaire d’affichage
    public String getCreatedAt() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(createdAtMs));
    }

    public String getUpdatedAt() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(updatedAtMs));
    }
}

