package org.flipkart;


import org.flipkart.coding.ratelimiter.RateLimiter;
import org.flipkart.coding.snakemobilegame.SnakeGame;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            String inp = scanner.nextLine();
//            inp = inp.trim();
//            String[] inpArr = inp.split("~");
//            try {
//                switch (inpArr[0]) {
//                }
//            } catch (RuntimeException runtimeException) {
//                System.out.println(runtimeException);
//            }
//        }

        RateLimiter rateLimiter = new RateLimiter(5, 2);
        System.out.println(rateLimiter.rateLimit(1));
        Thread.sleep(2000);
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        Thread.sleep(2000);
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));
        System.out.println(rateLimiter.rateLimit(1));

        //generate 2d array [[1, 2], [0, 1]]
        int[][] food = {
                {1, 2},
                {0, 1}
        };
        SnakeGame snakeGame = new SnakeGame(3, 2, food);
        snakeGame.move("R"); // return 0
        snakeGame.move("D"); // return 0
        snakeGame.move("R"); // return 1, snake eats the first piece of food. The second piece of food appears at (0, 1).
        snakeGame.move("U"); // return 1
        snakeGame.move("L"); // return 2, snake eats the second food. No more food appears.
        snakeGame.move("U"); // return -1, game over because snake collides with border
    }


}