package io.github.evancolewright.royaleftop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase extends Database
{
    /**
     * TODO   Add port implementation
     */

    private final String host, name, username, password, port;

    /**
     * Open a new connection to a MySQL database without a port
     *
     * @param host  the database host credential
     * @param name  the database name
     * @param username  the username login for the database
     * @param password  the password login for the database
     */
    public MySQLDatabase(String host, String name, String username, String password)
    {
        this.host = host;
        this.name = name;
        this.username = username;
        this.password = password;
        this.port = "3306";
    }

    /**
     * Open a new connection to a MySQL database with a port
     *
     * @param host  the database host credential
     * @param name  the database name
     * @param username  the username login for the database
     * @param password  the password login for the database
     * @param port  the database port
     */
    public MySQLDatabase(String host, String name, String username, String password, String port)
    {
        this.host = host;
        this.name = name;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    /**
     * Open the connection to the mysql database.
     *
     * @return  The open connection
     * @throws ClassNotFoundException  if Driver class is not found
     * @throws SQLException  if mysql database credentials are incorrect or not valid.
     */
    @Override
    public Connection openConnection() throws ClassNotFoundException, SQLException
    {
        if (this.checkConnection())
        {
            return this.getConnection();
        }
        Class.forName("com.mysql.jdbc.Driver");
        this.setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.name, this.username, this.password));
        return this.getConnection();
    }
}
