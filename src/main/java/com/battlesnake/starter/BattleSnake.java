package com.battlesnake.starter;

import java.util.Vector;

public class BattleSnake {
    String id;
    String name;
    int health;
    Vector<Coordinates> body;
    String latency;
    Coordinates head;
    int length;
    String shout;
    String squad;

    public BattleSnake(String id, String name, int health, Vector<Coordinates> body, String latency, Coordinates head, int length, String shout, String squad) {
        this.id = id;
        this.name = name;
        this.health = health;
        this.body = body;
        this.latency = latency;
        this.head = head;
        this.length = length;
        this.shout = shout;
        this.squad = squad;
    }
}
