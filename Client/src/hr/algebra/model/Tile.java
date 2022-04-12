package hr.algebra.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;

import static hr.algebra.controller.GameScreenController.TILE_SIZE;

public class Tile extends Rectangle implements Serializable {

    private int xCoordinate;
    private int yCoordinate;
    private Player player;

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Tile(int x, int y) {
        this.xCoordinate = x;
        this.yCoordinate = y;
        setupScene();
    }

    public Tile(int x, int y, Player player) {
        this.xCoordinate = x;
        this.yCoordinate = y;
        this.player = player;
    }

    public void setupScene() {
        setWidth(TILE_SIZE);
        setHeight(TILE_SIZE);
        relocate(xCoordinate * TILE_SIZE, yCoordinate * TILE_SIZE);
        setFill(Color.valueOf("#fff"));
    }
}
