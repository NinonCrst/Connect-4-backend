package com.connect4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modèle du jeu Connect4.
 *
 * Changements par rapport à la version Swing :
 *  - Suppression de toutes les dépendances javax.swing (Timer, JOptionPane)
 *  - Suppression des paramètres VueBoard / VueWinner dans toutes les méthodes
 *  - Les méthodes playGame* jouent UN coup et retournent l'état ; c'est le
 *    GameService qui orchestre la boucle tour par tour via les appels REST.
 *  - tabScore est stocké dans le modèle et exposé via getTabScore()
 *  - Toute la logique métier (victory, addJeton, back/forward, IA...) est
 *    strictement identique à l'original.
 */
public class ModelConnect4 {

    // -------------------------------------------------------------------------
    // État de la partie
    // -------------------------------------------------------------------------
    private int[][] grille;
    private int ligne;
    private int col;
    private int player;          // joueur qui commence (1 ou 2)
    private int index;

    private boolean[][] grilleWin;

    private int currentTurn = -1;   // joueur dont c'est le tour (1 ou 2)
    private int currentModeP;       // mode courant mémorisé entre les tours
    private int currentModeG = -1;  // mode global de la partie

    private boolean pauseGame   = false;
    private boolean currentModeIA = false;

    private List<Coup> coups;
    private List<Coup> coupsAnnules;

    private boolean inGame  = false;
    private boolean statut  = false;
    private int playerTurn  = -1;   // 0 = humain commence, 1 = IA commence

    private int profondeur = 2;
    private int[] tabScore;
    private int confiance = 0;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    public ModelConnect4(int firstPlayer, int ligne, int col, int index) {
        this.player = firstPlayer;
        this.ligne  = ligne;
        this.col    = col;
        this.index  = index;

        this.grille    = new int[this.ligne][this.col];
        this.grilleWin = new boolean[this.ligne][this.col];
        this.coups         = new ArrayList<>();
        this.coupsAnnules  = new ArrayList<>();

        this.initBoard();
    }

    public ModelConnect4() {}

    // -------------------------------------------------------------------------
    // Sauvegarde / chargement  (inchangé)
    // -------------------------------------------------------------------------

    public void openSave(Sauvegarde save) {
        this.player       = save.getFirstPlayerSave();
        this.ligne        = save.getLigneSave();
        this.col          = save.getColSave();
        this.index        = save.getIndexSave();
        this.currentTurn  = save.getCurrentTurnSave();
        this.currentModeG = save.getModeSave();
        this.statut       = save.getStatutSave();
        this.grille       = copyGrille(save.getGrilleSave());
        this.grilleWin    = copyGrilleWin(save.getGrilleWinSave());
        this.coups        = copyCoups(save.getCoupsSave());
        this.coupsAnnules = copyCoups(save.getCoupsAnnulesSave());
    }

    public Sauvegarde setSave() {
        return new Sauvegarde(this);
    }

    // -------------------------------------------------------------------------
    // Initialisation  (inchangé)
    // -------------------------------------------------------------------------

    public void initBoard() {
        for (int i = 0; i < ligne; i++)
            for (int j = 0; j < col; j++) {
                this.grille[i][j]    = 0;
                this.grilleWin[i][j] = false;
            }
    }

    public void initWin() {
        for (int i = 0; i < ligne; i++)
            for (int j = 0; j < col; j++)
                this.grilleWin[i][j] = false;
    }

    // -------------------------------------------------------------------------
    // Copies utilitaires  (inchangé)
    // -------------------------------------------------------------------------

    public int[][] copyGrille(int[][] originalGrille) {
        int[][] copy = new int[ligne][col];
        for (int i = 0; i < ligne; i++)
            for (int j = 0; j < col; j++)
                copy[i][j] = originalGrille[i][j];
        return copy;
    }

    public boolean[][] copyGrilleWin(boolean[][] originalGrilleWin) {
        boolean[][] copy = new boolean[ligne][col];
        for (int i = 0; i < ligne; i++)
            for (int j = 0; j < col; j++)
                copy[i][j] = originalGrilleWin[i][j];
        return copy;
    }

