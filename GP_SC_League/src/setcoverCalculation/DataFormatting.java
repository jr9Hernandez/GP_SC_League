package setcoverCalculation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataFormatting {
	int [][] matrixCovering ;
	String [] idsActions;
	HashMap<String, List<Integer>> data;
	int numberScripts;
	
	public DataFormatting(HashMap<String, List<Integer>> data,int numberScripts)
	{
		this.data=data;
		this.numberScripts=numberScripts;
	}
	
	public void fillMatrix()
	{
		matrixCovering =new int [data.size()][numberScripts];
		idsActions=new String[data.size()];
		int counterIds=0;
	    
	    //Criate list of actions
	    for (Map.Entry<String, List<Integer>> pair : data.entrySet()) {
	
	        //System.out.println(pair.getKey() + " = " + pair.getValue());
	        idsActions[counterIds]=(String)pair.getKey();
	        counterIds++;
	        //System.out.println(Arrays.toString(((List<String>) pair.getValue()).toArray()));

	    }
	    
	    //Fill Matrix with zeros
	    for(int i=0;i<data.size();i++)
	    {
	    	for(int j=0;j<numberScripts;j++)
	    	{	
	    		matrixCovering[i][j]=0;
	    	}
	    }
	    
	  //Update the matrix with current actions //CHECK ORDER IDS HERE!!!
	    for(int i=0;i<data.size();i++)
	    {
	    	List<Integer> l= data.get(idsActions[i]);
			for (Integer e: l) {
			    //System.out.println(e);
			    matrixCovering[i][e]=1;
			}
	    }
	}
	
	public void printMatrix()
	{
	    for(int i=0;i<data.size();i++)
	    {
	    	for(int j=0;j<numberScripts;j++)
	    	{
	    		System.out.print(matrixCovering[i][j]+" ");
	    		
	    	}
	    	System.out.println("");
	    }
	}
	


}
