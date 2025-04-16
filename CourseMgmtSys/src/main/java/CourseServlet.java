import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static Cons.Constants.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/CourseServlet")
public class CourseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String searchQuery = request.getParameter("search");
        String courseId = request.getParameter("courseId");

        try (PrintWriter out = response.getWriter()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Searching courses by name or ID
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                PreparedStatement stmt = conn.prepareStatement(COURSE_SEARCH_QUERY);
                stmt.setString(1, searchQuery);
                stmt.setString(2, "%" + searchQuery + "%");

                ResultSet rs = stmt.executeQuery();
                StringBuilder json = new StringBuilder("[");
                boolean found = false;

                while (rs.next()) {
                    if (json.length() > 1) json.append(",");
                    json.append("{\"id\":\"").append(rs.getString("course_id"))
                            .append("\",\"name\":\"").append(rs.getString("course_name")).append("\"}");
                    found = true;
                }
                json.append("]");
                if (!found) {
                    System.out.println(NO_COURSES_FOUND + searchQuery);
                }

                out.print(json.toString());
                conn.close();
                return;
            }

            // Fetch course details including prerequisites
            if (courseId != null && !courseId.trim().isEmpty()) {
                PreparedStatement courseStmt = conn.prepareStatement(
                        "SELECT course_id, course_name, faculty_id, department_id, prerequisite_id " +
                                "FROM course WHERE course_id = ?"
                );
                courseStmt.setString(1, courseId);
                ResultSet courseRs = courseStmt.executeQuery();

                if (!courseRs.next()) {
                    out.print("{\"error\":\"Course Not Found\"}");
                    return;
                }

                StringBuilder json = new StringBuilder("{");
                json.append("\"id\":\"").append(courseRs.getString("course_id")).append("\",");
                json.append("\"name\":\"").append(courseRs.getString("course_name")).append("\",");
                json.append("\"faculty_id\":").append(courseRs.getInt("faculty_id")).append(",");
                json.append("\"department_id\":").append(courseRs.getInt("department_id")).append(",");

                // ✅ Initialize visited set to track cycles
                Set<String> visited = new HashSet<>();
                json.append("\"prerequisites\":").append(fetchPrerequisites(conn, courseId, visited)); // ✅ Fixed method call
                json.append("}");

                out.print(json.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray fetchPrerequisites(Connection conn, String courseId, Set<String> visited) throws SQLException {
        JSONArray prereqArray = new JSONArray();

        if (visited.contains(courseId)) {
            return prereqArray; // Stop infinite loop
        }

        visited.add(courseId); // Mark this course as visited

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT course_id, course_name, prerequisite_id " +
                        "FROM course WHERE course_id = ?"
        );

        stmt.setString(1, courseId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String prerequisiteId = rs.getString("prerequisite_id");

            if (prerequisiteId != null) {
                PreparedStatement prereqStmt = conn.prepareStatement(
                        "SELECT course_id, course_name, prerequisite_id " +
                                "FROM course WHERE course_id = ?"
                );
                prereqStmt.setString(1, prerequisiteId);
                ResultSet prereqRs = prereqStmt.executeQuery();

                while (prereqRs.next()) {
                    JSONObject prereqObj = new JSONObject();
                    prereqObj.put("id", prereqRs.getString("course_id"));
                    prereqObj.put("name", prereqRs.getString("course_name"));
                    prereqObj.put("prerequisites", fetchPrerequisites(conn, prereqRs.getString("course_id"), visited));
                    prereqArray.put(prereqObj);
                }
            }
        }

        return prereqArray;
    }
}