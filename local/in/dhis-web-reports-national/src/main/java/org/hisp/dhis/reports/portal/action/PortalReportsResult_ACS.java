package org.hisp.dhis.reports.portal.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.patientdatavalue.PatientDataValueService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.reports.util.ReportService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.xwork2.Action;

public class PortalReportsResult_ACS implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private PatientService patientService;

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }
    
    private PatientIdentifierService patientIdentifierService;

    public void setPatientIdentifierService( PatientIdentifierService patientIdentifierService )
    {
        this.patientIdentifierService = patientIdentifierService;
    }

    private ProgramStageInstanceService programStageInstanceService;

    public void setProgramStageInstanceService( ProgramStageInstanceService programStageInstanceService )
    {
        this.programStageInstanceService = programStageInstanceService;
    }

    private PatientDataValueService patientDataValueService;

    public void setPatientDataValueService( PatientDataValueService patientDataValueService )
    {
        this.patientDataValueService = patientDataValueService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataElementCategoryService dataElementCategoryOptionComboService;

    public void setDataElementCategoryOptionComboService(
            DataElementCategoryService dataElementCategoryOptionComboService )
    {
        this.dataElementCategoryOptionComboService = dataElementCategoryOptionComboService;
    }
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }
    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    private String reportFileNameTB;

    public void setReportFileNameTB( String reportFileNameTB )
    {
        this.reportFileNameTB = reportFileNameTB;
    }
    
    private String reportLevelTB;

    public void setReportLevelTB( String reportLevelTB )
    {
        this.reportLevelTB = reportLevelTB;
    }
    private String reportList;

    public void setReportList( String reportList )
    {
        this.reportList = reportList;
    }
    private int ouIDTB;

    public void setOuIDTB( int ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    private String startDate;

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }
    private String endDate;

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    private String inputTemplatePath;
    private String outputReportPath;
    private String raFolderName;
    private List<String> serviceType;
    private List<String> deCodeType;
    private List<Integer> sheetList;
    private List<Integer> rowList;
    private List<Integer> colList;
    private List<Integer> progList;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
            throws Exception 
    {
        deCodeType = new ArrayList<String>();
        serviceType = new ArrayList<String>();
        sheetList = new ArrayList<Integer>();
        rowList = new ArrayList<Integer>();
        colList = new ArrayList<Integer>();
        progList = new ArrayList<Integer>();

        raFolderName = reportService.getRAFolderName();
        
        inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "template" + File.separator + reportFileNameTB;
        outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";
        
        System.out.println( "Report Generation Start Time is : \t" + new Date() );
        
        
        // query for CVD Report
        
        if( reportLevelTB.trim().equalsIgnoreCase( "1" ) )
        {
            generateCVDReport();
        }
        
        //String queryCVD = "SELECT  programstageinstanceid, programstageid,executiondate FROM programstageinstance WHERE programstageid IN ("+ reportLevelTB +")AND executiondate between '"+startDate+"' AND '"+endDate+"' ";
        
        else
        {
            generateReport();
        }
        
        System.out.println( "Report Generation End Time is : \t" + new Date() );
        
        return SUCCESS;
    }
    
    public void generateReport() throws Exception
    {
        Workbook templateWorkbook = Workbook.getWorkbook( new File( inputTemplatePath ) );
        WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new File( outputReportPath ), templateWorkbook );
        
        // Cell formatting
        WritableCellFormat wCellformat = new WritableCellFormat();
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( false );

        WritableCellFormat deWCellformat = new WritableCellFormat();
        deWCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        deWCellformat.setAlignment( Alignment.CENTRE );
        deWCellformat.setVerticalAlignment( VerticalAlignment.JUSTIFY );
        deWCellformat.setWrap( true );

        //WritableSheet sheet = outputReportWorkbook.getSheet( 0 );
        
        // OrgUnit Related Info
        OrganisationUnit selectedOrgUnit = new OrganisationUnit();
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        //System.out.println( "Report Level TB : " + reportLevelTB );
        
        // Getting Programs
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        int rowCount = 0;
        String deCodesXMLFileName = reportList + "DECodes.xml";
        List<String> deCodesList = getDECodes( deCodesXMLFileName );
        String[] programIds = reportLevelTB.split( "," );
        
        String query = "SELECT patient.patientid, patient.firstname, patient.gender, patient.birthdate, programstageinstanceid, executiondate,patient.middlename,patient.lastname,patient.registrationdate FROM programstageinstance " +
                                        " INNER JOIN programinstance ON programinstance.programinstanceid = programstageinstance.programinstanceid " +
                                        " INNER JOIN patient on programinstance.patientid = patient.patientid " +
                                        " WHERE programinstance.programid IN ("+ reportLevelTB +") " +
                                        " AND executiondate >= '"+startDate+"'" +
                                        " AND executiondate <= '"+endDate+"' "+
                                        " AND patient.organisationunitid = "+ ouIDTB +" ORDER BY executiondate" ;
        
      // System.out.println( query );
        
        SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
        if ( sqlResultSet1 != null )
        {
            
            int rowNo = 0;
            sqlResultSet1.beforeFirst();
            while ( sqlResultSet1.next() )
            {
                //System.out.println( "----HHHHH-----" );
                int patientId = sqlResultSet1.getInt( 1 );
                String temppatientId = sqlResultSet1.getString( 1 );
                String patientName = sqlResultSet1.getString( 2 );
                String patientMiddleName = sqlResultSet1.getString( 7 );
                String patientLastName = sqlResultSet1.getString( 8 );
                String patinetGender = sqlResultSet1.getString( 3 );
                Date patientBirthDate = sqlResultSet1.getDate( 4 );
                int programStageInstanceId = sqlResultSet1.getInt( 5 );
                Date executionDate = sqlResultSet1.getDate( 6 );
                Date registrationDate = sqlResultSet1.getDate( 9 );
                
                Patient patient = patientService.getPatient( patientId );
                List<PatientIdentifier> patientIdentifiers = new ArrayList<PatientIdentifier>( patientIdentifierService.getPatientIdentifiers( patient ) );
                ProgramStageInstance prgStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
                int count1 = 0;
                Iterator<String> deCodeIterator = deCodesList.iterator();
                while( deCodeIterator.hasNext() )
                {
                    String deCode = deCodeIterator.next();
                    int tempColNo = colList.get( count1 );
                    int sheetNo = sheetList.get( count1 );
                    String tempStr = "";
                    String sType = serviceType.get( count1 );
                    int tempRowNo = rowList.get( count1 );

                    if( sType.equalsIgnoreCase("slno") )
                    {
                        tempStr = "" + rowNo;
                    }
                    else if( deCode.equalsIgnoreCase("FACILITYPPPP") )
                    {
                        tempStr = selectedOrgUnit.getParent().getParent().getParent().getName();
                    }
                    else if( deCode.equalsIgnoreCase("FACILITY") )
                    {
                        tempStr = selectedOrgUnit.getName();
                    }
                    else if( deCode.equalsIgnoreCase("PERIOD-FROM") )
                    {
                        tempStr = simpleDateFormat.format( startDate );
                    }
                    else if( deCode.equalsIgnoreCase("PERIOD-TO") )
                    {
                        tempStr = simpleDateFormat.format( endDate );
                    }
                    else if( deCode.equalsIgnoreCase("PATIENTID") )
                    {
                        tempStr = temppatientId;
                    }
                    else if( deCode.equalsIgnoreCase("PATIENTNAME") )
                    {
                        tempStr = patientName + " " + patientMiddleName + " " + patientLastName;
                    }
                    else if( deCode.equalsIgnoreCase("AGE") )
                    {
                        tempStr = getAge( patientBirthDate );
                    }
                    else if( deCode.equalsIgnoreCase("DOB") )
                    {
                        tempStr = simpleDateFormat.format( patientBirthDate );
                    }
                    else if( deCode.equalsIgnoreCase("GENDER") )
                    {
                        tempStr = patinetGender;
                    }
                    else if( deCode.equalsIgnoreCase("HOSPITALNAME") )
                    {
                        tempStr = selectedOrgUnit.getName();
                    }
                    
                    else if( deCode.equalsIgnoreCase("REGDATE") )
                    {
                        tempStr = simpleDateFormat.format( registrationDate );
                    }
                    
                    else if( sType.equalsIgnoreCase("dataelement") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                                tempStr = patientDV.getValue();
                        }
                    }
                    else if( sType.equalsIgnoreCase("dataelement-dd") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                            //Integer optionComboId = Integer.parseInt( patientDV.getValue() );
                            //DataElementCategoryOptionCombo deCOC = dataElementCategoryOptionComboService.getDataElementCategoryOptionCombo( optionComboId );
                            //tempStr = deCOC.getName();
                            tempStr = patientDV.getValue();
                            //tempStr = tempStr.replace("(", "");
                            //tempStr = tempStr.replace(")", "");
                            /*
                            if ( deCode.equalsIgnoreCase( "76" ) )
                            {
                              System.out.println( sType + " : " + deCode + " : " + tempStr );
                            }
                            */
                        }
                        
                    }
                    else if( sType.equalsIgnoreCase("dataelement-b") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                                if( patientDV.getValue().equalsIgnoreCase("false") )
                                        tempStr = "no";
                                else
                                        tempStr = "yes";
                        }
                    }

                    else if( sType.equalsIgnoreCase("reasonfordelay") )
                    {
                        
                        String[] tempDe = deCode.split( ":" );
                        String dealy = tempDe[0];
                        String reason = tempDe[1];
                        
                        //System.out.println( " dealy : " + dealy + " ,reason: " + reason );
                        
                        DataElement deDealy = dataElementService.getDataElement( Integer.parseInt( dealy ) );
                        PatientDataValue patientDelayDV = patientDataValueService.getPatientDataValue( prgStageInstance, deDealy, selectedOrgUnit );
                        if( patientDelayDV != null && patientDelayDV.getValue() != null )
                        {
                            if( patientDelayDV.getValue().equalsIgnoreCase("true") )
                            {
                                DataElement deReason = dataElementService.getDataElement( Integer.parseInt( reason ) );
                                PatientDataValue patientReasonDV = patientDataValueService.getPatientDataValue( prgStageInstance, deReason, selectedOrgUnit );
                                if( patientReasonDV != null && patientReasonDV.getValue() != null )
                                {
                                 //Integer optionComboId = Integer.parseInt( patientReasonDV.getValue() );
                                 // DataElementCategoryOptionCombo deCOC = dataElementCategoryOptionComboService.getDataElementCategoryOptionCombo( optionComboId );
                                 //tempStr = deCOC.getName();
                                    tempStr = patientReasonDV.getValue();
                                    //tempStr = tempStr.replace("(", "");
                                    //tempStr = tempStr.replace(")", "");
                                //System.out.println( " Reason -- : -- " + tempStr );
                                }
                            }
                            else
                            {
                                tempStr = "";
                            }
                        }
                    }
                    else if( sType.equalsIgnoreCase("BMI") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String weightDE = "";
                        String heightDE = "";
                        String tempStrWeight = "";
                        String tempStrHeight = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            weightDE = tempDe[0];
                            heightDE = tempDe[1];
                            //System.out.println( " weight -- : -- " + weightDE );
                            //System.out.println( " height -- : -- " + heightDE );
                        }
                        
                        DataElement deWeight = dataElementService.getDataElement( Integer.parseInt( weightDE ) );
                        PatientDataValue patientWeightDV = patientDataValueService.getPatientDataValue( prgStageInstance, deWeight, selectedOrgUnit );
                        if( patientWeightDV != null && patientWeightDV.getValue() != null )
                        {
                            tempStrWeight = patientWeightDV.getValue();
                        }
                        
                        DataElement deHeight = dataElementService.getDataElement( Integer.parseInt( heightDE ) );
                        PatientDataValue patientHeightDV = patientDataValueService.getPatientDataValue( prgStageInstance, deHeight, selectedOrgUnit );
                        if( patientHeightDV != null && patientHeightDV.getValue() != null )
                        {
                            tempStrHeight = patientHeightDV.getValue();
                        }
                        
                        if (  tempStrWeight != null && !tempStrWeight.trim().equalsIgnoreCase( "" ) &&  tempStrHeight != null && !tempStrHeight.trim().equalsIgnoreCase( "" )  )
                        {
                            //System.out.println( " Weight : " + tempStrWeight + " ,Height: " + tempStrHeight );
                            
                            double w = Double.parseDouble( tempStrWeight );
                            double h = Double.parseDouble( tempStrHeight );
                            
                            //Integer w = Integer.parseInt( weight );
                            //Integer h = Integer.parseInt( height );
                            //double tempBMI = Math.round(w/(h*h)* 100 )/100;
                            double tempBMI = Math.round(w/(h*h)* Math.pow( 10, 1 ))/ Math.pow( 10, 1 );

                            //dataStatusPercentatge = Math.round( dataStatusPercentatge * Math.pow( 10, 0 ) ) / Math.pow( 10, 0 );
                            
                            String bmi = Double.toString( tempBMI );
                            
                            tempStr = bmi;
                        }
                        else
                        {
                            tempStr = "";
                            
                        }

                        //tempStr = getBMI( weight, height);
                    }
                    
                    // Code for Hospital STAY
                    
                    else if( sType.equalsIgnoreCase("hstay") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String startDE = "";
                        String endDE = "";
                        String tempStartDate = "";
                        String tempEndDate = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            startDE = tempDe[0];
                            endDE = tempDe[1];
                        }
                        
                        DataElement deStart = dataElementService.getDataElement( Integer.parseInt( startDE ) );
                        PatientDataValue patientStartDV = patientDataValueService.getPatientDataValue( prgStageInstance, deStart, selectedOrgUnit );
                        if( patientStartDV != null && patientStartDV.getValue() != null )
                        {
                            tempStartDate = patientStartDV.getValue();
                        }
                        
                        DataElement deEnd = dataElementService.getDataElement( Integer.parseInt( endDE ) );
                        PatientDataValue patientEndDV = patientDataValueService.getPatientDataValue( prgStageInstance, deEnd, selectedOrgUnit );
                        if( patientEndDV != null && patientEndDV.getValue() != null )
                        {
                            tempEndDate = patientEndDV.getValue();
                        }
                        
                        if (  tempStartDate != null && !tempStartDate.trim().equalsIgnoreCase( "" ) &&  tempEndDate != null && !tempEndDate.trim().equalsIgnoreCase( "" )  )
                        {
                            //System.out.println( " Start Date : " + tempStartDate + " ,End Date: " + tempEndDate );
                            
                            
                            //simpleDateFormat.format( registrationDate );
                            
                            //Date startDate = simpleDateFormat.format( tempStartDate );
                            
                            Date sDate = format.parseDate( tempStartDate );
                            Date eDate = format.parseDate( tempEndDate );
                            
                            //String tempFromDate = simpleDateFormat.format( sDate );
                            //String tempToDate = simpleDateFormat.format( eDate );
                            
                            
                            Calendar tempSDate = Calendar.getInstance();
                            Calendar tempEDate = Calendar.getInstance();

                            tempSDate.setTime( sDate );
                            tempEDate.setTime( eDate );
                            
                           // int age = tempEDate.get( Calendar.DATE ) - tempSDate.get( Calendar.DATE );
                            
                            //tempStr = "" + age ;
                            
                            
                            
                            //Calendar c1 = Calendar.getInstance();
                            //Calendar c2 = Calendar.getInstance();
                              
                                
                             long milis1 = tempSDate.getTimeInMillis();
                             long milis2 = tempEDate.getTimeInMillis();

                             long d = milis2 - milis1;
                              
                             //long dSeconds = diff / 1000;
                              
                      
                            // long dM = d / (60 * 1000);
                            
                             //long dH = d / (60 * 60 * 1000);

                             long dD = d / (24 * 60 * 60 * 1000);
                              
                             //System.out.println("In milliseconds: " + d + " milliseconds.");
                             //System.out.println("In seconds: " + dS + " seconds.");
                             //System.out.println("In minutes: " + dM + " minutes.");
                             //System.out.println("In hours: " + dH + " hours.");
                             //System.out.println("In days: " + dD + " days.");

                            
                             tempStr = "" + dD ;
                            
                        }
                        else
                        {
                            tempStr = "";
                            
                        }
                        //System.out.println( " Hospital Stay  : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                    
                    
                    // Date and time of presentation of patient to hospital
                    
                    else if( sType.equalsIgnoreCase("datetime") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String pdate = "";
                        String pTime = "";
                        String tempPDate = "";
                        String tempPTime = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            pdate = tempDe[0];
                            pTime = tempDe[1];
                        }
                        
                        DataElement dePDate = dataElementService.getDataElement( Integer.parseInt( pdate ) );
                        PatientDataValue patientPdateDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePDate, selectedOrgUnit );
                        if( patientPdateDV != null && patientPdateDV.getValue() != null )
                        {
                            tempPDate = patientPdateDV.getValue();
                        }
                        
                        DataElement dePTime = dataElementService.getDataElement( Integer.parseInt( pTime ) );
                        PatientDataValue patientPTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePTime, selectedOrgUnit );
                        if( patientPTimeDV != null && patientPTimeDV.getValue() != null )
                        {
                            tempPTime = patientPTimeDV.getValue();
                        }
                        
                        if (  tempPDate == null && tempPDate.trim().equalsIgnoreCase( "" ) &&  tempPTime == null && tempPTime.trim().equalsIgnoreCase( "" )  )
                        {
                            tempStr = "";
                        }
                        
                        /*
                        if (  tempPDate != null && !tempPDate.trim().equalsIgnoreCase( "" ) &&  tempPTime != null && !tempPTime.trim().equalsIgnoreCase( "" )  )
                        {
                            //System.out.println( " Start Date : " + tempPDate + " ,End Date: " + tempPTime );
                            tempPTime = tempPTime.replace( ":", "");
                            
                            if( tempPTime.length() < 4 )
                            {
                                tempPTime = "0" + tempPTime;
                            }
                            
                            tempStr = tempPDate + "," + tempPTime ;
                        }
                        */
                        else
                        {
                            
                            if( tempPTime != null && !tempPTime.trim().equalsIgnoreCase( "" ) )
                            {
                                tempPTime = tempPTime.replace( ":", "");
                                
                                if( tempPTime.length() < 4 )
                                {
                                    tempPTime = "0" + tempPTime;
                                }
                            }
                            
                            tempStr = tempPDate + "," + tempPTime ;
                            
                        }
                        //System.out.println( " Date and Time is   : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                    
                    // Code for Date & Time of Symptom 
                    
                    else if( sType.equalsIgnoreCase("datetimesymptom") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String sydate = "";
                        String syTime = "";
                        String tempsyDate = "";
                        String tempsyTime = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            sydate = tempDe[0];
                            syTime = tempDe[1];
                        }
                        
                        DataElement desyDate = dataElementService.getDataElement( Integer.parseInt( sydate ) );
                        PatientDataValue patientsydateDV = patientDataValueService.getPatientDataValue( prgStageInstance, desyDate, selectedOrgUnit );
                        if( patientsydateDV != null && patientsydateDV.getValue() != null )
                        {
                            tempsyDate = patientsydateDV.getValue();
                        }
                        
                        DataElement desyTime = dataElementService.getDataElement( Integer.parseInt( syTime ) );
                        PatientDataValue patientsyTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, desyTime, selectedOrgUnit );
                        if( patientsyTimeDV != null && patientsyTimeDV.getValue() != null )
                        {
                            tempsyTime = patientsyTimeDV.getValue();
                        }
                        
                        if (  tempsyDate == null && tempsyDate.trim().equalsIgnoreCase( "" ) &&  tempsyTime == null && tempsyTime.trim().equalsIgnoreCase( "" )  )
                        {
                            tempStr = "";
                        }
                        
                        /*
                        
                        if (  tempsyDate != null && !tempsyDate.trim().equalsIgnoreCase( "" ) &&  tempsyTime != null && !tempsyTime.trim().equalsIgnoreCase( "" )  )
                        {
                            //System.out.println( " Start Date : " + tempPDate + " ,End Date: " + tempPTime );
                            tempsyTime = tempsyTime.replace( ":", "");
                            
                            if( tempsyTime.length() < 4 )
                            {
                                tempsyTime = "0" + tempsyTime;
                            }
                            
                            tempStr = tempsyDate + "," + tempsyTime ;
                        }
                        */
                        else
                        {
                            if( tempsyTime != null && !tempsyTime.trim().equalsIgnoreCase( "" ) )
                            {
                                tempsyTime = tempsyTime.replace( ":", "");
                                if( tempsyTime.length() < 4 )
                                {
                                    tempsyTime = "0" + tempsyTime;
                                }
                            }
                            tempStr = tempsyDate + "," + tempsyTime ;
                            
                        }
                        //System.out.println( " Date and Time is   : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                    
    
                    // Date & Time of admission in CCU
                    
                    else if( sType.equalsIgnoreCase("datetimeccu") )
                    {
                        String[] tempCCUDe = deCode.split( ":" );
                        String ccuDate = "";
                        String ccuTime = "";
                        String tempCCUDate = "";
                        String tempCCUTime = "";
                        for ( int i = 0 ; i < tempCCUDe.length ; i++ )
                        {
                            ccuDate = tempCCUDe[0];
                            ccuTime = tempCCUDe[1];
                        }
                        
                        DataElement deCCUDate = dataElementService.getDataElement( Integer.parseInt( ccuDate ) );
                        PatientDataValue patientsydateDV = patientDataValueService.getPatientDataValue( prgStageInstance, deCCUDate, selectedOrgUnit );
                        if( patientsydateDV != null && patientsydateDV.getValue() != null )
                        {
                            tempCCUDate = patientsydateDV.getValue();
                        }
                        
                        DataElement deCCUTime = dataElementService.getDataElement( Integer.parseInt( ccuTime ) );
                        PatientDataValue patientsyTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, deCCUTime, selectedOrgUnit );
                        if( patientsyTimeDV != null && patientsyTimeDV.getValue() != null )
                        {
                            tempCCUTime = patientsyTimeDV.getValue();
                        }
                        
                        if (  tempCCUDate == null && tempCCUDate.trim().equalsIgnoreCase( "" ) &&  tempCCUTime == null && tempCCUTime.trim().equalsIgnoreCase( "" )  )
                        {
                            tempStr = "";
                        }
                        
                        else
                        {
                            if( tempCCUTime != null && !tempCCUTime.trim().equalsIgnoreCase( "" ) )
                            {
                                tempCCUTime = tempCCUTime.replace( ":", "");
                                if( tempCCUTime.length() < 4 )
                                {
                                    tempCCUTime = "0" + tempCCUTime;
                                }
                            }
                            tempStr = tempCCUDate + "," + tempCCUTime ;
                            
                        }
                    }
                    // Time lapsed from reporting to Emergency department to CCU where Treatment of ACS Started
                    else if( sType.equalsIgnoreCase("datetimediffccu") )
                    {
                        String[] tempCCUDiffDe = deCode.split( ":" );
                        String preHopDate = "";
                        String preHopTime = "";
                        String ccuDate = "";
                        String ccuTime = "";
                        String tempPreHopDate = "";
                        String tempPreHopTime = "";
                        String tempCCUDate = "";
                        String tempCCUTime = "";
                        for ( int i = 0 ; i < tempCCUDiffDe.length ; i++ )
                        {
                            preHopDate = tempCCUDiffDe[0];
                            preHopTime = tempCCUDiffDe[1];
                            ccuDate = tempCCUDiffDe[2];
                            ccuTime = tempCCUDiffDe[3];
                            
                        }
                        
                        DataElement dePreHopDate = dataElementService.getDataElement( Integer.parseInt( preHopDate ) );
                        PatientDataValue patientPreHopdateDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePreHopDate, selectedOrgUnit );
                        if( patientPreHopdateDV != null && patientPreHopdateDV.getValue() != null )
                        {
                            tempPreHopDate = patientPreHopdateDV.getValue();
                        }
                        
                        
                        DataElement dePreHopTime = dataElementService.getDataElement( Integer.parseInt( preHopTime ) );
                        PatientDataValue patientPreHopTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePreHopTime, selectedOrgUnit );
                        if( patientPreHopTimeDV != null && patientPreHopTimeDV.getValue() != null )
                        {
                            tempPreHopTime = patientPreHopTimeDV.getValue();
                        }
                        
                        DataElement deCCUDate = dataElementService.getDataElement( Integer.parseInt( ccuDate ) );
                        PatientDataValue patientCCUDateDV = patientDataValueService.getPatientDataValue( prgStageInstance, deCCUDate, selectedOrgUnit );
                        if( patientCCUDateDV != null && patientCCUDateDV.getValue() != null )
                        {
                            tempCCUDate = patientCCUDateDV.getValue();
                        }
                        
                        
                        
                        DataElement deCCUTime = dataElementService.getDataElement( Integer.parseInt( ccuTime ) );
                        PatientDataValue patientCCUTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, deCCUTime, selectedOrgUnit );
                        if( patientCCUTimeDV != null && patientCCUTimeDV.getValue() != null )
                        {
                            tempCCUTime = patientCCUTimeDV.getValue();
                        }
                        
                        
                        if (  tempPreHopDate != null && !tempPreHopDate.trim().equalsIgnoreCase( "" ) &&  tempPreHopTime != null && !tempPreHopTime.trim().equalsIgnoreCase( "" ) && tempCCUDate != null && !tempCCUDate.trim().equalsIgnoreCase( "" ) &&  tempCCUTime != null && !tempCCUTime.trim().equalsIgnoreCase( "" ) )
                        {
                            //System.out.println( " Acs Date : " + tempAcsDate + " ,Acs Time: " + tempAcsTime +  " Pre Date : " + tempPreDate + " ,Pre Time: " + tempPreTime );
                                                        
                            Date sDate = format.parseDate( tempPreHopDate );
                            Date eDate = format.parseDate( tempCCUDate );
                            
                            //String tempFromDate = simpleDateFormat.format( sDate );
                            //String tempToDate = simpleDateFormat.format( eDate );
                            
                            
                            Calendar tempSDate = Calendar.getInstance();
                            Calendar tempEDate = Calendar.getInstance();
                            
                            tempSDate.setTime( sDate );
                            tempEDate.setTime( eDate );

                            
                            long millisecondsStart = tempSDate.getTimeInMillis();
                            long millisecondsEnd = tempEDate.getTimeInMillis();
                            long tempDiffinSecond = millisecondsEnd - millisecondsStart;
                            
                            
                            
                            String[] tempPreHopTIME = tempPreHopTime.split( ":" );
                            String[] tempCCUTIME = tempCCUTime.split( ":" );
                            
                            String tempPreHopHour = tempPreHopTIME[0];
                            String tempPreHopMin = tempPreHopTIME[1];
                            
                            String tempCCUHour = tempCCUTIME[0];
                            String tempCCUMin = tempCCUTIME[1];
                            
                            long diffInSecond = (Long.parseLong( tempCCUHour)*3600 + Long.parseLong(tempCCUMin)*60) - (Long.parseLong( tempPreHopHour)*3600 + Long.parseLong(tempPreHopMin)*60);
                            
                            long finalDiffInMilisecond = tempDiffinSecond + diffInSecond*1000;
                            
                            /*
                            long daysDifference = finalDiffInMilisecond/1000/60/60/24;
                            finalDiffInMilisecond -= daysDifference*1000*60*60*24;
                            */ 
                            long hoursDifference = finalDiffInMilisecond/1000/60/60;
                            finalDiffInMilisecond -= hoursDifference*1000*60*60;
                     
                            long minutesDifference = finalDiffInMilisecond/1000/60;
                            finalDiffInMilisecond -= minutesDifference*1000*60;
                     
                            long secondsDifference = finalDiffInMilisecond/1000;
                            
                            //String tm_lapsed = Long.toString( daysDifference ) + ":" + Long.toString( hoursDifference ) + ":" + Long.toString( minutesDifference );
                            
                            String tm_lapsed =  Long.toString( hoursDifference ) + ":" + Long.toString( minutesDifference );
                            
                            tempStr = tm_lapsed;
                        }
                        else
                        {
                            tempStr = "";
                            
                        }
                        //System.out.println( " Total diff in time   : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                                        
                    // Date and Time of Thrombolysis 
                    
                    else if( sType.equalsIgnoreCase("datetimett") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String ttdate = "";
                        String ttTime = "";
                        String tempttDate = "";
                        String tempttTime = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            ttdate = tempDe[0];
                            ttTime = tempDe[1];
                        }
                        
                        DataElement dettDate = dataElementService.getDataElement( Integer.parseInt( ttdate ) );
                        PatientDataValue patientttdateDV = patientDataValueService.getPatientDataValue( prgStageInstance, dettDate, selectedOrgUnit );
                        if( patientttdateDV != null && patientttdateDV.getValue() != null )
                        {
                            tempttDate = patientttdateDV.getValue();
                        }
                        
                        DataElement dettTime = dataElementService.getDataElement( Integer.parseInt( ttTime ) );
                        PatientDataValue patientttTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, dettTime, selectedOrgUnit );
                        if( patientttTimeDV != null && patientttTimeDV.getValue() != null )
                        {
                            tempttTime = patientttTimeDV.getValue();
                        }
                        
                        if (  tempttDate != null && !tempttDate.trim().equalsIgnoreCase( "" ) &&  tempttTime != null && !tempttTime.trim().equalsIgnoreCase( "" )  )
                        {
                            //System.out.println( " Start Date : " + tempPDate + " ,End Date: " + tempPTime );
                            tempttTime = tempttTime.replace( ":", "");
                            
                            if( tempttTime.length() < 4 )
                            {
                                tempttTime = "0" + tempttTime;
                            }
                            
                            tempStr = tempttDate + "," + tempttTime ;
                        }
                        else
                        {
                            tempStr = "";
                            
                        }
                        //System.out.println( " Date and Time is   : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                    
                    
                    // Code for Date and time Diffrence
                    
                    else if( sType.equalsIgnoreCase("datetimediff") )
                    {
                        String[] tempDe = deCode.split( ":" );
                        String acsDate = "";
                        String acsTime = "";
                        String preDate = "";
                        String preTime = "";
                        String tempAcsDate = "";
                        String tempAcsTime = "";
                        String tempPreDate = "";
                        String tempPreTime = "";
                        for ( int i = 0 ; i < tempDe.length ; i++ )
                        {
                            acsDate = tempDe[0];
                            acsTime = tempDe[1];
                            preDate = tempDe[2];
                            preTime = tempDe[3];
                            
                        }
                        
                        DataElement deAcsDate = dataElementService.getDataElement( Integer.parseInt( acsDate ) );
                        PatientDataValue patientAcsdateDV = patientDataValueService.getPatientDataValue( prgStageInstance, deAcsDate, selectedOrgUnit );
                        if( patientAcsdateDV != null && patientAcsdateDV.getValue() != null )
                        {
                            tempAcsDate = patientAcsdateDV.getValue();
                        }
                        
                        
                        DataElement deAcsTime = dataElementService.getDataElement( Integer.parseInt( acsTime ) );
                        PatientDataValue patientAcsTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, deAcsTime, selectedOrgUnit );
                        if( patientAcsTimeDV != null && patientAcsTimeDV.getValue() != null )
                        {
                            tempAcsTime = patientAcsTimeDV.getValue();
                        }
                        
                        DataElement dePreDate = dataElementService.getDataElement( Integer.parseInt( preDate ) );
                        PatientDataValue patientPredateDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePreDate, selectedOrgUnit );
                        if( patientPredateDV != null && patientPredateDV.getValue() != null )
                        {
                            tempPreDate = patientPredateDV.getValue();
                        }
                        
                        DataElement dePreTime = dataElementService.getDataElement( Integer.parseInt( preTime ) );
                        PatientDataValue patientPreTimeDV = patientDataValueService.getPatientDataValue( prgStageInstance, dePreTime, selectedOrgUnit );
                        if( patientPreTimeDV != null && patientPreTimeDV.getValue() != null )
                        {
                            tempPreTime = patientPreTimeDV.getValue();
                        }
                        
                        
                        if (  tempAcsDate != null && !tempAcsDate.trim().equalsIgnoreCase( "" ) &&  tempAcsTime != null && !tempAcsTime.trim().equalsIgnoreCase( "" ) && tempPreDate != null && !tempPreDate.trim().equalsIgnoreCase( "" ) &&  tempPreTime != null && !tempPreTime.trim().equalsIgnoreCase( "" ) )
                        {
                            //System.out.println( " Acs Date : " + tempAcsDate + " ,Acs Time: " + tempAcsTime +  " Pre Date : " + tempPreDate + " ,Pre Time: " + tempPreTime );
                                                        
                            Date sDate = format.parseDate( tempAcsDate );
                            Date eDate = format.parseDate( tempPreDate );
                            
                            //String tempFromDate = simpleDateFormat.format( sDate );
                            //String tempToDate = simpleDateFormat.format( eDate );
                            
                            
                            Calendar tempSDate = Calendar.getInstance();
                            Calendar tempEDate = Calendar.getInstance();
                            
                            tempSDate.setTime( sDate );
                            tempEDate.setTime( eDate );

                            
                            long millisecondsStart = tempSDate.getTimeInMillis();
                            long millisecondsEnd = tempEDate.getTimeInMillis();
                            long tempDiffinSecond = millisecondsEnd - millisecondsStart;
                            
                            
                            
                            String[] tempACSTIME = tempAcsTime.split( ":" );
                            String[] tempPRETIME = tempPreTime.split( ":" );
                            
                            String tempACSHour = tempACSTIME[0];
                            String tempACSMin = tempACSTIME[1];
                            
                            String tempPREHour = tempPRETIME[0];
                            String tempPREMin = tempPRETIME[1];
                            
                            long diffInSecond = (Long.parseLong( tempPREHour)*3600 + Long.parseLong(tempPREMin)*60) - (Long.parseLong( tempACSHour)*3600 + Long.parseLong(tempACSMin)*60);
                            
                            long finalDiffInMilisecond = tempDiffinSecond + diffInSecond*1000;
                            
                            /*
                            long daysDifference = finalDiffInMilisecond/1000/60/60/24;
                            finalDiffInMilisecond -= daysDifference*1000*60*60*24;
                            */ 
                            long hoursDifference = finalDiffInMilisecond/1000/60/60;
                            finalDiffInMilisecond -= hoursDifference*1000*60*60;
                     
                            long minutesDifference = finalDiffInMilisecond/1000/60;
                            finalDiffInMilisecond -= minutesDifference*1000*60;
                     
                            long secondsDifference = finalDiffInMilisecond/1000;
                            
                            //String tm_lapsed = Long.toString( daysDifference ) + ":" + Long.toString( hoursDifference ) + ":" + Long.toString( minutesDifference );
                            
                            String tm_lapsed =  Long.toString( hoursDifference ) + ":" + Long.toString( minutesDifference );
                            
                            tempStr = tm_lapsed;
                        }
                        else
                        {
                            tempStr = "";
                            
                        }
                        //System.out.println( " Total diff in time   : " + tempStr );
                        
                        //tempStr = getBMI( weight, height);
                    }
                    // System.out.println( sType + " : " + deCode + " : " + tempStr );
                    //System.out.println( sType + " : " + tempStr );
                    
                    tempRowNo =  rowNo+1;
                    WritableSheet sheet = outputReportWorkbook.getSheet( sheetNo );
                    try
                    {
                        //double tempDouble = Double.parseDouble( tempStr );
                        sheet.addCell( new Number( tempColNo, tempRowNo, Double.parseDouble( tempStr ), wCellformat ) );
                    }
                    catch( Exception e )
                    {
                        sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, wCellformat ) );
                    }
                    
                    count1++;
                }
                
                rowNo++;
            }
        }

        outputReportWorkbook.write();
        outputReportWorkbook.close();
        //fileName = reportFileNameTB.replace( ".xls", "" );
        //fileName += "_" + selectedOrgUnit.getShortName() + ".xls";
        fileName = "ACS Report.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        outputReportFile.deleteOnExit();
    }
    

    public void generateCVDReport() throws Exception
    {
        Workbook templateWorkbook = Workbook.getWorkbook( new File( inputTemplatePath ) );
        WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new File( outputReportPath ), templateWorkbook );
        
        // Cell formatting
        WritableCellFormat wCellformat = new WritableCellFormat();
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( false );

        WritableCellFormat deWCellformat = new WritableCellFormat();
        deWCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        deWCellformat.setAlignment( Alignment.CENTRE );
        deWCellformat.setVerticalAlignment( VerticalAlignment.JUSTIFY );
        deWCellformat.setWrap( true );

        //WritableSheet sheet = outputReportWorkbook.getSheet( 0 );
        
        // OrgUnit Related Info
        OrganisationUnit selectedOrgUnit = new OrganisationUnit();
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        //System.out.println( "Report Level TB : " + reportLevelTB );
        
        // Getting Programs
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        int rowCount = 0;
        String deCodesXMLFileName = reportList + "DECodes.xml";
        List<String> deCodesList = getDECodes( deCodesXMLFileName );
        String[] programIds = reportLevelTB.split( "," );
        
        //String queryCVD = "SELECT  programstageinstanceid,programstageid,executiondate FROM programstageinstance WHERE programstageid IN ("+ reportLevelTB +")AND executiondate between '"+startDate+"' AND '"+endDate+"' ";
        String queryCVD = "SELECT  programstageinstanceid,executiondate FROM programstageinstance WHERE programstageid IN ("+ reportLevelTB +") AND executiondate >= '"+startDate+"' AND executiondate <='"+endDate+"' ORDER BY executiondate ";
        //System.out.println( queryCVD );
        
        SqlRowSet sqlResultSetCVD = jdbcTemplate.queryForRowSet( queryCVD );
        if ( sqlResultSetCVD != null )
        {
            int rowNo = 0;
            sqlResultSetCVD.beforeFirst();
            while ( sqlResultSetCVD.next() )
            {
                int cvdProgramStageInstanceId = sqlResultSetCVD.getInt( 1 );
                Date executionDate = sqlResultSetCVD.getDate( 2 );
                ProgramStageInstance prgStageInstance = programStageInstanceService.getProgramStageInstance( cvdProgramStageInstanceId );
                int count1 = 0;
                //System.out.println( " CVD ProgramStage Instance Id " + cvdProgramStageInstanceId );
                Iterator<String> deCodeIterator = deCodesList.iterator();
                while( deCodeIterator.hasNext() )
                {
                    String deCode = deCodeIterator.next();
                    int tempColNo = colList.get( count1 );
                    int sheetNo = sheetList.get( count1 );
                    String tempStr = "";
                    String sType = serviceType.get( count1 );
                    int tempRowNo = rowList.get( count1 );
                    
                    if( sType.equalsIgnoreCase("slno") )
                    {
                        tempStr = "" + rowNo;
                    }
                    else if( deCode.equalsIgnoreCase("EXECUTIONDATE") )
                    {
                        tempStr = simpleDateFormat.format( executionDate );
                    }

                    
                    else if( sType.equalsIgnoreCase("dataelement") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                                tempStr = patientDV.getValue();
                        }
                    }
                    else if( sType.equalsIgnoreCase("dataelement-dd") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                            //Integer optionComboId = Integer.parseInt( patientDV.getValue() );
                            //DataElementCategoryOptionCombo deCOC = dataElementCategoryOptionComboService.getDataElementCategoryOptionCombo( optionComboId );
                            //tempStr = deCOC.getName();
                            tempStr = patientDV.getValue();
                        }
                    }
                    else if( sType.equalsIgnoreCase("dataelement-b") )
                    {
                        DataElement de = dataElementService.getDataElement( Integer.parseInt( deCode ) );
                        PatientDataValue patientDV = patientDataValueService.getPatientDataValue( prgStageInstance, de, selectedOrgUnit );
                        if( patientDV != null && patientDV.getValue() != null )
                        {
                                if( patientDV.getValue().equalsIgnoreCase("false") )
                                        tempStr = "no";
                                else
                                        tempStr = "yes";
                        }
                    }

                   // System.out.println( sType + " : " + deCode + " : " + tempStr );
                    //System.out.println( sType + " : " + tempStr );
                    tempRowNo =  rowNo+1;  
                    WritableSheet sheet = outputReportWorkbook.getSheet( sheetNo );
                    try
                    {
                        //double tempDouble = Double.parseDouble( tempStr );
                        sheet.addCell( new Number( tempColNo, tempRowNo, Double.parseDouble( tempStr ), wCellformat ) );
                    }
                    catch( Exception e )
                    {
                        sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, wCellformat ) );
                    }
                    
                    count1++;
                }
                
                rowNo++;
            }
        }

        outputReportWorkbook.write();
        outputReportWorkbook.close();
        //fileName = reportFileNameTB.replace( ".xls", "" );
        //fileName += "_" + selectedOrgUnit.getShortName() + ".xls";
        fileName = "CVD Report.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        outputReportFile.deleteOnExit();
    }

    
    
    public String getAge( Date birthDate )
    {
        if ( birthDate == null )
        {
            return "0";
        }

        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime( birthDate );

        Calendar todayCalendar = Calendar.getInstance();

        int age = todayCalendar.get( Calendar.YEAR ) - birthCalendar.get( Calendar.YEAR );

        if ( todayCalendar.get( Calendar.MONTH ) < birthCalendar.get( Calendar.MONTH ) )
        {
            age--;
        }
        else if ( todayCalendar.get( Calendar.MONTH ) == birthCalendar.get( Calendar.MONTH )
            && todayCalendar.get( Calendar.DAY_OF_MONTH ) < birthCalendar.get( Calendar.DAY_OF_MONTH ) )
        {
            age--;
        }

        if ( age < 1 )
        {
            //return "( < 1 yr )";
            return "< 1 yr";
        }
        else
        {
            //return "( " + age + " yr )";
            return "" + age + " yr";
        }
    }
    
    public List<String> getDECodes( String fileName )
    {
        List<String> deCodes = new ArrayList<String>();
        String path = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + fileName;

        try 
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
            int totalDEcodes = listOfDECodes.getLength();

            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = ( Element ) listOfDECodes.item( s );
                NodeList textDECodeList = deCodeElement.getChildNodes();
                deCodes.add( ( ( Node ) textDECodeList.item( 0 ) ).getNodeValue().trim() );
                serviceType.add( deCodeElement.getAttribute( "stype" ) );
                deCodeType.add( deCodeElement.getAttribute( "type" ) );
                sheetList.add( new Integer( deCodeElement.getAttribute( "sheetno" ) ) );
                rowList.add( new Integer( deCodeElement.getAttribute( "rowno" ) ) );
                colList.add( new Integer( deCodeElement.getAttribute( "colno" ) ) );
                progList.add( new Integer( deCodeElement.getAttribute( "progno" ) ) );
            }// end of for loop with s var

        }// try block end
        catch ( SAXParseException err )
        {
        } 
        catch ( SAXException e )
        {
            Exception x = e.getException();
            ( ( x == null ) ? e : x ).printStackTrace();
        } 
        catch ( Throwable t )
        {
            t.printStackTrace();
        }

        return deCodes;
    }
}
