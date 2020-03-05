package ga.util.Evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;



import SetCoverSampling.ConfigurationsSC;
import SetCoverSampling.GameSampling;
import SetCoverSampling.IndividualFitness;
import SetCoverSampling.StateAction;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.model.Chromosome;
import ga.model.Population;
import model.EvalResult;
import rts.GameState;
import rts.PlayerAction;
import util.LeitorLog;
import util.SOA.RoundRobinClusterLeve_Cluster_GP;
import util.SOA.SOAClusterTesteLeve_Cluster_GP;

public class SetCoverEval implements RatePopulation {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 2;

	// private static final String pathSOA ="/home/rubens/cluster/ExecAIGASOA/configSOA/";
	private static final String pathSOA = System.getProperty("user.dir").concat("/configSOA/");

	// private static final String pathCentral =
	// "/home/rubens/cluster/ExecAIGASOA/centralSOA";
	private static final String pathCentral = System.getProperty("user.dir").concat("/centralSOA/");
	//private static final String pathCentral = "centralSOA/";
	
	private final String dirPathPlayer = System.getProperty("user.dir").concat("/logs_game/logs_states/");
    //private final String dirPathPlayer = "logs_game/logs_states/";
    
    private final static String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
    //private final static String pathTableScripts = "Table/";
	

	// Classes de informação
	private int atualGeneration = 0;

	// Atributos locais
	ArrayList<String> SOA_Folders = new ArrayList<>();
	ArrayList<String> SOA_arqs = new ArrayList<>();

	public SetCoverEval() {
		super();
	}

	@Override
	public Population evalPopulation(Population population, int generation, ScriptsTable scriptsTable) {
		this.atualGeneration = generation;
		SOA_Folders.clear();
		removeContents(pathCentral);
		// clean values in population
		population.clearValueChromosomes();

		// configure the individuals
		configureIndividuals(population);
		
		//Apply the fitness IMPORTANT: This task will be done by the mRTS jars when we put it in the cluster
		ArrayList<IndividualFitness> listIndividuals=applyFitness();

		// Só permite continuar a execução após terminar os JOBS.
		//controllExecute();

		// remove qualquer aquivo que não possua um vencedor
		//removeLogsEmpty();

		// clean results
		removeContents(pathCentral);
		// atualizar valores das populacoes
		updatePopulationValue(population,listIndividuals);

		return population;
	}

	private void removeContents(String path) {
		File toRemoveContent=new File(path);
		String[]entries = toRemoveContent.list();
		for(String s: entries){
		    File currentFile = new File(toRemoveContent.getPath(),s);
		    currentFile.delete();
		}
		
	}

	private ArrayList<IndividualFitness> applyFitness() {
		File folder = new File(pathCentral);
		File[] listOfFilesIndividuals = folder.listFiles();
		File[] files = new File(dirPathPlayer).listFiles();
		ArrayList<IndividualFitness> listIndividualFitness=new ArrayList<IndividualFitness>();
	    
		for (int i = 0; i < listOfFilesIndividuals.length; i++) {
			String config = getLine(pathCentral+listOfFilesIndividuals[i].getName());
			IndividualFitness indFit=new IndividualFitness(config,0);
			listIndividualFitness.add(indFit);
			//String[] parts = config.split(";");
			//System.out.println(Arrays.toString(parts));
			
			
		}
		
		presampling(files,listIndividualFitness);
        return listIndividualFitness;
		
	}
	
	public static void presampling(File[] files, ArrayList<IndividualFitness> listIndividualFitness) {
	    for (File file : files) {
	            //System.out.println("Directory: " + file.getName());
	            //sampling(file.listFiles()); // Calls same method again.
	    	
	    		//For player0 //we should interchange player here in order to avoid influence of the map side
	    		File filePlayer=new File(file.getAbsolutePath()+"/player1");

	        	samplingByFiles(filePlayer.getName(), filePlayer.listFiles(), listIndividualFitness);	        	
	    }
	}
	
