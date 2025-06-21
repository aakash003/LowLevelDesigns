package org.flipkart.coding;

import java.util.HashMap;
import java.util.Map;

class RouterImp {

    class PathNode {
        private Map<String, PathNode> children;
        private String value;

        PathNode() {
            children = new HashMap<>();
        }
    }

    private PathNode root;

    RouterImp() {
        root = new PathNode();
        root.children.put("", new PathNode());
    }

    public void addRoute(String path, String result) {
        PathNode cur = root;
        String[] folders = path.split("/");
        for (String folder : folders) {
            if (!cur.children.containsKey(folder)) {
                cur.children.put(folder, new PathNode());
            }
            cur = cur.children.get(folder);
        }
        cur.value = result;
    }

    public String callRoute(String path) {
        if (path == null || path.length() == 0) {
            return "";
        }
        return helper(path.split("/"), 0, root);
    }

    private String helper(String[] path, int idx, PathNode cur) {
        if (idx == path.length) {
            return cur.value == null ? "" : cur.value;
        }
        if (cur == null) {
            return "";
        }
        String folder = path[idx];
        if (folder.equals("*")) {
            for (String nextFolder : cur.children.keySet()) {
                String result = helper(path, idx + 1, cur.children.get(nextFolder));
                if (!result.isEmpty()) {
                    return result;
                }
            }
            return "";
        } else {
            return helper(path, idx + 1, cur.children.get(folder));
        }
    }

    public static void main(String[] args) {
        RouterImp router = new RouterImp();
        router.addRoute("/home", "home");
        router.addRoute("/foo", "foo");
        router.addRoute("/bar/*/baz", "bar");


        System.out.println(router.callRoute("/bar/a/baz"));
        router.addRoute("/foo/baz", "foo");
        router.addRoute("/foo/*", "bar");
        System.out.println(router.callRoute("/bar/a/baz"));

    }
}