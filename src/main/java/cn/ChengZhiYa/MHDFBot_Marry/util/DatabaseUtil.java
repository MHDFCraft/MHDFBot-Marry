package cn.ChengZhiYa.MHDFBot_Marry.util;

import cn.ChengZhiYa.MHDFBot.api.MHDFBot;
import cn.ChengZhiYa.MHDFBot_Marry.entity.DatabaseConfig;
import cn.ChengZhiYa.MHDFBot_Marry.entity.Marry;
import cn.ChengZhiYa.MHDFBot_Marry.entity.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public final class DatabaseUtil {
    @Getter
    static final HashMap<User, Marry> marryHashMap = new HashMap<>();
    @Getter
    static final HashMap<User, Integer> changeWifeTimesHashMap = new HashMap<>();
    @Getter
    static final HashMap<Long, String> roleHashMap = new HashMap<>();

    private static HikariDataSource dataSource;

    public static void connectDatabase(DatabaseConfig database) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + database.getHost() + "/" + database.getDatabase() + "?autoReconnect=true&serverTimezone=" + TimeZone.getDefault().getID());
        config.setUsername(database.getUsername());
        config.setPassword(database.getPassword());
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);
    }

    public static void intiDatabase() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS mhdfbot_marry" +
                                "(" +
                                "    `ID` BIGINT NOT NULL AUTO_INCREMENT," +
                                "    `Group` BIGINT DEFAULT 0 NOT NULL COMMENT '群号'," +
                                "    `Mr` BIGINT DEFAULT 0 NOT NULL COMMENT '老公'," +
                                "    `Mrs` BIGINT DEFAULT 0 NOT NULL COMMENT '老婆'," +
                                "    `ChangeTimes` INT DEFAULT 0 NOT NULL COMMENT '更换老婆次数'," +
                                "    PRIMARY KEY (ID)," +
                                "    INDEX `Mr` (`Mr`)," +
                                "    INDEX `Mrs` (`Mrs`)" +
                                ")" +
                                "COLLATE=utf8mb4_bin;"
                )) {
                    ps.executeUpdate();
                }
            }

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS mhdfbot_marryrole" +
                                "(" +
                                "    `User` BIGINT DEFAULT 0 NOT NULL COMMENT '用户ID'," +
                                "    `Role` VARCHAR(50) DEFAULT 0 NOT NULL COMMENT '身份'," +
                                "    PRIMARY KEY (User)" +
                                ")" +
                                "COLLATE=utf8mb4_bin;"
                )) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Long> getMarryList(Long group) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select * from mhdfbot_marry where `Group`=?;")) {
                ps.setLong(1, group);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Long> marryList = new ArrayList<>();
                    while (rs.next()) {
                        marryList.add(rs.getLong("Mr"));
                        marryList.add(rs.getLong("Mrs"));
                    }
                    return marryList;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Marry getMarry(Long group, Long qq) {
        if (getMarryHashMap().get(new User(group, qq)) == null) {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("select * from mhdfbot_marry where `Group`=? and (`Mr`=? or `Mrs`=?);")) {
                    ps.setLong(1, group);
                    ps.setLong(2, qq);
                    ps.setLong(3, qq);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Marry marry = new Marry(rs.getLong("Mr"), rs.getLong("Mrs"));
                            getMarryHashMap().put(new User(group, qq), marry);
                            return marry;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getMarryHashMap().get(new User(group, qq));
        }
        return null;
    }

    public static int getChangeWifeTimesHashMap(Long group, Long qq) {
        if (getChangeWifeTimesHashMap().get(new User(group, qq)) == null) {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("select * from mhdfbot_marry where `Group`=? and `Mr`=?;")) {
                    ps.setLong(1, group);
                    ps.setLong(2, qq);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int changeTimes = rs.getInt("ChangeTimes");
                            getChangeWifeTimesHashMap().put(new User(group, qq), changeTimes);
                            return changeTimes;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getChangeWifeTimesHashMap().get(new User(group, qq));
        }
        return 0;
    }

    public static String getRole(Long qq) {
        if (getRoleHashMap().get(qq) == null) {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("select * from mhdfbot_marryrole where `User`=?;")) {
                    ps.setLong(1, qq);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String role = rs.getString("Role");
                            getRoleHashMap().put(qq, role);
                            return role;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getRoleHashMap().get(qq);
        }
        return null;
    }

    public static void setMarry(Long group, Marry marry) {
        MHDFBot.getScheduler().runTaskAsynchronously(() -> {
            getMarryHashMap().put(new User(group, marry.getMr()), marry);
            getMarryHashMap().put(new User(group, marry.getMrs()), marry);

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("insert into mhdfbot_marry (`Group`,`Mr`,`Mrs`) values (?,?,?);")) {
                    ps.setLong(1, group);
                    ps.setLong(2, marry.getMr());
                    ps.setLong(3, marry.getMrs());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void changeMarry(Long group, Marry marry) {
        MHDFBot.getScheduler().runTaskAsynchronously(() -> {
            getMarryHashMap().put(new User(group, marry.getMr()), marry);
            getMarryHashMap().put(new User(group, marry.getMrs()), marry);

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("update mhdfbot_marry set `Mrs`=? where `Group`=? and `Mr`=?;")) {
                    ps.setLong(1, marry.getMrs());
                    ps.setLong(2, group);
                    ps.setLong(3, marry.getMr());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setRole(Long qq, String role) {
        getRoleHashMap().put(qq, role);
        MHDFBot.getScheduler().runTaskAsynchronously(() -> {

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("insert into mhdfbot_marryrole (User,Role) values (?,?);")) {
                    ps.setLong(1, qq);
                    ps.setString(2, role);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void addChangeWifeTimes(Long group, Long qq) {
        MHDFBot.getScheduler().runTaskAsynchronously(() -> {
            User user = new User(group, qq);
            getChangeWifeTimesHashMap().put(user, getChangeWifeTimesHashMap().get(user) + 1);
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("update mhdfbot_marry set `ChangeTimes`=`ChangeTimes`+1 where `Group`=? and `Mr`=?;")) {
                    ps.setLong(1, group);
                    ps.setLong(2, qq);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void clearMarryAndRole() {
        MHDFBot.getScheduler().runTaskAsynchronously(() -> {
            getMarryHashMap().clear();
            getRoleHashMap().clear();
            getChangeWifeTimesHashMap().clear();

            try {
                try (Connection connection = dataSource.getConnection()) {
                    try (PreparedStatement ps = connection.prepareStatement("delete from mhdfbot_marry;")) {
                        ps.executeUpdate();
                    }
                }
                try (Connection connection = dataSource.getConnection()) {
                    try (PreparedStatement ps = connection.prepareStatement("delete from mhdfbot_marryrole;")) {
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}