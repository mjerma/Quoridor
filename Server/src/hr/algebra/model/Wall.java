package hr.algebra.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;

import static hr.algebra.constants.Values.*;

public class Wall extends Rectangle implements Serializable {

    private int xCoordinate;
    private int yCoordinate;
    private boolean isVertical;
    private Player placedBy;

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public boolean isVertical() { return isVertical; }

    public boolean isPlaced() {
        return placedBy != null;
    }

    public Player getPlayer() { return placedBy; }

    public Wall(int x, int y, boolean isVertical) {
        this.xCoordinate = x;
        this.yCoordinate = y;
        this.isVertical = isVertical;
        setupScene();
    }

    public Wall(int x, int y, boolean isVertical, Player placedBy) {
        this.xCoordinate = x;
        this.yCoordinate = y;
        this.isVertical = isVertical;
        this.placedBy = placedBy;
    }

    public void setupScene() {
        if (isVertical) {
            setWidth(TILE_SIZE / 12);
            setHeight(TILE_SIZE);
        }
        else {
            if (xCoordinate == WIDTH - 1 && yCoordinate == HEIGHT) {
                setWidth(TILE_SIZE + TILE_SIZE / 12);
            }
            else {
                setWidth(TILE_SIZE);
            }
            setHeight(TILE_SIZE / 12);
        }
        relocate((xCoordinate * TILE_SIZE), yCoordinate * TILE_SIZE);
        if (isPlaced()) {
            placeWall(placedBy);
        }
        else {
            setFill(Color.valueOf("#000"));
        };
    }

    public void placeWall(Player player) {
        placedBy = player;
        setFill(Color.valueOf("#fc9611"));
    }
}
