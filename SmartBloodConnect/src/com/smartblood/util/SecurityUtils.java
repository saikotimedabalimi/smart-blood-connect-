package com.smartblood.util;

import com.smartblood.model.User;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    public static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = getAuthenticatedUser(request);
        boolean adminLoggedIn = session != null && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"));
        return adminLoggedIn || (user != null && "ADMIN".equalsIgnoreCase(user.getRole()));
    }

    public static boolean requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (getAuthenticatedUser(request) != null) {
            return true;
        }
        FlashUtils.error(request, "Please login to continue.");
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=login");
        return false;
    }

    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAdmin(request)) {
            return true;
        }
        FlashUtils.error(request, "Admin access is required for that action.");
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=admin");
        return false;
    }
}
