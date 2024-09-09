package cn.ChengZhiYa.MHDFBot_Marry.entity;

import lombok.Data;

@Data
public final class DatabaseConfig {
    String host;
    String database;
    String username;
    String password;

    public DatabaseConfig(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
    }
}
