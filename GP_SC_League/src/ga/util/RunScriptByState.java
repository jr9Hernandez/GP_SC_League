package ga.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import SetCoverSampling.ConfigurationsSC;
import SetCoverSampling.GameSampling;
import SetCoverSampling.IndividualFitness;
import SetCoverSampling.StateAction;
import ai.ScriptsGenerator.Command.BasicBoolean.IfFunction;
import ai.ScriptsGenerator.CommandInterfaces.ICommand;
import ai.ScriptsGenerator.GPCompiler.ConditionalGPCompiler;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import rts.GameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.sqlLite.Log_Facade;


public class RunScriptByState {
	
	private final String dirPathPlayer = System.getProperty("user.dir").concat("/logs_game/logs_states/");
    //private final String dirPathPlayer = "logs_game/logs_states/";
    
    private final static String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
    //private final static String pathTableScripts = "Table/";
    public HashMap<String, List<Integer>> dataH=new HashMap<String, List<Integer>>();
    public HashSet<String> booleansUsed=new HashSet<>();
	public RunScriptByState()
	{
		dataH=new HashMap<String, List<Integer>>();
		ScriptsTable st=new ScriptsTable(pathTableScripts);
		booleansUsed=new HashSet<>();
		ArrayList<String> basicFunctions= st.allBasicFunctions();
		ArrayList<String> conditionalsFunctions= st.allConditionalFunctions();
//		System.out.println(Arrays.toString(conditionalsFunctions.toArray()));
//		System.out.println("size "+conditionalsFunctions.size());
		File[] files = new File(dirPathPlayer).listFiles();	
		
		presampling(files,basicFunctions,conditionalsFunctions);
		

	}

	public  void presampling(File[] files, ArrayList<String> allCommands,ArrayList<String> allConditionals) {
	    for (File file : files) {
	            //System.out.println("Directory: " + file.getName());
	            //sampling(file.listFiles()); // Calls same method again.
	    	
	    		//For player0 //we should interchange player here in order to avoid influence of the map side
	    		String pathPlayer=file.getAbsolutePath()+"/player1";
	    		File filePlayer=new File(pathPlayer);
					
	        	samplingByFiles(filePlayer.getName(), filePlayer.listFiles(), allCommands, pathPlayer,allConditionals);	        	
	    }
	}
	
