package io.graphine.test.util;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

import javax.sql.DataSource;

/**
 * @author Oleg Marchenko
 */
public final class DataSourceProvider {
    public static final DataSource DATA_SOURCE = createDataSource();
    public static final DataSource PROXY_DATA_SOURCE = createProxyDataSource();

    private static DataSource createProxyDataSource() {
        return ProxyDataSourceBuilder.create()
                                     .name("proxied-hsqldb-pool")
                                     .dataSource(DATA_SOURCE)
                                     .logQueryToSysOut()
                                     .build();
    }

    private static DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("hsqldb-pool");
        dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        dataSource.setJdbcUrl("jdbc:hsqldb:mem:graphine_test_db;sql.syntax_pgs=true");
        dataSource.setUsername("SA");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(2);
        return dataSource;
    }

    private DataSourceProvider() {
    }
}
