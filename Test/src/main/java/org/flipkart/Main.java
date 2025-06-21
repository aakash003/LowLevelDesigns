package org.flipkart;


import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split("~");
            try {
                switch (inpArr[0]) {
                }
            } catch (RuntimeException runtimeException) {
                System.out.println(runtimeException);
            }
        }

    }


}