package org.anythingmc.updatechecker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public Connection connection;

    public Database() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:./data", "admin", "");
            createTables();
        } catch (SQLException error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        String createTable = "CREATE TABLE IF NOT EXISTS ";

        // SQL queries as strings
        String spigotPluginTable = createTable + "SpigotPlugin(id INT, PRIMARY KEY (id), name VARCHAR(255) NOT NULL, version INT NOT NULL)";

        // Add queries to statement
        statement.addBatch(spigotPluginTable);

        statement.executeBatch();
        connection.commit();
        statement.close();
    }
}
