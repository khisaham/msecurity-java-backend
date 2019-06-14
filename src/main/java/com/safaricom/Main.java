package com.safaricom;

import com.safaricom.akka.ActorSystemSingletone;
import com.safaricom.commons.Log;
import com.safaricom.commons.SafHikariConnection;
import com.zaxxer.hikari.HikariConfig;

/**
 *
 */
public class Main {
    public static int HIKARI_MAX_POOL_SIZE=400;
    public static void main(String[] args) {startApp();
    }

    /** This method starts the app */
    public static void startApp() {

        Log.d("Starting app....");
        String parcelConUrl ="jdbc:mysql://35.224.120.251:3306/tumaxpress?useConfigs=maxPerformance";
        String mysqlUser = "root";
        String mysqlPassword = "@OPENsystems";
        int hikariMinPool = 0;
        int HIKARI_MAX_POOL_SIZE = 10;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(parcelConUrl);
        hikariConfig.setUsername(mysqlUser);
        hikariConfig.setPassword(mysqlPassword);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "400");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setPoolName("safcom_app");
        hikariConfig.setRegisterMbeans(true);
        hikariConfig.setMinimumIdle(hikariMinPool); //for maximum performance and responsiveness to spike demands, let HikariCP  act as a fixed size connection pool
        hikariConfig.setMaximumPoolSize(HIKARI_MAX_POOL_SIZE);

        SafHikariConnection.initialize(hikariConfig);

        //initialize the actor system
        ActorSystemSingletone.init();

    }

}
