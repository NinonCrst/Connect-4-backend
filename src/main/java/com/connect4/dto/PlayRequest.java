package com.connect4.dto;

/**
 * Body de la requête POST /api/game/{id}/play
 *
 * Le frontend envoie uniquement la colonne choisie par le joueur humain.
 */
public class PlayRequest {

    private int column;

    public PlayRequest() {}

    public int getColumn()         { return column; }
    public void setColumn(int col) { this.column = col; }
}
