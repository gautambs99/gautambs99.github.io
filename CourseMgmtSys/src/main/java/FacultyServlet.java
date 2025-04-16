import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static Cons.Constants.*;

@WebServlet("/FacultyServlet")
public class FacultyServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String studentEmail = request.getParameter("email");

        if (studentEmail == null || studentEmail.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid email\"}");
            return;
        }

        try (PrintWriter out = response.getWriter();
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT f.user_id, f.name, f.department_id, f.teaching_courses, f.office_hours " +
                             "FROM faculty f " +
                             "JOIN student s ON s.advisor_id = f.user_id " +
                             "JOIN user u ON s.user_id = u.user_id " +
                             "WHERE u.email = ?")) {

            stmt.setString(1, studentEmail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String facultyId = rs.getString("user_id");
                String facultyName = rs.getString("name");
                String department = rs.getString("department_id");
                String teachingCourses = rs.getString("teaching_courses");
                String officeHours = rs.getString("office_hours");

                String jsonResponse = String.format(
                        "{\"success\": true, \"userID\": \"%s\", \"name\": \"%s\", \"department\": \"%s\", \"teaching_courses\": \"%s\", \"office_hours\": \"%s\"}",
                        facultyId, facultyName, department, teachingCourses, officeHours);

                out.print(jsonResponse);
            } else {
                out.print("{\"success\": false, \"message\": \"Faculty not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Server error\"}");
        }
    }
}
