package com.connect4.service;

import com.connect4.dto.GameStateDTO;
import com.connect4.game.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SaveService — orchestre toutes les opérations de sauvegarde.
 *
 * Remplace ControllerSauvegarde (Swing) en déléguant à SauvegardeDatabase
 * et Sauvegarde exactement comme dans l'original, sans aucune logique UI.
 */
@Service
public class SaveService {

    private final SauvegardeDatabase saveData = new SauvegardeDatabase();

    // Sessions de jeu partagées avec GameService — injectées au démarrage
    // pour pouvoir charger une sauvegarde dans une partie existante.
    private final GameService gameService;

    public SaveService(GameService gameService) {
        this.gameService = gameService;
    }

    // -------------------------------------------------------------------------
    // Sauvegarder une partie en base
    // Correspond à ControllerSauvegarde.savePartie()
    // -------------------------------------------------------------------------

    public boolean savePartie(long gameId) {
        ModelConnect4 partie = gameService.getPartieById(gameId);
        Sauvegarde save = partie.setSave();
        boolean success = saveData.saveDatabase(save);

        if (success) {
            partie.setIndex(save.getIndexSave() + 1);
        }

        return success;
    }

    // -------------------------------------------------------------------------
    // Lister toutes les sauvegardes (vue normale)
    // Correspond à ControllerSauvegarde.saveOpenBdd()
    // -------------------------------------------------------------------------

    public List<SaveSummaryDTO> getAllSaves() {
        List<Sauvegarde> allSaves = saveData.openDatabase();
        return allSaves.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Lister toutes les sauvegardes (vue outils BDD avec canonique + symétrie)
    // Correspond à ControllerSauvegarde.saveOpenOutilsBdd()
    // -------------------------------------------------------------------------

    public List<SaveSummaryDTO> getAllSavesOutils() {
        List<Sauvegarde> allSaves = saveData.openDatabaseOutils();
        return allSaves.stream()
                .map(this::toSummaryOutils)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Charger une sauvegarde dans une partie existante
    // Correspond à ControllerSauvegarde.ouvrirSauvegarde()
    // -------------------------------------------------------------------------

    public GameStateDTO loadSave(long gameId, int saveIndex) {
        Sauvegarde save = saveData.openData(saveIndex);
        if (save == null)
            throw new IllegalArgumentException("Sauvegarde introuvable : index=" + saveIndex);

        ModelConnect4 partie = gameService.getPartieById(gameId);
        partie.openSave(save);

        return gameService.getState(gameId);
    }

    // -------------------------------------------------------------------------
    // Charger une sauvegarde outils (canonique + symétrie)
    // Correspond à ControllerSauvegarde.ouvrirSauvegardeOutils()
    // -------------------------------------------------------------------------

    public OutilsSaveDTO loadSaveOutils(int saveIndex) {
        List<Sauvegarde> saveCS = saveData.openDataOutilsBdd(saveIndex);
        if (saveCS == null || saveCS.size() < 2 || saveCS.get(0) == null || saveCS.get(1) == null)
            throw new IllegalArgumentException("Sauvegarde outils introuvable : index=" + saveIndex);

        return new OutilsSaveDTO(
                toSummaryOutils(saveCS.get(0)),
                toSummaryOutils(saveCS.get(1))
        );
    }

    // -------------------------------------------------------------------------
    // DTOs internes
    // -------------------------------------------------------------------------

    /**
     * Résumé d'une sauvegarde pour la liste (équivalent des DrawLine de VueData).
     */
    public static class SaveSummaryDTO {
        public int       index;
        public int       firstPlayer;
        public int       ligne;
        public int       col;
        public int       mode;
        public int       currentTurn;
        public boolean   statut;
        public int[][]   grille;
        public boolean[][] grilleWin;
        public List<CoupDTO> coups;
    }

    /**
     * Résumé étendu incluant la grille symétrique (pour VueOutilsBdd).
     */
    public static class OutilsSaveDTO {
        public SaveSummaryDTO canonique;
        public SaveSummaryDTO symetrie;

        public OutilsSaveDTO(SaveSummaryDTO canonique, SaveSummaryDTO symetrie) {
            this.canonique = canonique;
            this.symetrie  = symetrie;
        }
    }

    /**
     * Représentation JSON d'un Coup (pour éviter d'exposer la classe interne).
     */
    public static class CoupDTO {
        public int ligne;
        public int col;
        public int player;

        public CoupDTO(int ligne, int col, int player) {
            this.ligne  = ligne;
            this.col    = col;
            this.player = player;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de conversion  Sauvegarde → DTO
    // -------------------------------------------------------------------------

    private SaveSummaryDTO toSummary(Sauvegarde save) {
        SaveSummaryDTO dto = new SaveSummaryDTO();
        dto.index       = save.getIndexSave();
        dto.firstPlayer = save.getFirstPlayerSave();
        dto.ligne       = save.getLigneSave();
        dto.col         = save.getColSave();
        dto.mode        = save.getModeSave();
        dto.currentTurn = save.getCurrentTurnSave();
        dto.statut      = save.getStatutSave();
        dto.grille      = save.getGrilleSave();
        dto.grilleWin   = save.getGrilleWinSave();
        dto.coups       = save.getCoupsSave().stream()
                .map(c -> new CoupDTO(c.getLigneCoup(), c.getColCoup(), c.getPlayerCoup()))
                .collect(Collectors.toList());
        return dto;
    }

    private SaveSummaryDTO toSummaryOutils(Sauvegarde save) {
        SaveSummaryDTO dto = toSummary(save);
        // La grille symétrique est stockée dans getSym() pour les sauvegardes outils
        if (save.getSym() != null) dto.grille = save.getSym();
        return dto;
    }
}
