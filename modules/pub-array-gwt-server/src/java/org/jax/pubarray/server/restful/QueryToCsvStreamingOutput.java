/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
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
