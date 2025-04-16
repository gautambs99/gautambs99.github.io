import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet("/UploadfileServlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadfileServlet extends HttpServlet {

    public static final String DB_URL = "jdbc:mysql://localhost:3306/collegedb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root123";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        Part filePart = request.getPart("file-upload");
        String fileName = extractFileName(filePart);
        String fileDescription = request.getParameter("file-description");

        try (InputStream fileContent = filePart.getInputStream()) {
            if (fileName.endsWith(".csv")) {
                processCsvFile(fileContent);
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                processExcelFile(fileContent);
            } else {
                response.getWriter().write("Unsupported file format.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error processing file: " + e.getMessage());
            return;
        }

        response.getWriter().write("File uploaded and processed successfully!");
    }

    private void processCsvFile(InputStream inputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             Connection conn = getConnection()) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header row
                    continue;
                }
                String[] columns = line.split(",");
                insertIntoDatabase(conn, columns);
            }
        }
    }

    private void processExcelFile(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream);
             Connection conn = getConnection()) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String[] columns = new String[row.getLastCellNum()];

                for (Cell cell : row) {
                    columns[cell.getColumnIndex()] = cell.toString().trim();
                }

                insertIntoDatabase(conn, columns);
            }
        }
    }

    private void insertIntoDatabase(Connection conn, String[] columns) throws SQLException {
        String sql = "INSERT INTO course (course_id, course_name, department_id, faculty_id, prerequisite_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, columns[0].trim()); // course_id
            stmt.setString(2, columns[1].trim()); // course_name
            stmt.setInt(3, (int) Double.parseDouble(columns[2].trim())); // department_id
            stmt.setInt(4, (int) Double.parseDouble(columns[3].trim())); // faculty_id
            stmt.setString(5, (columns.length > 4 && !columns[4].trim().isEmpty()) ? columns[4].trim() : null); // prerequisite_id
            stmt.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String content : contentDisp.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf("=") + 2, content.length() - 1);
            }
        }
        return "unknown";
    }
}
