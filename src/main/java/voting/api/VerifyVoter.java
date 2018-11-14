package voting.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class VerifyVoter extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    private static PrintWriter out = null;

    private final boolean valid(String rawData) throws Exception {

        JSONObject jsonObject = new JSONObject(rawData);

        String poll_id = jsonObject.getString("poll_id");
        String number = jsonObject.getString("number");
        String checkerString = poll_id + '&' + number;

        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("create table if not exists voters (voter_id varchar(64) primary key);");

        ResultSet res = stmt.executeQuery("select voter_id from voters where voter_id = '" + checkerString + "';");
        res.last();

        if (res.getRow() == 1) {
            return false;
        }

        res.close();
        res = stmt.executeQuery("select voters from polls where id_no = " + poll_id + ";");
        res.last();
        if (res.getRow() == 0) {
            return false;
        }

        String[] voterList = res.getString("voters").split("|");
        for (String s : voterList) {
            if (s.equals(checkerString)) {
                return true;
            }
        }

        return false;

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        try {

            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            BufferedReader reader = request.getReader();
            StringBuffer jb = new StringBuffer();
            String line = null;

            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }

            String rawData = jb.toString();

            // Expecting 'poll_id' and 'number' as a single JSON Object in request
            if (rawData.length() > 0) {
                if (valid(rawData)) {
                    // Here I have to do the Socket Route thing
                } else {
                    out.print(false);
                }
            } else {
                out.print(false);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace(out);
        }

        if (out != null) {
            out.close();
            out = null;
        }

    }

}
