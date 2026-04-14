package com.connect4.controller;

import com.connect4.dto.GameStateDTO;
import com.connect4.service.SaveService;
import com.connect4.service.SaveService.OutilsSaveDTO;
import com.connect4.service.SaveService.SaveSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SaveController — expose les routes REST de sauvegarde.
 *
 * Remplace ControllerSauvegarde (Swing) sans aucune logique UI.
 * Correspond aux boutons Save / Open ex / Open in / Outils BDD
 * de l'ancienne VueBoard.
 */
@RestController
@RequestMapping("/api/save")
public class SaveController {

    private final SaveService saveService;

    public SaveController(SaveService saveService) {
        this.saveService = saveService;
    }

    // -------------------------------------------------------------------------
    // POST /api/save/{gameId}
    // Sauvegarde la partie courante en base de données
    // Correspond au bouton "Save" → ControllerSauvegarde.savePartie()
    // -------------------------------------------------------------------------

    @PostMapping("/{gameId}")
    public ResponseEntity<Boolean> savePartie(@PathVariable long gameId) {
        boolean success = saveService.savePartie(gameId);
        return ResponseEntity.ok(success);
    }

    // -------------------------------------------------------------------------
    // GET /api/save
    // Liste toutes les sauvegardes (vue normale)
    // Correspond au bouton "Open in" → ControllerSauvegarde.saveOpenBdd()
    // -------------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<SaveSummaryDTO>> getAllSaves() {
        return ResponseEntity.ok(saveService.getAllSaves());
    }

    // -------------------------------------------------------------------------
    // GET /api/save/outils
    // Liste toutes les sauvegardes avec canonique + symétrie
    // Correspond au bouton "Outils BDD" → ControllerSauvegarde.saveOpenOutilsBdd()
    // -------------------------------------------------------------------------

    @GetMapping("/outils")
    public ResponseEntity<List<SaveSummaryDTO>> getAllSavesOutils() {
        return ResponseEntity.ok(saveService.getAllSavesOutils());
    }

    // -------------------------------------------------------------------------
    // POST /api/save/{gameId}/load/{saveIndex}
    // Charge une sauvegarde dans une partie existante
    // Correspond au bouton "Ouvrir" → ControllerSauvegarde.ouvrirSauvegarde()
    // -------------------------------------------------------------------------

    @PostMapping("/{gameId}/load/{saveIndex}")
    public ResponseEntity<GameStateDTO> loadSave(@PathVariable long gameId,
                                                  @PathVariable int saveIndex) {
        GameStateDTO state = saveService.loadSave(gameId, saveIndex);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // GET /api/save/outils/{saveIndex}
    // Charge une sauvegarde outils (canonique + symétrie)
    // Correspond au bouton "Ouvrir" dans VueOutilsBdd
    // → ControllerSauvegarde.ouvrirSauvegardeOutils()
    // -------------------------------------------------------------------------

    @GetMapping("/outils/{saveIndex}")
    public ResponseEntity<OutilsSaveDTO> loadSaveOutils(@PathVariable int saveIndex) {
        OutilsSaveDTO dto = saveService.loadSaveOutils(saveIndex);
        return ResponseEntity.ok(dto);
    }
}
