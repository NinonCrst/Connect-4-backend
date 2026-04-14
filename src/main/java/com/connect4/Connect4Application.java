package com.connect4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Connect4Application — remplace l'ancien Main.java Swing.
 *
 * L'ancien Main faisait :
 *   - Connexion BDD                → géré par Spring Data / application.properties
 *   - Création du modèle           → géré par GameService (à la demande, par session)
 *   - Création des vues Swing      → supprimées, remplacées par React
 *   - Wiring des controllers Swing → supprimé, remplacé par @RestController Spring
 */
@SpringBootApplication
public class Connect4Application {

    public static void main(String[] args) {
        SpringApplication.run(Connect4Application.class, args);
    }
}
