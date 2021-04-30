package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * This is a simple Battlesnake server written in Java.
 * 
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port != null) {
            LOG.info("Found system provided port: {}", port);
        } else {
            LOG.info("Using default port: {}", port);
            port = "8080";
        }
        port(Integer.parseInt(port));
        get("/",  HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }
                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));
                return snakeResponse;
            } catch (Exception e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

    
        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @return a response back to the engine containing the Battlesnake setup
         *         values.
         */
        public Map<String, String> index() {         
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", "simsog");        
            response.put("color", "#123456");     
            response.put("head", "default");  // TODO: Personalize
            response.put("tail", "default");  // TODO: Personalize
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        /**
         * This method is called on every turn of a game. It's how your snake decides
         * where to move.
         * 
         * Valid moves are "up", "down", "left", or "right".
         *
         * @param moveRequest a map containing the JSON sent to this snake. Use this
         *                    data to decide your next move.
         * @return a response back to the engine containing Battlesnake movement values.
         */
        public Map<String, String> move(JsonNode moveRequest) {
            try {
                LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            /*
                Example how to retrieve data from the request payload:

                String gameId = moveRequest.get("game").get("id").asText();
                int height = moveRequest.get("board").get("height").asInt();

            */
            Move move = parseMove(moveRequest);


            Board_2 board = new Board_2(moveRequest);

            String[] possibleMoves = { "up", "down", "left", "right" };

            // Choose a random direction to move in
            int choice = new Random().nextInt(possibleMoves.length);
            String next_move = possibleMoves[choice];

            LOG.info("MOVE {}", next_move);

            Map<String, String> response = new HashMap<>();
            response.put("move", next_move);
            return response;
        }

        Move parseMove(JsonNode moveRequest) {

            System.out.println("id: ");
            System.out.print(moveRequest.get("game").get("id"));
            Game game = parseGame(moveRequest.get("game"));
            int turn = moveRequest.get("turn").asInt();
            Board board = parseBoard(moveRequest.get("board"));
            BattleSnake you = parseSnake(moveRequest.get("you"));

            return new Move(game, turn, board, you);
        }

        private Game parseGame(JsonNode gameReq) {
            return new Game(gameReq.get("id").asText(),
                    gameReq.get("timeout").asInt());
        }


        private Board parseBoard(JsonNode boardReq) {

            Vector<Coordinates> food = new Vector<Coordinates>();
            food.add(new Coordinates (1,1));
            // TODO parse food

            Vector<Snake> snakes = new Vector<>();
            // TODO parse snakes

            return new Board(boardReq.get("height").asInt(),
                             boardReq.get("width").asInt(),
                             food,
                             snakes);
        }

        private BattleSnake parseSnake(JsonNode snakeReq) {

            return new BattleSnake(snakeReq.get("id").asText(),
                    snakeReq.get("name").asText(),
                    snakeReq.get("health").asInt(),
                    parseBody(snakeReq.get("body")),
                    snakeReq.get("latency").asText(),
                    parseCoordinates(snakeReq.get("head")),
                    snakeReq.get("length").asInt(),
                    snakeReq.get("shout").asText(),
                    snakeReq.get("squad").asText());
        }


        private Coordinates parseCoordinates(JsonNode coordinates)
        {
            return new Coordinates(coordinates.get("x").asInt(),
                                   coordinates.get("y").asInt());
        }

        private Vector<Coordinates> parseBody(JsonNode bodyReq) {

            Vector<Coordinates> body = new Vector<>();

            boolean empty = false;
            int i = 0;

            while (!empty);
            {
                if (bodyReq.get(i).isNull()) {
                    empty = true;
                } else {
                    body.addElement(parseCoordinates(bodyReq.get(i)));
                    System.out.println(body.get(i).x);
                    System.out.println(body.get(i).y);
                }
                i++;
            }
            return body;
        }

        /**
         * This method is called when a game your Battlesnake was in ends.
         * 
         * It is purely for informational purposes, you don't have to make any decisions
         * here.
         *
         * @param endRequest a map containing the JSON sent to this snake. Use this data
         *                   to know which game has ended
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {

            LOG.info("END");
            return EMPTY;
        }
    }

}
