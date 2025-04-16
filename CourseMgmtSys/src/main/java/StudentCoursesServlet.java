import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/StudentCoursesServlet")
public class StudentCoursesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String userId = request.getParameter("userID");
        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Missing userID parameter\"}");
            return;
        }

        List<Map<String, String>> courses = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/collegedb", "root", "root123");

            String sql = "SELECT " +
                    "    c.course_id, " +
                    "    c.course_name, " +
                    "    c.department_id, " +
                    "    CASE " +
                    "        WHEN FIND_IN_SET(c.course_id, s.completed_courses) > 0 THEN 'Completed' " +
                    "        WHEN FIND_IN_SET(c.course_id, s.enrolled_courses) > 0 THEN 'Ongoing' " +
                    "        WHEN FIND_IN_SET(c.course_id, s.to_do_courses) > 0 THEN 'Pending' " +
                    "        ELSE 'Not Taken' " +
                    "    END AS status " +
                    "FROM " +
                    "    student s " +
                    "JOIN " +
                    "    course c " +
                    "    ON FIND_IN_SET(c.course_id, CONCAT(s.completed_courses, ',', s.enrolled_courses, ',', s.to_do_courses)) > 0 " +
                    "WHERE " +
                    "    s.user_id = ?";  // Only one parameter




            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            /*ps.setString(2, userId);
            ps.setString(3, userId);
            ps.setString(4, userId);
            ps.setString(5, userId);
*/
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                json.append("{\"courseID\":\"").append(rs.getString("course_id"))
                        .append("\", \"courseName\":\"").append(rs.getString("course_name"))
                        .append("\", \"department\":\"").append(rs.getString("department_id"))
                        .append("\", \"status\":\"").append(rs.getString("status"))
                        .append("\"}");
                first = false;
            }
            json.append("]");

            rs.close();
            ps.close();
            conn.close();

            out.write(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}