	public static void samplingByFiles(String folderLeader, File[] Files, ArrayList<IndividualFitness> listIndividualFitness)
	{
		GameSampling game = new GameSampling(pathTableScripts);
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
			

			GameState gsSimulator = GameState.fromJSON(sa.getState(),game.utt);
			String []listactionsAllStates=unitActionSplitted(sa.getAction());
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
				for (int j = 0; j < listIndividualFitness.size(); j++) {
					//System.out.println("new individual");
					ArrayList<String> actionsCovered=new ArrayList<>();
					IndividualFitness ind=listIndividualFitness.get(j);
					//System.out.println("ind"+ind.getIndividual());
					String scripts=ind.getIndividual();
					String[] parts=scripts.split(";");
					
					for(int k=0; k<parts.length;k++)
					{
						PlayerAction pa= game.generateActionbyScript(gsSimulator, Integer.parseInt(parts[k]), 1);
//						System.out.println("actions script "+pa.getActions().toString());
//						System.out.println("actions state "+sa.getAction());
						
						//This fitness is calculated according the playerAction
//						if(fitnessPlayerAction(pa, sa))
//						{
//							ind.setFitness(ind.getFitness()+1);
//							break;
//						}
						
						//This fitness is calculated according the unitAction
						//System.out.println("new script");
						
						ind.setFitness(ind.getFitness()+fitnessUnitAction(pa, sa,actionsCovered));
						if(actionsCovered.size()==unitActionSplitted(sa.getAction()).length)
						{
							break;
						}
						
//						System.out.println(Arrays.toString(parts));
//						System.out.println(parts[k]);
//						System.out.println(pa.getActions().toString());						
//						try {
//							Writer writer = new FileWriter("samplings/"+folderLeader+"_state_"+stateForSampling+"_idLogs_"+pathLog+"_player_1"+".txt",true);
//							writer.write(pa.getActions().toString());
//							writer.write("\n");
//							writer.flush();
//							writer.close();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						System.gc(); 
					}

				}
			}

		}	
		System.out.println("");
		System.out.println("AllActionsAllStates "+totalActionsAllStates);
		System.out.println("");
		//print the objects 
