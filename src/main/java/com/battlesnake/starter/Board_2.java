package com.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;

public class Board_2 {
    public BoardState[][] board;

    Board_2(JsonNode moveRequest)
    {
        int width = 1;
        int height = 0;

        board = new BoardState[width][height];
        System.out.print("test");
    }


}
