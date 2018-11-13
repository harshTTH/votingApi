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

    // A Bundle class to group all of polling data
    // Makes it convenient to access DataBase with this Bundle kind of class
    private final class Data {

        private String title, poll_date, candidates, voters, numCandidates, numVoters;

        private Data(String title, String poll_date, String candidates, String voters, String numCandidates,
                        String numVoters) throws Exception {
            this.title = title;
            this.poll_date = poll_date;
            this.candidates = candidates;
            this.voters = voters;
            this.numCandidates = numCandidates;
            this.numVoters = numVoters;
        }

    }

    // This function gets all data from JSON Object got from request (Front End)
    // 'title' is just a String, converted it to all lower case
    // 'poll_date' is a String in 'yyyy-mm-dd' in MySQL format
    // 'candidates' is a String Array, stored like "name1|name2" (We can split it later to retrieve data)
    // 'voters' is a 2-D String Array, each row contains exactly two columns (Name and Phone Number)
    // 'voters' is stored like "name1&phone1|name2&phone2"
    // (We'll have to do splits for rows and then for columns separately)
    private final Data getAllData(String rawData) throws Exception {

        JSONObject jsonObject = new JSONObject(rawData);

        String title = jsonObject.getString("title").toLowerCase();

        String poll_date = jsonObject.getString("date");

        JSONArray candidatesJsonArray = jsonObject.getJSONArray("candidates");
        String candidates = "";
        for (int i = 0; i < candidatesJsonArray.length(); i++) {
            candidates += candidatesJsonArray.getString(i) + '|';
        }
        candidates = candidates.substring(0, candidates.length() - 1);

        JSONArray votersJsonArray = jsonObject.getJSONArray("voters");
        String voters = "";
        for (int i = 0; i < votersJsonArray.length(); i++) {
            JSONArray row = votersJsonArray.getJSONArray(i);
            String rowString = "";
            for (int j = 0; j < row.length(); j++) {
                rowString += row.getString(j) + '&';
            }
            rowString = rowString.substring(0, rowString.length() - 1);
            voters += rowString + '|';
        }
        voters = voters.substring(0, voters.length() - 1);

        String numCandidates = "" + candidatesJsonArray.length();

        String numVoters = "" + votersJsonArray.length();

        return new Data(title, poll_date, candidates, voters, numCandidates, numVoters);

    }

    // This function will fill the database with the current poll's data
    // Table:
    // 'polls(title(P), poll_date, candidates, voters, numcandidates, numvoters, id_no(AUTO))'
    // [Keeps a track of all the polls]
    // [Assigns a unique mapping ID to all the distinct polls]
    private final boolean fillDataBase(Data data) throws Exception {
    
        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.execute("create table if not exist/s polls(title varchar(64), poll_date date, "
                        + "candidates text, voters text, numcandidates int, numvoters int, "
                        + "id_no int primary key auto_increment, unique(title));");

        ResultSet res = stmt.executeQuery("select title from polls where title = '" + data.title + "';");
        res.last();

        if (res.getRow() == 1) {
            res.close();
            stmt.close();
            conn.close();
            return false; // if title already exists 
        }

        res.close();

        stmt.execute("insert into polls(title, poll_date, candidates, voters, numcandidates, numvoters) values('"
                        + data.title + "', '" + data.poll_date + "', '" + data.candidates + "', '" + data.voters + "', "
                        + data.numCandidates + ", " + data.numVoters + ");");

        stmt.close();
        conn.close();
        return true; // if title doesn't exists
    }

    // Will give the title, poll_date and id_no of all the polls as a JSONObject
    // (All information from the DB)
    private final JSONObject accumulateAllData() throws Exception {

        JSONObject jsonObject = new JSONObject();

        Class.forName("com.mysql.jdbc.Driver");

        Connection conn = DriverManager.getConnection(DB, USER, PASS);
        Statement stmt = conn.createStatement();

        ResultSet res = stmt.executeQuery("select title, poll_date, id_no from polls;");

        while (res.next()) {

            JSONObject tempObject = new JSONObject();

            tempObject.append("title", res.getString("title"));
            tempObject.append("poll_date", res.getString("poll_date"));
            tempObject.append("id_no", res.getString("id_no"));

            jsonObject.accumulate("all", tempObject);

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

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

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
            // voters: [["name1", "phone1"], ["name2", "phone2"]] -> 2-D Array of Names and Phone Numbers of Voters

            String rawData = new String(data.toString());

            // Adding a new poll to DataBase, if got JSON request from front end
            if (rawData.length() > 0) {
                Data pollData = getAllData(rawData);
                out.println(fillDataBase(pollData));
            }

            // Any which ways we are returning all of the polls currently
            // in DataBase to front end (possibly empty set)
            out.print(accumulateAllData());

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
