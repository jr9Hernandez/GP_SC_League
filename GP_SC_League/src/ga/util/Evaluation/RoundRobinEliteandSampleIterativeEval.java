package ga.util.Evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import ai.core.AI;

import java.util.Random;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.PreSelection;
import model.EvalResult;
import rts.units.UnitTypeTable;
import util.LeitorLog;

public class RoundRobinEliteandSampleIterativeEval implements RatePopulation {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 1;

	//private static final String pathSOA = "/home/rubens/cluster/TesteNewGASG/configSOA/";
	private static final String pathSOA = System.getProperty("user.dir").concat("/configSOA/");

	//private static final String pathCentral = "/home/rubens/cluster/TesteNewGASG/centralSOA";
	private static final String pathCentral = System.getProperty("user.dir").concat("/centralSOA");
	
	private static final String pathLogsGrammars = System.getProperty("user.dir").concat("/LogsGrammars/");
	
	private static final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");

	// Classes de informação
	private int atualGeneration = 0;

	// Atributos locais
	ArrayList<String> SOA_Folders = new ArrayList<>();
	ArrayList<String> SOA_arqs = new ArrayList<>();

	ArrayList<Chromosome> ChromosomeSample = new ArrayList<>();
	
	private HashMap<BigDecimal, String> scriptsTable;
	int maxLinesLogsGrammar=100000;
	int counterLinesLogsGrammar=0;
	HashMap<Chromosome, BigDecimal> eliteIndividuals;

	public RoundRobinEliteandSampleIterativeEval() {
		super();
	}

	@Override
	public Population evalPopulation(Population population, int generation, ScriptsTable scriptsTable) {
		//recordMarkNewGeneration();
		buildScriptsTable();
		this.atualGeneration = generation;
		SOA_Folders.clear();
		// limpa os valores existentes na population
		population.clearValueChromosomes();

		// executa os confrontos
		runBattles(population);

		// Só permite continuar a execução após terminar os JOBS.
		//controllExecute();
		iterativeControll(population);

		// remove qualquer aquivo que não possua um vencedor
		removeLogsEmpty();

		// ler resultados
		ArrayList<EvalResult> resultados = lerResultados();
		
		// atualizar valores das populacoes
		if(resultados.size() > 0){
			
			updatePopulationValue(resultados, population);
		}

		return population;
	}
	
    public HashMap<BigDecimal, String> buildScriptsTable() {
        scriptsTable = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.substring(line.indexOf(" "), line.length());
                String[] strArray = line.split(" ");
                int idScript = Integer.decode(strArray[0]);
                scriptsTable.put(BigDecimal.valueOf(idScript), code);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return scriptsTable;
    }

	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	public Population updatePopulationValue(ArrayList<EvalResult> results, Population pop) {
		//ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		ArrayList<EvalResult> resultsNoDraw = results;
		/*
		 * System.out.println("Avaliações sem Draw"); for (EvalResult evalResult
		 * : resultsNoDraw) { evalResult.print(); }
		 */
		
		for (EvalResult evalResult : resultsNoDraw) {
			updateChomoPopulation(evalResult, pop);
		}

		return pop;
	}

	private void updateChomoPopulation(EvalResult evalResult, Population pop) {
		
		if (evalResult.getEvaluation() == 0) {
            //IAWinner = evalResult.getIA1();
            updateChromo(pop, evalResult.getIA1(), BigDecimal.ONE);
        } else if (evalResult.getEvaluation() == 1){
            updateChromo(pop, evalResult.getIA2(), BigDecimal.ONE);
        }else{
            updateChromo(pop, evalResult.getIA1(), new BigDecimal(0.5));
            updateChromo(pop, evalResult.getIA2(), new BigDecimal(0.5));
        }
		
		if(counterLinesLogsGrammar<maxLinesLogsGrammar)
		{
			String portfolioGrammar0=buildCompleteGrammar(convertBasicTupleToInteger(evalResult.getIA1()));
			//System.out.println("portfolio0 "+portfolioGrammar0);
	     
			String portfolioGrammar1=buildCompleteGrammar(convertBasicTupleToInteger(evalResult.getIA2()));
			//System.out.println("portfolio1 "+portfolioGrammar1);
	     
			counterLinesLogsGrammar++;
	     
			portfolioGrammar0=portfolioGrammar0.substring(0, portfolioGrammar0.length() - 1);
			portfolioGrammar1=portfolioGrammar1.substring(0, portfolioGrammar1.length() - 1);
	    
			recordGrammars(Integer.toString(evalResult.getEvaluation()), portfolioGrammar0, portfolioGrammar1);
		}
        
    }
	
