package com.wallet.services;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;

public class DataSourceService {
    public static DataSource createDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setDatabaseName("wallethub");
        dataSource.setUser("wallet");
        dataSource.setPassword("1234");
        dataSource.setServerName("localhost");

        return dataSource;
    }
}
