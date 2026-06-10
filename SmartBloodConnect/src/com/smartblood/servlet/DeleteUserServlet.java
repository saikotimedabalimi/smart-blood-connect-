package com.smartblood.servlet;

import com.smartblood.dao.UserDAO;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/delete-user")
public class DeleteUserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireAdmin(request, response)) {
            return;
        }

        Integer userId = ValidationUtils.parsePositiveInteger(request.getParameter("userId"));
        if (userId == null) {
            FlashUtils.error(request, "A valid user ID is required for deletion.");
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        User currentUser = SecurityUtils.getAuthenticatedUser(request);
        if (currentUser != null && currentUser.getId() == userId) {
            FlashUtils.error(request, "Admin users cannot delete their own active account.");
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        new UserDAO().deleteUser(userId);
        response.sendRedirect(request.getContextPath() + "/admin?deleted=true");
    }
}
