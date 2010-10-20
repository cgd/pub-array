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

package org.jax.pubarray.gwtcommon.client;

import java.io.Serializable;

import org.jax.gwtutil.client.MessageType;

/**
 * A message for the user
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class UserMessage implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -553421615152650017L;

    private MessageType level;
    
    private String message;
    
    /**
     * Default constructor
     */
    public UserMessage()
    {
    }

    /**
     * @param level
     * @param message
     */
    public UserMessage(MessageType level, String message)
    {
        this.level = level;
        this.message = message;
    }
    
    /**
     * Getter for the message level
     * @return the message level
     */
    public MessageType getLevel()
    {
        return this.level;
    }
    
    /**
     * Setter for the message level
     * @param level the level
     */
    public void setLevel(MessageType level)
    {
        this.level = level;
    }
    
    /**
     * Getter for the message text
     * @return the message
     */
    public String getMessage()
    {
        return this.message;
    }
    
    /**
     * Setter for the message text
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.getLevel().toString() + ": " + this.getMessage();
    }
}
