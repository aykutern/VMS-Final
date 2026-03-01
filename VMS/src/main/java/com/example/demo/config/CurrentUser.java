package com.example.demo.config;

public class CurrentUser {

    private static final ThreadLocal<String> user = new ThreadLocal<>();

    public static void set(String username) {
        user.set(username);
    }

    public static String get() {
        return user.get();
    }

    public static void clear() {
        user.remove();
    }
}
