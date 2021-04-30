package com.battlesnake.starter;

public class Move {
    Game game;
    int turn;
    Board board;
    BattleSnake you;

    public Move(Game game, int turn, Board board, BattleSnake you) {
        this.game = game;
        this.turn = turn;
        this.board = board;
        this.you = you;
    }
}
