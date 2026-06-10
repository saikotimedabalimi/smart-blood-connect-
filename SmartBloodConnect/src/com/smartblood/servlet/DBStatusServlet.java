package com.smartblood.servlet;

import com.smartblood.dao.DBConnection;
import com.smartblood.dao.SchemaManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/db-status")
public class DBStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<String> tables = new ArrayList<>();
        request.setAttribute("dbUrl", DBConnection.getConfiguredUrl());
        request.setAttribute("dbUser", DBConnection.getConfiguredUser());

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                request.setAttribute("dbConnected", Boolean.FALSE);
                request.setAttribute("dbMessage", "Connection returned null. Check MySQL service and database settings.");
            } else {
                SchemaManager.ensureSchema();
                DatabaseMetaData metaData = connection.getMetaData();
                request.setAttribute("dbConnected", Boolean.TRUE);
                request.setAttribute("dbMessage", "MySQL JDBC driver loaded and database connection established successfully.");
                request.setAttribute("dbProduct", metaData.getDatabaseProductName());
                request.setAttribute("dbVersion", metaData.getDatabaseProductVersion());

                try (ResultSet resultSet =
                             metaData.getTables(connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
                    while (resultSet.next()) {
                        tables.add(resultSet.getString("TABLE_NAME"));
                    }
                }
            }
        } catch (Exception ex) {
            request.setAttribute("dbConnected", Boolean.FALSE);
            request.setAttribute("dbMessage", ex.getMessage());
        }

        request.setAttribute("tables", tables);
        request.getRequestDispatcher("/db-status.jsp").forward(request, response);
    }
}
