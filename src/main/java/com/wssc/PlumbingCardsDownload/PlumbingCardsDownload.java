package com.wssc.PlumbingCardsDownload;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.filenet.api.property.Properties;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;


public class PlumbingCardsDownload {
	static Logger log = Logger.getLogger(PlumbingCardsDownload.class.getName());
	
	
	public static void main(String[] args) throws IOException {
		PlumbingCardsDownload obj = new PlumbingCardsDownload();
		ObjectStore p8MigOS = CEConnection.getInstance().getObjectStore();
		obj.readDataFromSource(p8MigOS);
		//obj.getPlumbingCards();
	}
	
	public void readDataFromSource(ObjectStore objectStore) throws IOException{
		java.util.Properties props = PropertyReader.getInstance().getPropertyBag();
		String filePath = props.getProperty("filePath").trim();
		String fileName = props.getProperty("fileName").trim();
		
		String fullFileName = filePath + fileName;		
		
		String permitNo =null;
		String houseNo ="";
		String streetName = "";
		
		//String fileName = "P:\\Prod_Support\\Engineering\\PlumbingCards_Download\\ForMohan.xls";
		
		FileInputStream fileIS=new FileInputStream(new File(fullFileName));
		
		//creating workbook instance that refers to .xls file
		HSSFWorkbook workBook = new HSSFWorkbook(fileIS);
		
		//creating a Sheet object to retrieve the object  
		HSSFSheet sheet=workBook.getSheetAt(0);  
		
		//evaluating cell type   
		FormulaEvaluator formulaEvaluator=workBook.getCreationHelper().createFormulaEvaluator();  
		
		for(Row row: sheet){//iteration over row using for each loop  
			for(Cell cell: row){//iteration over cell using for each loop
				if(cell.getStringCellValue().equalsIgnoreCase("PREMISE_HNUM") || cell.getStringCellValue().equalsIgnoreCase("PREMISE_STNAME") || cell.getStringCellValue().equalsIgnoreCase("Parcelnumber"))
					continue;
				if(cell.getColumnIndex()==0 && cell.getStringCellValue().length() !=1 && !cell.getStringCellValue().isEmpty()){
					
					houseNo = cell.getStringCellValue();
					
					
				}else if(cell.getColumnIndex()==1){
					streetName = cell.getStringCellValue();
					
				}else
					continue;
			}
			
			
			if(houseNo.equalsIgnoreCase(String.valueOf(0)) || houseNo.isEmpty() || houseNo == null || houseNo.length()<1){
				continue;
			}else{
				
				getPlumbingCards(objectStore, houseNo, streetName);
			}
			
		}
		
		
	}
	
	public void getPlumbingCards(ObjectStore objStore, String houseNo, String streetName) throws IOException{
		
		log.info(houseNo+ "......"+ streetName);
		
		System.out.println("Searching for House: "+ houseNo +" and STREET NAME: "+ streetName);
	
		
		//String sqlQuery = "SELECT * FROM PlumbingCards WHERE PERMITNO = '135626' AND HouseNo='6032' AND StreetName='AVON DR'";
		//String sqlQuery = "SELECT * FROM PlumbingCards WHERE PERMITNO LIKE '%"+ permitNo+ " %' AND HouseNo = '"+ houseNo+"' AND StreetName = '"+streetName+"'";
		String sqlQuery = "SELECT * FROM PlumbingCards WHERE HouseNo = '"+ houseNo+"' AND StreetName LIKE '"+streetName+"%'";
		SearchSQL searchSQL = new SearchSQL(sqlQuery);
        SearchScope searchScope = new SearchScope(objStore);
		
        RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, new Boolean(true));
        Iterator<?> rowIter = rowSet.iterator();
        
        System.out.println("IS Search Result EMPTY?: "+rowSet.isEmpty());
        
        if(rowSet.isEmpty()){        	
        	log.info("No RESULTS found from CPE");
        }
        
        while(rowIter.hasNext()) {
        	//System.out.println("Search result: "+ ++count);
        	RepositoryRow row = (RepositoryRow) rowIter.next();
            // Access the document properties
        	Properties properties = row.getProperties();
            String documentId = properties.getIdValue("Id").toString();
            String documentName = properties.getStringValue("DocumentTitle");
            String mimeType = properties.getStringValue("MimeType");

            
            if(mimeType.equalsIgnoreCase("image/tiff") || mimeType.equalsIgnoreCase("image/tif")){
            	continue;
            }
            
            //Content
            com.filenet.api.core.Document document = (com.filenet.api.core.Document) properties.getObjectValue("This");
            
            ContentElementList contentElements = document.get_ContentElements();
            System.out.println("No of ContentElements"+ contentElements.size());
            
            Iterator iter = contentElements.iterator();
            
            //String filePath = "P:\\Prod_Support\\Engineering\\PlumbingCards_Download\\";
            String filePath = "Z:\\";
    		String fileName = document.get_Name();
    		
    		System.out.println("fileNAME is: "+ document.get_Name());
    		
    		String extension = "";
    		System.out.println("MIME TYPE: "+document.get_MimeType());
    		
    		if(document.get_MimeType().equalsIgnoreCase("application/pdf")){
    			extension=".pdf";
    		}
    		
    		String fullFileName = filePath+fileName+extension;
    		
    		try{
    			FileOutputStream fOS= new FileOutputStream(fullFileName);
    		
    		while (iter.hasNext()){
    	         ContentTransfer ct = (ContentTransfer) iter.next();
    	         
    	         
    	         if(ct.get_ContentType().equalsIgnoreCase("application/pdf")){
    	         
    	         // Print element sequence number and content type of the element.
    	         //System.out.println("\nElement Sequence number: " + ct.get_ElementSequenceNumber().intValue() + "\n" +"Retrieval Name: " +ct.get_RetrievalName() +"\n"    +"Content type: " + ct.get_ContentType() + "\n");
    	         InputStream stream = ct.accessContentStream();
    	         byte[] buffer = new byte[4096000];
    	         int bytesRead = 0;
    	         while ((bytesRead = stream.read(buffer)) != -1) {
    	          System.out.print(".");
    	          fOS.write(buffer,0,bytesRead);
    	         }
    	         //System.out.println("done!");
    	         fOS.close();
    	         stream.close();
    	         } else{
    	         log.info("NO PDF.. hence not downloading");
    	    	 System.out.println("NOT PDF.. hence not downloading");
    	     }     
            
        	}
    		}catch(Exception e){
    			
    			e.printStackTrace();
    			//log.info(e.printStackTrace());
    			log.info(e.getMessage());
    		}
		}
	}
	
	
}
	
	
	
	