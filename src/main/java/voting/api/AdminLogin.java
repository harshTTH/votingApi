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
            StringBuffer jb = new StringBuffer();
            String line = null;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
            JSONObject jsonObject = new JSONObject(jb.toString());
            if (jsonObject.getString("email").equals("dummy@admin.com")
                            && jsonObject.getString("password").equals("123456")) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                session = request.getSession(true);
                out.print(true);
            } else {
                out.print(false);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace(out);
        }
        out.close();
    }

}
