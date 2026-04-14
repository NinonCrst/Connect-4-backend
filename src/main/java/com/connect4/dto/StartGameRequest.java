package com.connect4.dto;

/**
 * Body de la requête POST /api/game/start
 *
 * Correspond aux paramètres que React envoie pour démarrer une partie,
 * soit les réglages de l'ancienne VueParametre + Configuration.
 */
public class StartGameRequest {

    private int mode;         // 0-6  (voir constantes GameService)
    private int firstPlayer;  // 1 = rouge commence, 2 = jaune commence
    private int ligne;        // hauteur de la grille (défaut 6)
    private int col;          // largeur de la grille  (défaut 7)
    private int index;        // index de sauvegarde courant
    private int profondeur;   // profondeur Minimax     (défaut 2)

    public StartGameRequest() {}

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getMode()        { return mode; }
    public int getFirstPlayer() { return firstPlayer; }
    public int getLigne()       { return ligne; }
    public int getCol()         { return col; }
    public int getIndex()       { return index; }
    public int getProfondeur()  { return profondeur; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setMode(int mode)               { this.mode = mode; }
    public void setFirstPlayer(int firstPlayer) { this.firstPlayer = firstPlayer; }
    public void setLigne(int ligne)             { this.ligne = ligne; }
    public void setCol(int col)                 { this.col = col; }
    public void setIndex(int index)             { this.index = index; }
    public void setProfondeur(int profondeur)   { this.profondeur = profondeur; }
}
