package com.smartblood.servlet;

import com.smartblood.dao.RequestDAO;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/request/formalities")
public class ProcurementDetailsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        Integer requestId = ValidationUtils.parsePositiveInteger(request.getParameter("requestId"));
        String contactName = ValidationUtils.trimToEmpty(request.getParameter("contactName"));
        String contactPhone = ValidationUtils.normalizePhone(request.getParameter("contactPhone"));
        String hospitalName = ValidationUtils.trimToEmpty(request.getParameter("hospitalName"));
        String requiredBy = ValidationUtils.trimToEmpty(request.getParameter("requiredBy"));
        String formalityNotes = ValidationUtils.trimToEmpty(request.getParameter("formalityNotes"));

        if (requestId == null) {
            FlashUtils.error(request, "A valid request ID is required.");
            response.sendRedirect(request.getContextPath() + "/dashboard?formalities=error");
            return;
        }

        String validationError = ValidationUtils.validateProcurementDetails(
                contactName, contactPhone, hospitalName, requiredBy, formalityNotes);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/dashboard?formalities=error");
            return;
        }

        boolean updated = new RequestDAO().updateProcurementDetails(
                requestId, user.getId(), contactName, contactPhone, hospitalName, requiredBy, formalityNotes);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/dashboard?formalities=updated");
            return;
        }

        FlashUtils.error(request, "Formalities could not be saved. Wait until a donor accepts the request.");
        response.sendRedirect(request.getContextPath() + "/dashboard?formalities=error");
    }
}
