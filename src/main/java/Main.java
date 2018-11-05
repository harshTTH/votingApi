import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends HttpServlet {
  /**
	 * 
	 */
	private static Connection conn = null; 
	private static final long serialVersionUID = 1L;

@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    	if(connectDB()) {
    		System.out.println("Database Connection Successful");
    		
    	}else {
    		System.out.println("Database Connection Failed");
    	}
	}
	
	private boolean connectDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://us-cdbr-gcp-east-01.cleardb.net/gcp_d3a947905984c5db5bb5", "b4285c8592ce72", "a3ca3bab");
			if(conn != null) {
				return true;
			}else {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

}
