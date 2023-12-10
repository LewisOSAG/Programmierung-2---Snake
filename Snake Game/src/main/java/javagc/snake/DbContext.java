package javagc.snake;

import java.sql.Connection;

public class DbContext {


    private final Connection connection;

    public DbContext(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