    private void recordGrammars(String winner, String portfolioGrammar0, String portfolioGrammar1) {
		
    	try(FileWriter fw = new FileWriter(pathLogsGrammars+"LogsGrammars.txt", true);
    		    BufferedWriter bw = new BufferedWriter(fw);
    		    PrintWriter out = new PrintWriter(bw))
    		{
    		    out.println(portfolioGrammar0+"/"+portfolioGrammar1+"="+winner);
    		} catch (IOException e) {
    		    //exception handling left as an exercise for the reader
    		}
		
	}
    
    private void recordMarkNewGeneration() {
		
    	try(FileWriter fw = new FileWriter(pathLogsGrammars+"LogsGrammars.txt", true);
    		    BufferedWriter bw = new BufferedWriter(fw);
    		    PrintWriter out = new PrintWriter(bw))
    		{
    		    out.println("New Generation!");
    		} catch (IOException e) {
    		    //exception handling left as an exercise for the reader
    		}
		
	}
	
    public String buildCompleteGrammar(ArrayList<Integer> iScripts) {
        List<AI> scriptsAI = new ArrayList<>();
        String portfolioGrammar="";

        for (Integer idSc : iScripts) {
        	portfolioGrammar=portfolioGrammar+scriptsTable.get(BigDecimal.valueOf(idSc))+";";
        }

        return portfolioGrammar;
    }

