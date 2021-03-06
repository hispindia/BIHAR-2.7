package org.hisp.dhis.system.notification;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Date;

import org.hisp.dhis.common.CodeGenerator;

/**
 * @author Lars Helge Overland
 */
public class Notification
{
    private String uid;
    
    private NotificationLevel level;
    
    private NotificationCategory category;
    
    private Date time;
    
    private String message;
    
    private boolean completed;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Notification()
    {
        this.uid = CodeGenerator.generateCode();
    }

    public Notification( NotificationLevel level, NotificationCategory category, Date time, String message, boolean completed )
    {
        this.uid = CodeGenerator.generateCode();
        this.level = level;
        this.category = category;
        this.time = time;
        this.message = message;
        this.completed = completed;
    }

    // -------------------------------------------------------------------------
    // Get and set
    // -------------------------------------------------------------------------

    public NotificationLevel getLevel()
    {
        return level;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }

    public void setLevel( NotificationLevel level )
    {
        this.level = level;
    }

    public NotificationCategory getCategory()
    {
        return category;
    }

    public void setCategory( NotificationCategory category )
    {
        this.category = category;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime( Date time )
    {
        this.time = time;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public void setCompleted( boolean completed )
    {
        this.completed = completed;
    }

    // -------------------------------------------------------------------------
    // equals, hashCode, toString
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return uid.hashCode();
    }

    @Override
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        
        if ( object == null )
        {
            return false;
        }
        
        if ( getClass() != object.getClass() )
        {
            return false;
        }
        
        final Notification other = (Notification) object;
        
        return uid.equals( other.uid );
    }

    @Override
    public String toString()
    {
        return "[Level: " + level + ", category: " + category + ", time: " + time + ", message: " + message + "]";
    }
}
