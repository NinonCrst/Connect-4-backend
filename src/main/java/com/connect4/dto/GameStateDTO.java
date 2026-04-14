package com.connect4.dto;

/**
 * GameStateDTO — snapshot complet de la partie envoyé à React après chaque action.
 *
 * Contient tout ce dont le frontend a besoin pour afficher l'état du jeu :
 * la grille, qui joue, le statut, les scores Minimax, etc.
 */
public class GameStateDTO {

    private long      gameId;
    private int[][]   grille;       // plateau de jeu  (0 = vide, 1 = rouge, 2 = jaune)
    private boolean[][] grilleWin;  // cases gagnantes à highlighter
    private int       currentTurn;  // joueur dont c'est le tour (1 ou 2)
    private int       mode;         // mode de jeu (0-6)
    private int       playerTurn;   // 0 = humain commence, 1 = IA commence, -1 = N/A
    private String    status;       // "ONGOING" | "WIN_P1" | "WIN_P2" | "DRAW"
    private int[]     tabScore;     // scores Minimax par colonne (null si non applicable)
    private int       confiance;    // niveau de confiance IA (0-5)
    private int       ligne;        // hauteur de la grille
    private int       col;          // largeur de la grille
    private int       profondeur;   // profondeur Minimax courante

    // -------------------------------------------------------------------------
    // Constructeur complet
    // -------------------------------------------------------------------------

    public GameStateDTO(long gameId, int[][] grille, boolean[][] grilleWin,
                        int currentTurn, int mode, int playerTurn,
                        String status, int[] tabScore, int confiance,
                        int ligne, int col, int profondeur) {
        this.gameId      = gameId;
        this.grille      = grille;
        this.grilleWin   = grilleWin;
        this.currentTurn = currentTurn;
        this.mode        = mode;
        this.playerTurn  = playerTurn;
        this.status      = status;
        this.tabScore    = tabScore;
        this.confiance   = confiance;
        this.ligne       = ligne;
        this.col         = col;
        this.profondeur  = profondeur;
    }

    public GameStateDTO() {}

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public long       getGameId()      { return gameId; }
    public int[][]    getGrille()      { return grille; }
    public boolean[][] getGrilleWin()  { return grilleWin; }
    public int        getCurrentTurn() { return currentTurn; }
    public int        getMode()        { return mode; }
    public int        getPlayerTurn()  { return playerTurn; }
    public String     getStatus()      { return status; }
    public int[]      getTabScore()    { return tabScore; }
    public int        getConfiance()   { return confiance; }
    public int        getLigne()       { return ligne; }
    public int        getCol()         { return col; }
    public int        getProfondeur()  { return profondeur; }

    // -------------------------------------------------------------------------
    // Setters (requis par Jackson pour la sérialisation JSON)
    // -------------------------------------------------------------------------

    public void setGameId(long gameId)           { this.gameId = gameId; }
    public void setGrille(int[][] grille)         { this.grille = grille; }
    public void setGrilleWin(boolean[][] grilleWin) { this.grilleWin = grilleWin; }
    public void setCurrentTurn(int currentTurn)  { this.currentTurn = currentTurn; }
    public void setMode(int mode)                { this.mode = mode; }
    public void setPlayerTurn(int playerTurn)    { this.playerTurn = playerTurn; }
    public void setStatus(String status)         { this.status = status; }
    public void setTabScore(int[] tabScore)      { this.tabScore = tabScore; }
    public void setConfiance(int confiance)      { this.confiance = confiance; }
    public void setLigne(int ligne)              { this.ligne = ligne; }
    public void setCol(int col)                  { this.col = col; }
    public void setProfondeur(int profondeur)    { this.profondeur = profondeur; }
}
