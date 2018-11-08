package voting.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.IllegalFormatException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminLogin extends HttpServlet {

    private static PrintWriter out = null;
    private static Connection conn = null;
    private static Statement stmt = null;
    private static ResultSet res = null;

    private static final long serialVersionUID = 1L;

    private static final String query = "select * from admin where user = '%1s' and pass = '%2s';";
    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        out = response.getWriter();
        String userName = request.getParameter("username");
        String passWord = request.getParameter("password");
        if (connectDB(userName, passWord)) {
            out.println("True");
            out.println(request.getSession().getId());
        } else {
            out.println("False");
        }
        closeAll();
    }

    private final void closeAll() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (res != null) {
                res.close();
            }
        } catch (SQLException e) {
            e.printStackTrace(out);
        }
        out.close();
    }

    private final boolean connectDB(String userName, String passWord) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(out);
            return false;
        }
        try {
            conn = DriverManager.getConnection(DB, USER, PASS);
            if (conn != null) {
                stmt = conn.createStatement();
                if (stmt != null) {
                    res = stmt.executeQuery(String.format(query, userName, passWord));
                    if (res != null) {
                        res.last();
                        if (res.getRow() == 1) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(out);
        } catch (IllegalFormatException e) {
            e.printStackTrace(out);
        }
        return false;
    }

}
