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

import org.json.JSONArray;
import org.json.JSONObject;

public class AddNewPoll extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DB = "jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5";
    private static final String USER = "b4285c8592ce72";
    private static final String PASS = "a3ca3bab";

    private static PrintWriter out = null;

    // This function gets all data from JSON Object got from request (Front End)
    // 'title' is just a String, converted it to all lower case
    // 'poll_date' is a String in 'yyyy-mm-dd' in MySQL format
    // 'candidates' is a String Array, stored like "name1|name2" (We can split it
    // later to retrieve data)
    // 'voters' is a 2-D String Array, each row contains exactly two columns (Name
    // and Phone Number)
    // 'voters' is stored like "name1&phone1|name2&phone2"
    // (We'll have to do splits for rows and then for columns separately)
    private final Data getAllData(String rawData) throws Exception {

        JSONObject jsonObject = new JSONObject(rawData);

        String title = jsonObject.getString("title").toLowerCase();

        String poll_date = jsonObject.getString("date");

        JSONArray candidatesJsonArray = jsonObject.getJSONArray("candidates");
        StringBuffer candidateBuffer = new StringBuffer();
        for (int i = 0; i < candidatesJsonArray.length(); i++) {
            String str = candidatesJsonArray.getString(i).replace(' ', '_');
            candidateBuffer.append(str);
            candidateBuffer.append('|');
        }
        String candidates = candidateBuffer.substring(0, candidateBuffer.length() - 1);

        JSONArray votersJsonArray = jsonObject.getJSONArray("voters");
        StringBuffer votersBuffer = new StringBuffer();
        for (int i = 0; i < votersJsonArray.length(); i++) {
            JSONArray row = votersJsonArray.getJSONArray(i);
            StringBuffer rowBuffer = new StringBuffer();
            rowBuffer.append(row.getString(0));
            rowBuffer.append('&');
            rowBuffer.append(row.getString(1));
            rowBuffer.append('|');
            votersBuffer.append(rowBuffer);
        }
        String voters = votersBuffer.substring(0, votersBuffer.length() - 1);

        String numCandidates = "" + candidatesJsonArray.length();

        String numVoters = "" + votersJsonArray.length();

        return new Data(title, poll_date, candidates, voters, numCandidates, numVoters);

    }

    // This function will fill the database with the current poll's data
    // Table:
    // 'polls(title(P), poll_date, candidates, voters, numcandidates, numvoters,
    // id_no(AUTO))'
    // [Keeps a track of all the polls]
    // [Assigns a unique mapping ID to all the distinct polls]
    private final boolean fillDataBase(Data data) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("create table if not exists polls(title varchar(64), poll_date date, "
                        + "candidates text, voters text, numcandidates int, numvoters int, "
                        + "id_no int primary key auto_increment, unique(title));");

        ResultSet res = stmt.executeQuery("select title from polls where title = '" + data.title + "';");

        res.last();

        if (res.getRow() == 1) {
            res.close();
            stmt.close();
            conn.close();
            return false;
        }

        res.close();
        stmt.execute("insert into polls(title, poll_date, candidates, voters, numcandidates, numvoters) values('"
                        + data.title + "', '" + data.poll_date + "', '" + data.candidates + "', '" + data.voters + "', "
                        + data.numCandidates + ", " + data.numVoters + ");");

        // This block of code creates a unique polling table for each new poll
        // StringBuffer is Thread Safe, StringBuilder is not, functionality is same
        // So, I changed it, rest all was okay
        // Except the way Mohit created the tables, it would have given some serious SQL Exceptions
        res = stmt.executeQuery("select id_no from polls where title = '" + data.title + "';");
        res.next();
        String poll_id = res.getString("id_no");
        res.close();

        StringBuffer pollTableQuery = new StringBuffer();
        pollTableQuery.append("create table `" + poll_id + "` (");
        String[] candidates = data.candidates.split("\\|");

        for (String s : candidates) {
            pollTableQuery.append("`" + s + "` int default 0, ");
        }

        pollTableQuery.deleteCharAt(pollTableQuery.length() - 1);
        pollTableQuery.deleteCharAt(pollTableQuery.length() - 1);
        pollTableQuery.append(");");
        String pollTableFinalQuery = pollTableQuery.toString();
        stmt.execute(pollTableFinalQuery);

        stmt.close();
        conn.close();

        return true;

    }

    // Will give the title, poll_date and id_no of all the polls as a JSONObject
    // (All information from the DB)
    private final JSONObject accumulateAllData() throws Exception {

        JSONObject jsonObject = new JSONObject();

        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("create table if not exists polls(title varchar(64), poll_date date, "
                        + "candidates text, voters text, numcandidates int, numvoters int, "
                        + "id_no int primary key auto_increment, unique(title));");

        ResultSet res = stmt.executeQuery("select title, poll_date, id_no from polls;");

        while (res.next()) {

            JSONObject tempObject = new JSONObject();

            tempObject.put("title", res.getString("title"));
            tempObject.put("poll_date", res.getString("poll_date"));
            tempObject.put("id_no", res.getString("id_no"));

            jsonObject.append("all", tempObject);

        }

        res.close();
        stmt.close();
        conn.close();

        return jsonObject;

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
        try {

            StringBuffer data = new StringBuffer();
            String line = null;
            BufferedReader reader = request.getReader();

            while ((line = reader.readLine()) != null) {
                data.append(line);
            }

            // Data must be come in this way from front end:
            // title: "name" -> The title of polls
            // candidates: ["name1", "name2"] -> Array of candidates
            // poll_date: "yyyy-mm-dd" -> The polling date (MySQL format)
            // voters: [["name1", "phone1"], ["name2", "phone2"]] -> 2-D Array of Names and
            // Phone Numbers of Voters

            String rawData = new String(data.toString());

            // Adding a new poll to DataBase, if got JSON request from front end
            // If poll exists, returning error signal (false)
            // Else (even if no request from front end), printing all data

            String contentType;
            boolean valid;

            if (rawData.length() > 0) {
                Data pollData = getAllData(rawData);
                if (fillDataBase(pollData)) {
                    contentType = "application/json";
                    valid = true;
                } else {
                    contentType = "text/html";
                    valid = false;
                }
            } else {
                contentType = "application/json";
                valid = true;
            }

            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            if (valid) {
                out.print(accumulateAllData());
            } else {
                out.print(valid);
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
