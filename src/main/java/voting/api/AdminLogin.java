package voting.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

public class AdminLogin extends HttpServlet {

    // This annotation is added for the time being just to remove that
    // irritating yellow warning line
    @SuppressWarnings("unused")
    private final class PseudoServer extends WebSocketServer {

        private PseudoServer(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
            // Successful Connection
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
        }

        @Override
        public void onError(WebSocket conn, Exception e) {
        }

    }

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

                // Setting root path for just created session cookie
                if (response.containsHeader("SET-COOKIE")) {
                    String sessionID = session.getId();
                    response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionID + ";Path=/;");
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
