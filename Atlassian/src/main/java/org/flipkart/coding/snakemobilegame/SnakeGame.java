package org.flipkart.coding.snakemobilegame;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class SnakeGame {
    private int m;
    private int n;
    private int[][] food;
    private int score;
    private int idx;
    private Deque<Integer> q = new ArrayDeque<>();
    private Set<Integer> vis = new HashSet<>();

    public SnakeGame(int width, int height, int[][] food) {
        m = height;
        n = width;
        this.food = food;
        q.offer(0);
        vis.add(0);
    }

    public int move(String direction) {
        int p = q.peekFirst();
        int i = p / n, j = p % n;
        int x = i, y = j;
        if ("U".equals(direction)) {
            --x;
        } else if ("D".equals(direction)) {
            ++x;
        } else if ("L".equals(direction)) {
            --y;
        } else {
            ++y;
        }
        if (x < 0 || x >= m || y < 0 || y >= n) {
            return -1;
        }

        int cur = f(x, y);
        if (vis.contains(cur) && cur != q.peekLast()) {
            return -1;
        }

        if (idx < food.length && x == food[idx][0] && y == food[idx][1]) {
            ++score;
            ++idx;
        } else {
            int t = q.pollLast();
            vis.remove(t);
        }

        q.offerFirst(cur);
        vis.add(cur);
        return score;
    }

    private int f(int i, int j) {
        return i * n + j;
    }


    public void printSnake() {
        char[][] grid = new char[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = '.';
            }
        }
        for (int[] f : food) {
            if (f[0] < m && f[1] < n) {
                grid[f[0]][f[1]] = 'F';
            }
        }
        for (int pos : q) {
            int i = pos / n;
            int j = pos % n;
            grid[i][j] = 'S';
        }
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        int width = 3;
        int height = 2;
        int[][] food = {{1, 2}, {0, 1}};

        SnakeGame game = new SnakeGame(width, height, food);

        System.out.println(game.move("R")); // Output: 0
        game.printSnake();
        System.out.println(game.move("D")); // Output: 0
        game.printSnake();
        System.out.println(game.move("R")); // Output: 1
        game.printSnake();
        System.out.println(game.move("U")); // Output: 1
        game.printSnake();
        System.out.println(game.move("L")); // Output: 2
        game.printSnake();
        System.out.println(game.move("U")); // Output: -1 (Game over)
    }

}