package com.connect4.controller;

import com.connect4.dto.GameStateDTO;
import com.connect4.dto.PlayRequest;
import com.connect4.dto.StartGameRequest;
import com.connect4.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GameController — expose les routes REST consommées par React.
 *
 * Remplace l'ensemble des Controller* Swing (ControllerStart,
 * ControllerGamePlay, ControllerModel, ControllerNavigation).
 *
 * Toutes les routes retournent un GameStateDTO qui contient
 * l'état complet de la partie : React n'a jamais besoin de
 * maintenir d'état local côté jeu.
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:3000")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // -------------------------------------------------------------------------
    // POST /api/game/start
    // Correspond au clic sur un des 7 boutons de mode dans le menu
    // (équivalent de ControllerStart + startPlay dans l'original)
    // -------------------------------------------------------------------------

    @PostMapping("/start")
    public ResponseEntity<GameStateDTO> startGame(@RequestBody StartGameRequest request) {
        GameStateDTO state = gameService.startGame(
            request.getMode(),
            request.getFirstPlayer(),
            request.getLigne(),
            request.getCol(),
            request.getIndex(),
            request.getProfondeur()
        );
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // GET /api/game/{id}
    // Récupère l'état courant d'une partie sans jouer de coup
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<GameStateDTO> getState(@PathVariable long id) {
        GameStateDTO state = gameService.getState(id);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // POST /api/game/{id}/play
    // Le joueur humain joue dans une colonne
    // (équivalent de ControllerModel.getColChoix + playGameP dans l'original)
    // Si c'est un mode mixte, le service enchaîne le coup IA automatiquement
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/play")
    public ResponseEntity<GameStateDTO> play(@PathVariable long id,
                                             @RequestBody PlayRequest request) {
        GameStateDTO state = gameService.playColumn(id, request.getColumn());
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // POST /api/game/{id}/ia-move
    // Déclenche le prochain coup IA dans les modes tout-IA (CvC, IAviIA...)
    // React appellera cette route en boucle avec un setTimeout pour simuler
    // le délai qu'avaient les Timer(500ms) Swing dans l'original
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/ia-move")
    public ResponseEntity<GameStateDTO> iaMove(@PathVariable long id) {
        GameStateDTO state = gameService.playIAMove(id);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // POST /api/game/{id}/back
    // Annule le dernier coup (équivalent de ControllerGamePlay.backGame)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/back")
    public ResponseEntity<GameStateDTO> back(@PathVariable long id) {
        GameStateDTO state = gameService.backCoup(id);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // POST /api/game/{id}/forward
    // Rejoue le dernier coup annulé (équivalent de ControllerGamePlay.frowardGame)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/forward")
    public ResponseEntity<GameStateDTO> forward(@PathVariable long id) {
        GameStateDTO state = gameService.forwardCoup(id);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // POST /api/game/{id}/profondeur
    // Modifie la profondeur Minimax (équivalent de ControllerGamePlay.modifProf)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/profondeur")
    public ResponseEntity<GameStateDTO> setProfondeur(@PathVariable long id,
                                                      @RequestParam int valeur) {
        GameStateDTO state = gameService.setProfondeur(id, valeur);
        return ResponseEntity.ok(state);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/game/{id}
    // Supprime la session en mémoire
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
