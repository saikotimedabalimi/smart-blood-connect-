package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
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
import javax.servlet.http.HttpSession;

@WebServlet("/profile/update")
public class UpdateProfileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        String name = ValidationUtils.trimToEmpty(request.getParameter("name"));
        String phone = ValidationUtils.normalizePhone(request.getParameter("phone"));
        String city = ValidationUtils.trimToEmpty(request.getParameter("city"));
        String location = ValidationUtils.trimToEmpty(request.getParameter("location"));
        String accountType = ValidationUtils.normalizeAccountType(request.getParameter("accountType"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String bloodGroup = ValidationUtils.normalizeBloodGroup(request.getParameter("bloodGroup"));

        String validationError = ValidationUtils.validateProfileUpdate(name, phone, city, location, accountType);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/dashboard?profile=error");
            return;
        }

        if (password != null && !password.trim().isEmpty()) {
            if (!password.equals(confirmPassword)) {
                FlashUtils.error(request, "Passwords do not match.");
                response.sendRedirect(request.getContextPath() + "/dashboard?profile=error");
                return;
            }
            if (!ValidationUtils.isStrongPassword(password)) {
                FlashUtils.error(request, "Password must be 8+ chars with upper, lower, digit, and special char.");
                response.sendRedirect(request.getContextPath() + "/dashboard?profile=error");
                return;
            }
            user.setPassword(password);
        }

        // If switching to DONOR, ensure blood group is provided
        if ("DONOR".equals(accountType) && !"DONOR".equals(user.getAccountType())) {
            if (!ValidationUtils.isValidBloodGroup(bloodGroup)) {
                FlashUtils.error(request, "Please provide a valid blood group to register as a donor.");
                response.sendRedirect(request.getContextPath() + "/dashboard?profile=error");
                return;
            }
            try {
                new DonorDAO().createDonorProfile(user.getId(), bloodGroup, location, city, null);
            } catch (Exception e) {
                e.printStackTrace();
                FlashUtils.error(request, "Failed to create donor profile.");
                response.sendRedirect(request.getContextPath() + "/dashboard?profile=error");
                return;
            }
        }

        user.setName(name);
        user.setPhone(phone);
        user.setCity(city);
        user.setLocation(location);
        user.setAccountType(accountType);

        UserDAO userDAO = new UserDAO();
        userDAO.updateProfile(user);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("user", userDAO.findById(user.getId()));
        }

        response.sendRedirect(request.getContextPath() + "/dashboard?profile=updated");
    }
}
