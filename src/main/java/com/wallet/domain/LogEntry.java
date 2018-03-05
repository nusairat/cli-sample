package com.wallet.domain;

import java.time.LocalDateTime;

/**
 * Entry for storing log files parsed from:
 *
 * 2017-01-01 00:00:11.763|192.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
 */
public class LogEntry {

    public LocalDateTime date;
    public String ip;
    public String request;
    public int status;
    public String userAgent;

    public LogEntry(LocalDateTime date, String ip, String request, int status, String userAgent) {
        this.date = date;
        this.ip = ip;
        this.request = request;
        this.status = status;
        this.userAgent = userAgent;
    }
}

