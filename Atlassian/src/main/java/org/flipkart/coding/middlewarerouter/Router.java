package org.flipkart.coding.middlewarerouter;

public interface Router {
    public void add(String route, String data);
    public String get(String route);
}
