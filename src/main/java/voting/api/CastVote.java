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

public class CastVote extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    private static PrintWriter out = null;

    private final JSONObject getInfo(String poll_id) throws Exception {

        JSONObject jsonObject = new JSONObject();

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery("select candidates, title from polls where id_no = " + poll_id + ";");
        res.next();
        JSONObject finalObject = new JSONObject();
        String[] candidatesRaw = res.getString("candidates").split("\\|");
        String title = res.getString("title");

        jsonObject.put("candidates", candidatesRaw);
        jsonObject.put("title", title);
        finalObject.put("all", jsonObject);

        res.close();
        stmt.close();
        conn.close();

        return finalObject;

    }

    private final void updateDataBase(String poll_id, String candidate, String number) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("update `" + poll_id + "` set " + candidate + " = " + candidate + " + 1;");
        stmt.execute("insert into voters values ('" + poll_id + '&' + number + "');");

        stmt.close();
        conn.close();

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        try {

            BufferedReader reader = request.getReader();
            StringBuffer data = new StringBuffer();
            String line = null;

            response.setCharacterEncoding("UTF-8");

            while ((line = reader.readLine()) != null) {
                data.append(line);
            }

            String rawData = data.toString();
            JSONObject jsonObject = new JSONObject(rawData);
            String poll_id = jsonObject.getString("poll_id");
            if (jsonObject.keySet().size() == 1) {
                response.setContentType("application/json");
                out = response.getWriter();
                out.print(getInfo(poll_id));
            } else {
                String number = jsonObject.getString("number");
                String candidate = jsonObject.getString("candidate");
                response.setContentType("text/html");
                out = response.getWriter();
                updateDataBase(poll_id, candidate.replace(' ', '_'), number);
                out.print(true);
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
