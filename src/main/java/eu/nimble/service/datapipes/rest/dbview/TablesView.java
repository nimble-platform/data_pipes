package eu.nimble.service.datapipes.rest.dbview;

import eu.nimble.service.datapipes.rest.check.DataPipeCheckService;
import eu.nimble.service.datapipes.common.Helper;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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

@Controller
@RequestMapping(path = "/table")
@Api("Data Pipes service table API")
public class TablesView {
    private static final Logger logger = Logger.getLogger(TablesView.class);

    public TablesView() {
        logger.info("starting TablesView Main...");
    }


    @Path("/{tableName}")
    @GET
    public Response readTable(@PathParam("tableName") String tableName) {
        logger.info("Reading table - " + tableName);
        try {
            StringBuilder sb = new StringBuilder();

            ResultSet rs = DataPipeCheckService.dbManager.readAllTable(tableName);
            ResultSetMetaData rsmd = rs.getMetaData();

            sb.append("<!DOCTYPE html><html lang=\"en\">")
                    .append(getHeader())
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

    private String getHeader() {
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
