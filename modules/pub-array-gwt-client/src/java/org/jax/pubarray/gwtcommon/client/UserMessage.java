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
