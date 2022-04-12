package hr.algebra.model;

import java.io.Serializable;
import java.util.ArrayList;

public class BoardState implements Serializable {

    private Tile[][] tiles;
    private Wall[][] horizontalWalls;
    private Wall[][] verticalWalls;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private int timeElapsed;

    public Tile[][] getTiles() {
        return tiles;
    }

    public Wall[][] getHorizontalWalls() {
        return horizontalWalls;
    }

    public Wall[][] getVerticalWalls() {
        return verticalWalls;
    }

    public ArrayList<Player> getPlayers() { return players; }

    public Player getCurrentPlayer() { return currentPlayer; }

    public int getTimeElapsed() { return timeElapsed; }

    public BoardState(Tile[][] tiles, Wall[][] horizontalWalls, Wall[][] verticalWalls, ArrayList<Player> players, Player currentPlayer, int timeElapsed) {
        this.tiles = tiles;
        this.horizontalWalls = horizontalWalls;
        this.verticalWalls = verticalWalls;
        this.players = players;
        this.currentPlayer = currentPlayer;
        this.timeElapsed = timeElapsed;
    }
}
