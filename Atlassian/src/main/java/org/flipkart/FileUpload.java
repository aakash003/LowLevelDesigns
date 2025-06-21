package org.flipkart;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileUpload {

    public static void main(String[] args) {
        String inputFilePath = "/Users/aakash.kumar1/Downloads/test.csv";
        String outputFilePath = "/Users/aakash.kumar1/Downloads/output.csv";
        String prefix = "LST";
        Map<String, String> map = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                if (arr.length < 2) {
                    continue;
                }
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].startsWith(prefix)) {
                        map.put(arr[i], arr[0]);

//                        writer.write(arr[i]);
//                        writer.newLine();
                    }
                }
            }

            System.out.println("CSV file has been written to " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String inputFilePath2 = "/Users/aakash.kumar1/Downloads/BRAND_OR_VERTICAL_REGULATION_CHANGE.csv";
        String outputFilePath2 = "/Users/aakash.kumar1/Downloads/output2.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath2));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath2))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                arr[5] = arr[5].replace("\"", "");
                if (map.containsKey(arr[5])) {
                    System.out.println(map.get(arr[5]));
                    writer.write(arr[5] + " " + map.get(arr[5]));
                    writer.newLine();
                }
            }

            System.out.println("CSV file has been written to " + outputFilePath2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
