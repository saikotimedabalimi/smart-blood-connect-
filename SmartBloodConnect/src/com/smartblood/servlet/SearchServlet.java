package com.smartblood.servlet;

import com.smartblood.dao.DonorDAO;
import com.smartblood.util.ValidationUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bloodGroup = ValidationUtils.normalizeBloodGroup(request.getParameter("bloodGroup"));
        String city = ValidationUtils.trimToEmpty(request.getParameter("city"));
        request.setAttribute("donors", new DonorDAO().searchDonors(bloodGroup, city));
        request.getRequestDispatcher("/search.jsp").forward(request, response);
    }
}
