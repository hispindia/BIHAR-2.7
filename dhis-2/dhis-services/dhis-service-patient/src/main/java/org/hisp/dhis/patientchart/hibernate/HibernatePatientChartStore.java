/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.patientchart.hibernate;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.patientchart.PatientChart;
import org.hisp.dhis.patientchart.PatientChartStore;
import org.hisp.dhis.program.Program;

/**
 * @author Chau Thu Tran
 * @version $ HibernatePatientChartStore.java Sep 5, 2011 8:32:56 AM $
 * 
 */
public class HibernatePatientChartStore
    extends HibernateGenericStore<PatientChart>
    implements PatientChartStore

{
    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<PatientChart> getPatientCharts ( Collection<Program> programs )
    {
        Criteria criteria = getCriteria();
        criteria.add( Restrictions.in( "program", programs ) );
        return criteria.list();
    }
    
    @Override
    public PatientChart getByTitle( String title )
    {
        return getObject( Restrictions.eq( "title", title ) );
    }
}
