import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

import com.google.gson.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

@WebServlet("/chatbot")
public class ChatbotServlet extends HttpServlet {

    public static final String DB_URL = "jdbc:mysql://localhost:3306/collegedb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root123";
  

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userQuery = request.getParameter("query").trim().toLowerCase();

        // ✅ Smart reply for greetings/small talk
        String smallTalkResponse = getFriendlyReply(userQuery);
        if (smallTalkResponse != null) {
            respondJSON(response, smallTalkResponse);
            return;
        }

        // ✅ Fetch schema and course data
        String schemaInfo = getDatabaseMetadata();
        String courseData = getCourseData();

        // ✅ Construct prompt
        String dataPrompt = "Database Schema:\n" + schemaInfo + "\n"
                + "Course Data:\n" + courseData + "\n"
                + "User Question:\n" + userQuery;

        String answer = callLLM(dataPrompt);
        respondJSON(response, answer);
    }

    private void respondJSON(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = "{\"response\": \"" + message.replace("\"", "\\\"") + "\"}";
        response.getWriter().write(jsonResponse);
    }

    private String getFriendlyReply(String query) {
        switch (query) {
            case "hi":
            case "hello":
            case "hey":
                return "Hello! How can I assist you with course-related questions?";
            case "thank you":
            case "thanks":
                return "You're welcome! Let me know if you need help with courses.";
            case "bye":
            case "goodbye":
                return "Goodbye! Feel free to return if you have more course questions.";
            default:
                return null;
        }
    }

    private String getDatabaseMetadata() {
        StringBuilder schemaInfo = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                schemaInfo.append("Table: ").append(table).append("\n");
                ResultSet columns = metaData.getColumns(null, null, table, "%");
                while (columns.next()) {
                    schemaInfo.append(" - ").append(columns.getString("COLUMN_NAME"))
                            .append(" (").append(columns.getString("TYPE_NAME")).append(")\n");
                }
                schemaInfo.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading DB schema.";
        }
        return schemaInfo.toString();
    }

    private String getCourseData() {
        StringBuilder courseInfo = new StringBuilder();
        String query = "SELECT course_id, course_name, prerequisite_id FROM course";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String name = rs.getString("course_name");
                String prereq = rs.getString("prerequisite_id");

                courseInfo.append("Course ID: ").append(courseId)
                        .append(", Name: ").append(name);

                if (prereq != null) {
                    courseInfo.append(", Prerequisite: ").append(prereq);
                } else {
                    courseInfo.append(", Prerequisite: None");
                }
                courseInfo.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving course data.";
        }

        return courseInfo.toString();
    }

    private String callLLM(String prompt) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.openai.com/v1/chat/completions");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");

            JsonArray messages = new JsonArray();

            // ✅ System instruction: be strict
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content",
                    "You are a course advisor assistant. ONLY use the provided database schema and course data to answer. " +
                            "If the user's question is not related to university courses, reply: 'I can only assist with course-related questions.'");

            // 👤 User prompt
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);

            messages.add(systemMessage);
            messages.add(userMessage);
            requestBody.add("messages", messages);

            post.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

            CloseableHttpResponse resp = client.execute(post);
            int statusCode = resp.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(resp.getEntity());

            System.out.println(">>> OpenAI Status Code: " + statusCode);
            System.out.println(">>> OpenAI Response:\n" + result);

            if (statusCode != 200) {
                return "OpenAI API Error: " + statusCode;
            }

            JsonObject jsonResponse = JsonParser.parseString(result).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject messageObj = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                String content = messageObj.get("content").getAsString();
                return content.replace("\n", "<br>");
            } else {
                return "No response from ChatGPT.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error contacting ChatGPT API.";
        }
    }
}
