package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnakeTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private Snake.Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Snake.Handler();
    }

//    @Test
//    void indexTest() throws IOException {
//
//        Map<String, String> response = handler.index();
//        assertEquals("#123456", response.get("color"));
//        assertEquals("default", response.get("headType"));
//        assertEquals("default", response.get("tailType"));
//    }

    @Test
    void startTest() throws IOException {
        JsonNode startRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(startRequest);
        assertEquals(0, response.size());
    }

    @Test
    void boardTest() throws IOException {
        File jsonFile = new File("D:\\Jobb\\battlesnakejava\\src\\test\\java\\com\\battlesnake\\starter\\data.json");
        JsonNode moveRequest = OBJECT_MAPPER.readTree(jsonFile);
        Board board = handler.parseBoard(moveRequest.get("board"));
        handler.fillBoard(board);
        board.printBoard();
        assertTrue(true);
    }
    @Test
    void moveTest() throws IOException {
        File jsonFile = new File("D:\\Jobb\\battlesnakejava\\src\\test\\java\\com\\battlesnake\\starter\\movetest.json");
        JsonNode moveRequest = OBJECT_MAPPER.readTree(jsonFile);

        Map<String, String> response = handler.move(moveRequest);

        List<String> options = new ArrayList<String>();
        options.add("up");
        options.add("down");
        options.add("left");
        options.add("right");

        assertTrue(options.contains(response.get("move")));
    }

    @Test
    void endTest() throws IOException {
        JsonNode endRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(endRequest);
        assertEquals(0, response.size());
    }
}
