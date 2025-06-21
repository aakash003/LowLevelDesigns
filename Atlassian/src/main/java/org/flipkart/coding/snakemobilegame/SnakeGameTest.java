package org.flipkart.coding.snakemobilegame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnakeGameTest {

    private SnakeGame game;

    @BeforeEach
    void setUp() {
        int width = 3;
        int height = 2;
        int[][] food = {{1, 2}, {0, 1}};
        game = new SnakeGame(width, height, food);
    }

    @Test
    void testInitialMove() {
        assertEquals(0, game.move("R"), "Initial move to the right should not score");
    }

    @Test
    void testMoveToFood() {
        game.move("R");
        game.move("D");
        assertEquals(1, game.move("R"), "Move to the food should score 1");
    }

    @Test
    void testMoveOutOfBounds() {
        game.move("R");
        game.move("R");
        assertEquals(-1, game.move("R"), "Move out of bounds should end the game");
    }

    @Test
    void testMoveIntoSelf() {
        game.move("R");
        game.move("D");
        game.move("R");
        game.move("U");
        game.move("L");
        assertEquals(-1, game.move("D"), "Move into itself should end the game");
    }

    @Test
    void testPrintSnake() {
        game.move("R");
        game.move("D");
        game.move("R");
        game.printSnake();
        // You can manually verify the printed output if needed
    }
}