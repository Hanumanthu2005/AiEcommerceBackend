package com.hanu.AiEcommerce.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timeStamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
