package com.battlesnake.starter;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum BoardTile
{
    EMPTY, FOOD, ME, HEAD, ENEMY
}

public class Board {
    int height;
    int width;
    Vector<Coordinates> food;
    Vector<BattleSnake> snakes;
    BoardTile[][] boardTiles;

    public Board(int height, int width, Vector<Coordinates> food, Vector<BattleSnake> snakes) {
        this.height = height;
        this.width = width;
        this.food = food;
        this.snakes = snakes;
    }

    public static String printTile(BoardTile tile){
         switch(tile){
             case FOOD: return "*";
             case ME: return  "M";
             case HEAD: return "H";
             case ENEMY: return "E";
             case EMPTY: return " ";
         }
        return " ";
    }

    public void printBoard() {
        for (int h = 0; h < this.height; h++){
            String line = "";

            for (int w = 0; w < this.width; w++){

                line += "[" + Board.printTile(this.boardTiles[w][h]) + "]";
            }
            System.out.println(line);
        }
    }
}
