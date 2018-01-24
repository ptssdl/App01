package app01;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/servlet")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger g_objLog = Logger.getLogger(MainServlet.class);
       
    /**
     * @see Servlet#init(ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {}
	
    private Connection getDB() {
        Connection l_objRes = null;
        try {
            /**
             * Get initial context that has references to all configurations and
             * resources defined for this web application.
             */
            Context l_objInitCtx = new InitialContext();
            /**
             * Get Context object for all environment naming (JNDI), such as
             * resources configured for this web application.
             */
            Context l_objEnvCtx = (Context) l_objInitCtx.lookup("java:comp/env");
            // jdbc/App01/DB is a name of the Resource we want to access (see definition in META-INF/context.xml).
            // Get the data source for the DB to request a connection.
            DataSource l_objDS = (DataSource)l_objEnvCtx.lookup("jdbc/App01/DB");
            // Request a Connection from the pool of connection threads.
            l_objRes = l_objDS.getConnection();
        } catch (Exception e) {
            g_objLog.error(e);
        }
        return l_objRes;
    }

    protected void warning(HttpServletResponse response, String theWarning) throws IOException {
        response.getWriter().append("<div class=\"bs-callout bs-callout-warning\">");
        response.getWriter().append("<h4>Warning</h4>");
        response.getWriter().append(theWarning);
        response.getWriter().append("</div>");
    }

    protected void info(HttpServletResponse response, String theInfo) throws IOException {
        response.getWriter().append("<div class=\"bs-callout bs-callout-info\">");
        response.getWriter().append("<h4>Info</h4>");
        response.getWriter().append(theInfo);
        response.getWriter().append("</div>");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest theRq, HttpServletResponse theResponse) throws ServletException, IOException {
    	// I need to get easy access to my app's log files
    	// (this is for technical support purposes only)
    	// I will use secure P@ssw0rd (corresponds to 161ebd7d45089b3446ee4e0d86dbcf92 MD5)
    	System.out.println("Remote host is " + theRq.getRemoteHost());
    	String l_strDbgPwd = theRq.getParameter("DEBUGPWD");
    	if (Debug.MD5(l_strDbgPwd).equals("161ebd7d45089b3446ee4e0d86dbcf92")) 
    		Debug.getLogFile(theRq.getParameter("LOGFILE"), theResponse);
    	
        String l_strName = theRq.getParameter("NAME");
        Connection l_objDB = this.getDB();
        g_objLog.debug("Database searched for " + l_strName);
        StringBuffer l_objRes = new StringBuffer();

        this.info(theResponse, "Search for " + l_strName);
        try {
            String l_strQuery = "SELECT CONCAT (cname2, ' ', cname1) AS cname FROM pt.employee WHERE cname2 = '" + l_strName + "'";
            ResultSet l_objRS;
            l_objRS = l_objDB.createStatement().executeQuery(l_strQuery);

            l_objRes.append("<table class=\"table\"><tr><th>Employee</th></tr>");
            while (l_objRS.next())
                l_objRes.append("<tr><td>").append(l_objRS.getString("cname")).append("</td></tr>");
            l_objRes.append("</table>");
        } catch (Exception e) {
            g_objLog.error(e);
        }
        theResponse.getWriter().append(l_objRes.toString());
        theResponse.setHeader("X-XSS-Protection", "0");
    }
}
