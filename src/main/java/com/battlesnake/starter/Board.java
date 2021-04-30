package com.battlesnake.starter;

import java.util.Vector;

public class Board {
    int height;
    int width;
    Vector<Coordinates> food;
    Vector<Snake> snakes;

    public Board(int height, int width, Vector<Coordinates> food, Vector<Snake> snakes) {
        this.height = height;
        this.width = width;
        this.food = food;
        this.snakes = snakes;
    }
}
