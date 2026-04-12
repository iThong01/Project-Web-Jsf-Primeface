package com.greenmarket.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@DataSourceDefinition(
    name = "java:app/jdbc/GreenMarketDS2",
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    url = "jdbc:mysql://localhost:3306/greenmarket?serverTimezone=UTC&sslMode=DISABLED&allowPublicKeyRetrieval=true",
    user = "root",
    password = "T12345678"
)

@Named
@ApplicationScoped
public class AppConfig {
    
}