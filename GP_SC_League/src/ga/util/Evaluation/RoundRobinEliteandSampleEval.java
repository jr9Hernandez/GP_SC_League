package ga.util.Evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.PreSelection;
import model.EvalResult;
import util.LeitorLog;

public class RoundRobinEliteandSampleEval implements RatePopulation {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 2;

	//private static final String pathSOA = "/home/rubens/cluster/TesteNewGASG/configSOA/";
	private static final String pathSOA = System.getProperty("user.dir").concat("/configSOA/");

	//private static final String pathCentral = "/home/rubens/cluster/TesteNewGASG/centralSOA";
	private static final String pathCentral = System.getProperty("user.dir").concat("/centralSOA");
	
	/*
	 * key - name of the file
	 * value - data of the file
	 */
	private final LinkedHashMap<String, String> battleFiles = new LinkedHashMap<String, String>();

	// Classes de informaÃ§Ã£o
	private int atualGeneration = 0;

	// Atributos locais
	ArrayList<String> SOA_Folders = new ArrayList<>();
	ArrayList<String> SOA_arqs = new ArrayList<>();

	ArrayList<Chromosome> ChromosomeSample = new ArrayList<>();
	HashMap<Chromosome, BigDecimal> eliteIndividuals;

	public RoundRobinEliteandSampleEval() {
		super();
	}

	@Override
	public Population evalPopulation(Population population, int generation, ScriptsTable scriptsTable) {
		this.atualGeneration = generation;
		SOA_Folders.clear();
		// limpa os valores existentes na population
		population.clearValueChromosomes();

		// executa os confrontos
		runBattles(population);

		// SÃ³ permite continuar a execuÃ§Ã£o apÃ³s terminar os JOBS.
		controllExecute();

		// remove qualquer aquivo que nÃ£o possua um vencedor
		removeLogsEmpty();

		// ler resultados
		ArrayList<EvalResult> resultados = lerResultados();
		//check if all files were read
				while(resultados.size() < this.battleFiles.size()) {
					//record missing files
					generatedMissingFiles(resultados);
					//continue with the iterative controll
					controllExecute();
					ArrayList<EvalResult> missResultados = lerResultados();
					resultados.addAll(missResultados);
				}
				
		System.out.println("Number of matchs necessary "+ this.battleFiles.size());
		System.out.println("Total of matchs read "+resultados.size());
		
		// atualizar valores das populacoes
		updatePopulationValue(resultados, population);

		return population;
	}

	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	public Population updatePopulationValue(ArrayList<EvalResult> results, Population pop) {
		//ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		ArrayList<EvalResult> resultsNoDraw = results;

		/*
		 * System.out.println("AvaliaÃ§Ãµes sem Draw"); for (EvalResult evalResult
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
        
    }

    private void updateChromo(Population pop, String IAWinner, BigDecimal value) {
        // buscar na população a IA compatível.
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

	/**
	 * Verifica se os jobs jÃ¡ foram encerrados no cluster.
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
	 * irÃ¡ verificar se todas as pastas SOA estÃ£o vazias
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
	 * IrÃ¡ verificar a pasta central nÃ£o tem mais arquivos.
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
	 * MetÃ³do para enviar todas as batalhas ao cluster.
	 * 
	 * @param population
	 *            Que contÃ©m as configuracoes para a IA
	 */
	private void runBattles(Population population) {
		int numberSOA = 1;
		this.battleFiles.clear();
		// montar a lista de batalhas que irÃ£o ocorrer
		
		
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
						// escreve a configuraÃ§Ã£o de teste
						try {
							FileWriter arq = new FileWriter(arqConfig, false);
							PrintWriter gravarArq = new PrintWriter(arq);

							String infFile = convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2) + ")#" + i + "#"
									+ atualGeneration;
							gravarArq.println(infFile);
							this.battleFiles.put(strConfig, infFile);

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

							String infFile = "(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#" + i
									+ "#" + atualGeneration;
							gravarArq.println(infFile);
							this.battleFiles.put(strConfig, infFile);

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
		System.out.print("Random set ");
		while (samples.size() < ConfigurationsGA.QTD_ENEMIES_SAMPLE_RANDOM) {
			
			Chromosome cTemp;
			do {
				cTemp = temp.get(rand.nextInt(totalPop));
			}while(ChromosomeSample.contains(cTemp));
			System.out.print("Random set "+cTemp.getGenes().toString());
			samples.add(cTemp);
		}
		System.out.println("");
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
	
	private void generatedMissingFiles(ArrayList<EvalResult> resultados) {
		HashMap<String, String> intersect = new HashMap<String, String>();
		//check by data
		for(String key : this.battleFiles.keySet()) {
			String fileData = this.battleFiles.get(key);
			String[] itens = fileData.split("#");
			if(!thereIsMatch(itens[0],itens[1], resultados)) {
				intersect.put(key, fileData);
			}
			
		}
		//record the necessary files
		int cont = 1;
		System.out.println("Missed Files found "+ intersect.size()+ " missed files to be generated");
		for(String key : intersect.keySet()) {
			String data = intersect.get(key);
			String newName = key.substring(0, key.lastIndexOf("/")+1).concat("file"+cont).concat(".txt");
			System.out.println("File: "+key+" data: "+ data+" generated with name "+newName);
			saveNewBattle(newName,data);
			cont++;
		}
	}
	
	private boolean thereIsMatch(String ia1, String ia2, ArrayList<EvalResult> resultados) {
		for (EvalResult evalResult : resultados) {
			if(evalResult.getIA1().equals(ia1) && evalResult.getIA2().equals(ia2)) {
				return true;
			}
		}
		return false;
	}
	
	private void saveNewBattle(String miss, String data) {
		File arqConfig = new File(miss);
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
			gravarArq.println(data);
			gravarArq.flush();
			gravarArq.close();
			arq.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<Chromosome, BigDecimal> getEliteIndividuals() {
		return eliteIndividuals;
	}

	public void setEliteIndividuals(HashMap<Chromosome, BigDecimal> eliteIndividuals) {
		this.eliteIndividuals = eliteIndividuals;
	}

}
