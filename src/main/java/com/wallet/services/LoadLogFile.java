package com.wallet.services;

import com.wallet.Logger;
import com.wallet.utils.DateParser;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

public class LoadLogFile implements Logger {

    private static final String INSERT_SQL = "INSERT INTO log_entry(entry_date, ip, request, status, user_agent) values(?, ?, ?, ?, ?)";

    public boolean loadAccessLogs(String s) {
        info("Loads Log file at path: {}", s);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            DataSource dataSource = DataSourceService.createDataSource();
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(INSERT_SQL);

            BufferedReader in = new BufferedReader(new FileReader(s));
            String line;
            while((line = in.readLine())!=null) {
                String[] pair = line.split("\\|", -1);
                trace("Pair: " + pair.length + " / " + Arrays.asList(pair));
                if (pair.length == 5) {
                    addEntry(stmt, pair[0], pair[1], pair[2], Integer.valueOf(pair[3]), pair[4]);
                }
                else {
                    warn("Invalid Record : {}", Arrays.asList(pair));
                }
            }

            // now execute
            int[] updates = stmt.executeBatch();
            info("Added {} Records : ", updates.length);
            conn.commit();
        } catch (FileNotFoundException e) {
            error("File not found for path {}", s);
            return false;
        } catch (IOException e) {
            error("IO Error parsing and storing", e);
            return false;
        } catch (SQLException e) {
            error("SQL Exception:",e);
            return false;
        } catch (Exception e) {
            error("Exception:",e);
            return false;
        }
        finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                error("Error closing statement");
            }

            try {
                if (conn != null) {
                    conn.commit();
                    conn.close();
                }
            } catch (SQLException e) {
                error("Error closing connection");
            }
        }


        return true;
    }

    private void addEntry(PreparedStatement stmt,
            String date,
            String ip,
            String request,
            int status,
            String userAgent) throws SQLException {

        LocalDateTime dateTime = DateParser.toLocalDateTime(date);
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        stmt.setTimestamp(1, timestamp);
        stmt.setString(2, ip);
        stmt.setString(3, request);
        stmt.setInt(4, status);
        stmt.setString(5, userAgent);

        stmt.addBatch();
    }
}
