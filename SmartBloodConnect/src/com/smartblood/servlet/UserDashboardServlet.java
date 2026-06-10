package com.smartblood.servlet;

import com.smartblood.dao.BroadcastDAO;
import com.smartblood.dao.DonorDAO;
import com.smartblood.dao.NotificationDAO;
import com.smartblood.dao.RequestDAO;
import com.smartblood.dao.UserDAO;
import com.smartblood.model.User;
import com.smartblood.util.SecurityUtils;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class UserDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = SecurityUtils.getAuthenticatedUser(request);
        User refreshedUser = new UserDAO().findById(currentUser.getId());
        if (refreshedUser == null) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=login");
            return;
        }

        if (session != null) {
            session.setAttribute("user", refreshedUser);
        }

        DonorDAO donorDAO = new DonorDAO();
        RequestDAO requestDAO = new RequestDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        BroadcastDAO broadcastDAO = new BroadcastDAO();

        request.setAttribute("donorProfile", donorDAO.findByUserId(refreshedUser.getId()));
        request.setAttribute("requests", requestDAO.getRequestsByUser(refreshedUser.getId()));
        request.setAttribute(
                "donorRequests",
                "DONOR".equalsIgnoreCase(refreshedUser.getAccountType())
                        ? requestDAO.getRequestsForDonor(refreshedUser.getId())
                        : Collections.emptyList());
        request.setAttribute("notifications", notificationDAO.getLatestNotifications(refreshedUser.getId(), 8));
        request.setAttribute("unreadCount", notificationDAO.getUnreadCount(refreshedUser.getId()));
        request.setAttribute("broadcasts", broadcastDAO.getAllActiveBroadcasts());
        
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
