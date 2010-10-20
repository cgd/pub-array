/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.pubarray.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A vendor agnostic interface for managing DB connections
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface ConnectionManager
{
    /**
     * Create a new connection
     * @return
     *          the connection
     * @throws SQLException
     *          if there's some problem creating the connection
     */
    public Connection createConnection() throws SQLException;
    
    /**
     * Shutdown the database driver
     * @param connection
     *          the connection to use when shutting down
     */
    public void shutdownDatabase(Connection connection);
}
