package com.smartblood.servlet;

import com.smartblood.dao.CertificateDAO;
import com.smartblood.model.Certificate;
import com.smartblood.model.User;
import com.smartblood.util.SecurityUtils;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/certificate/download")
public class DownloadCertificateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isAdmin = SecurityUtils.isAdmin(request);
        if (!isAdmin && !SecurityUtils.requireUser(request, response)) {
            return;
        }

        Integer requestId = ValidationUtils.parsePositiveInteger(request.getParameter("requestId"));
        if (requestId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid request ID is required.");
            return;
        }

        CertificateDAO certificateDAO = new CertificateDAO();
        Certificate certificate;
        if (isAdmin) {
            certificate = certificateDAO.findByRequestId(requestId);
        } else {
            User user = SecurityUtils.getAuthenticatedUser(request);
            certificate = certificateDAO.findByRequestAndDonor(requestId, user.getId());
        }

        if (certificate == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"certificate-" + escapeFileName(certificate.getCertificateNumber()) + ".html\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head><meta charset=\"UTF-8\"><title>Donation Certificate</title>");
            writer.println("<style>"
                    + "body{font-family:Georgia,serif;background:#f4efe5;color:#1e1b16;padding:32px;}"
                    + ".sheet{max-width:880px;margin:0 auto;background:#fffdf8;border:10px solid #8b1e3f;padding:48px;"
                    + "box-shadow:0 22px 50px rgba(0,0,0,.12);}"
                    + ".top{text-align:center;margin-bottom:24px;}"
                    + ".tag{display:inline-block;padding:8px 14px;border:1px solid #8b1e3f;border-radius:999px;"
                    + "font-size:13px;letter-spacing:.08em;text-transform:uppercase;color:#8b1e3f;}"
                    + "h1{font-size:42px;margin:18px 0 10px;} .lead{font-size:18px;line-height:1.7;text-align:center;}"
                    + ".grid{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin:32px 0;}"
                    + ".card{border:1px solid #d8c9b4;border-radius:18px;padding:18px;background:#fffaf2;}"
                    + ".label{font-size:12px;text-transform:uppercase;letter-spacing:.08em;color:#725642;}"
                    + ".value{font-size:20px;font-weight:700;margin-top:6px;}"
                    + ".footer{margin-top:36px;display:flex;justify-content:space-between;gap:18px;align-items:flex-end;}"
                    + ".signature{border-top:2px solid #1e1b16;padding-top:12px;min-width:240px;}"
                    + "</style></head><body>");
            writer.println("<div class=\"sheet\">");
            writer.println("<div class=\"top\">");
            writer.println("<span class=\"tag\">" + escape(certificate.getIssuerType()) + " Certificate</span>");
            writer.println("<h1>Certificate of Blood Donation</h1>");
            writer.println("<p class=\"lead\">This certifies that <strong>" + escape(certificate.getDonorName())
                    + "</strong> donated blood for patient <strong>" + escape(certificate.getPatientName())
                    + "</strong> and supported emergency care through Smart Blood Connect.</p>");
            writer.println("</div>");
            writer.println("<div class=\"grid\">");
            writeCard(writer, "Certificate Number", certificate.getCertificateNumber());
            writeCard(writer, "Issued On", certificate.getIssuedOn());
            writeCard(writer, "Blood Group", certificate.getBloodGroup());
            writeCard(writer, "Hospital / Centre", certificate.getHospitalName());
            writeCard(writer, "City", certificate.getCity());
            writeCard(writer, "Issued By", certificate.getIssuerName());
            writer.println("</div>");
            writer.println("<div class=\"footer\">");
            writer.println("<div><p>This certificate is digitally generated for verified blood donation closure.</p></div>");
            writer.println("<div class=\"signature\"><strong>" + escape(certificate.getIssuerName())
                    + "</strong><br>" + escape(certificate.getIssuerType()) + " Issuing Authority</div>");
            writer.println("</div></div></body></html>");
        }
    }

    private void writeCard(PrintWriter writer, String label, String value) {
        writer.println("<div class=\"card\"><div class=\"label\">" + escape(label)
                + "</div><div class=\"value\">" + escape(value) + "</div></div>");
    }

    private String escapeFileName(String value) {
        return value == null ? "certificate" : value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
