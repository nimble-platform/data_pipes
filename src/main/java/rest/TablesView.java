package rest;

import common.Helper;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by evgeniyh on 6/4/18.
 */

@Path("/table")
public class TablesView {
    private static final Logger logger = Logger.getLogger(TablesView.class);

    private static String header = getHeader();


    @Path("/{tableName}")
    @GET
    public Response readTable(@PathParam("tableName") String tableName) {
        logger.info("Reading table - " + tableName);
        try {
            StringBuilder sb = new StringBuilder();

            ResultSet rs = Main.dbManager.readAllTable(tableName);
            ResultSetMetaData rsmd = rs.getMetaData();

            sb.append("<!DOCTYPE html><html lang=\"en\">")
                    .append(header)
                    .append("<body>")
                    .append("<table style=\"width:100%\">");

            addTableHeaders(sb, rsmd);
            addTableRows(sb, rs, rsmd.getColumnCount());

            sb.append("</table>")
                    .append("</body>")
                    .append("</html>");

            return Helper.createResponse(Status.OK, sb.toString());
        } catch (Exception e) {
            logger.error("Error during reading of table - " + tableName, e);
            return Helper.createResponse(Status.INTERNAL_SERVER_ERROR, "Error during the read of the table");
        }
    }

    private void addTableRows(StringBuilder sb, ResultSet rs, int columnCount) throws SQLException {
        while(rs.next()) {
            sb.append("<tr>");
            for (int i=1 ; i<= columnCount ; i++) {
                sb.append("<td>").append(rs.getString(i)).append("</td>");
            }
            sb.append("</tr>");
        }
    }

    private void addTableHeaders(StringBuilder sb, ResultSetMetaData rsmd) throws SQLException {
        sb.append("<tr>");
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            sb.append("<th>")
                    .append(rsmd.getColumnName(i))
                    .append("</th>");

        }
        sb.append("</tr>");
    }

    private static String getHeader() {
        try {
            try (InputStream is = TablesView.class.getClassLoader().getResourceAsStream("/head.html")) {
                if (is == null) {
                    throw new RuntimeException("Failed to read the resource");
                }
                return Helper.inputStreamToString(is);
            }
        } catch (IOException e) {
            logger.error("Error during read of the header");
            return null;
        }
    }
}
