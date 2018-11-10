package voting.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

public class AdminLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static PrintWriter out = null;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        try {

            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();

            StringBuffer data = new StringBuffer();
            String line = null;
            BufferedReader reader = request.getReader();

            while ((line = reader.readLine()) != null) {
                data.append(line);
            }

            JSONObject jsonObject = new JSONObject(data.toString());

            if (jsonObject.getString("email").equals("dummy@admin.com")
                            && jsonObject.getString("password").equals("123456")) {

                HttpSession session = request.getSession(false);

                if (session != null) {
                    session.invalidate();
                }

                session = request.getSession(true);

                // Added attribute on the session for admin account
                session.setAttribute("email", "dummy@admin.com");

                // I'm not sure about this, but just gave a try to set the path as you asked
                if (response.containsHeader("SET-COOKIE")) {
                    String sessionID = session.getId();
                    response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionID + ";Path=/adminPanel;");
                }

                out.print(true);

            } else {
                out.print(false);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace(out);
        }

        // Removed separate try - catch and just made a single one with Stack Trace being
        // printed on the HTTP Response Writer, so in case we get something unexpected, we
        // can see it there and take necessary action accordingly.
        // [Done this in all of the project]

        if (out != null) {
            out.close();
            out = null;
        }

    }

}
