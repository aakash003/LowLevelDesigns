package org.flipkart;

import org.flipkart.client.Logger;
import org.flipkart.enums.LoggerType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.flipkart.constants.LoggerConstants.*;

public class Main {


    public static void main(String[] args) {
        /* Client adding logging configuration as per conveience*/

        int bufferSize = 15;

        //initializing with a configuration eg:logger name, sink(s), buffer size.
        Logger logger = new Logger();
        logger.setBufferSize(bufferSize);


        /**
         * Console:Fatal Error warn debug
         * File:Error
         * Database:Fatal
         */

        List<String> fatalSinkType = new ArrayList<>();
        fatalSinkType.add(DATABASE);
        fatalSinkType.add(CONSOLE);
        logger.addLoggerConfig("FATAL", LoggerType.SYNC, fatalSinkType);


        List<String> errorSinkType = new ArrayList<>();
        errorSinkType.add(FILE);
        errorSinkType.add(CONSOLE);
        logger.addLoggerConfig("ERROR", LoggerType.ASYNC, errorSinkType);

        List<String> warnSinkType = new ArrayList<>();
        warnSinkType.add(CONSOLE);
        logger.addLoggerConfig("WARN", LoggerType.SYNC, warnSinkType);

        List<String> infoSinkType = new ArrayList<>();
        infoSinkType.add(CONSOLE);
        logger.addLoggerConfig("INFO", LoggerType.ASYNC, infoSinkType);

        List<String> debugSinkType = new ArrayList<>();
        debugSinkType.add(CONSOLE);
        logger.addLoggerConfig("DEBUG", LoggerType.ASYNC, debugSinkType);



        // Setting global Logger Level
        logger.addGlobalLogLevel("INFO");


        //enriches message with current timestamp while directing message to a sink
        logger.log("I'm fatal and I Know it!", "FATAL", LocalDateTime.now());
        logger.log("I'm Error and I Know it!", "ERROR", LocalDateTime.now());
        logger.log("I'm Warn and I Know it!", "WARN", LocalDateTime.now());
        logger.log("I'm debug and I Know it!", "DEBUG", LocalDateTime.now());
        logger.log("I'm Info and I Know it!", "INFO", LocalDateTime.now());
    }
}