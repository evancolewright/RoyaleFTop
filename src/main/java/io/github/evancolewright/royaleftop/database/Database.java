package io.github.evancolewright.royaleftop.database;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Database
{
    @Getter @Setter
    private Connection connection;

    public abstract Connection openConnection() throws ClassNotFoundException, SQLException;

    public boolean checkConnection() throws SQLException
    {
        return this.connection != null && !this.connection.isClosed();
    }

    public boolean closeConnection() throws SQLException
    {
        if (this.connection == null)
        {
            return false;
        }
        this.connection.close();
        return true;
    }

    public void executeUpdate(String queryString) throws SQLException, ClassNotFoundException
    {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        final PreparedStatement preparedStatement = this.connection.prepareStatement(queryString);
        preparedStatement.executeUpdate();
    }
}
