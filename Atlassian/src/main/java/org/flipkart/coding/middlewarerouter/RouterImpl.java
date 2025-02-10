package org.flipkart.coding.middlewarerouter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class RouterImpl implements Router {
    /**
     * (matches any single character)
     */
    public static final String WILDCARD_QUESTION_MARK = "?";

    /**
     * matches any sequence of characters
     */
    public static final String WILDCARD_STAR = "*";


    static class TrieNode {
        Map<String, TrieNode> children;
        String value;
        boolean endOfWord;

        public TrieNode() {
            children = new HashMap<>();
            value = "";
            endOfWord = false;
        }
    }

    static TrieNode root = new TrieNode();

    @Override
    public void add(String route, String data) {
        TrieNode pCrawl = root;
        String[] urlComponents = route.split("/");
        for (String component : urlComponents) {
            if (!pCrawl.children.containsKey(component)) {
                pCrawl.children.put(component, new TrieNode());
            }
            pCrawl = pCrawl.children.get(component);
        }
        pCrawl.endOfWord = true;
        pCrawl.value = data;
    }



    @Override
    public String get(String route) {
        TrieNode pCrawl = root;
        String[] urlComponents = route.split("/");
        Set<String> resultSet = wildcardMatches(route);
        return resultSet.toString();
    }


    public Set<String> wildcardMatches(String path) {
        String[] pathArr = path.split("/");
        Set<String> wildcardMatches = new HashSet<>();
        wildcardTraverse(pathArr, root, 0, wildcardMatches);
        return wildcardMatches;
    }

    /*
     * traverses the trie and adds all words matching the string with wildcards * to
     * list
     */
    private void wildcardTraverse(String[] pathArr, TrieNode root, int index,
                                  Set<String> wildcardMatches) {
        if (root == null) {
            return;
        }
        if (index == pathArr.length) {
            if (root.endOfWord) {
                wildcardMatches.add(root.value);
            }
            return;
        }
        if (WILDCARD_QUESTION_MARK.equals(pathArr[index])) {
            for (Map.Entry<String, TrieNode> e : root.children.entrySet()) {
                wildcardTraverse(pathArr, e.getValue(), index + 1, wildcardMatches);
            }
        } else if (WILDCARD_STAR.equals(pathArr[index])) {

            // This is the key statement!!
            // Check the root's children to see if it is the next path after *
            wildcardTraverse(pathArr, root, index + 1, wildcardMatches);

            // Goto to each children and check if their children is the next path after *
            for (Map.Entry<String, TrieNode> e : root.children.entrySet()) {
                wildcardTraverse(pathArr, e.getValue(), index, wildcardMatches);
            }
        } else {
            wildcardTraverse(pathArr, root.children.get(pathArr[index]), index + 1,
                    wildcardMatches);
        }
    }


    public static void main(String[] args) {
        Router rm = new RouterImpl();
        rm.add("jira.atlassian.com/testRoute/abc", "fooData1");
        rm.add("jira.atlassian.com/testRoute/xyz", "fooData2");
        rm.add("jira.atlassian.com/testRoute/", "fooData3");
        rm.add("jira.atlassian.com/testRoute/abc/ghf/xyz", "fooData4");

        String v = rm.get("jira.atlassian.com/testRoute/*");
//        String w = rm.get("jira.atlassian.com/testRoute");
//        String x = rm.get("jira.atlassian.com/testRoute/abc");
//        String y = rm.get("jira.atlassian.com/testRoute/*");
//        String z = rm.get("jira.atlassian.com/*/xyz");
        System.out.println(v);
//        System.out.println(w);
//        System.out.println(x);
//        System.out.println(y);
//        System.out.println(z);
    }
}