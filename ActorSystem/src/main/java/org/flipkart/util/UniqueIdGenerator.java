package org.flipkart.util;

import java.util.UUID;

public class UniqueIdGenerator {

    private static Integer count = 1;
    public static String generateNewID(){
        return (count++).toString();
    }
}
