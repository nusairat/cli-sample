package com.wallet.services;

import com.wallet.Duration;
import com.wallet.Logger;
import com.wallet.domain.LogEntry;
import com.wallet.domain.ReportEntry;
import com.wallet.utils.DateParser;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wallet.Duration.DAILY;

public class QueryLogFile implements Logger {

    private static final String SELECT_IP_LIST =      "Select count(id) as count, ip "
                                                    + "from log_entry "
                                                    + "where entry_date Between ? And ?"
                                                    + "Group By ip "
                                                    + "Having count > ?";

    private static final String SELECT_FROM_IP =    "Select entry_date, request, status, user_agent "
                                                  + "from log_entry "
                                                  + "where ip = ?";

    private static final String INSERT_SQL = "INSERT INTO report_entry(entry_date, ip, request, status, user_agent, comment) values(?, ?, ?, ?, ?, ?)";

    public List<ReportEntry> createLogFileReport(LocalDateTime startDate, Duration duration, int threshold) {
        List<ReportEntry> entries = new ArrayList<>();


        info("Query for startDate: {}, duration: {}, threshold: {}", startDate, duration, threshold);

        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement pstmt = null;
        try {
            DataSource dataSource = DataSourceService.createDataSource();
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(SELECT_IP_LIST);
            Timestamp start = Timestamp.valueOf(startDate);
            Timestamp end = createEndTS(startDate, duration);
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            stmt.setInt(3, threshold);

            // Find all Records but get the ones for a count
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("count");
                String ip = rs.getString("ip");

                info("Found IP : {} with a count of: {}", ip, count);
                entries.add(new ReportEntry(ip, count));
            }
            stmt.close();

            if (entries.size() > 0) {
                pstmt = conn.prepareStatement(INSERT_SQL);

                // Now lets get all the items for our entries.
                // This could be batched with an in but then you take a risk on the amount of entries
                for (ReportEntry entry : entries) {
                    stmt = conn.prepareStatement(SELECT_FROM_IP);
                    stmt.setString(1, entry.ip);
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        entry.entries.add(new LogEntry(
                                rs.getTimestamp(1).toLocalDateTime(),
                                entry.ip,
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getString(4)
                        ));

                        // now add for the batch file for it
                        // entry_date, ip, request, status, user_agent, comment
                        pstmt.setTimestamp(1, rs.getTimestamp(1));
                        pstmt.setString(2, entry.ip);
                        pstmt.setString(3, rs.getString(2));
                        pstmt.setInt(4, rs.getInt(3));
                        pstmt.setString(5, rs.getString(4));
                        pstmt.setString(6, "Blocked due to having " + entry.count + " in a timespan of " + duration.name().toLowerCase());
                        pstmt.addBatch();
                    }
                }
                int[] reportsCreated = pstmt.executeBatch();
                conn.commit();
                info("Stored in Report Entries : " + reportsCreated.length);
            }
        } catch (SQLException e) {
            error("SQL Exception:",e);
            return null;
        } catch (Exception e) {
            error("Exception:",e);
            return null;
        }
        finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                warn("Error closing statement");
            }

            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                warn("Error closing statement");
            }

            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                warn("Error closing connection");
            }
        }


        return entries;
    }

    private Timestamp createEndTS(LocalDateTime startDate, Duration duration) {
        LocalDateTime newTime = null;
        switch(duration) {
            case DAILY:
                newTime = startDate.plusDays(1);
                break;
            case HOURLY:
                newTime = startDate.plusHours(1);
                break;
        }
        return Timestamp.valueOf(newTime);
    }
}
