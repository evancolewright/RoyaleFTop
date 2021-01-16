package io.github.evancolewright.royaleftop.database;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Database
{
    @Getter
    @Setter
    private Connection connection;

    public abstract Connection openConnection() throws ClassNotFoundException, SQLException;

    public boolean checkConnection()
    {
        try
        {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException exception)
        {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Close the current DB connection
     *
     * @return  whether or not the db was closed successfully
     */
    public boolean closeConnection()
    {
        if (this.connection == null)
            return false;
        try
        {
            this.connection.close();
        } catch (SQLException exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Execute an update to the database
     *
     * @param queryString   The query to execute
     * @param closeAfter    close the connection after complete
     */
    public void executeUpdate(String queryString, boolean closeAfter)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            if (!this.checkConnection())
                this.openConnection();
            preparedStatement = this.connection.prepareStatement(queryString);
            preparedStatement.executeUpdate();

        } catch (SQLException | ClassNotFoundException exception)
        {
            exception.printStackTrace();
        } finally
        {
            try
            {
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (SQLException exception)
            {
                exception.printStackTrace();
            } finally
            {
                try
                {
                    if (closeAfter && !connection.isClosed())
                        closeConnection();
                } catch (SQLException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

}
