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

package org.jax.pubarray.server.restful;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.jax.pubarray.db.PersistenceManager;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileWriter;

class QueryToCsvStreamingOutput implements StreamingOutput
{
    private final Query query;
    
    private final Connection connection;
    
    /**
     * Constructor
     * @param query
     *          the query to use
     * @param connection
     *          the connection to use
     */
    public QueryToCsvStreamingOutput(
            Query query,
            Connection connection)
    {
        this.query = query;
        this.connection = connection;
    }

    /**
     * {@inheritDoc}
     */
    public void write(OutputStream os)
    throws IOException, WebApplicationException
    {
        FlatFileWriter flatFile = new FlatFileWriter(
                new OutputStreamWriter(os),
                CommonFlatFileFormat.CSV_RFC_4180);
        
        try
        {
            // write the header
            QualifiedColumnMetadata[] terms = this.query.getTermsOfInterest();
            String[] tableHeader = new String[terms.length];
            for(int i = 0; i < tableHeader.length; i++)
            {
                tableHeader[i] = terms[i].getQualifiedName();
            }
            flatFile.writeRow(tableHeader);
            
            // write the data
            PersistenceManager persistenceManager = new PersistenceManager();
            persistenceManager.writeQueryResultsToFlatFile(
                    this.connection,
                    this.query,
                    flatFile);
        }
        catch(SQLException ex)
        {
            throw new WebApplicationException(ex);
        }
        
        flatFile.flush();
    }
}
