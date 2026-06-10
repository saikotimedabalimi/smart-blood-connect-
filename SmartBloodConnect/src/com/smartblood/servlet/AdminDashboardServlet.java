package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.dao.ReportDAO;
import com.smartblood.dao.RequestDAO;
import com.smartblood.dao.UserDAO;
import com.smartblood.util.SecurityUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SecurityUtils.requireAdmin(request, response)) {
            return;
        }

        request.setAttribute("users", new UserDAO().getAllUsers());
        request.setAttribute("donors", new DonorDAO().getAllDonors());
        request.setAttribute("requests", new RequestDAO().getAllRequests());
        request.setAttribute("summary", new ReportDAO().getSummary());
        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }
}
