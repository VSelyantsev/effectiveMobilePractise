package ru.itis.kpfu.selyantsev.exceptions;

public class ExpiredLink extends RuntimeException {
    public ExpiredLink(String shortUrl) {
        super(String.format("Your link: %s has already expired!", shortUrl));
    }
}
