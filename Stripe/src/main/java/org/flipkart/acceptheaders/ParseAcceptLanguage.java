package org.flipkart.acceptheaders;

import java.util.*;
import java.util.stream.Collectors;

public class ParseAcceptLanguage {

    public static List<String> parseAcceptLanguage(String header, Set<String> supportedLanguages) {
        // Split the header into individual language tags and strip whitespace
        String[] requestedLanguages = header.split(",");

        List<String> matchedLanguages = new ArrayList<>();
        for (String lang : requestedLanguages) {
            String trimmedLang = lang.trim();
            if (supportedLanguages.contains(trimmedLang)) {
                matchedLanguages.add(trimmedLang);
            }
        }

        return matchedLanguages;
    }

    // Test cases
    public static void runTests() {
        assert parseAcceptLanguage("en-US, fr-CA, fr-FR", Set.of("fr-FR", "en-US")).equals(List.of("en-US", "fr-FR"));
        assert parseAcceptLanguage("fr-CA, fr-FR", Set.of("en-US", "fr-FR")).equals(List.of("fr-FR"));
        assert parseAcceptLanguage("en-US", Set.of("en-US", "fr-CA")).equals(List.of("en-US"));
        assert parseAcceptLanguage("de-DE, es-ES", Set.of("en-US", "fr-FR")).isEmpty(); // No matches
        assert parseAcceptLanguage(" fr-CA , fr-FR ", Set.of("fr-FR")).equals(List.of("fr-FR")); // Extra spaces handled

        System.out.println("All tests passed!");
    }

    public static void main(String[] args) {
        runTests();
    }
}
