package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.model.Donor;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/donor/availability")
public class UpdateAvailabilityServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        DonorDAO donorDAO = new DonorDAO();
        Donor donorProfile = donorDAO.findByUserId(user.getId());
        if (donorProfile == null) {
            FlashUtils.error(request, "Only donor accounts can update donation availability.");
            response.sendRedirect(request.getContextPath() + "/dashboard?donor=error");
            return;
        }

        String lastDonationDate = ValidationUtils.trimToEmpty(request.getParameter("lastDonationDate"));
        String validationError = ValidationUtils.validateDonorAvailability(lastDonationDate);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/dashboard?donor=error");
            return;
        }

        boolean available = "on".equalsIgnoreCase(request.getParameter("available"));
        donorDAO.updateAvailability(user.getId(), available, lastDonationDate.isEmpty() ? null : lastDonationDate);

        response.sendRedirect(request.getContextPath() + "/dashboard?donor=updated");
    }
}
