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

package org.jax.pubarray.gwtqueryapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
@RemoteServiceRelativePath("probeintensitygraphservice")
public interface GraphingServiceAsync extends RemoteService
{
    /**
     * Build a probe intensity graph image using the given configuration
     * @param graphConfiguration
     *          the configuration to use
     * @param callback
     *          callback for the key assigned by the server side
     */
    public void buildProbeIntensityGraph(
            ProbeIntensityGraphConfiguration graphConfiguration,
            AsyncCallback<String> callback);
}
