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
@WebServlet(USER_SERVLET)
public class UserServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter(EMAIL);
        System.out.println("Received email: " + email);

        if (email == null || email.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid email\"}");
            return;
        }

        try (PrintWriter out = response.getWriter();
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(USER_DETAILS_QUERY)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userName = rs.getString("student_name");
                String userEmail = rs.getString("email");
                String studentID = rs.getString("student_id");
                String department = rs.getString("student_department");
                int advisorID = rs.getInt("advisor_id");  // ✅ Get advisor_id safely
                String advisorName = rs.getString("advisor_name");

                String jsonResponse = String.format(
                        "{\"success\": true, \"name\": \"%s\", \"email\": \"%s\", \"userID\": \"%s\", \"department\": \"%s\", \"advisor_id\": %d, \"advisor\": \"%s\"}",
                        userName, userEmail, studentID,
                        (department != null ? department : "N/A"),
                        advisorID,
                        (advisorName != null ? advisorName : "No Advisor Assigned")
                );

                out.print(jsonResponse);
            } else {
                out.print("{\"success\": false, \"message\": \"User not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Server error\"}");
        }
    }
}
