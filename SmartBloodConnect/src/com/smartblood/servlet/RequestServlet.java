package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.dao.RequestDAO;
import com.smartblood.model.Donor;
import com.smartblood.model.Request;
import com.smartblood.model.User;
import com.smartblood.util.FlashUtils;
import com.smartblood.util.BloodCompatibility;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/request")
public class RequestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SecurityUtils.requireUser(request, response)) {
            return;
        }

        User user = SecurityUtils.getAuthenticatedUser(request);
        String patientName = ValidationUtils.trimToEmpty(request.getParameter("patientName"));
        String bloodGroup = ValidationUtils.normalizeBloodGroup(request.getParameter("bloodGroup"));
        String location = ValidationUtils.trimToEmpty(request.getParameter("location"));
        String city = ValidationUtils.trimToEmpty(request.getParameter("city"));
        String reason = ValidationUtils.trimToEmpty(request.getParameter("reason"));
        String unitsRequiredValue = ValidationUtils.trimToEmpty(request.getParameter("unitsRequired"));
        Integer donorUserId = ValidationUtils.parsePositiveInteger(request.getParameter("donorUserId"));
        boolean emergency = "on".equalsIgnoreCase(request.getParameter("emergency"));

        String validationError = ValidationUtils.validateRequest(
                patientName, bloodGroup, location, city, reason, unitsRequiredValue);
        if (validationError != null) {
            FlashUtils.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/request.jsp?error=true");
            return;
        }

        Integer unitsRequired = ValidationUtils.parsePositiveInteger(unitsRequiredValue);
        DonorDAO donorDAO = new DonorDAO();
        Donor selectedDonor = null;
        if (donorUserId != null) {
            selectedDonor = donorDAO.findAvailableDonorByUserId(donorUserId);
            if (selectedDonor == null
                    || !BloodCompatibility.isCompatible(selectedDonor.getBloodGroup(), bloodGroup)
                    || !selectedDonor.getCity().equalsIgnoreCase(city)) {
                FlashUtils.error(request, "The selected donor is no longer available for this city or blood group.");
                response.sendRedirect(request.getContextPath() + "/request.jsp?error=donor");
                return;
            }
        }

        Request bloodRequest = new Request();
        bloodRequest.setRequesterUserId(user.getId());
        bloodRequest.setRequesterName(user.getName());
        bloodRequest.setPatientName(patientName);
        bloodRequest.setBloodGroup(bloodGroup);
        bloodRequest.setLocation(location);
        bloodRequest.setCity(city);
        bloodRequest.setReason(reason);
        bloodRequest.setUnitsRequired(unitsRequired);
        bloodRequest.setEmergency(emergency);
        bloodRequest.setSelectedDonorUserId(donorUserId == null ? 0 : donorUserId);
        if (selectedDonor == null) {
            bloodRequest.setMatchedDonorSummary(donorDAO.buildMatchSummary(bloodGroup, city));
        }

        boolean created = new RequestDAO().createRequest(bloodRequest);
        if (created) {
            response.sendRedirect(request.getContextPath() + "/dashboard?request=success");
            return;
        }

        FlashUtils.error(request, selectedDonor == null
                ? "Request could not be submitted right now. Please try again."
                : "The direct donor request could not be submitted. Please try another donor.");
        response.sendRedirect(request.getContextPath() + "/request.jsp?error=true");
    }
}
