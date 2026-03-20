package com.lumitest.datainspection.infrastructure.adapter.in.web;

import lombok.Data;

@Data
public class ConnectionParams {
    private String dbType; // MONGODB or POSTGRESQL
    private String host;
    private int port;
    private String username;
    private String password;
    private String databaseName;
    private String connectionString;
    private String fieldName;
}
