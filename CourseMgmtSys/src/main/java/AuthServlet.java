import java.io.BufferedReader;
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
import org.json.JSONObject;
import static Cons.Constants.*;

@WebServlet(AUTH_SERVLET)
public class AuthServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject jsonData = new JSONObject(sb.toString());

            String userType = jsonData.getString(USER_TYPE);
            String email = jsonData.getString(EMAIL);
            String password = jsonData.getString(PASSWORD);

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(AUTH_QUERY);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, userType);

            ResultSet rs = stmt.executeQuery();

            jsonResponse.put(SUCCESS, rs.next());

            conn.close();
        } catch (Exception e) {
            jsonResponse.put(SUCCESS, false);
            jsonResponse.put(ERROR, e.getMessage());
        }

        out.print(jsonResponse.toString());
        out.flush();
    }
}
