package ru.itis.kpfu.selyantsev.util;

public class ConfirmationCodeGenerator {

    private ConfirmationCodeGenerator() { }

    public static String generateConfirmationCode() {
        return String.valueOf((int) (Math.random() * 10000));
    }
}
