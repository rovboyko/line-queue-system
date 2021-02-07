package ru.home.linequeue.utils;

public class Checker {

    public static <T> T notNull(T val) {
        return notNull(val, "Unexpected null value");
    }

    public static <T> T notNull(T val, String msg) {
        if (val == null) {
            throw new IllegalStateException(msg);
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
