package org.flipkart;

import java.util.*;

public class Sudoku {

    // ---------------------- VALIDATOR ----------------------
    public static boolean validateSudoku(int[][] board) {
        Map<Integer, Set<Integer>> rowMap = new HashMap<>();
        Map<Integer, Set<Integer>> colMap = new HashMap<>();
        Map<Integer, Set<Integer>> boxMap = new HashMap<>();

        for (int i = 0; i < 9; i++) {
            rowMap.put(i, new HashSet<>());
            colMap.put(i, new HashSet<>());
            boxMap.put(i, new HashSet<>());
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int num = board[i][j];

                // Skip empty cells (for partial boards)
                if (num == 0) continue;

                // 1️⃣ Range Check
                if (num < 1 || num > 9) {
                    System.out.printf("❌ Invalid number %d at row=%d, col=%d%n", num, i + 1, j + 1);
                    return false;
                }

                // Compute box ID
                int boxId = (i / 3) * 3 + (j / 3);

                // 2️⃣ Duplicate checks
                if (rowMap.get(i).contains(num)) {
                    System.out.printf("❌ Duplicate %d found in row %d%n", num, i + 1);
                    return false;
                }
                if (colMap.get(j).contains(num)) {
                    System.out.printf("❌ Duplicate %d found in column %d%n", num, j + 1);
                    return false;
                }
                if (boxMap.get(boxId).contains(num)) {
                    System.out.printf("❌ Duplicate %d found in 3x3 box %d%n", num, boxId + 1);
                    return false;
                }

                // 3️⃣ Mark as seen
                rowMap.get(i).add(num);
                colMap.get(j).add(num);
                boxMap.get(boxId).add(num);
            }
        }

        System.out.println("✅ Sudoku board is valid!");
        return true;
    }

    // ---------------------- SOLVER ----------------------
    public static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) { // Empty cell
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(board))
                                return true;
                            board[row][col] = 0; // Backtrack
                        }
                    }
                    return false; // No valid number fits
                }
            }
        }
        return true; // Solved
    }

    private static boolean isValid(int[][] board, int row, int col, int num) {
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;

        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num) return false; // Row check
            if (board[i][col] == num) return false; // Col check
            if (board[boxRow + i / 3][boxCol + i % 3] == num) return false; // Box check
        }
        return true;
    }

    // ---------------------- UTILS ----------------------
    private static void printBoard(int[][] board) {
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0 && i != 0)
                System.out.println("---------------------");
            for (int j = 0; j < 9; j++) {
                if (j % 3 == 0 && j != 0) System.out.print("| ");
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    // ---------------------- TESTS ----------------------
    public static void main(String[] args) {

        int[][] validBoard = {
                {5,3,4,6,7,8,9,1,2},
                {6,7,2,1,9,5,3,4,8},
                {1,9,8,3,4,2,5,6,7},
                {8,5,9,7,6,1,4,2,3},
                {4,2,6,8,5,3,7,9,1},
                {7,1,3,9,2,4,8,5,6},
                {9,6,1,5,3,7,2,8,4},
                {2,8,7,4,1,9,6,3,5},
                {3,4,5,2,8,6,1,7,9}
        };

        int[][] invalidBoard = {
                {5,3,4,6,7,8,9,1,2},
                {6,7,2,1,9,5,3,4,8},
                {1,9,8,3,4,2,5,6,7},
                {8,5,9,7,6,1,4,2,3},
                {4,2,6,8,5,3,7,9,1},
                {7,1,3,9,2,4,8,5,6},
                {9,6,1,5,3,7,2,8,4},
                {2,8,7,4,1,9,6,3,5},
                {3,4,5,2,8,6,1,7,5} // duplicate '5' in last row
        };

        int[][] unsolved = {
                {5,3,0,0,7,0,0,0,0},
                {6,0,0,1,9,5,0,0,0},
                {0,9,8,0,0,0,0,6,0},
                {8,0,0,0,6,0,0,0,3},
                {4,0,0,8,0,3,0,0,1},
                {7,0,0,0,2,0,0,0,6},
                {0,6,0,0,0,0,2,8,0},
                {0,0,0,4,1,9,0,0,5},
                {0,0,0,0,8,0,0,7,9}
        };

        System.out.println("---- VALID BOARD ----");
        validateSudoku(validBoard);

        System.out.println("\n---- INVALID BOARD ----");
        validateSudoku(invalidBoard);

        System.out.println("\n---- UNSOLVED BOARD ----");
        printBoard(unsolved);

        System.out.println("\nSolving...");
        if (solveSudoku(unsolved)) {
            System.out.println("✅ Sudoku solved successfully!");
            printBoard(unsolved);
            System.out.println("\nRe-validating solved board...");
            validateSudoku(unsolved);
        } else {
            System.out.println("❌ No solution exists.");
        }
    }
}
