package ru.itis.kpfu.selyantsev.exceptions.handler;

import java.util.Map;

public record ExceptionMessage(Map<String, String> errorMap) { }
