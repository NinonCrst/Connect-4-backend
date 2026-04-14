package com.connect4.service;

import com.connect4.game.*;
import com.connect4.dto.GameStateDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GameService — orchestre les parties Connect4.
 *
 * Remplace l'enchaînement Timer Swing de l'original :
 * chaque appel REST déclenche un ou plusieurs coups selon le mode,
 * et retourne l'état complet de la partie au frontend.
 *
 * Sessions stockées en mémoire (ConcurrentHashMap).
 * Pour une vraie persistance multi-utilisateurs, remplacer par un
 * repository Spring Data.
 */
@Service
public class GameService {

    // -------------------------------------------------------------------------
    // Gestion des sessions de jeu en mémoire
    // -------------------------------------------------------------------------

    private final Map<Long, ModelConnect4> sessions = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    // -------------------------------------------------------------------------
    // Constantes de mode  (identiques aux case du switch de startPlay)
    // -------------------------------------------------------------------------

    public static final int MODE_CVC       = 0;  // IA random vs IA random
    public static final int MODE_IAVIA     = 1;  // IA minimax vs IA minimax
    public static final int MODE_IAVIA_BDD = 2;  // IA minimax+BDD vs IA minimax+BDD
    public static final int MODE_CVP       = 3;  // IA random vs Joueur
    public static final int MODE_IAVP      = 4;  // IA minimax vs Joueur
    public static final int MODE_IAVP_BDD  = 5;  // IA minimax+BDD vs Joueur
    public static final int MODE_PVP       = 6;  // Joueur vs Joueur

    // -------------------------------------------------------------------------
    // Démarrer une nouvelle partie
    // -------------------------------------------------------------------------

    /**
     * Crée une session, initialise le modèle et joue le premier coup IA
     * si le mode l'exige (IA commence) ou si c'est un mode tout-IA.
     */
    public GameStateDTO startGame(int mode, int firstPlayer, int ligne, int col, int index, int profondeur) {
        ModelConnect4 partie = new ModelConnect4(firstPlayer, ligne, col, index);
        partie.setProfondeur(profondeur);
        partie.startPlay(mode);

        long gameId = idCounter.incrementAndGet();
        sessions.put(gameId, partie);

        // Pour les modes tout-IA, on joue le premier coup immédiatement
        // Pour les modes mixtes, si l'IA commence (playerTurn == 1), on joue aussi
        if (isFullIAMode(mode) || (isMixedMode(mode) && partie.getPlayerTurn() == 1)) {
            playIATurn(partie, mode);
        }

        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Jouer un coup humain
    // -------------------------------------------------------------------------

    /**
     * Le joueur humain choisit une colonne.
     * Après son coup, si c'est au tour de l'IA, on joue automatiquement.
     */
    public GameStateDTO playColumn(long gameId, int col) {
        ModelConnect4 partie = getPartie(gameId);
        int mode = partie.getCurrentModeG();

        // Coup du joueur humain
        int nextTurn = partie.playGameP(col, mode);

        // Si la partie continue et que c'est au tour de l'IA, on enchaîne
        if (partie.getInGame() && isMixedMode(mode)) {
            playIATurn(partie, mode);
        }

        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Jouer un coup IA (pour les modes tout-IA appelés depuis le frontend)
    // -------------------------------------------------------------------------

    /**
     * Utilisé par le frontend pour déclencher le prochain coup dans un mode
     * tout-IA (le frontend appelle cette route en boucle avec un délai).
     */
    public GameStateDTO playIAMove(long gameId) {
        ModelConnect4 partie = getPartie(gameId);
        int mode = partie.getCurrentModeG();

        if (partie.getInGame() && isFullIAMode(mode)) {
            playIATurn(partie, mode);
        }

        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Navigation (back / forward)
    // -------------------------------------------------------------------------

    public GameStateDTO backCoup(long gameId) {
        ModelConnect4 partie = getPartie(gameId);
        partie.backCoup();
        return buildState(gameId, partie);
    }

    public GameStateDTO forwardCoup(long gameId) {
        ModelConnect4 partie = getPartie(gameId);
        partie.forwardCoup();
        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Paramètres en cours de partie
    // -------------------------------------------------------------------------

    public GameStateDTO setProfondeur(long gameId, int profondeur) {
        ModelConnect4 partie = getPartie(gameId);
        if (!partie.getInGame()) partie.setProfondeur(profondeur);
        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Récupérer l'état courant
    // -------------------------------------------------------------------------

    public GameStateDTO getState(long gameId) {
        ModelConnect4 partie = getPartie(gameId);
        return buildState(gameId, partie);
    }

    // -------------------------------------------------------------------------
    // Supprimer une session
    // -------------------------------------------------------------------------

    public void deleteGame(long gameId) {
        sessions.remove(gameId);
    }

    // -------------------------------------------------------------------------
    // Méthodes privées
    // -------------------------------------------------------------------------

    /**
     * Joue un coup IA selon le mode de jeu.
     * Correspond aux anciens appels dans les Timer Swing de l'original.
     */
    private void playIATurn(ModelConnect4 partie, int mode) {
        int turn = partie.getCurrentTurn();

        switch (mode) {
            case MODE_CVC:
            case MODE_CVP:
                partie.playGameC(turn, mode);
                break;

            case MODE_IAVIA:
            case MODE_IAVP:
                partie.playGameIa(turn, mode);
                break;

            case MODE_IAVIA_BDD:
            case MODE_IAVP_BDD:
                partie.playGameIaBdd(turn, mode);
                break;

            default:
                break;
        }
    }

    /**
     * Construit le DTO renvoyé au frontend à partir de l'état courant du modèle.
     */
    private GameStateDTO buildState(long gameId, ModelConnect4 partie) {
        int winner = partie.victory();
        String status;

        if (!partie.getInGame() && winner == 1)  status = "WIN_P1";
        else if (!partie.getInGame() && winner == 2) status = "WIN_P2";
        else if (!partie.getInGame())                status = "DRAW";
        else                                         status = "ONGOING";

        return new GameStateDTO(
            gameId,
            partie.getGrille(),
            partie.getGrilleWin(),
            partie.getCurrentTurn(),
            partie.getCurrentModeG(),
            partie.getPlayerTurn(),
            status,
            partie.getTabScore(),
            partie.getConfiance(),
            partie.getLigne(),
            partie.getCol(),
            partie.getProfondeur()
        );
    }

    /** Retourne la partie ou lève une exception lisible si l'id est inconnu. */
    private ModelConnect4 getPartie(long gameId) {
        ModelConnect4 partie = sessions.get(gameId);
        if (partie == null)
            throw new IllegalArgumentException("Partie introuvable : id=" + gameId);
        return partie;
    }

    /** Modes où les deux joueurs sont des IA. */
    private boolean isFullIAMode(int mode) {
        return mode == MODE_CVC || mode == MODE_IAVIA || mode == MODE_IAVIA_BDD;
    }

    /** Modes où un humain joue contre une IA. */
    private boolean isMixedMode(int mode) {
        return mode == MODE_CVP || mode == MODE_IAVP || mode == MODE_IAVP_BDD;
    }

    public ModelConnect4 getPartieById(long gameId) {
        return getPartie(gameId);
    }
}
