package setcoverCalculation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ga.ScriptTableGenerator.ScriptsTable;


public class Data {
	
	HashMap<String, List<Integer>> data; 
	List<List<String>> sets;
	int numberScripts;
	

	private final String dirPathPlayer = System.getProperty("user.dir").concat("/logs_game/logs_states/");
	
	public Data(int numberScripts) 
	{
		this.numberScripts=numberScripts;
	}
	
	public HashMap<String, List<Integer>> loadDataFromSampling() throws FileNotFoundException, IOException
	{
		data=new HashMap<String, List<Integer>>();
		List<Integer> scripts;
		File[] filesPath = new File(dirPathPlayer).listFiles();	
	    String pathPlayer=filesPath[0].getAbsolutePath()+"/player1/sampling";       	
  
		File[] files = new File(pathPlayer).listFiles();
	    for(int i=0;i<files.length;i++)
	    {
	    	String fileName=files[i].getName();
	    	try (BufferedReader br = new BufferedReader(new FileReader(files[i]))) {
	    	    String line;
	    	    int counterLine=0;
	    	    while ((line = br.readLine()) != null && counterLine<numberScripts) {
	    	       // process the line.
	    	    	Matcher m = Pattern.compile("\\<(.*?)\\>").matcher(line);
	    	        while(m.find()) {
	    	          //System.out.println(fileName+"_"+m.group(1));  
	    	        	fileName=fileName.replace(".txt","");
	    	        	scripts=data.get(fileName+"_"+m.group(1));
	    	        	if(scripts==null)
	    	        	{
	    	        		scripts=new ArrayList<Integer>();
	    	        	}
	    	        	scripts.add(counterLine);
	    	        	data.put(fileName+"_"+m.group(1), scripts);
	    	        	
	    	        }
	    	        counterLine++;
	    	    }
	    	}
	    }
	    return data;
	}
	
	public List<List<String>> loadDataFromSampling(File[] files) throws FileNotFoundException, IOException
	{
		sets=new ArrayList<List<String>>();

		//List<String> scripts;
		
	    for(int i=0;i<files.length;i++)
	    {
	    	try (BufferedReader br = new BufferedReader(new FileReader(files[i]))) {
		    	List<String> actions=new ArrayList<String>();
		    	String fileName=files[i].getName();
	    	    String line;
	    	    int counterLine=0;
	    	    while ((line = br.readLine()) != null && counterLine<300) {
	    	       // process the line.
	    	    	Matcher m = Pattern.compile("\\<(.*?)\\>").matcher(line);
	    	        while(m.find()) {
	    	          //System.out.println(fileName+"_"+m.group(1));  
	    	        	fileName=fileName.replace(".txt","");
	    	        	if(sets.size()>counterLine)
	    	        	{
	    	        		actions=sets.get(counterLine);
	    	        	}
	    	        	actions.add(fileName+"_"+m.group(1));	    	        	
	    	        }
	    	        counterLine++;
	    	        if(sets.size()<=counterLine)
    	        	{
	    	        	sets.add(actions);
    	        	}
	    	    }
	    	}
	    }
		
		return sets;
	}
	
	public void printDataMap(HashMap<String, List<Integer>> data)
	{
		//System.out.println(data.size());
		for (Map.Entry<String, List<Integer>> pair : data.entrySet()) {
			
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
	}
	
	public void printDataList(List<List<String>> data)
	{
		//int counter=0;
		for (List<String> e: data) {
		    System.out.println(e);
			//System.out.println(counter++);
		}
	}

}
