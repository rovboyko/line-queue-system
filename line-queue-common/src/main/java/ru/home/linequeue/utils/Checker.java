package ru.home.linequeue.utils;

public class Checker {

    public static <T> T notNull(T val) {
        if (val == null) {
            throw new IllegalStateException("Unexpected null value");
        }
        return val;
    }

    public static void checkCondition(boolean condition) {
        checkCondition(condition, "Unexpected program condition");
    }

    public static void checkCondition(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalStateException(msg);
        }
    }

}