    public List<Coup> copyCoups(List<Coup> originalCoups) {
        List<Coup> copy = new ArrayList<>();
        for (Coup co : originalCoups) copy.add(co);
        return copy;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int[][]     getGrille()        { return this.grille; }
    public boolean[][] getGrilleWin()     { return this.grilleWin; }
    public int         getLigne()         { return this.ligne; }
    public int         getCol()           { return this.col; }
    public int         getPlayer()        { return this.player; }
    public int         getCurrentTurn()   { return this.currentTurn; }
    public boolean     getCurrentModeIA() { return this.currentModeIA; }
    public int         getCurrentModeG()  { return this.currentModeG; }
    public boolean     getInGame()        { return this.inGame; }
    public int         getIndex()         { return this.index; }
    public int         getPlayerTurn()    { return this.playerTurn; }
    public List<Coup>  getCoups()         { return this.coups; }
    public int         getProfondeur()    { return this.profondeur; }
    public int[]       getTabScore()      { return this.tabScore; }
    public int         getConfiance()     { return this.confiance; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setIndex(int index)             { this.index = index; }
    public void setGrille(int[][] grille)       { this.grille = grille; }
    public void setProfondeur(int profondeur)   { this.profondeur = profondeur; }
    public void setPause()                      { this.pauseGame = true; }
    public void removePause()                   { this.pauseGame = false; }

    public void setChanges(int firstPlayer, int ligne, int col) {
        this.player    = firstPlayer;
        this.ligne     = ligne;
        this.col       = col;
        this.grille    = new int[ligne][col];
        this.grilleWin = new boolean[ligne][col];
        this.initBoard();
    }

    // -------------------------------------------------------------------------
    // Logique de victoire  ── identique à l'original ──
    // -------------------------------------------------------------------------

    public int victory() {
        int winner = 0;
        int count  = 1;

        // Horizontal
        for (int i = 0; i < ligne; i++) {
            for (int j = 0; j < (col - 1); j++) {
                if ((grille[i][j] == grille[i][j + 1]) && (grille[i][j] != 0)) {
                    winner = grille[i][j];
                    count++;
                    grilleWin[i][j] = true;
                    if (j == (col - 2)) grilleWin[i][j + 1] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[i][j] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        // Vertical
        winner = 0; count = 1;
        for (int j = 0; j < col; j++) {
            for (int i = 0; i < (ligne - 1); i++) {
                if ((grille[i][j] != 0) && (grille[i][j] == grille[i + 1][j])) {
                    winner = grille[i][j];
                    count++;
                    grilleWin[i][j] = true;
                    if (i == (ligne - 2)) grilleWin[i + 1][j] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[i][j] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        // Diagonale ↘ par colonne de départ
        winner = 0; count = 1;
        for (int j = 0; j < (col - 3); j++) {
            for (int l = 0, c = j; (l < (ligne - 1)) && (c < (col - 1)); l++, c++) {
                if ((grille[l][c] != 0) && (grille[l][c] == grille[l + 1][c + 1])) {
                    winner = grille[l][c];
                    count++;
                    grilleWin[l][c] = true;
                    if ((l == (ligne - 2)) || (c == (col - 2))) grilleWin[l + 1][c + 1] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[l][c] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        // Diagonale ↘ par ligne de départ
        winner = 0; count = 1;
        for (int i = 0; i < (ligne - 3); i++) {
            for (int l = i, c = 0; (l < (ligne - 1)) && (c < (col - 1)); l++, c++) {
                if ((grille[l][c] != 0) && (grille[l][c] == grille[l + 1][c + 1])) {
                    winner = grille[l][c];
                    count++;
                    grilleWin[l][c] = true;
                    if ((l == (ligne - 2)) || (c == (col - 2))) grilleWin[l + 1][c + 1] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[l][c] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        // Diagonale ↙ par colonne de départ
        winner = 0; count = 1;
        for (int j = (col - 1); j > 2; j--) {
            for (int l = 0, c = j; (l < (ligne - 1)) && (c > 0); l++, c--) {
                if ((grille[l][c] != 0) && (grille[l][c] == grille[l + 1][c - 1])) {
                    winner = grille[l][c];
                    count++;
                    grilleWin[l][c] = true;
                    if ((l == (ligne - 2)) || (c == 1)) grilleWin[l + 1][c - 1] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[l][c] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        // Diagonale ↙ par ligne de départ
        winner = 0; count = 1;
        for (int i = 0; i < (ligne - 3); i++) {
            for (int l = i, c = (col - 1); (l < (ligne - 1)) && (c > 0); l++, c--) {
                if ((grille[l][c] != 0) && (grille[l][c] == grille[l + 1][c - 1])) {
                    winner = grille[l][c];
                    count++;
                    grilleWin[l][c] = true;
                    if ((l == (ligne - 2)) || (c == 1)) grilleWin[l + 1][c - 1] = true;
                } else {
                    if (count < 4) { winner = 0; count = 1; this.initWin(); }
                    else           { grilleWin[l][c] = true; return winner; }
                }
            }
            if (count >= 4) return winner;
            winner = 0; count = 1; this.initWin();
        }

        return winner;
    }

    // -------------------------------------------------------------------------
    // Logique de jeu  ── identique à l'original ──
    // -------------------------------------------------------------------------

    public int addJeton(int col, int player) {
        for (int i = (ligne - 1); i >= 0; i--) {
            if (grille[i][col] == 0) {
                grille[i][col] = player;
                Coup move = new Coup(i, col, player);
                coups.add(move);
                coupsAnnules = new ArrayList<>();
                if (player == 1) return 2;
                else return 1;
            }
        }
        return -1;
    }

    public boolean matchNull() {
        for (int i = 0; i < ligne; i++)
            for (int j = 0; j < col; j++)
                if (grille[i][j] == 0) return false;
        return true;
    }

    public void backCoup() {
        if (!coups.isEmpty()) {
            Coup back = coups.removeLast();
            coupsAnnules.add(back);
            grille[back.getLigneCoup()][back.getColCoup()] = 0;
            this.victory();
            if (currentTurn == 1) currentTurn = 2;
            else currentTurn = 1;
        }
    }

    public void forwardCoup() {
        if (!coupsAnnules.isEmpty()) {
            Coup forward = coupsAnnules.removeLast();
            coups.add(forward);
            grille[forward.getLigneCoup()][forward.getColCoup()] = forward.getPlayerCoup();
            this.victory();
            if (forward.getPlayerCoup() == 1) currentTurn = 2;
            else currentTurn = 1;
        }
    }

    // -------------------------------------------------------------------------
    // Tours joueur / IA
    // Seul changement : VueBoard retiré des paramètres de tourIAAlgoMinMax
    // et tourIAAlgoMinMaxBdd. tabScore et confiance sont stockés dans le modèle.
    // -------------------------------------------------------------------------

    public int tourPlayer(int col) {
        return addJeton(col, currentTurn);
    }

    public int tourIARandom(int player) {
        List<Integer> choixIA = new ArrayList<>();
        for (int j = 0; j < col; j++) choixIA.add(j);
        Collections.shuffle(choixIA);

        for (int j = 0; j < col; j++) {
            int nextTurn = addJeton(choixIA.get(j), player);
            if (nextTurn != -1) return nextTurn;
        }
        return -1;
    }

    public int tourIAAlgoMinMax(int player) {
        AlgoMiniMax miniMax = new AlgoMiniMax();
        int colMiniMax = miniMax.calculCoup(grille, profondeur, ligne, col, player);
        int nextTurn   = addJeton(colMiniMax, player);
        tabScore       = miniMax.getScoreTab();
        confiance      = miniMax.convertConfiance(miniMax.calculConfiance(profondeur));
        return nextTurn;
    }

    public int tourIAAlgoMinMaxBdd(int player) {
        AlgoMiniMax miniMax     = new AlgoMiniMax();
        SauvegardeDatabase saveData = new SauvegardeDatabase();
        Coup coupBdd            = saveData.openCoup(coups);
        int colChoose;

        if (coupBdd != null) colChoose = coupBdd.getColCoup();
        else colChoose = miniMax.calculCoup(grille, profondeur, ligne, col, player);

        int nextTurn = addJeton(colChoose, player);
        tabScore     = miniMax.getScoreTab();
        return nextTurn;
    }

    // -------------------------------------------------------------------------
    // Méthodes playGame*
    // Changement : les Timers Swing sont supprimés. Chaque méthode joue
    // exactement UN coup et retourne le prochain joueur (ou 0 = match nul,
    // ou winner si la partie est terminée). C'est le GameService qui appellera
    // ces méthodes au bon moment selon les requêtes REST.
    // -------------------------------------------------------------------------

    public int playGameC(int turn, int currentMode) {
        int winner = victory();
        if (winner != 0) { inGame = false; currentModeG = -1; return winner; }
        if (matchNull())  { inGame = false; currentModeG = -1; return 0; }

        int nextTurn = tourIARandom(turn);
        currentTurn  = nextTurn;
        currentModeP = currentMode;
        return nextTurn;
    }

    public int playGameIa(int turn, int currentMode) {
        int winner = victory();
        if (winner != 0) { inGame = false; currentModeG = -1; return winner; }
        if (matchNull())  { inGame = false; currentModeG = -1; return 0; }

        int nextTurn = tourIAAlgoMinMax(turn);
        currentTurn  = nextTurn;
        currentModeP = currentMode;
        return nextTurn;
    }

    public int playGameIaBdd(int turn, int currentMode) {
        int winner = victory();
        if (winner != 0) { inGame = false; currentModeG = -1; return winner; }
        if (matchNull())  { inGame = false; currentModeG = -1; return 0; }

        int nextTurn = tourIAAlgoMinMaxBdd(turn);
        currentTurn  = nextTurn;
        currentModeP = currentMode;
        return nextTurn;
    }

    public int playGameP(int col, int currentMode) {
        int winner = victory();
        if (winner != 0) { inGame = false; currentModeG = -1; return winner; }
        if (matchNull())  { inGame = false; currentModeG = -1; return 0; }

        int nextTurn = addJeton(col, currentTurn);
        if (nextTurn != -1) currentTurn = nextTurn;
        currentModeP = currentMode;
        return nextTurn;
    }

    // -------------------------------------------------------------------------
    // Démarrage d'une partie
    // Changement : VueBoard et VueWinner retirés des paramètres.
    // La logique de qui commence (playerTurn, currentModeIA) est inchangée.
    // -------------------------------------------------------------------------

    public void playCvc() {
        currentModeIA = true;
        currentTurn   = player;
    }

    public void playIavia() {
        currentModeIA = true;
        currentTurn   = player;
    }

    public void playIaviaBdd() {
        currentModeIA = true;
        currentTurn   = player;
    }

    public void playCvp() {
        currentTurn = player;
        int start   = (int) (Math.random() * 2);
        playerTurn  = start;
    }

    public void playIavp() {
        currentTurn = player;
        int start   = (int) (Math.random() * 2);
        playerTurn  = start;
    }

    public void playIavpBdd() {
        currentTurn = player;
        int start   = (int) (Math.random() * 2);
        playerTurn  = start;
    }

    public void playPvp() {
        currentTurn = player;
    }

    public void startPlay(int mode) {
        currentModeG  = mode;
        currentModeIA = false;
        inGame        = true;
        pauseGame     = false;

        this.initBoard();
        coups        = new ArrayList<>();
        coupsAnnules = new ArrayList<>();

        switch (mode) {
            case 0: this.playCvc();      break;
            case 1: this.playIavia();    break;
            case 2: this.playIaviaBdd(); break;
            case 3: this.playCvp();      break;
            case 4: this.playIavp();     break;
            case 5: this.playIavpBdd();  break;
            case 6: this.playPvp();      break;
        }
    }
}
