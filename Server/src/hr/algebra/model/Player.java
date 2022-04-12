package hr.algebra.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;

import static hr.algebra.constants.Values.*;

public class Player extends Circle implements Serializable {

    private PlayerColor color;
    private double mouseX;
    private double mouseY;
    private double currentX;
    private double currentY;
    private int wallCount = 10;

    public PlayerColor getColor() {
        return color;
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public int getWallCount() { return wallCount; }

    public void setWallCount(int wallCount) { this.wallCount = wallCount; }

    public Player(int x, int y, PlayerColor color) {
        this.color = color;
        setupScene(x,y,color);
    }

    public Player(double currentX, double currentY, PlayerColor color, int wallCount) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.color = color;
        this.wallCount = wallCount;
    }

    public void setupScene(int x, int y, PlayerColor color) {
        setRadius(TILE_SIZE * 0.25);
        setFill(color == PlayerColor.RED ? Color.valueOf("#FF0022") : Color.valueOf("#0022EE"));
        move(x,y);
        setTranslateX((TILE_SIZE - TILE_SIZE * 0.25 * 2) / 1.8);
        setTranslateY((TILE_SIZE - TILE_SIZE * 0.25 * 2) / 1.8);

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + currentX, e.getSceneY() - mouseY + currentY);
        });
    }

    public void move(int x, int y) {
        currentX = x * TILE_SIZE;
        currentY = y * TILE_SIZE;
        relocate(currentX, currentY);
    }

    public void undoMove() {
        relocate(currentX, currentY);
    }

    public void highlight() {
        setStrokeWidth(4);
        setStroke(Color.valueOf("#ffcc00"));
    }

    public void undoHighlight() {
        setStrokeWidth(0);
    }
}