    private void updateChromo(Population pop, String IAWinner, BigDecimal value) {
        // buscar na populacao a IA compatavel.
                Chromosome chrUpdate = null;
                for (Chromosome ch : pop.getChromosomes().keySet()) {
                    if (convertBasicTuple(ch).equals(IAWinner)) {
                        chrUpdate = ch;
                    }
                }
                
                if (chrUpdate != null) {
                    // atualizar valores.
                    BigDecimal toUpdate = pop.getChromosomes().get(chrUpdate);
                    if (toUpdate != null) {
                        toUpdate = toUpdate.add(value);
                        HashMap<Chromosome, BigDecimal> chrTemp = pop.getChromosomes();
                        chrTemp.put(chrUpdate, toUpdate);
                    }
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

	
	private Population iterativeControll(Population population) {
		// look for clients and share the data.
		while (hasSOACentralFile()) {
			// update the quantity of SOA Clients.
			updateSOAClients();
			// update the file to process
			updateFiles();
			// share the files between SOA Clients
			shareFiles();

			// run iterative process
			population = iterativeEvaluation(population);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while (hasSOAArq()) {
			try {
				// run iterative process
				population = iterativeEvaluation(population);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return population;
	}
	
	
	private Population iterativeEvaluation(Population population) {
		// ler resultados
		ArrayList<EvalResult> resultados = lerResultadosIterative();
		
		// atualizar valores das populacoes
		if(resultados.size() > 0 ){
			 population = updatePopulationValue(resultados, population);
		}
		
		return population;
	}
	
	public ArrayList<EvalResult> lerResultadosIterative() {
		LeitorLog leitor = new LeitorLog();
		ArrayList<EvalResult> resultados = leitor.processarIterative();
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
	private void runBattles(Population population) {
		int numberSOA = 1;
		// montar a lista de batalhas que irão ocorrer
		
		
		defineChromosomeSample(population);
		defineRandomSet(population);

		for (int i = 0; i < TOTAL_PARTIDAS_ROUND; i++) {

			for (Chromosome cIA1 : population.getChromosomes().keySet()) {
				

				for (Chromosome cIA2 : this.ChromosomeSample) {

					//if (!cIA1.equals(cIA2)) {
						// System.out.println("IA1 = "+ convertTuple(cIA1)+ "
						// IA2 = "+ convertTuple(cIA2));

						// first position
						String strConfig = pathCentral + "/" + convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2)
								+ ")#" + i + "#" + atualGeneration + ".txt";
						File arqConfig = new File(strConfig);
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

							gravarArq.println(convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2) + ")#" + i + "#"
									+ atualGeneration);

							gravarArq.flush();
							gravarArq.close();
							arq.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// second position
						strConfig = pathCentral + "/(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#"
								+ i + "#" + atualGeneration + ".txt";
						arqConfig = new File(strConfig);
						if (!arqConfig.exists()) {
							try {
								arqConfig.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							FileWriter arq = new FileWriter(arqConfig, false);
							PrintWriter gravarArq = new PrintWriter(arq);

							gravarArq.println("(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#" + i
									+ "#" + atualGeneration);

							gravarArq.flush();
							gravarArq.close();
							arq.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					//}

				}
			}
		}
	}
	
	private void defineRandomSet(Population population) {
		
		
		int totalPop = population.getChromosomes().size();
		Random rand = new Random();
		HashSet<Chromosome> samples = new HashSet<>();
		ArrayList<Chromosome> temp = new ArrayList<>(population.getChromosomes().keySet());
		
		while (samples.size() < ConfigurationsGA.QTD_ENEMIES_SAMPLE_RANDOM) {
			
			Chromosome cTemp;
			do {
				cTemp = temp.get(rand.nextInt(totalPop));
			}while(ChromosomeSample.contains(cTemp));
			
			samples.add(cTemp);
		}
		
		this.ChromosomeSample.addAll(samples);

	}

	private void defineChromosomeSample(Population population) {
		
		this.ChromosomeSample.clear();
		PreSelection ps=new PreSelection(population);	
		HashMap<Chromosome, BigDecimal> elite=(HashMap<Chromosome, BigDecimal>)ps.sortByValue(population.getChromosomes());
		ArrayList<Entry<Chromosome, BigDecimal>> arrayElite = new ArrayList<>();
		
		if(getEliteIndividuals().size()>0)
		{
			arrayElite.addAll(getEliteIndividuals().entrySet());
			System.out.println("have elite");
		}
		else
		{
			arrayElite.addAll(elite.entrySet());
		}
		
		System.out.println("Elite last generation");
		HashSet<Chromosome> eliteH = new HashSet<>();
		for(int i=0;i<arrayElite.size();i++)
		{
			eliteH.add(arrayElite.get(i).getKey());
			System.out.print(arrayElite.get(i).getKey().getGenes().toString()+" ");
		}
		System.out.println("");
		this.ChromosomeSample.addAll(eliteH);
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
	
	private ArrayList<Integer> convertBasicTupleToInteger(String cromo) {
		ArrayList<Integer> gens = new ArrayList<>();;
		
		cromo=cromo.replace("(", "");
		cromo=cromo.replace(")", "");
		String[] arr = cromo.split(";");
		
		
		for (int i=0; i<arr.length;i++) {
			gens.add(Integer.parseInt(arr[i]));
		}

		return gens;
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
			File f = new File(strConfig + "/exit");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public HashMap<Chromosome, BigDecimal> getEliteIndividuals() {
		return eliteIndividuals;
	}

	public void setEliteIndividuals(HashMap<Chromosome, BigDecimal> eliteIndividuals) {
		this.eliteIndividuals = eliteIndividuals;
	}

}