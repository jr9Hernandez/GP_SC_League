package setcoverCalculation;

import java.util.ArrayList;
import java.util.List;


public class HillClimbing {
	
	int [][] matrixCovering ;
	String [] idsActions;

	List<Integer> setCover=new ArrayList<Integer>();
	List<Integer> actionsCovered=new ArrayList<Integer>();
	int numberScripts;
	
	public HillClimbing(int [][] matrixCovering, String [] idsActions, int numberScripts)
	{
		this.matrixCovering=matrixCovering;
		this.idsActions=idsActions;
		this.numberScripts=numberScripts;
	}
	
	public List<Integer> doHillCLimbing()
	{
		while(actionsCovered.size()<matrixCovering.length)
		{
			setCover.add(countMax());
		}
		return setCover;
	}
	
	public Integer countMax()
	{
		int idScriptMoreFreq=0;
		int mostFrequency=0;
		List<Integer> actionsCoveredMoreFreq=new ArrayList<Integer>();		
		for(int j=0;j<numberScripts;j++)
		{
			int frequency=0;
			if(!setCover.contains(j))
			{
				List<Integer> actionsCoveredTemp=new ArrayList<Integer>();
				for(int i=0;i<matrixCovering.length;i++){
				
					if(matrixCovering[i][j]>0 && !actionsCovered.contains(i))
					{
						frequency++;
						actionsCoveredTemp.add(i);
					}
				}
				if(frequency>mostFrequency)
				{
					idScriptMoreFreq=j;
					mostFrequency=frequency;
					actionsCoveredMoreFreq=actionsCoveredTemp;
				}
			}
		}
		actionsCovered.addAll(actionsCoveredMoreFreq);
		
		return idScriptMoreFreq;
	}

}
