package com.wallet.domain;

import java.util.ArrayList;
import java.util.List;

public class ReportEntry {
    public String ip;
    public int count;
    public List<LogEntry> entries = new ArrayList<>();

    public ReportEntry(String ip, int count) {
        this.ip = ip;
        this.count = count;
    }
}
