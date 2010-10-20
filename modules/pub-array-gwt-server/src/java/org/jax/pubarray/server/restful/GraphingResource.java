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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jax.pubarray.db.PersistenceManager;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableMetadata;
import org.jax.pubarray.gwtqueryapp.client.ProbeIntensityGraphConfiguration;
import org.jax.pubarray.gwtqueryapp.client.ProbeIntensityGraphConfiguration.GroupedGraphType;
import org.jax.pubarray.server.DatabaseServletContextListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;

/**
 * A JAX-RS implementation of a table administration resource
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
@Path("/graph-images")
public class GraphingResource
{
    private static final Logger LOG = Logger.getLogger(
            GraphingResource.class.getName());
    private static final double LOG2_FACTOR = Math.log(2);
    
    @Context
    private ServletContext context;
    
    @Context
    private HttpServletRequest request;
    
    private final PersistenceManager persistenceManager =
        new PersistenceManager();

    /**
     * Constructor
     */
    public GraphingResource()
    {
    }
    
    /**
     * A RESTful interface for getting a graph image (well, we are cheating
     * by using an image key so it probably isn't true REST)
     * @param graphImageKey the graph image key
     * @param imageWidthPixles the image width in pixles
     * @param imageHeightPixles the image height in pixles
     * @return
     *          the image response
     */
    @GET
    @Path("/probe-intensity-graph-{graphImageKey}-" +
          "{imageWidthPixles}x{imageHeightPixles}.png")
    public Response getProbeIntensityGraph(
            @PathParam("graphImageKey")     String graphImageKey,
            @PathParam("imageWidthPixles")  int imageWidthPixles,
            @PathParam("imageHeightPixles") int imageHeightPixles)
    {
        try
        {
            LOG.info("creating a graph image for key: " + graphImageKey);
            
            ProbeIntensityGraphConfiguration graphConfig =
                this.getProbeIntensityGraphConfiguration(graphImageKey);
            
            LOG.info("using graph configuration: " + graphConfig);
            
            JFreeChart graph = this.createProbeIntensityGraph(graphConfig);
            
            LOG.info(
                    "done creating graph, now rendering to an image of size: " +
                    imageWidthPixles + "x" + imageHeightPixles);
            BufferedImage bi = graph.createBufferedImage(
                    imageWidthPixles,
                    imageHeightPixles);
            StreamingPNGOutput streamingImageOutput = new StreamingPNGOutput(
                    bi);
            return Response.ok(streamingImageOutput).type("image/png").build();
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to create probe intensity image",
                    ex);
            return null;
        }
    }
    
    /**
     * Create a graph for the given configuration
     * @param graphConfiguration
     *          the key
     * @return
     *          the graph
     */
    @SuppressWarnings("unchecked")
    private JFreeChart createProbeIntensityGraph(
            ProbeIntensityGraphConfiguration graphConfiguration)
    {
        try
        {
            Connection connection = this.getConnection();
            
            String[] probeIds = graphConfiguration.getProbeIds();
            double[][] probeDataRows = new double[probeIds.length][];
            for(int i = 0; i < probeIds.length; i++)
            {
                probeDataRows[i] = this.persistenceManager.getDataRowForProbeID(
                        connection,
                        probeIds[i]);
            }
            
            TableColumnMetadata orderBy = graphConfiguration.getOrderProbesBy();
            final List<Comparable> orderByItems;
            if(orderBy != null)
            {
                LOG.info("We are ordering by: " + orderBy);
                orderByItems = this.persistenceManager.getDesignDataColumn(
                        connection,
                        orderBy);
            }
            else
            {
                orderByItems = null;
            }
            
            TableMetadata metadata =
                this.persistenceManager.getDataTableMetadata(connection);
            final CategoryDataset categoryDataset;
            if(graphConfiguration.getGroupReplicates())
            {
                switch(graphConfiguration.getGroupedGraphType())
                {
                    case BOX_PLOT:
                    {
                        categoryDataset = new DefaultBoxAndWhiskerCategoryDataset();
                    }
                    break;
                    
                    case SCATTER_PLOT:
                    {
                        categoryDataset = new DefaultMultiValueCategoryDataset();
                    }
                    break;
                    
                    default: throw new IllegalArgumentException(
                            "don't know how to deal with plot type: " +
                            graphConfiguration.getGroupedGraphType());
                }
            }
            else
            {
                categoryDataset = new DefaultCategoryDataset();
            }
            
            // iterate through all of the selected probesets
            List<QualifiedColumnMetadata> termsOfInterest =
                Arrays.asList(graphConfiguration.getTermsOfInterest());
            for(int rowIndex = 0; rowIndex < probeDataRows.length; rowIndex++)
            {
                double[] currRow = probeDataRows[rowIndex];
                assert currRow.length == metadata.getColumnMetadata().length - 1;
                
                // should we log2 transform the data?
                if(graphConfiguration.getLog2TransformData())
                {
                    for(int i = 0; i < currRow.length; i++)
                    {
                        currRow[i] = Math.log(currRow[i])/LOG2_FACTOR;
                    }
                }
                
                // iterate through the columns in the data table (each column
                // represents a different array)
                List<ComparableContainer<Double, Comparable>> rowElemList =
                    new ArrayList<ComparableContainer<Double, Comparable>>();
                for(int colIndex = 0; colIndex < currRow.length; colIndex++)
                {
                    // we use +1 indexing here because we want to skip over
                    // the probesetId metadata and get right to the
                    // array intensity metadata
                    TableColumnMetadata colMeta =
                        metadata.getColumnMetadata()[colIndex + 1];
                    
                    // check to see if we need to skip this data
                    if(!graphConfiguration.getIncludeDataFromAllArrays())
                    {
                        // if it's one of the "terms of interest" we will keep
                        // it. we're using a brute force search here
                        boolean keepThisOne = false;
                        for(QualifiedColumnMetadata termOfInterest: termsOfInterest)
                        {
                            if(termOfInterest.getTableName().equals(metadata.getTableName()) &&
                               termOfInterest.getName().equals(colMeta.getName()))
                            {
                                keepThisOne = true;
                                break;
                            }
                        }
                        
                        if(!keepThisOne)
                        {
                            continue;
                        }
                    }
                    
                    final String columnName = colMeta.getName();
                    final Comparable columnKey;
                    if(orderByItems == null)
                    {
                        columnKey = columnName;
                    }
                    else
                    {
                        // the ordering will be done on the selected
                        // "order by" design criteria
                        columnKey = new ComparableContainer<String, Comparable>(
                                columnName,
                                orderByItems.get(colIndex),
                                !graphConfiguration.getGroupReplicates());
                        
                        // TODO remove me!!!!
                        System.out.println(
                                "For array " + columnName + " the order by " +
                                "value is: " + orderByItems.get(colIndex));
                        // end of remove me
                    }
                    
                    rowElemList.add(new ComparableContainer<Double, Comparable>(
                            currRow[colIndex],
                            columnKey,
                            false));
                }
                
                Collections.sort(rowElemList);
                
                if(graphConfiguration.getGroupReplicates())
                {
                    switch(graphConfiguration.getGroupedGraphType())
                    {
                        case BOX_PLOT:
                        {
                            DefaultBoxAndWhiskerCategoryDataset dataset =
                                (DefaultBoxAndWhiskerCategoryDataset)categoryDataset;
                            for(int i = 0; i < rowElemList.size(); i++)
                            {
                                List<Double> groupList = new ArrayList<Double>();
                                groupList.add(rowElemList.get(i).getElement());
                                Comparable colKey = rowElemList.get(i).getComparable();
                                
                                i++;
                                for(; i < rowElemList.size() && rowElemList.get(i).getComparable().equals(colKey); i++)
                                {
                                    groupList.add(rowElemList.get(i).getElement());
                                }
                                i--;
                                
                                dataset.add(groupList, probeIds[rowIndex], colKey);
                            }
                        }
                        break;
                        
                        case SCATTER_PLOT:
                        {
                            DefaultMultiValueCategoryDataset dataset =
                                (DefaultMultiValueCategoryDataset)categoryDataset;
                            for(int i = 0; i < rowElemList.size(); i++)
                            {
                                List<Double> groupList = new ArrayList<Double>();
                                groupList.add(rowElemList.get(i).getElement());
                                Comparable colKey = rowElemList.get(i).getComparable();
                                
                                i++;
                                for(; i < rowElemList.size() && rowElemList.get(i).getComparable().equals(colKey); i++)
                                {
                                    groupList.add(rowElemList.get(i).getElement());
                                }
                                i--;
                                
                                dataset.add(groupList, probeIds[rowIndex], colKey);
                            }
                        }
                        break;
                    }
                }
                else
                {
                    DefaultCategoryDataset dataset =
                        (DefaultCategoryDataset)categoryDataset;
                    for(ComparableContainer<Double, Comparable> rowElem: rowElemList)
                    {
                        dataset.addValue(
                                rowElem.getElement(),
                                probeIds[rowIndex],
                                rowElem.getComparable());
                    }
                }
            }
            
            CategoryAxis xAxis = new CategoryAxis();
            if(graphConfiguration.getGroupReplicates() && orderBy != null)
            {
                xAxis.setLabel(orderBy.getName());
            }
            else
            {
                if(orderBy != null)
                {
                    xAxis.setLabel(
                            "Arrays (Ordered By " + orderBy.getName() + ")");
                }
                else
                {
                    xAxis.setLabel("Arrays");
                }
            }
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            
            final NumberAxis yAxis;
            if(graphConfiguration.getLog2TransformData())
            {
                yAxis = new NumberAxis("log2(Intensity)");
            }
            else
            {
                yAxis = new NumberAxis("Intensity");
            }
            yAxis.setAutoRange(true);
            yAxis.setAutoRangeIncludesZero(false);
            
            // TODO: this is a HACK to deal with auto-range bug in JFreeChart
            //       which occurs when doing the grouped scatter plot
            if(graphConfiguration.getGroupReplicates() &&
               graphConfiguration.getGroupedGraphType() == GroupedGraphType.SCATTER_PLOT)
            {
                double minVal = Double.POSITIVE_INFINITY;
                double maxVal = Double.NEGATIVE_INFINITY;
                for(double[] dataRow : probeDataRows)
                {
                    for(double datum: dataRow)
                    {
                        if(datum > maxVal)
                        {
                            maxVal = datum;
                        }
                        
                        if(datum < minVal)
                        {
                            minVal = datum;
                        }
                    }
                    
                    if(minVal != Double.POSITIVE_INFINITY &&
                       maxVal != Double.NEGATIVE_INFINITY &&
                       minVal != maxVal)
                    {
                        yAxis.setAutoRange(false);
                        
                        double margin = (maxVal - minVal) * 0.02;
                        Range yRange = new Range(
                                minVal - margin,
                                maxVal + margin);
                        yAxis.setRange(yRange);
                    }
                }
            }
            // END HACK
            
            final CategoryItemRenderer renderer;
            if(graphConfiguration.getGroupReplicates())
            {
                switch(graphConfiguration.getGroupedGraphType())
                {
                    case BOX_PLOT:
                    {
                        BoxAndWhiskerRenderer boxRenderer = new BoxAndWhiskerRenderer();
                        boxRenderer.setMaximumBarWidth(0.03);
                        renderer = boxRenderer;
                    }
                    break;
                    
                    case SCATTER_PLOT:
                    {
                        renderer = new ScatterRenderer();
                    }
                    break;
                    
                    default: throw new IllegalArgumentException(
                            "don't know how to deal with plot type: " +
                            graphConfiguration.getGroupedGraphType());
                }
            }
            else
            {
                renderer = new LineAndShapeRenderer();
            }
            Plot plot = new CategoryPlot(
                    categoryDataset,
                    xAxis,
                    yAxis,
                    renderer);
            
            return new JFreeChart(
                    "Intensity Values",
                    plot);
        }
        catch(SQLException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to generate image",
                    ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private class ComparableContainer<E, C extends Comparable>
    implements Comparable<ComparableContainer<E, C>>
    {
        private final E element;
        private final C comparable;
        private final boolean compareOnElementToo;
        
        public ComparableContainer(
                E element,
                C comparable,
                boolean compareOnElementToo)
        {
            this.compareOnElementToo = compareOnElementToo;
            this.element = element;
            this.comparable = comparable;
        }
        
        public E getElement()
        {
            return this.element;
        }
        
        public C getComparable()
        {
            return this.comparable;
        }
        
        /**
         * {@inheritDoc}
         */
        public int compareTo(ComparableContainer<E, C> otherCompCont)
        {
            int topComp = this.getComparable().compareTo(
                    otherCompCont.getComparable());
            if(topComp == 0 && this.compareOnElementToo)
            {
                if(this.getElement() instanceof Comparable &&
                   otherCompCont.getElement() instanceof Comparable)
                {
                    Comparable thisElemComp = (Comparable)this.getElement();
                    Comparable otherElemComp = (Comparable)otherCompCont.getElement();
                    
                    return thisElemComp.compareTo(otherElemComp);
                }
                else
                {
                    return 0;
                }
            }
            else
            {
                return topComp;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object otherObj)
        {
            if(otherObj instanceof ComparableContainer)
            {
                ComparableContainer<E, C> otherCompCont =
                    (ComparableContainer<E, C>)otherObj;
                
                if(this.compareOnElementToo)
                {
                    return this.getComparable().equals(otherCompCont.getComparable()) &&
                           this.getElement().equals(otherCompCont.getElement());
                }
                else
                {
                    return this.getComparable().equals(otherCompCont.getComparable());
                }
            }
            else
            {
                return false;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if(this.compareOnElementToo)
            {
                return this.getElement().toString();
            }
            else
            {
                return this.getComparable().toString();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return this.getComparable().hashCode();
        }
    }
    
    /**
     * Get the probe intensity graph configuration for the given key
     * @param graphImageKey
     *          the key
     * @return
     *          the configuration
     */
    private ProbeIntensityGraphConfiguration getProbeIntensityGraphConfiguration(
            String graphImageKey)
    {
        HttpSession session = this.request.getSession();
        return (ProbeIntensityGraphConfiguration)session.getAttribute(graphImageKey);
    }

    /**
     * TODO this class is also at: org/jax/haplotype/restful/StreamingImageOutput.java
     * This duplication should be resolved
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    private class StreamingPNGOutput implements StreamingOutput
    {
        private final BufferedImage image;
        
        /**
         * Constructor
         * @param image
         *          the image to stream
         */
        public StreamingPNGOutput(BufferedImage image)
        {
            this.image = image;
        }
        
        /**
         * {@inheritDoc}
         */
        public void write(OutputStream outputStream)
        throws IOException, WebApplicationException
        {
            ImageIO.write(this.image, "png", outputStream);
        }
    }

    /**
     * A helper function to get the DB connection
     * @return
     *          the DB connection
     */
    private Connection getConnection()
    {
        return (Connection)this.context.getAttribute(
                DatabaseServletContextListener.CONNECTION_ATTRIBUTE);
    }
}
