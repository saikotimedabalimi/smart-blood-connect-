package com.smartblood.servlet;

import com.smartblood.dao.RequestDAO;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import java.time.LocalDate;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/complete-donation")
public class CompleteDonationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireAdmin(request, response)) {
            return;
        }

        Integer requestId = ValidationUtils.parsePositiveInteger(request.getParameter("requestId"));
        String issuerType = ValidationUtils.normalizeIssuerType(request.getParameter("issuerType"));
        String issuerName = ValidationUtils.trimToEmpty(request.getParameter("issuerName"));
        String issuedOn = ValidationUtils.trimToEmpty(request.getParameter("issuedOn"));
        String certificateNumber = ValidationUtils.trimToEmpty(request.getParameter("certificateNumber"));

        if (requestId == null) {
            FlashUtils.error(request, "A valid request ID is required.");
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        String validationError = ValidationUtils.validateCertificateIssue(issuerType, issuerName, issuedOn);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        if (certificateNumber.isEmpty()) {
            certificateNumber = "SBC-" + requestId + "-" + LocalDate.parse(issuedOn).toString().replace("-", "");
        }

        boolean updated = new RequestDAO().completeDonation(requestId, issuerType, issuerName, certificateNumber, issuedOn);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/admin?completed=true");
            return;
        }

        FlashUtils.error(request, "Donation completion or certificate issue failed.");
        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
