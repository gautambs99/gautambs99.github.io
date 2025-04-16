import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // ✅ Import ResultSet
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static Cons.Constants.*;

@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {

    // ✅ Handle Appointment Booking (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String studentId = request.getParameter("studentId");
        String facultyId = request.getParameter("facultyId");
        String appointmentDateTime = request.getParameter("appointmentDateTime");

        System.out.println("🔄 Booking Appointment Request Received:");
        System.out.println("Student ID: " + studentId);
        System.out.println("Faculty ID: " + facultyId);
        System.out.println("Appointment DateTime: " + appointmentDateTime);

        if (studentId == null || facultyId == null || appointmentDateTime == null ||
                studentId.isEmpty() || facultyId.isEmpty() || appointmentDateTime.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid input data\"}");
            return;
        }

        try (PrintWriter out = response.getWriter();
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO appointments (student_id, faculty_id, appointment_datetime) VALUES (?, ?, ?)")) {

            stmt.setInt(1, Integer.parseInt(studentId));
            stmt.setInt(2, Integer.parseInt(facultyId));
            stmt.setString(3, appointmentDateTime);

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Appointment successfully stored in database.");
                out.print("{\"success\": true, \"message\": \"Appointment booked successfully\"}");
            } else {
                System.out.println("❌ Database insert failed.");
                out.print("{\"success\": false, \"message\": \"Failed to book appointment\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Server error: " + e.getMessage() + "\"}");
        }
    }

    // ✅ Handle Appointment Retrieval (GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String studentId = request.getParameter("userId");

        if (studentId == null || studentId.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid student ID\"}");
            return;
        }

        try (PrintWriter out = response.getWriter();
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.appointment_datetime, f.name AS faculty_name " +
                             "FROM appointments a " +
                             "JOIN faculty f ON a.faculty_id = f.user_id " +
                             "WHERE a.student_id = ? ORDER BY a.appointment_datetime ASC")) {

            stmt.setInt(1, Integer.parseInt(studentId));

            ResultSet rs = stmt.executeQuery();
            StringBuilder json = new StringBuilder("[");

            while (rs.next()) {
                json.append("{\"date\":\"").append(rs.getString("appointment_datetime"))
                        .append("\", \"faculty\":\"").append(rs.getString("faculty_name"))
                        .append("\"},");
            }

            if (json.length() > 1) json.setLength(json.length() - 1); // Remove last comma
            json.append("]");

            out.print(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Server error\"}");
        }
    }

}
