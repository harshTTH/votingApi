import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Main extends HttpServlet {

    private static Connection conn = null;
    private static PrintWriter out = null;

    private static final long serialVersionUID = 1L;

    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html");
    	out = response.getWriter();
        if (connectDB()) {
            out.println("Database Connection Successful");
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace(out);
            }
        } else {
            out.println("Database Connection Failed");
        }
        out.close();
    }

    private boolean connectDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(out);
            return false;
        }

        try {
            conn = DriverManager.getConnection(DB, USER, PASS);
            if (conn != null) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace(out);
            return false;
        }

    }

}
