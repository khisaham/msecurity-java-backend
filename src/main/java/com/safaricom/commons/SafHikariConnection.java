package com.safaricom.commons;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Created by khisahamphrey
 */
public class SafHikariConnection {

    private static volatile SafHikariConnection hikariCP = null;
    private static HikariDataSource hikariDataSource = null;
    private static HikariDataSource reportsDbHikariDataSource = null;


    private SafHikariConnection(HikariConfig config) {
        hikariDataSource = new HikariDataSource(config);
    }

    public static void initialize(HikariConfig hikariConfig) {
        if (hikariCP == null) {
            synchronized (SafHikariConnection.class)
            {
                if (hikariCP == null)
                {
                    hikariCP = new SafHikariConnection (hikariConfig);
                }
            }
        }
    }

    public static SafHikariConnection getInstance() {
        return hikariCP;
    }

    public static HikariDataSource getDataSource() {
        hikariCP = getInstance();
        return hikariDataSource;
    }

    public static void connectReportsDb(HikariConfig config) {
        hikariCP = getInstance();
        reportsDbHikariDataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getReportsDbDataSource() {
        hikariCP = getInstance();
        return reportsDbHikariDataSource;
    }

    public static HikariDataSource getDataSourceReportsDb() {
        hikariCP = getInstance();
        HikariDataSource hikariDataSourceReportsDb = hikariDataSource;

        //TODO its better to actually pick it from a properties files that is is either passed in the init or whichever way that makes sense
        String mainDbConUrl = "jdbc:mysql://34.66.51.110:3306/tumaxpress?useConfigs=maxPerformance";
        String mysqlMainUser = "root";
        String mysqlMainPassword = "@OPENsystems";
        int hikariMinPool = 0;
        int hikariMaxPool = 10;

        hikariDataSourceReportsDb.setJdbcUrl(mainDbConUrl);
        hikariDataSourceReportsDb.setUsername(mysqlMainUser);
        hikariDataSourceReportsDb.setPassword(mysqlMainPassword);
        hikariDataSourceReportsDb.addDataSourceProperty("cachePrepStmts", "true");
        hikariDataSourceReportsDb.addDataSourceProperty("prepStmtCacheSize", "400");
        hikariDataSourceReportsDb.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariDataSourceReportsDb.setAutoCommit(true);
        hikariDataSourceReportsDb.setPoolName("safcom_app");
        hikariDataSourceReportsDb.setRegisterMbeans(true);
        hikariDataSourceReportsDb.setMinimumIdle(hikariMinPool); //for maximum performance and responsiveness to spike demands, let HikariCP  act as a fixed size connection pool
        hikariDataSourceReportsDb.setMaximumPoolSize(hikariMaxPool);


        return hikariDataSourceReportsDb;
    }
}
