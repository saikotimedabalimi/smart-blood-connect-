package com.smartblood.servlet;

import com.smartblood.dao.NotificationDAO;
import com.smartblood.model.Notification;
import com.smartblood.model.User;
import com.smartblood.util.SecurityUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/notifications/feed")
public class NotificationFeedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SecurityUtils.getAuthenticatedUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"notifications\":[]}");
            return;
        }

        List<Notification> notifications = new NotificationDAO().getUnreadNotificationsAndMarkRead(user.getId());
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"notifications\":[");
            for (int i = 0; i < notifications.size(); i++) {
                Notification notification = notifications.get(i);
                if (i > 0) {
                    writer.write(',');
                }
                writer.write("{");
                writer.write("\"id\":" + notification.getId() + ",");
                writer.write("\"title\":\"" + escape(notification.getTitle()) + "\",");
                writer.write("\"message\":\"" + escape(notification.getMessage()) + "\",");
                writer.write("\"category\":\"" + escape(notification.getCategory()) + "\",");
                writer.write("\"createdAt\":\"" + escape(notification.getCreatedAt()) + "\"");
                writer.write("}");
            }
            writer.write("]}");
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
