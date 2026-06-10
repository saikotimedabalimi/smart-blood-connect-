package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.dao.UserDAO;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = ValidationUtils.trimToEmpty(request.getParameter("name"));
        String email = ValidationUtils.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");
        String phone = ValidationUtils.normalizePhone(request.getParameter("phone"));
        String city = ValidationUtils.trimToEmpty(request.getParameter("city"));
        String location = ValidationUtils.trimToEmpty(request.getParameter("location"));
        String accountType = ValidationUtils.normalizeAccountType(request.getParameter("accountType"));
        String bloodGroup = ValidationUtils.normalizeBloodGroup(request.getParameter("bloodGroup"));
        String lastDonationDate = ValidationUtils.trimToEmpty(request.getParameter("lastDonationDate"));

        String validationError = ValidationUtils.validateRegistration(
                name, email, password, phone, city, location, accountType, bloodGroup, lastDonationDate);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=validation");
            return;
        }

        UserDAO userDAO = new UserDAO();
        DonorDAO donorDAO = new DonorDAO();

        if (userDAO.emailExists(email)) {
            FlashUtils.error(request, "This email is already registered.");
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=exists");
            return;
        }

        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhone(phone);
            user.setCity(city);
            user.setLocation(location);
            user.setAccountType(accountType);
            user.setRole("USER");
            user.setNotificationEnabled(true);

            int userId = userDAO.registerUser(user);
            if (userId <= 0) {
                FlashUtils.error(request, "Registration could not be completed. Please try again.");
                response.sendRedirect(request.getContextPath() + "/register.jsp?error=true");
                return;
            }

            if ("DONOR".equals(accountType)) {
                donorDAO.createDonorProfile(
                        userId,
                        bloodGroup,
                        location,
                        city,
                        lastDonationDate.isEmpty() ? null : lastDonationDate);
            }

            response.sendRedirect(request.getContextPath() + "/login.jsp?registered=true");
        } catch (Exception ex) {
            ex.printStackTrace();
            FlashUtils.error(request, "Registration failed. Please check your database setup.");
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=true");
        }
    }
}
