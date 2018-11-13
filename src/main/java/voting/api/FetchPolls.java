package voting.api;

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

public class FetchPolls extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    private static PrintWriter out = null;

    // Will give 'title' and 'id_no' of today's polls as a JSON Object
    // containing a JSONArray
    private final JSONObject getTodaysPolls() throws Exception {

        JSONObject jsonObject = new JSONObject();

        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("create table if not exist/s polls(title varchar(64), poll_date date, "
                        + "candidates text, voters text, numcandidates int, numvoters int, "
                        + "id_no int primary key auto_increment, unique(title));");

        ResultSet res = stmt.executeQuery("select title, id_no from polls where poll_date = curdate();");

        while (res.next()) {

            JSONObject tempObject = new JSONObject();

            tempObject.append("title", res.getString("title"));
            tempObject.append("id_no", res.getString("id_no"));

            jsonObject.accumulate("all", tempObject);

        }

        res.close();
        stmt.close();
        conn.close();

        return jsonObject;

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        try {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            // Possibly empty set
            JSONObject jsonObject = getTodaysPolls();
            out.print(jsonObject);

        } catch (Exception e) {
            e.printStackTrace(out);
        }

        if (out != null) {
            out.close();
            out = null;
        }

    }

}
