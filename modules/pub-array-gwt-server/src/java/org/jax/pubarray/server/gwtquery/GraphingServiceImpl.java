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

package org.jax.pubarray.server.gwtquery;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.jax.pubarray.gwtqueryapp.client.GraphingService;
import org.jax.pubarray.gwtqueryapp.client.ProbeIntensityGraphConfiguration;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GraphingServiceImpl
extends RemoteServiceServlet
implements GraphingService
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -7182131754589986107L;
    
    // TODO this may fail to work in some load balancing configurations
    private AtomicInteger graphCounter = new AtomicInteger();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    /**
     * {@inheritDoc}
     */
    public String buildProbeIntensityGraph(
            ProbeIntensityGraphConfiguration graphConfiguration)
    {
        String graphKey = Integer.toString(this.graphCounter.getAndIncrement());
        this.getThreadLocalRequest().getSession().setAttribute(
                graphKey,
                graphConfiguration);
        return graphKey;
    }
}
