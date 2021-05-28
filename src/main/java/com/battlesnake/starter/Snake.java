package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * This is a simple Battlesnake server written in Java.
 *
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
enum Direction{
    UP, RIGHT, DOWN, LEFT
}
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
                switch (uri) {
                    case "/":
                        snakeResponse = index();
                        break;
                    case "/start":
                        snakeResponse = start(parsedRequest);
                        break;
                    case "/move":
                        LOG.info("MOVE-START");
                        snakeResponse = move(parsedRequest);
                        LOG.info("MOVE-END");
                        break;
                    case "/end":
                        snakeResponse = end(parsedRequest);
                        break;
                    default:
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
                LOG.info("Error somethiethingiawe");
                e.printStackTrace();
            }

            /*
                Example how to retrieve data from the request payload:

                String gameId = moveRequest.get("game").get("id").asText();
                int height = moveRequest.get("board").get("height").asInt();

            */
            LOG.info("--------- Begin Parsing -------");

            Move move = parseMove(moveRequest);

            // LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));

            LOG.info("+++++++++++++++++++++++++++++++++++++");
            LOG.info("Turn: " + move.turn);
            LOG.info("+++++++++++++++++++++++++++++++++++++");

            String next_move = findNextMove(move);

            LOG.info("MOVE {}", next_move);

            Map<String, String> response = new HashMap<>();
            response.put("move", next_move);
            return response;
        }

        Move parseMove(JsonNode moveRequest) {

            Game game = parseGame(moveRequest.get("game"));
            LOG.info("id: {}, timeout: {}.", game.id, game.timeout);
            int turn = moveRequest.get("turn").asInt();
            Board board = parseBoard(moveRequest.get("board"));
            fillBoard(board);
            board.printBoard();
            BattleSnake you = parseSnake(moveRequest.get("you"));

            return new Move(game, turn, board, you);
        }

        private Game parseGame(JsonNode gameReq) {
            return new Game(gameReq.get("id").asText(),
                    gameReq.get("timeout").asInt());
        }

        private Board parseBoard(JsonNode boardReq) {

            Vector<Coordinates> food = parseCoordinatesVector(boardReq.get("food"));

            Vector<BattleSnake> snakes = parseSnakeVector(boardReq.get("snakes"));

            return new Board(boardReq.get("height").asInt(),
                    boardReq.get("width").asInt(),
                    food,
                    snakes);
        }

        private void fillBoard(Board board){
            BoardTile[][] boardTiles = new BoardTile[board.width][board.height];
            // boardTiles[0][0] = BoardTile.ME;

            for(int x=0;x<board.width;x++){
                Arrays.fill(boardTiles[x], BoardTile.EMPTY);
            }

            // Fill foods
            Iterator foodIt = board.food.iterator();
            while (foodIt.hasNext()){
                Coordinates co = (Coordinates) foodIt.next();
                boardTiles[co.x][co.y] = BoardTile.FOOD;
            }
            Iterator SnakeIt = board.snakes.iterator();
            boolean me = true;
            while (SnakeIt.hasNext()){
                BattleSnake snake = (BattleSnake) SnakeIt.next();
                Iterator bodyIt = snake.body.iterator();

                while (bodyIt.hasNext()) {
                    Coordinates co = (Coordinates) bodyIt.next();
                    if (me) {
                        boardTiles[co.x][co.y] = BoardTile.ME;
                    } else {
                        boardTiles[co.x][co.y] = BoardTile.ENEMY;
                    }
                }
                me = false;  //Only the first snake is yourself.
            }
            board.boardTiles = boardTiles;
        }

        private BattleSnake parseSnake(JsonNode snakeReq) {

            BattleSnake snake =  new BattleSnake(snakeReq.get("id").asText(),
                    snakeReq.get("name").asText(),
                    snakeReq.get("health").asInt(),
                    parseCoordinatesVector(snakeReq.get("body")),
                    snakeReq.get("latency").asText(),
                    parseCoordinates(snakeReq.get("head")),
                    snakeReq.get("length").asInt(),
                    snakeReq.get("shout").asText());

            return snake;
        }

        private Coordinates parseCoordinates(JsonNode coordinates)
        {

            return new Coordinates(coordinates.get("x").asInt(),
                    coordinates.get("y").asInt());
        }

        private Vector<Coordinates> parseCoordinatesVector(JsonNode foodReq){

            Vector<Coordinates> food = new Vector<>();

            for (int i = 0; i < foodReq.size(); i++){
                food.add(parseCoordinates(foodReq.get(i)));
            }
            return food;
        }

        private Vector<BattleSnake> parseSnakeVector(JsonNode snakeReq){

            Vector<BattleSnake> snakes = new Vector<>();

            for (int i = 0; i < snakeReq.size(); i++){
                snakes.add(parseSnake(snakeReq.get(i)));
            }
            return snakes;
        }

        /**
         * Check if the move is inside game board
         */
        private boolean legalMove(Direction dir, Coordinates head, Board board){
            System.out.println("checking dir: " + convertMove(dir));
            switch (dir){
                case UP: {
                    if (head.y >= board.height) return false;
                    else return true;
                }
                case DOWN: {
                    if (head.y <= 0) return false;
                    else return true;
                }
                case RIGHT: {
                    if (head.x >= board.width) return false;
                    else return true;
                }
                case LEFT: {
                    if (head.x >= 0) return false;
                    else return true;
                }
            }
            return false;
        }

        private String convertMove(Direction dir) {
            switch (dir) {
                case UP:
                    return "up";
                case RIGHT:
                    return "right";
                case DOWN:
                    return "down";
                case LEFT:
                    return "left";
            }
            return "up";
        }

        private String findNextMove(Move move) {
            return  convertMove(simpleMove(move));
        }


        /**
         *  Just move to any empty tile next to head.
         *  Priority: UP > Right > Down > Left
         */
        private Direction simpleMove(Move move) {
            Coordinates head = move.you.head;

            if (legalMove(Direction.UP, head, move.board)) return Direction.UP;
            else if (legalMove(Direction.RIGHT, head, move.board)) return Direction.RIGHT;
            else if (legalMove(Direction.DOWN, head, move.board)) return Direction.DOWN;
            else if (legalMove(Direction.LEFT, head, move.board)) return Direction.LEFT;
            else return Direction.UP;
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
