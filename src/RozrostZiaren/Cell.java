package RozrostZiaren;

import javafx.scene.paint.Color;

class Cell {
    private boolean alive;
    private Color color;

    Cell() {
        this.alive = false;
        this.color = Color.WHITE;
    }

    boolean isAlive() {
        return alive;
    }

    void setAlive() {
        this.alive = true;
    }

    Color getColor() {
        return color;
    }

    void setColor(Color color) {
        this.color = color;
    }
}

