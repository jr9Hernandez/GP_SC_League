package ga.ScriptTableGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import ai.ScriptsGenerator.TableGenerator.FunctionsforGrammar;
import ga.config.ConfigurationsGA;

public class Sketch {
	static Random rand = new Random();
	ScriptsTable st=new ScriptsTable();
	String portfolioFromSetCover="";
	ArrayList<String> allBasicFunctions;
	ArrayList<String> allBasicFunctionsRedefined;
	ArrayList<String> allBooleansFunctions;
	ArrayList<String> allBooleansFunctionsRedefined;
	ArrayList<String> allBooleansMatchingTypeByCommands;
	int maxComponents;
	HashSet<String> booleansUsed;
	HashSet<String> typesUnitsinCommands=new HashSet();
	private final String pathTrackingSC = System.getProperty("user.dir").concat("/Tracking/SCcommands.txt");
	
	public Sketch()
	{
		allBasicFunctions=st.allBasicFunctions();
		allBasicFunctionsRedefined=st.allBasicFunctions();
		
		allBooleansFunctions=st.allConditionalFunctions();
		allBooleansFunctionsRedefined=st.allConditionalFunctions();
		
		maxComponents=ConfigurationsGA.MAX_QTD_COMPONENTS;
//		System.out.println(Arrays.toString(allBasicFunctions.toArray()));
//		System.out.println(Arrays.toString(allBasicFunctionsRedefined.toArray()));
	}
	public 	Sketch(String portfolioFromSetCover, HashSet<String> booleansUsed)
	{
		this.portfolioFromSetCover=portfolioFromSetCover;
		this.booleansUsed=booleansUsed;
		allBasicFunctions=st.allBasicFunctions();
		allBasicFunctionsRedefined=redefiningCommandsForScripts();
		allBooleansFunctions=st.allConditionalFunctions();
		if(ConfigurationsGA.setCoverBooleans)
		{
			allBooleansFunctionsRedefined=redefiningBooleansForScripts();
			allBooleansMatchingTypeByCommands=st.allBooleansMatchingTypeBYCommands(typesUnitsinCommands);
			System.out.println("booleanos from commands "+Arrays.toString(allBooleansMatchingTypeByCommands.toArray()));
			allBooleansFunctionsRedefined.addAll(allBooleansMatchingTypeByCommands);
		}
		else
		{
			allBooleansFunctionsRedefined=st.allConditionalFunctions();
		}
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathTrackingSC));

			System.out.println(Arrays.toString(allBasicFunctions.toArray()));
			f0.println(Arrays.toString(allBasicFunctions.toArray()));
			System.out.println(Arrays.toString(allBasicFunctionsRedefined.toArray()));
			f0.println(Arrays.toString(allBasicFunctionsRedefined.toArray()));
			System.out.println("Booleans "+allBooleansFunctions.size()+" "+allBooleansFunctionsRedefined.size());
			f0.println("Booleans "+allBooleansFunctions.size()+" "+allBooleansFunctionsRedefined.size());
			System.out.println(Arrays.toString(allBooleansFunctions.toArray()));
			f0.println(Arrays.toString(allBooleansFunctions.toArray()));
			System.out.println(Arrays.toString(allBooleansFunctionsRedefined.toArray()));
			f0.println(Arrays.toString(allBooleansFunctionsRedefined.toArray()));
			f0.flush();
			f0.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		maxComponents=ConfigurationsGA.MAX_QTD_COMPONENTS;
	}
	public String sketchA(String genotypeScript,int numberComponentsAdded) {
		
		boolean isOpenFor=false;
		//basic function
		if(rand.nextInt(2)>0 || numberComponentsAdded>=maxComponents-1)
		{
			genotypeScript=genotypeScript+" "+returnBasicFunction(isOpenFor);

		}
		else
		{
			genotypeScript=genotypeScript+" "+returnBasicFunction(isOpenFor);
			genotypeScript=sketchA(genotypeScript,numberComponentsAdded+1);
		}
		return genotypeScript;
	}
	
	public String sketchB(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;
		boolean isOpenFor=false;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		do
		{

			//basic function
			int coin=rand.nextInt(2);
			if(coin==0)
			{
				genotypeScript=genotypeScript+returnBasicFunction(isOpenFor)+" ";
				numberComponentsAdded++;
				canCloseParenthesisIf=true;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}


			}
			//conditional
			else if(coin==1 && collectionofIfs.size()==0)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+returnBoolean(isOpenFor)+" ";
				genotypeScript=genotypeScript+"(";

				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}

			}



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				//int coinOpenClose=;
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()==0)
					{
						for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

							if(collectionofIfs.get(i).isLastOpen()==false)
							{

								collectionofIfs.remove(i);

							}
							else
							{
								break;
							}
						}
					}
					canOpenParenthesisIf=true;

				}
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
				{
					genotypeScript=genotypeScript+"(";

					int counterLastIf=collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens();
					counterLastIf--;
					collectionofIfs.get(collectionofIfs.size()-1).setMaxOpens(counterLastIf);
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

					canOpenParenthesisIf=false;
					canCloseParenthesisIf=false;

					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

				}
				
			}

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1);
		
		//ensure close open parenthesis

		while(collectionofIfs.size()>0)
		{
			if(collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
			{
				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
				genotypeScript=genotypeScript+") ";
				collectionofIfs.remove(collectionofIfs.size()-1);
			}
			else
			{
				collectionofIfs.remove(collectionofIfs.size()-1);
			}

		}

		//

		return genotypeScript.trim();
	}
	
	public String sketchBLimitedSize(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;
		boolean isOpenFor=false;
		int sizeGenotypeScript=ConfigurationsGA.MAX_QTD_COMPONENTS;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		do
		{

			//basic function
			int coin=rand.nextInt(2);
			if(coin==0)
			{
				genotypeScript=genotypeScript+returnBasicFunction(isOpenFor)+" ";
				numberComponentsAdded++;
				canCloseParenthesisIf=true;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}


			}
			//conditional
			else if(coin==1 && collectionofIfs.size()==0  && numberComponentsAdded<sizeGenotypeScript-1)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+returnBoolean(isOpenFor)+" ";
				genotypeScript=genotypeScript+"(";

				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}

			}



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				//int coinOpenClose=;
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()==0)
					{
						for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

							if(collectionofIfs.get(i).isLastOpen()==false)
							{

								collectionofIfs.remove(i);

							}
							else
							{
								break;
							}
						}
					}
					canOpenParenthesisIf=true;

				}
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
				{
					genotypeScript=genotypeScript+"(";

					int counterLastIf=collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens();
					counterLastIf--;
					collectionofIfs.get(collectionofIfs.size()-1).setMaxOpens(counterLastIf);
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

					canOpenParenthesisIf=false;
					canCloseParenthesisIf=false;

					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

				}
				
			}

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1 && numberComponentsAdded<sizeGenotypeScript);
		
		//ensure close open parenthesis

		while(collectionofIfs.size()>0 )
		{
			if(collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
			{
				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
				genotypeScript=genotypeScript+") ";
				collectionofIfs.remove(collectionofIfs.size()-1);
			}
			else
			{
				collectionofIfs.remove(collectionofIfs.size()-1);
			}

		}

		//

		return genotypeScript.trim();
	}	
	

	public int counterIfsOpen(List<itemIf> collectionofIfs)
	{
		//List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int counterOpensIfs=0;
		for(itemIf item: collectionofIfs)
		{
			if(item.getType().equals("if"))
			{
				counterOpensIfs++;
			}
		}
		//System.out.println("counter "+counterOpensIfs);
		return counterOpensIfs;
	}
	
	public String sketchC(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		boolean isOpenFor=false;
		do
		{
			int coin=rand.nextInt(3);
			//for
			if(coin==0 && isOpenFor==false && collectionofIfs.size()==0)
			{
				collectionofIfs.add(new itemIf(0,true,"for"));
				genotypeScript=genotypeScript+returnForFunction();
				isOpenFor=true;
				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}
				
			}

			//basic function
			
			else if(coin==1)
			{
				genotypeScript=genotypeScript+returnBasicFunction(isOpenFor)+" ";
				numberComponentsAdded++;
				canCloseParenthesisIf=true;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}


			}
			//conditional
			
			else if(coin==2 && counterIfsOpen(collectionofIfs)==0)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+returnBoolean(isOpenFor)+" ";
				genotypeScript=genotypeScript+"(";

				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}

			}



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				int coinOpenClose=rand.nextInt(2);
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getType()=="for")
					{
						isOpenFor=false;
					}
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()==0)
					{
						for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

							if(collectionofIfs.get(i).isLastOpen()==false)
							{

								collectionofIfs.remove(i);

							}
							else
							{
								break;
							}
						}
					}
					canOpenParenthesisIf=true;

				}
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
				{
					genotypeScript=genotypeScript+"(";

					int counterLastIf=collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens();
					counterLastIf--;
					collectionofIfs.get(collectionofIfs.size()-1).setMaxOpens(counterLastIf);
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

					canOpenParenthesisIf=false;
					canCloseParenthesisIf=false;

					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

				}
				
			}

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1);
		
		//ensure close open parenthesis

		while(collectionofIfs.size()>0)
		{
			if(collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
			{
				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
				genotypeScript=genotypeScript+") ";
				collectionofIfs.remove(collectionofIfs.size()-1);
			}
			else
			{
				collectionofIfs.remove(collectionofIfs.size()-1);
			}

		}

		//

		return genotypeScript.trim();
	}
	
	public String sketchCLimitedSize(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;
		int sizeGenotypeScript=ConfigurationsGA.MAX_QTD_COMPONENTS;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		boolean isOpenFor=false;
		do
		{
			int coin=rand.nextInt(3);
			//for
			if(coin==0 && isOpenFor==false && collectionofIfs.size()==0)
			{
				collectionofIfs.add(new itemIf(0,true,"for"));
				genotypeScript=genotypeScript+returnForFunction();
				isOpenFor=true;
				//numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}
				
			}

			//basic function
			
			else if(coin==1)
			{
				genotypeScript=genotypeScript+returnBasicFunction(isOpenFor)+" ";
				numberComponentsAdded++;
				canCloseParenthesisIf=true;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}


			}
			//conditional
			
			else if(coin==2 && counterIfsOpen(collectionofIfs)==0 && numberComponentsAdded<sizeGenotypeScript-1)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+returnBoolean(isOpenFor)+" ";
				genotypeScript=genotypeScript+"(";

				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}

			}



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				int coinOpenClose=rand.nextInt(2);
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getType()=="for")
					{
						isOpenFor=false;
					}
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()==0)
					{
						for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

							if(collectionofIfs.get(i).isLastOpen()==false)
							{

								collectionofIfs.remove(i);

							}
							else
							{
								break;
							}
						}
					}
					canOpenParenthesisIf=true;

				}
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
				{
					genotypeScript=genotypeScript+"(";

					int counterLastIf=collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens();
					counterLastIf--;
					collectionofIfs.get(collectionofIfs.size()-1).setMaxOpens(counterLastIf);
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

					canOpenParenthesisIf=false;
					canCloseParenthesisIf=false;

					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

				}
				
			}

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1 && numberComponentsAdded<sizeGenotypeScript);
		
		//ensure close open parenthesis

		while(collectionofIfs.size()>0)
		{
			if(collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
			{
				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
				genotypeScript=genotypeScript+") ";
				collectionofIfs.remove(collectionofIfs.size()-1);
			}
			else
			{
				collectionofIfs.remove(collectionofIfs.size()-1);
			}

		}

		//

		return genotypeScript.trim();
	}
	
	public String returnForFunction()
	{
		String forClausule="";
		forClausule="for(u) (";
		return forClausule;
	}
	
	public ArrayList<String> redefiningCommandsForScripts()
	{
		ArrayList<String> commandsRedefined=new ArrayList<>();
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        portfolioFromSetCover = portfolioFromSetCover.replaceAll("\\s+","");
        //System.out.println("port1 "+portfolioFromSetCover);
        String[] itens = portfolioFromSetCover.replace("[", "").replace("]", "").split(",");

        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }
        
        for (Integer idSc : iScriptsAi1) {
            //System.out.println("tam tab"+scriptsTable.size());
            //System.out.println("id "+idSc+" Elems "+scriptsTable.get(BigDecimal.valueOf(idSc)));
        	commandsRedefined.add(allBasicFunctions.get(idSc));
        	
            //here I added the types of units

            if(allBasicFunctions.get(idSc).contains("Ranged"))
            	typesUnitsinCommands.add("Ranged");
            if(allBasicFunctions.get(idSc).contains("Light"))
            	typesUnitsinCommands.add("Light");
            if(allBasicFunctions.get(idSc).contains("Heavy"))
            	typesUnitsinCommands.add("Heavy");
            if(allBasicFunctions.get(idSc).contains("Worker"))
            	typesUnitsinCommands.add("Worker");
        }
        

        return commandsRedefined;
	}
	
	public ArrayList<String> redefiningBooleansForScripts()
	{
		ArrayList<String> booleansRedefined=new ArrayList<>();
		for (String subset : booleansUsed) {
			booleansRedefined.add(subset);
		}
        return booleansRedefined;
	}
	
	public String returnBasicFunction(boolean isOpenFor)
	{
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(allBasicFunctionsRedefined.size());
        String item = allBasicFunctionsRedefined.get(index);
        if(isOpenFor)
        {
        	item = item.substring(0, item.length() - 1);
        	item=item+",u)";
        }        
        return item;
	}
	
	public String returnBoolean(boolean isOpenFor)
	{
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(allBooleansFunctionsRedefined.size());
        String item = "if("+allBooleansFunctionsRedefined.get(index);
        if(isOpenFor)
        {
        	item = item.substring(0, item.length() - 1);
        	item=item+",u)";
        }
        item=item+")";
        return item;
	}
}