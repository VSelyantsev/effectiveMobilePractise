package ru.itis.kpfu.selyantsev.exceptions;

public class UrlNotFoundException extends NotFoundException {
    public UrlNotFoundException(String shortUrl) {
        super(String.format("Url with this alias/shortUrl: %s NOT FOUND", shortUrl));
    }
}
