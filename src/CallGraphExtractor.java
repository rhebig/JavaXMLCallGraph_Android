/*
 * author: Regina Hebig
 * created: August 2017
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;


public class CallGraphExtractor {

	
	public static ArrayList<String> xmlfiles; 
	public static ArrayList<String> codefiles;
	public static ArrayList<String> pngfiles;
	public static ArrayList<String> pnginjavasearchstrings;
	public static ArrayList<String> xmlinjavasearchstrings;
	public static ArrayList<String> xmlinxmlsearchstrings;
	public static ArrayList<String> pnginxmlsearchstrings;
	
	//path to your working folder (determines where output is stored)
	public static String startpath = ".../";
	
	//path to your project (choose the folder that contains the java and res sub-folders)
	public static String pathtoproject = startpath + "OctoPrint Original/OctoPrint_show_load_and_compile/PrinterApp/app/src/";
	
	public static void main(String[] args) {
		try{
			xmlfiles = ArrayList.class.newInstance();
			codefiles = ArrayList.class.newInstance();
			pngfiles = ArrayList.class.newInstance();
			pnginjavasearchstrings = ArrayList.class.newInstance();
			xmlinjavasearchstrings = ArrayList.class.newInstance();
			xmlinxmlsearchstrings = ArrayList.class.newInstance();
			pnginxmlsearchstrings = ArrayList.class.newInstance();
					
			try{
				//hard-coded: path to the file containing the list of files to be analyzed
				String path = startpath + "Analysis OctoPrintApp/ListOfFiles.txt"; 
				//collect those links that are .xmi files
				BufferedReader in = new BufferedReader(new FileReader(path));
		        String zeile = null;
		     
		        while ((zeile = in.readLine()) != null) {
		        	//System.out.println("a");
		           if(zeile.endsWith(".xml")){
		        		xmlfiles.add(zeile);
		        	}	
		           //System.out.println("b");
		           if(zeile.endsWith(".java")){
		        	    codefiles.add(zeile);
		        	}	
		           //System.out.println("b");
		           if(zeile.endsWith(".png")){
		        	    pngfiles.add(zeile);
		        	}
		        }

		        in.close();
		        
		        //Create Search Strings
		        createXMLSearchStrings();
		        createPNGSearchStrings();
		        
		        logGraph("digraph CallGraph {","Call");
		        
		        //DO: iterate .javas and search for usage of .xmls and .pngs
		        Iterator<String> javafiles = codefiles.iterator();
		        while(javafiles.hasNext()){
		        	String filename = javafiles.next();
		        	analyzeJavaForCalls(filename);
		        //	System.out.println(filename);
		        }
		        //DO: iterate .xmls and search for usages of .xmls and .pngs
		        Iterator<String> xmlit = xmlfiles.iterator();
		        while(xmlit.hasNext()){
		        	String filename = xmlit.next();
		        //	System.out.println(filename);
		        	analyzeXMLForCalls(filename);
		        }	        
		        
		        logGraph("}","Call");
		        
			}catch(Exception e){System.out.println("Excpetion 1 : " + e);}		
			
			
		}catch(InstantiationException e1) {
			e1.printStackTrace();
		}catch(IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
	}

	public static void createXMLSearchStrings(){
		
		Iterator<String> it = xmlfiles.iterator();
		while(it.hasNext()){
		//"res/"
			String xmlfile = it.next();
			//System.out.println(xmlfile);
			String[] substrings = xmlfile.split("res/"); //ToDO: check whether our string is "/" and not "\"
			Integer i = substrings.length;
			String target = substrings[i-1];
			if (target != null){
			
				String target1 = target.replace(".xml","");
				xmlinxmlsearchstrings.add("@" + target1);	
			
				String target2 = target1.replace("/", "."); //ToDO: check whether our string is "/" and not "\"
				xmlinjavasearchstrings.add("R." + target2);
				//System.out.println("@" + target1);
				//System.out.println("R." + target2);
			}else{
				System.out.println("could not create search string for " + xmlfile);
			}
			
		}
		System.out.println("done with xmls");
	}
	
	public static void createPNGSearchStrings(){
		Iterator<String> it = pngfiles.iterator();
		while(it.hasNext()){
		// /"res/" --> assumes that system is split to java folder (for java) and res folder (for xml and pngs)  
			String pngfile = it.next();
			String[] substrings = pngfile.split("res/"); 
			Integer i = substrings.length;
			String target = substrings[i-1];
			if (target != null){
			
				String target1 = target.replace(".png","");
				pnginxmlsearchstrings.add("@" + target1);	
			
				String target2 = target1.replace("/", "."); 
				pnginjavasearchstrings.add("R." + target2);
				
			}else{
				System.out.println("could not create search string for " + pngfile);
			}
			
		}
		System.out.println("done with pngs");
	
	}
	
	
	public static String convertBackToFilepathXMLinJava(String searchstring){
		String result = searchstring.replace("R.", ""); 
		result = result.replace(".", "/"); //
		
		return "res/" + result + ".xml";
	}
	
	public static String convertBackToFilepathPNGinJava(String searchstring){
		String result = searchstring.replace("R.", ""); 
		result = result.replace(".", "/"); //
		
		return "res/" + result + ".png";
	}
	
	public static String convertBackToFilepathXMLinXML(String searchstring){
		String result = searchstring.replace("@", ""); 
		
		return "res/" + result + ".xml";
	}
	
	public static String convertBackToFilepathPNGinXML(String searchstring){
		String result = searchstring.replace("@", ""); 
		
		return "res/" + result + ".png";
	}
	
	
	
	public static void analyzeJavaForCalls(String filepath){
	   //open file
	   try{
		   
			   String path = pathtoproject + filepath;
			   System.out.println(path);
				
			   BufferedReader in = new BufferedReader(new FileReader(path));
		       String zeile = null;
		      
		       ArrayList<String> foundInFile = ArrayList.class.newInstance();
		       
		        
		       while ((zeile = in.readLine()) != null) {
		    	   
		    	   System.out.println("XML in Java");
		    	   Iterator<String> tosearch = xmlinjavasearchstrings.iterator();
		    	   while(tosearch.hasNext()){
		    		   String name = tosearch.next();
			    	   if(zeile.contains(name)){
			    		   Boolean hasbeenfound = false;			    		   
			    		   Iterator<String> foundIt = foundInFile.iterator();
			    		   while(foundIt.hasNext()){
			    			   String found = foundIt.next();
			    			   if(name.equals(found)){
			    				   hasbeenfound = true;
			    			   }
			    		   }

			    		   if(!hasbeenfound){
			    			   log(filepath + " " + convertBackToFilepathXMLinJava(name), "Java_Uses_XML");
			    			   logGraph("\"" + filepath + "\"" + " -> "  + "\"" + convertBackToFilepathXMLinJava(name)  + "\"","Call");
			    			   foundInFile.add(name);
			    		   }
			    	   }
		    	   }
			    	   System.out.println("PNG in Java");
			    	   tosearch = pnginjavasearchstrings.iterator();
			    	   while(tosearch.hasNext()){
			    		   String name = tosearch.next();
				    	   if(zeile.contains(name)){
				    		   Boolean hasbeenfound = false;			    		   
				    		   Iterator<String> foundIt = foundInFile.iterator();
				    		   while(foundIt.hasNext()){
				    			   String found = foundIt.next();
				    			   if(name.equals(found)){
				    				   hasbeenfound = true;
				    			   }
				    		   }

				    		   if(!hasbeenfound){
				    			   log(filepath + " " + convertBackToFilepathPNGinJava(name), "Java_Uses_PNG");
				    			   logGraph("\"" + filepath + "\"" + " -> "  + "\"" + convertBackToFilepathPNGinJava(name)  + "\"","Call");
				    			   foundInFile.add(name);
				    		   }
				    	   }
			    	   }
		    	   }
				   
	
		       in.close(); 
		   
	   }catch(Exception e){System.out.println("Excpetion 2 : " + e);}		
		
	   
   }
	
	public static void analyzeXMLForCalls(String filepath){
		   //open file
		   try{
			   
				   String path = pathtoproject + filepath;
				   System.out.println(path);
					
				   BufferedReader in = new BufferedReader(new FileReader(path));
			       String zeile = null;
			      
			       ArrayList<String> foundInFile = ArrayList.class.newInstance();
			       
			        
			       while ((zeile = in.readLine()) != null) {
			    	   
			    	   System.out.println("XML in XML");
			    	   Iterator<String> tosearch = xmlinxmlsearchstrings.iterator();
			    	   while(tosearch.hasNext()){
			    		   String name = tosearch.next();
				    	   if(zeile.contains(name)){
				    		   Boolean hasbeenfound = false;			    		   
				    		   Iterator<String> foundIt = foundInFile.iterator();
				    		   while(foundIt.hasNext()){
				    			   String found = foundIt.next();
				    			   if(name.equals(found)){
				    				   hasbeenfound = true;
				    			   }
				    		   }

				    		   if(!hasbeenfound){
				    			   log(filepath + " " + convertBackToFilepathXMLinXML(name), "XML_Uses_XML");
				    			   logGraph("\"" + filepath + "\"" + " -> "  + "\"" + convertBackToFilepathXMLinXML(name)  + "\"","Call");
				    			   foundInFile.add(name);
				    		   }
				    	   }
			    	   }
			    		   System.out.println("PNG in XML");
				    	   tosearch = pnginxmlsearchstrings.iterator();
				    	   while(tosearch.hasNext()){
				    		   String name = tosearch.next();
					    	   if(zeile.contains(name)){
					    		   Boolean hasbeenfound = false;			    		   
					    		   Iterator<String> foundIt = foundInFile.iterator();
					    		   while(foundIt.hasNext()){
					    			   String found = foundIt.next();
					    			   if(name.equals(found)){
					    				   hasbeenfound = true;
					    			   }
					    		   }

					    		   if(!hasbeenfound){
					    			   log(filepath + " " + convertBackToFilepathPNGinXML(name), "XML_Uses_PNG");
					    			   logGraph("\"" + filepath + "\"" + " -> "  + "\"" + convertBackToFilepathPNGinXML(name)  + "\"","Call");
					    			   foundInFile.add(name);
					    		   }
					    	   }
				    	   }
			    	   }
					   
			       in.close(); 
			   
		   }catch(Exception e){System.out.println("Excpetion 2 : " + e);}		
			
		   
	   }
	
	
	
	/*
	 * Helper method to write the log files including the resulting link lists
	 */
	public static void log(String message, String filename) { 
		try{
	      PrintWriter out = new PrintWriter(new FileWriter(startpath + "Analysis/" +"OutputCallGraph/" + filename +".txt", true), true);
	      out.println(message);
	      out.close();
	    }catch(Exception e){}
	}	

	
	public static void logGraph(String message, String filename) {
		try{
		      PrintWriter out = new PrintWriter(new FileWriter(startpath + "Analysis/" +"OutputCallGraph/" + filename +"Graph.txt", true), true);
		      out.println(message);
		      out.close();
		    }catch(Exception e){}
	}
	

}