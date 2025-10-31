package org.flipkart;


import java.util.*;

class FunctionLibrary {

    class Function {
        String name;
        List<String> argumentTypes;
        boolean isVariadic;

        Function(String name, List<String> argumentTypes, boolean isVariadic) {
            this.name = name;
            this.argumentTypes = argumentTypes;
            this.isVariadic = isVariadic;
        }
    }

    Map<String, List<Function>> nonVariadic = new HashMap<>();
    Map<String, List<Function>> variadic = new HashMap<>();

    public void register(Set<Function> functionSet) {
        for (Function f : functionSet) {
            String key = appendArgs(f.argumentTypes, f.argumentTypes.size());
            if (f.isVariadic) {
                variadic.putIfAbsent(key, new LinkedList<Function>());
                variadic.get(key).add(f);
            } else {
                nonVariadic.putIfAbsent(key, new LinkedList<Function>());
                nonVariadic.get(key).add(f);
            }
        }
    }

    public List<Function> findMatches(List<String> argumentTypes) {
        List<Function> matches = new ArrayList<>();
        String key = appendArgs(argumentTypes, argumentTypes.size());

        if (nonVariadic.containsKey(key)) {
            matches.addAll(new LinkedList<Function>(nonVariadic.get(key)));
        }
        if (variadic.containsKey(key)) {
            matches.addAll(new LinkedList<Function>(variadic.get(key)));
        }

        int count = argumentTypes.size();
        for (int i = argumentTypes.size() - 2; i >= 0; --i) {
            if (argumentTypes.get(i).equals(argumentTypes.get(i + 1))) {
                --count;
            } else {
                break;
            }
            key = appendArgs(argumentTypes, count);
            if (variadic.containsKey(key)) {
                matches.addAll(new LinkedList<Function>(variadic.get(key)));
            }
        }

        return matches;
    }

    public String appendArgs(List<String> argumentTypes, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            String arg = argumentTypes.get(i);
            sb.append(arg);
            sb.append("+");
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        FunctionLibrary library = new FunctionLibrary();

        // Register functions
        Set<FunctionLibrary.Function> functions = new HashSet<>();
        functions.add(library.new Function("FuncA", List.of("String", "Integer", "Integer"), false));
        functions.add(library.new Function("FuncB", List.of("String", "Integer"), true));
        functions.add(library.new Function("FuncC", List.of("Integer"), true));
        functions.add(library.new Function("FuncD", List.of("Integer", "Integer"), true));
        functions.add(library.new Function("FuncE", List.of("Integer", "Integer", "Integer"), false));
        functions.add(library.new Function("FuncF", List.of("String"), false));
        functions.add(library.new Function("FuncG", List.of("Integer"), false));

        library.register(functions);

        // Perform findMatches queries
        System.out.println("findMatches({String}) -> " + library.findMatches(List.of("String")));
        System.out.println("findMatches({Integer}) -> " + library.findMatches(List.of("Integer")));
        System.out.println("findMatches({Integer, Integer, Integer, Integer}) -> " + library.findMatches(List.of("Integer", "Integer", "Integer", "Integer")));
        System.out.println("findMatches({Integer, Integer, Integer}) -> " + library.findMatches(List.of("Integer", "Integer", "Integer")));
        System.out.println("findMatches({String, Integer, Integer, Integer}) -> " + library.findMatches(List.of("String", "Integer", "Integer", "Integer")));
        System.out.println("findMatches({String, Integer, Integer}) -> " + library.findMatches(List.of("String", "Integer", "Integer")));
    }
}