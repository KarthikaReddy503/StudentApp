package crud;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class StudentServlet extends HttpServlet {
    Connection con;

    @Override
    public void init() throws ServletException {
        super.init(); // Ensures proper servlet lifecycle
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentdb", "root", "root"); // Change creds if needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        try {
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        if (con == null) {
            out.println("Connection is NULL in doGet.");
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("edit".equals(action)) {
                String id = req.getParameter("id");

                PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE id=?");
                ps.setInt(1, Integer.parseInt(id));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Display edit form
                    out.println("<h2>Edit Student</h2>");
                    out.println("<form method='post' action='students'>");
                    out.println("<input type='hidden' name='id' value='" + rs.getInt("id") + "'>");
                    out.println("Name: <input type='text' name='name' value='" + rs.getString("name") + "'><br>");
                    out.println("Email: <input type='text' name='email' value='" + rs.getString("email") + "'><br>");
                    out.println("Course: <input type='text' name='course' value='" + rs.getString("course") + "'><br>");
                    out.println("<input type='submit' value='Save'>");
                    out.println("</form><br>");
                    out.println("<a href='students'>Back to List</a>");
                } else {
                    out.println("No student found with ID: " + id);
                }

            } else {
                // Show all students
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM students");

                out.println("<h2>Student List</h2>");
                out.println("<a href='index.html'>Add Student</a><br><br>");
                out.println("<table border='1'><tr><th>ID</th><th>Name</th><th>Email</th><th>Course</th><th>Actions</th></tr>");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    out.println("<tr><td>" + id + "</td><td>" +
                                rs.getString("name") + "</td><td>" +
                                rs.getString("email") + "</td><td>" +
                                rs.getString("course") + "</td><td>" +
                                "<a href='students?action=edit&id=" + id + "'>Edit</a> | " +
                                "<a href='students?action=delete&id=" + id + "'>Delete</a></td></tr>");
                }

                out.println("</table>");
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (con == null) {
            res.getWriter().println("Connection is NULL in doPost.");
            return;
        }

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String course = req.getParameter("course");

        try {
            if (id == null || id.isEmpty()) {
                // Insert new student
                PreparedStatement ps = con.prepareStatement("INSERT INTO students(name, email, course) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, course);
                ps.executeUpdate();
            } else {
                // Update existing student
                PreparedStatement ps = con.prepareStatement("UPDATE students SET name=?, email=?, course=? WHERE id=?");
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, course);
                ps.setInt(4, Integer.parseInt(id));
                int rowsUpdated = ps.executeUpdate();
                System.out.println("Updated rows: " + rowsUpdated);
            }

            res.sendRedirect("students");

        } catch (Exception e) {
            res.getWriter().println("Post Error: " + e.getMessage());
        }
    }

    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (con == null) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Connection is NULL in doDelete.");
            return;
        }

        String id = req.getParameter("id");
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id=?");
            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            doDelete(req, res);
            res.sendRedirect("students");
        } else {
            super.service(req, res);  // Handles GET/POST normally
        }
    }
}
