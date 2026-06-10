package com.smartblood.servlet;

import com.smartblood.dao.RequestDAO;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/approve")
public class AdminApproveServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireAdmin(request, response)) {
            return;
        }

        Integer requestId = ValidationUtils.parsePositiveInteger(request.getParameter("requestId"));
        if (requestId == null) {
            FlashUtils.error(request, "A valid request ID is required for approval.");
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        boolean updated = new RequestDAO().updateRequestStatus(requestId, "Approved");
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/admin?approved=true");
            return;
        }

        FlashUtils.error(request, "The selected request could not be approved.");
        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
