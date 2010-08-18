///*
// * Copyright (c) 2008 The Jackson Laboratory
// * 
// * This software was developed by Gary Churchill's Lab at The Jackson
// * Laboratory (see http://research.jax.org/faculty/churchill).
// *
// * This is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this software.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package org.jax.pubarray.restful;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.servlet.ServletContext;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//
//import org.jax.pubarray.db.CandidateDatabaseManager;
//import org.jax.pubarray.gwtadminapp.client.FileFormat;
//import org.jax.util.io.CommonFlatFileFormat;
//import org.jax.util.io.FlatFileFormat;
//import org.jax.util.io.FlatFileReader;
//
//import com.sun.jersey.multipart.FormDataMultiPart;
//
///**
// * A JAX-RS implementation of a table administration resource
// * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
// */
//@Path("/")
//public class TableAdministrationResource
//{
//    private static final Logger LOG = Logger.getLogger(
//            TableAdministrationResource.class.getName());
//    
//    @Context
//    private ServletContext context;
//    
//    /**
//     * Constructor
//     */
//    public TableAdministrationResource()
//    {
//    }
//    
//    /**
//     * Getter for the candidate database
//     * @return the candidate database
//     */
//    public CandidateDatabaseManager getCandidateDatabase()
//    {
//        return (CandidateDatabaseManager)this.context.getAttribute(
//                CandidateDatabaseManager.class.getName());
//    }
//    
//    /**
//     * For uploading a candidate design file
//     * NOTE: MIME type is HTML instead of text/plain for reasons documented
//     * in the javadoc for GWT's FormPanel class
//     * @param formData
//     *          the form data to upload
//     * @return
//     *          if everything works we return a {@link Status#OK} with the given
//     *          file name. if we see a problem with the input we return a
//     *          {@link Status#BAD_REQUEST}
//     */
//    @Path("/candidate-design")
//    @POST
//    @Consumes("multipart/form-data")
//    @Produces("text/html")
//    public Response uploadDesignFile(
//            FormDataMultiPart formData)
//    {
//        try
//        {
//            String designFileFormat = formData.getField("fileFormat").getValue();
//            String designFileName = formData.getField("fileName").getValue();
//            InputStream designFile = formData.getField("file").getValueAs(
//                    InputStream.class);
//            
//            FileFormat ff = FileFormat.valueOf(designFileFormat);
//            final FlatFileFormat flatFileFormat;
//            switch(ff)
//            {
//                case CSV_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.CSV_RFC_4180;
//                }
//                break;
//
//                case TAB_DELIMITED_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.TAB_DELIMITED_UNIX;
//                }
//                break;
//
//                default: throw new IllegalArgumentException(
//                            "Bad file format string: " + designFileFormat);
//            }
//            
//            FlatFileReader ffr = new FlatFileReader(
//                    new InputStreamReader(designFile),
//                    flatFileFormat);
//            this.getCandidateDatabase().uploadDesignFile(
//                    designFileName,
//                    ffr);
//            
//            return Response.ok(designFileName).build();
//        }
//        catch(Exception ex)
//        {
//            // TODO need to get this message back to the user in the for of
//            // an error message of some sort!
//            LOG.log(Level.SEVERE,
//                    "failed to upload design file",
//                    ex);
//            return Response.status(Status.BAD_REQUEST).build();
//        }
//    }
//    
//    /**
//     * For uploading a candidate data file
//     * NOTE: MIME type is HTML instead of text/plain for reasons documented
//     * in the javadoc for GWT's FormPanel class
//     * @param formData
//     *          the form data to upload
//     * @return
//     *          if everything works we return a {@link Status#OK} with the given
//     *          file name. if we see a problem with the input we return a
//     *          {@link Status#BAD_REQUEST}
//     */
//    @Path("/candidate-data")
//    @POST
//    @Consumes("multipart/form-data")
//    @Produces("text/html")
//    public Response uploadDataFile(
//            FormDataMultiPart formData)
//    {
//        try
//        {
//            String dataFileFormat = formData.getField("fileFormat").getValue();
//            String dataFileName = formData.getField("fileName").getValue();
//            InputStream dataFile = formData.getField("file").getValueAs(
//                    InputStream.class);
//            
//            FileFormat ff = FileFormat.valueOf(dataFileFormat);
//            final FlatFileFormat flatFileFormat;
//            switch(ff)
//            {
//                case CSV_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.CSV_RFC_4180;
//                }
//                break;
//
//                case TAB_DELIMITED_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.TAB_DELIMITED_UNIX;
//                }
//                break;
//
//                default: throw new IllegalArgumentException(
//                            "Bad file format string: " + dataFileFormat);
//            }
//            
//            FlatFileReader ffr = new FlatFileReader(
//                    new InputStreamReader(dataFile),
//                    flatFileFormat);
//            this.getCandidateDatabase().uploadDataFile(dataFileName, ffr);
//            
//            return Response.ok(dataFileName).build();
//        }
//        catch(Exception ex)
//        {
//            LOG.log(Level.SEVERE,
//                    "failed to upload data file",
//                    ex);
//            return Response.status(Status.BAD_REQUEST).build();
//        }
//    }
//    
//    /**
//     * For uploading a candidate annotation files
//     * NOTE: MIME type is HTML instead of text/plain for reasons documented
//     * in the javadoc for GWT's FormPanel class
//     * @param formData
//     *          the form data to upload
//     * @return
//     *          if everything works we return a {@link Status#OK} with the given
//     *          file name. if we see a problem with the input we return a
//     *          {@link Status#BAD_REQUEST}
//     */
//    @Path("/candidate-annotations")
//    @POST
//    @Consumes("multipart/form-data")
//    @Produces("text/html")
//    public Response uploadAnnotationFile(
//            FormDataMultiPart formData)
//    {
//        try
//        {
//            String fileFormat = formData.getField("fileFormat").getValue();
//            String fileName = formData.getField("fileName").getValue();
//            InputStream file = formData.getField("file").getValueAs(
//                    InputStream.class);
//            
//            FileFormat ff = FileFormat.valueOf(fileFormat);
//            final FlatFileFormat flatFileFormat;
//            switch(ff)
//            {
//                case CSV_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.CSV_RFC_4180;
//                }
//                break;
//
//                case TAB_DELIMITED_FORMAT:
//                {
//                    flatFileFormat = CommonFlatFileFormat.TAB_DELIMITED_UNIX;
//                }
//                break;
//
//                default: throw new IllegalArgumentException(
//                            "Bad file format string: " + fileFormat);
//            }
//            
//            FlatFileReader ffr = new FlatFileReader(
//                    new InputStreamReader(file),
//                    flatFileFormat);
//            this.getCandidateDatabase().uploadAnnotationFile(
//                    fileName,
//                    ffr);
//            
//            return Response.ok(fileName).build();
//        }
//        catch(Exception ex)
//        {
//            LOG.log(Level.SEVERE,
//                    "failed to upload annotation file",
//                    ex);
//            return Response.status(Status.BAD_REQUEST).build();
//        }
//    }
//}