//		for(IndividualFitness ind:listIndividualFitness)
//		{
//			System.out.println("ind "+ind.getIndividual()+" "+ind.getFitness());
//		}
	}
	
	static boolean fitnessPlayerAction(PlayerAction pa, StateAction sa) {
		return pa.getActions().toString().equals(sa.getAction());
	}
	
	static int fitnessUnitAction(PlayerAction pa, StateAction sa, ArrayList<String> actionsCovered) {
		int counterFItness=0;
		String [] unitActionsPlayerAction=  unitActionSplitted(pa.getActions().toString());
		String [] unitActionsStateAction=  unitActionSplitted(sa.getAction());

		for(String uasa:unitActionsStateAction)
		{
			//System.out.println("uasa "+uasa);
			for(String uapa:unitActionsPlayerAction)
			{	//System.out.println("uapa "+uapa);
			
				if(uapa.equals(uasa) && !actionsCovered.contains(uasa))
				{
					counterFItness++;
					actionsCovered.add(uasa);
				}
			}
		}
		return counterFItness;
	}
	
	static String [] unitActionSplitted(String toSplit){
		toSplit= toSplit.replace("[<", "");
		toSplit= toSplit.replace(">]", "");
		String[] parts = toSplit.split(">, <");
		return parts;
	}
	
	static StateAction readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		StateAction sa=new StateAction();
		try {
			String line = br.readLine();

			while (line != null) {
				sa.setState(line);
				line = br.readLine();
				sa.setAction(line);
				line = br.readLine();
			}
			return sa;
		} finally {
			br.close();
		}
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

	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	public Population updatePopulationValue(Population pop, ArrayList<IndividualFitness> listIndividuals) {

		// buscar na população a IA compatível.
		HashMap<Chromosome, BigDecimal> newChromosomes =new HashMap<Chromosome, BigDecimal>();
		for(IndividualFitness ind:listIndividuals)
		{
			
			for (Chromosome ch : pop.getChromosomes().keySet()) {
				
				Chromosome newCh=new Chromosome();
				newCh.setGenes((ArrayList<Integer>) ch.getGenes().clone());
				
				if (convertBasicTuple(newCh).equals(ind.getIndividual())) {
					//System.out.println("myfit "+convertBasicTuple(ch)+"  "+ind.getIndividual()+" "+ind.getFitness());
					BigDecimal toUpdate = BigDecimal.valueOf(ind.getFitness());
					newChromosomes.put(newCh, toUpdate);
				}
			}

		}
		pop.setChromosomes(newChromosomes);


		return pop;
	}

	private void updateChomoPopulation(EvalResult evalResult, Population pop) {

		// identicar qual IA foi a vencedora
		String IAWinner = "";
		if (evalResult.getEvaluation() == 0) {
			IAWinner = evalResult.getIA1();
		} else {
			IAWinner = evalResult.getIA2();
		}

		// buscar na população a IA compatível.
		Chromosome chrUpdate = null;
		for (Chromosome ch : pop.getChromosomes().keySet()) {
			if (convertBasicTuple(ch).equals(IAWinner)) {
				chrUpdate = ch;
			}
		}
		// atualizar valores.
		BigDecimal toUpdate = pop.getChromosomes().get(chrUpdate);
		if (toUpdate != null) {
			toUpdate = toUpdate.add(BigDecimal.ONE);
			HashMap<Chromosome, BigDecimal> chrTemp = pop.getChromosomes();
			chrTemp.put(chrUpdate, toUpdate);
		} else {
			System.out.println("Problem to find " + chrUpdate.toString());
		}
	}

	private ArrayList<EvalResult> removeDraw(ArrayList<EvalResult> results) {
		ArrayList<EvalResult> rTemp = new ArrayList<>();

		for (EvalResult evalResult : results) {
			if (evalResult.getEvaluation() != -1) {
				rTemp.add(evalResult);
			}
		}

		return rTemp;
	}

	public ArrayList<EvalResult> lerResultados() {
		LeitorLog leitor = new LeitorLog();
		ArrayList<EvalResult> resultados = leitor.processar();
		/*
		 * for (EvalResult evalResult : resultados) { evalResult.print(); }
		 */
		return resultados;
	}

	/**
	 * Verifica se os jobs já foram encerrados no cluster.
	 */
	private void controllExecute() {

		// look for clients and share the data.
		while (hasSOACentralFile()) {
			// update the quantity of SOA Clients.
			updateSOAClients();
			// update the file to process
			updateFiles();
			// share the files between SOA Clients
			shareFiles();

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while (hasSOAArq()) {
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void shareFiles() {
		for (String folder : this.SOA_Folders) {

			for (int i = 0; i < BATCH_SIZE; i++) {

				if (SOA_arqs.size() == 0) {
					return;
				}
				String nFile = SOA_arqs.get(0);
				File f = new File(nFile);
				try {
					copyFileUsingStream(f, new File(folder + "/" + f.getName()));
					SOA_arqs.remove(nFile);
					f.delete();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	private void updateFiles() {
		this.SOA_arqs.clear();
		File CentralFolder = new File(pathCentral + "/");
		for (File file : CentralFolder.listFiles()) {
			SOA_arqs.add(file.getAbsolutePath());
		}
	}

	private void updateSOAClients() {
		this.SOA_Folders.clear();
		File configSOAFolder = new File(pathSOA);
		if (configSOAFolder != null) {
			for (File folder : configSOAFolder.listFiles()) {
				if (folder.listFiles().length == 0) {
					SOA_Folders.add(folder.getAbsolutePath());
				}
			}
		}

	}

	/**
	 * irá verificar se todas as pastas SOA estão vazias
	 * 
	 * @return True se estiver vazias
	 */
	private boolean hasSOAArq() {
		updateSOACLientFull();
		for (String soaFolder : this.SOA_Folders) {
			String strConfig = soaFolder;
			File f = new File(strConfig);
			String[] children = f.list();
			if (children.length > 0) {
				return true;
			}

		}

		return false;
	}

	private void updateSOACLientFull() {
		this.SOA_Folders.clear();
		File configSOAFolder = new File(pathSOA);
		for (File folder : configSOAFolder.listFiles()) {
			SOA_Folders.add(folder.getAbsolutePath());
		}

	}

	/**
	 * Irá verificar a pasta central não tem mais arquivos.
	 * 
	 * @return
	 */
	private boolean hasSOACentralFile() {
		File centralF = new File(pathCentral);
		if (centralF.list().length > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Metódo para enviar todas as batalhas ao cluster.
	 * 
	 * @param population
	 *            Que contém as configuracoes para a IA
	 */
	private void configureIndividuals(Population population) {
		int numberSOA = 1;
		// montar a lista de batalhas que irão ocorrer



			for (Chromosome cIA1 : population.getChromosomes().keySet()) {
				
				String strConfigSOA = pathCentral + "/" + convertBasicTuple(cIA1)+ "_"+atualGeneration + ".txt";
				File arqConfig = new File(strConfigSOA);
				if (!arqConfig.exists()) {
					try {
						arqConfig.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// escreve a configuração de teste
				try {
					FileWriter arq = new FileWriter(arqConfig, false);
					PrintWriter gravarArq = new PrintWriter(arq);

					gravarArq.println(convertBasicTuple(cIA1));

					gravarArq.flush();
					gravarArq.close();
					arq.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
	}

	private String convertTuple(Chromosome cromo) {
		String tuple = "'";

		for (Integer integer : cromo.getGenes()) {
			tuple += integer + ";";
		}

		return tuple += "'";
	}

	private String convertBasicTuple(Chromosome cromo) {
		String tuple = "";

		for (Integer integer : cromo.getGenes()) {
			tuple += integer + ";";
		}

		return tuple;
	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	/**
	 * Envia o sinal de exit para todos os SOA clientes
	 */
	@Override
	public void finishProcess() {
		for (String soaFolder : this.SOA_Folders) {
			String strConfig = soaFolder;
			File f = new File(strConfig+"/exit");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}