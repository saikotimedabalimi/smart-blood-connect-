package com.smartblood.servlet;

import com.smartblood.dao.UserDAO;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginType = ValidationUtils.trimToEmpty(request.getParameter("loginType"));
        String emailOrUsername = ValidationUtils.trimToEmpty(request.getParameter("email"));
        String password = request.getParameter("password");

        String validationError = ValidationUtils.validateLogin(loginType, emailOrUsername, password);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
            return;
        }

        UserDAO userDAO = new UserDAO();
        if ("admin".equalsIgnoreCase(loginType)) {
            if (userDAO.authenticateAdmin(emailOrUsername, password)) {
                HttpSession session = request.getSession();
                session.setAttribute("adminLoggedIn", Boolean.TRUE);
                session.setAttribute("adminName", emailOrUsername);
                session.removeAttribute("user");
                response.sendRedirect(request.getContextPath() + "/admin");
                return;
            }

            FlashUtils.error(request, "Invalid admin credentials.");
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
            return;
        }

        User user = userDAO.authenticateUser(ValidationUtils.normalizeEmail(emailOrUsername), password);
        if (user == null) {
            FlashUtils.error(request, "Invalid email or password.");
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("adminLoggedIn", Boolean.TRUE);
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        session.removeAttribute("adminLoggedIn");
        session.removeAttribute("adminName");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
