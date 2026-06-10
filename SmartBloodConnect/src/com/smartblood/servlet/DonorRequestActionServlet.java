package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.dao.RequestDAO;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/donor/request-action")
public class DonorRequestActionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        if (new DonorDAO().findByUserId(user.getId()) == null) {
            FlashUtils.error(request, "Only donor accounts can accept or reject blood requests.");
            response.sendRedirect(request.getContextPath() + "/dashboard?donorAction=error");
            return;
        }

        Integer requestId = ValidationUtils.parsePositiveInteger(request.getParameter("requestId"));
        String action = ValidationUtils.trimToEmpty(request.getParameter("action")).toUpperCase(Locale.ROOT);
        String note = ValidationUtils.trimToEmpty(request.getParameter("note"));
        if (requestId == null || (!"ACCEPT".equals(action) && !"REJECT".equals(action))) {
            FlashUtils.error(request, "A valid donor action is required.");
            response.sendRedirect(request.getContextPath() + "/dashboard?donorAction=error");
            return;
        }

        boolean updated = new RequestDAO().respondToRequest(requestId, user.getId(), action, note);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/dashboard?donorAction=success");
            return;
        }

        FlashUtils.error(request, "This request could not be updated. It may already be handled.");
        response.sendRedirect(request.getContextPath() + "/dashboard?donorAction=error");
    }
}
