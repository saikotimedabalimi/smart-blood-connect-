package com.smartblood.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class FlashUtils {
    public static final String MESSAGE_KEY = "flashMessage";
    public static final String TYPE_KEY = "flashType";

    private FlashUtils() {
    }

    public static void error(HttpServletRequest request, String message) {
        put(request, "error", message);
    }

    public static void success(HttpServletRequest request, String message) {
        put(request, "success", message);
    }

    private static void put(HttpServletRequest request, String type, String message) {
        if (request == null || message == null || message.trim().isEmpty()) {
            return;
        }
        HttpSession session = request.getSession();
        session.setAttribute(MESSAGE_KEY, message.trim());
        session.setAttribute(TYPE_KEY, type);
    }
}
