package com.smartblood.servlet;

import com.smartblood.dao.BroadcastDAO;
import com.smartblood.dao.DonorDAO;
import com.smartblood.model.Broadcast;
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

@WebServlet("/broadcast/post")
public class PostBroadcastServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        String message = ValidationUtils.trimToEmpty(request.getParameter("message"));
        String bloodGroup = ValidationUtils.normalizeBloodGroup(request.getParameter("bloodGroup"));
        String city = ValidationUtils.trimToEmpty(request.getParameter("city"));

        if (message.isEmpty() || !ValidationUtils.isValidBloodGroup(bloodGroup) || city.isEmpty()) {
            FlashUtils.error(request, "Please provide a message, valid blood group, and city.");
            response.sendRedirect(request.getContextPath() + "/dashboard?broadcast=error");
            return;
        }

        Broadcast broadcast = new Broadcast();
        broadcast.setUserId(user.getId());
        broadcast.setMessage(message);
        broadcast.setBloodGroup(bloodGroup);
        broadcast.setCity(city);

        try {
            new BroadcastDAO().createBroadcast(broadcast);
            FlashUtils.success(request, "Your urgent blood request has been broadcasted to all users!");
        } catch (Exception e) {
            e.printStackTrace();
            FlashUtils.error(request, "Failed to post broadcast.");
        }

        response.sendRedirect(request.getContextPath() + "/dashboard?broadcast=success");
    }
}
