package org.flipkart;


import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.flipkart.TopKHotelsFinder.awardTopKHotels;

public class Main {


    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
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

        String positive = "breakfast beach city center location metro view staff price";
        String negative = "not";
        List<Integer> hotelIds = Arrays.asList(1, 2, 1, 1, 2);
        List<String> reviews = Arrays.asList(
                "This hotel has a nice view of the city center. The location is perfect.",
                "The breakfast is ok. Regarding location, it is quite far from city center but the price is cheap so it is worth.",
                "Location is excellent, 5 minutes from the city center. There is also a metro station very close to the hotel.",
                "They said I couldn’t take my dog and there were other guests with dogs! That is not fair.",
                "Very friendly staff and a good cost-benefit ratio. Its location is a bit far from the city center."
        );
        int k = 2;

        System.out.println(awardTopKHotels(positive, negative, hotelIds, reviews, k)); // [2, 1]
    }

}
