package org.flipkart.coding.middlewarerouter;

import java.util.HashMap;
import java.util.Map;

class RouterImpl2 implements Router {
    private final TrieNode root = new TrieNode();

    @Override
    public void add(String route, String data) {
        String[] parts = route.split("/");
        TrieNode current = root;

        for (String part : parts) {
            current = current.getChildren().computeIfAbsent(part, k -> new TrieNode());
        }
        current.setData(data);
    }

    @Override
    public String get(String route) {
        String[] parts = route.split("/");
        return search(root, parts, 0);
    }

    private String search(TrieNode node, String[] parts, int index) {
        if (index == parts.length) {
            return node.getData() != null ? node.getData() : "notFound!";
        }

        String part = parts[index];

        // Check for exact match
        if (node.getChildren().containsKey(part)) {
            String result = search(node.getChildren().get(part), parts, index + 1);
            if (!result.equals("notFound!")) {
                return result;
            }
        }

        // Check for wildcard match
        if (node.getChildren().containsKey("*")) {
            // Try to match the rest of the parts with the wildcard
            String result = search(node.getChildren().get("*"), parts, index + 1);
            if (!result.equals("notFound!")) {
                return result;
            }

            // If the wildcard doesn't match the rest, try to skip the current part
            // and continue matching with the wildcard
            result = search(node.getChildren().get("*"), parts, index);
            if (!result.equals("notFound!")) {
                return result;
            }
        }

        return "notFound!";
    }

    private static class TrieNode {
        private String data;
        private final Map<String, TrieNode> children = new HashMap<>();

        public Map<String, TrieNode> getChildren() {
            return children;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public static void main(String[] args) {
        Router rm = new RouterImpl2();
        rm.add("jira.atlassian.com/testRoute/abc", "fooData1");
        rm.add("jira.atlassian.com/testRoute/xyz", "fooData2");
        rm.add("jira.atlassian.com/testRoute/", "fooData3");
        rm.add("jira.atlassian.com/testRoute/*/xyz", "fooData4");

        String v = rm.get("jira.atlassian.com/testRoute/abc/ghf/xyz");
        System.out.println(v); // should print fooData4
    }
}