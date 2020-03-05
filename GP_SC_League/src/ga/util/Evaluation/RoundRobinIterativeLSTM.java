package ga.util.Evaluation;

import java.io.BufferedReader;
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
import java.util.List;

import ai.core.AI;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.model.Chromosome;
import ga.model.Population;
import model.EvalResult;
import util.LeitorLog;

public class RoundRobinIterativeLSTM implements RatePopulation {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 1;

	// private static final String pathSOA =
	// "/home/rubens/Experimentos/LSTMDataset/pythonMod/configSOA/";
	private static final String pathSOA = System.getProperty("user.dir").concat("/configSOA/LSTM/");
	
	private static final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");

	// Classes de informação
	private int atualGeneration = 0;
	private HashMap<BigDecimal, String> scriptsTable;
	
	// Atributos locais
		ArrayList<String> SOA_Folders = new ArrayList<>();
		ArrayList<String> SOA_arqs = new ArrayList<>();

	public RoundRobinIterativeLSTM() {
		super();
	}

	@Override
	public Population evalPopulation(Population population, int generation, ScriptsTable scriptsTable) {
		this.atualGeneration = generation;
		buildScriptsTable();
		// Clear the population values
		population.clearValueChromosomes();

		// executa os confrontos
		runBattles(population);

		// Só permite continuar a execução após terminar os JOBS.
		// controllExecute();
		iterativeControll(population);

		// remove qualquer aquivo que não possua um vencedor
		removeLogsEmpty();

		// ler resultados
		ArrayList<EvalResult> resultados = lerResultados();
		// atualizar valores das populacoes
		if (resultados.size() > 0) {
			updatePopulationValue(resultados, population);
		}

		return population;
	}

	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	public Population updatePopulationValue(ArrayList<EvalResult> results, Population pop) {
		// ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		ArrayList<EvalResult> resultsNoDraw = results;

		/*
		 * System.out.println("Avaliações sem Draw"); for (EvalResult evalResult :
		 * resultsNoDraw) { evalResult.print(); }
		 */

		for (EvalResult evalResult : resultsNoDraw) {
			updateChomoPopulation(evalResult, pop);
		}

		return pop;
	}

	private void updateChomoPopulation(EvalResult evalResult, Population pop) {
		if (evalResult.getEvaluation() == 0) {
			// IAWinner = evalResult.getIA1();
			updateChromo(pop, evalResult.getIA1(), BigDecimal.ONE);
		} else if (evalResult.getEvaluation() == 1) {
			updateChromo(pop, evalResult.getIA2(), BigDecimal.ONE);
		} else {
			updateChromo(pop, evalResult.getIA1(), new BigDecimal(0.5));
			updateChromo(pop, evalResult.getIA2(), new BigDecimal(0.5));
		}

	}

	private void updateChromo(Population pop, String IAWinner, BigDecimal value) {
		// buscar na populacao a IA compativel.
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

	public ArrayList<EvalResult> lerResultadosIterative() {
		LeitorLog leitor = new LeitorLog();
		ArrayList<EvalResult> resultados = leitor.processarIterative();
		/*
		 * for (EvalResult evalResult : resultados) { evalResult.print(); }
		 */
		return resultados;
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
		/*
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
		*/
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
		if (resultados.size() > 0) {
			population = updatePopulationValue(resultados, population);
		}

		return population;
	}

	/**
	 * Verifica se os jobs já foram encerrados no cluster.
	 */
	private void controllExecute() {

		// look for clients and share the data.
		/*
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
		*/
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
		/*
		this.SOA_arqs.clear();
		File CentralFolder = new File(pathCentral + "/");
		for (File file : CentralFolder.listFiles()) {
			SOA_arqs.add(file.getAbsolutePath());
		}
		*/
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
		this.SOA_Folders.add(pathSOA);
	}

	/**
	 * Irá verificar a pasta central não tem mais arquivos.
	 * 
	 * @return
	 */
	private boolean hasSOACentralFile() {
		File centralF = new File(pathSOA);
		if (centralF.list().length > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Metódo para enviar todas as batalhas ao cluster.
	 * 
	 * @param population Que contém as configuracoes para a IA
	 */
	private void runBattles(Population population) {
		int numberSOA = 1;
		// generate the files
		File fileConfig = new File(pathSOA.concat("BattlesLSTM.txt"));
		if (!fileConfig.exists()) {
			try {
				fileConfig.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		FileWriter arq = null;
		try {
			arq = new FileWriter(fileConfig, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PrintWriter gravarArq = new PrintWriter(arq);

		// montar a lista de batalhas que irão ocorrer

		for (int i = 0; i < TOTAL_PARTIDAS_ROUND; i++) {

			for (Chromosome cIA1 : population.getChromosomes().keySet()) {

				for (Chromosome cIA2 : population.getChromosomes().keySet()) {
					if (!cIA1.equals(cIA2)) {
						// escreve a configuração de teste
						gravarArq.println(convertBasicTuple(cIA1) + "#" + convertBasicTuple(cIA2) + "#" + i + "#"
								+ atualGeneration+ "#" +convertBasicText(cIA1)+ "#" +convertBasicText(cIA2));
						gravarArq.flush();

					}

				}
			}
		}

		gravarArq.close();
		try {
			arq.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private String convertBasicText(Chromosome cromo) {

		String portfolioGrammar0=buildCompleteGrammar(convertBasicTupleToInteger(convertBasicTuple(cromo)));
		portfolioGrammar0=portfolioGrammar0.substring(0, portfolioGrammar0.length() - 1);

		return portfolioGrammar0;
	}
	
	private ArrayList<Integer> convertBasicTupleToInteger(String cromo) {
		ArrayList<Integer> gens = new ArrayList<>();;
		
		cromo=cromo.replace("(", "");
		cromo=cromo.replace(")", "");
		String[] arr = cromo.split(";");
		
		
		for (int i=0; i<arr.length;i++) {
			//System.out.println("moretime "+arr[i]);
			gens.add(Integer.parseInt(arr[i]));
		}

		return gens;
	}
	
	public String buildCompleteGrammar(ArrayList<Integer> iScripts) {
        List<AI> scriptsAI = new ArrayList<>();
        String portfolioGrammar="";

        for (Integer idSc : iScripts) {
            //System.out.println("tam tab"+scriptsTable.size());
            //System.out.println("id "+idSc+" Elems "+scriptsTable.get(BigDecimal.valueOf(idSc)));
        	portfolioGrammar=portfolioGrammar+scriptsTable.get(BigDecimal.valueOf(idSc))+";";
        }

        return portfolioGrammar;
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
		String strConfig = pathSOA;
		File f = new File(strConfig + "/exit");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}