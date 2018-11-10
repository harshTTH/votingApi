package voting.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminPanel extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static PrintWriter out = null;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
        try {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();
            // Currently not decided what to do for this page
            // So just created one Servlet for it
            // In case we need it in future.
        } catch (Exception e) {
            e.printStackTrace(out);
        }
        if (out != null) {
            out.close();
            out = null;
        }
    }

}