	public  void samplingByFiles(String folderLeader, File[] Files, ArrayList<String> allCommands, String pathPlayer, ArrayList<String> allConditionals)
	{
		new File(pathPlayer+"/sampling").mkdirs();
		GameSampling game = new GameSampling();
		Random rand = new Random();
		int numberStatesSampled=ConfigurationsSC.NUM_STATES_SAM;
		int stateForSampling=0;

		ArrayList<String> statesforSampling = new ArrayList<>();
		StateAction sa=new StateAction();
		int totalActionsAllStates=0;
		for (int i=0;i<Files.length;i++)
		{
			//System.out.println("new state "+i);
			//System.out.println("sa "+Files[i].getName());
//			try {
//				TimeUnit.SECONDS.sleep(2);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			try {
				sa = readFile(Files[i].getPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
//			GameState gsSimulator = GameState.fromJSON(sa.getState(),game.utt);
//			String []listactionsAllStates=unitActionSplitted(sa.getAction());
//			totalActionsAllStates=totalActionsAllStates+listactionsAllStates.length;

			GameState gsSimulator = GameState.fromJSON(sa.getState(),game.utt);
			PlayerAction paOriginal=PlayerAction.fromJSON(sa.getAction(), gsSimulator, game.utt);
			HashMap<Long, String> counterByFunction = recoverCounterByFunctionSetCover(sa.getCounterByFunction());
			
			String []listactionsAllStates=unitActionSplitted(paOriginal.getActions().toString());
			totalActionsAllStates=totalActionsAllStates+listactionsAllStates.length;
			
//			if (gsSimulator.canExecuteAnyAction(0)){
//				for (int j = 0; j < ConfigurationsSC.TOTAL_SCRIPTS; j++) {
//
//
//					gsSimulator = GameState.fromJSON(statesforSampling.get(i),game.utt);
//					//System.out.println(gsSimulator.toString());
//
//					PlayerAction pa= game.generateActionbyScript(gsSimulator, j, 0);
//					try {
//						Writer writer = new FileWriter("samplings/"+folderLeader+"_state_"+stateForSampling+"_idLogs_"+pathLog+"_player_0"+".txt",true);
//						writer.write(pa.getActions().toString());
//						writer.write("\n");
//						writer.flush();
//						writer.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					System.gc(); // forço o garbage para tentar liberar memoria....
//
//
//				}
//			}

			if (gsSimulator.canExecuteAnyAction(1)){
				for (int j = 0; j < allCommands.size(); j++) {

						PlayerAction pa= game.generateActionbyScriptByString(gsSimulator,allCommands.get(j) , 1, counterByFunction);
//						System.out.println("actions script "+pa.getActions().toString());
//						System.out.println("actions state "+sa.getAction());						
//						System.out.println(Arrays.toString(parts));
//						System.out.println(parts[k]);
//						System.out.println(pa.getActions().toString());		
						fitnessUnitAction(pa, sa,j,gsSimulator,game.utt);
//						try {
//							Writer writer = new FileWriter(pathPlayer+"/sampling/"+Files[i].getName()+".txt",true);
//							writer.write(pa.getActions().toString());
//							writer.write("\n");
//							writer.flush();
//							writer.close();
//						} catch (Exception e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
//						System.gc(); 
//					
//
//				}
				}
				//For player 1
				validateIfConditionalIsTrue(allConditionals,gsSimulator,1,paOriginal,game.utt, counterByFunction);
			}

		}	
		System.out.println("");
		System.out.println("AllActionsAllStatesT "+totalActionsAllStates);
		System.out.println("");
		//print the objects 
//		for(IndividualFitness ind:listIndividualFitness)
//		{
//			System.out.println("ind "+ind.getIndividual()+" "+ind.getFitness());
//		}
	}
	
	
	public HashMap<Long, String> recoverCounterByFunctionSetCover(String counterByFunctionFromSetcover) 
	{
		HashMap<Long, String> counterByFunction =new HashMap<Long, String>();
	
		if(counterByFunctionFromSetcover!=null)
		{	
			String[] parts = counterByFunctionFromSetcover.split(";");
			for(String part: parts)
			{
				String[] actions= part.split("\\s+");
				counterByFunction.put(Long.valueOf(actions[0]).longValue(), actions[1]);
			}
		}
		return counterByFunction;
	}
	
	public void validateIfConditionalIsTrue(ArrayList<String> allConditionals,GameState g, int player,PlayerAction currentPlayerAction, UnitTypeTable utt, HashMap<Long,String> counterByFunction)
	{
		ConditionalGPCompiler conditionalCompiler = new ConditionalGPCompiler();
		for(String conditional:allConditionals)
		{
			IfFunction ifFun = new IfFunction();
			String[] fragments = conditional.split(" ");
			//first build the if and get the conditional
			//	        if (isIfInitialClause(fragments[pos])) {
			//remove the tags and get the conditional
			String sCond = fragments[0];
			if (sCond.startsWith("(if(")) {
				sCond = sCond.replace("(if(", "").trim();
			} else {
				sCond = sCond.replace("if(", "").trim();
			}
			//sCond=sCond+")";
			ifFun.setConditional(conditionalCompiler.getConditionalByCode(sCond));
			if(ifFun.getConditional().runConditional(g, player, currentPlayerAction,new AStarPathFinding(), utt,new HashMap<Long,String>(counterByFunction)))
				{
					booleansUsed.add(sCond);				
				}
			//	        }
		}
		
	}
	
	 public void fitnessUnitAction(PlayerAction pa, StateAction sa, int idScript, GameState gsSimulator, UnitTypeTable utt) {
		int counterFItness=0;
		String [] unitActionsPlayerAction=  unitActionSplitted(pa.getActions().toString());
		PlayerAction paOriginal=PlayerAction.fromJSON(sa.getAction(), gsSimulator, utt);
		String [] unitActionsStateActionArr=  unitActionSplitted(paOriginal.getActions().toString());
		List<String> saList = Arrays.asList(unitActionsStateActionArr);
		List<String> coveringCommandsList = new ArrayList<>();
		//System.out.println("actionState "+sa.getAction());
		for(String uasa:saList)
		{
			
			//System.out.println("uasa "+uasa);
			for(String uapa:unitActionsPlayerAction)
			{	//System.out.println("uapa "+uapa);
				 String [] uapaSplited=uapa.split("\\(");
				 String [] uasaSplited=uasa.split("\\(");
				 
				 String [] uapaSplitedAux=uapa.split("\\),");
				 String [] uasaSplitedAux=uasa.split("\\),");
				 
//				 System.out.println("uasatype "+uasaSplited[0]);
//				 System.out.println("uapatype "+uapaSplited[0]);
//				 
//				 System.out.println("uasaAction "+uasaSplitedAux[uasaSplitedAux.length-1]);
//				 System.out.println("uapaAction "+uapaSplitedAux[uapaSplitedAux.length-1]);
				 
			
				if(uapaSplited[0].equals(uasaSplited[0]) && uapaSplitedAux[uapaSplitedAux.length-1].equals(uasaSplitedAux[uasaSplitedAux.length-1]) && !coveringCommandsList.contains(uapa) && !uasa.contains("wait"))
				{
					//System.out.println("Enter! "+uasa);
					coveringCommandsList.add(uapa);
					if(!dataH.containsKey(sa.getNameState()+"_"+uasa))
					{
						List<Integer> CommandsCovering=new ArrayList<Integer>();
						CommandsCovering.add(idScript);
						dataH.put(sa.getNameState()+"_"+uasa, CommandsCovering);
					}
					else
					{
						List<Integer> CommandsCovering=dataH.get(sa.getNameState()+"_"+uasa);
						CommandsCovering.add(idScript);
						dataH.put(sa.getNameState()+"_"+uasa, CommandsCovering);
					}
					break;
					
				}
			}
			
		}

	}
	
	
	static StateAction readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		StateAction sa=new StateAction();
		sa.setNameState(fileName);
		try {
			String line = br.readLine();

			while (line != null) {
				sa.setState(line);
				line = br.readLine();
				sa.setAction(line);
				line = br.readLine();
				if(line != null)
				{
					sa.setCounterByFunction(line);
					line = br.readLine();
				}
			}
			return sa;
		} finally {
			br.close();
		}
	}
	
	static String [] unitActionSplitted(String toSplit){
		toSplit= toSplit.replace("[<", "");
		toSplit= toSplit.replace(">]", "");
		String[] parts = toSplit.split(">, <");
		return parts;
	}
	
    private static String getLine(String arquivo) {
        File file = new File(arquivo);
        String linha = "";
        try {
            FileReader arq = new FileReader(file);
            java.io.BufferedReader learArq = new BufferedReader(arq);
            linha = learArq.readLine();

            arq.close();
        } catch (Exception e) {
            System.err.printf("Erro na leitura da linha de configuração");
            System.out.println(e.toString());
        }
        return linha;
    }
}
