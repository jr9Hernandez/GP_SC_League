package ga.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import ga.util.Evaluation.RoundRobinEliteandSampleEval;
import ga.util.Evaluation.RoundRobinEliteandSampleIterativeEval;
import util.sqlLite.Log_Facade;

public class RunGA {
	
	String curriculumportfolio;
	
	public RunGA(String curriculumportfolio)
	{
		this.curriculumportfolio=curriculumportfolio;
	}

	private Population population;
	private Instant timeInicial;
	private int generations = 0;
	private ScriptsTable scrTable;
	private HashMap<Chromosome, BigDecimal> eliteIndividuals=new HashMap<Chromosome, BigDecimal>();

	private final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
	private final String pathLogs = System.getProperty("user.dir").concat("/Tracking/");
	private final String pathInitialPopulation = System.getProperty("user.dir").concat("/InitialPopulation/");
	private final String pathUsedCommands = System.getProperty("user.dir").concat("/commandsUsed/");
	
	static int [] frequencyIdsRulesForUCB= new int[ConfigurationsGA.QTD_RULES];
	static int numberCallsUCB11=0;
	//private final String pathTableScripts = "/home/rubens/cluster/TesteNewGASG/Table/";

	/**
	 * Este metodo aplicará todas as fases do processo de um algoritmo Genético
	 * Fres
	 * @param evalFunction
	 *            Será a função de avaliação que desejamos utilizar
	 */
	public Population run(RoundRobinEliteandSampleEval evalFunction, String scriptsSetCover, HashSet<String> booleansUsed) {
		// Creating the table of scripts
		scrTable = new ScriptsTable(pathTableScripts);
		//do {
			if(ConfigurationsGA.portfolioSetCover)
			{
				scrTable = scrTable.generateScriptsTableFromSetCover(ConfigurationsGA.SIZE_TABLE_SCRIPTS,scriptsSetCover,booleansUsed,curriculumportfolio);
			}
			else
			{
				if(!ConfigurationsGA.recoverTable)
				{
					scrTable = scrTable.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
				}
				else
				{
					scrTable = scrTable.generateScriptsTableRecover();
				}
			}
		   //}while(scrTable.checkDiversityofTypes());
		scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());

		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathLogs+"Tracking.txt"));

		do {
			// Fase 1 = gerar a população inicial
			if(!ConfigurationsGA.curriculum)
			{
				population = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTable);
			}
			else
			{
				population = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTable, pathInitialPopulation);
			}			
			

			// Fase 2 = avalia a população
			evalFunction.setEliteIndividuals(eliteIndividuals);
			population = evalFunction.evalPopulation(population, this.generations, scrTable);			
			
//			population.printWithValue(f0);
//			System.out.println("sep");
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true)
				population.fillAllCommands(pathTableScripts);
//		    Iterator it = population.getAllCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		    }
			//Choose the used commands
			if(ConfigurationsGA.removeRules==true)
				population.chooseusedCommands(pathUsedCommands);
//		    Iterator it = population.getUsedCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		        //it.remove(); // avoids a ConcurrentModificationException
//		    }
			
			//Remove used commands from all commands
			if(ConfigurationsGA.removeRules==true)
				population.removeCommands(scrTable);
			
//		    Iterator it2 = population.getAllCommandsperGeneration().entrySet().iterator();
//		    while (it2.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it2.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		    }
			
			System.out.println("Log - Generation = " + this.generations);
			f0.println("Log - Generation = " + this.generations);
			population.printWithValue(f0);
			f0.flush();
		} while (resetPopulation(population));

		resetControls();
		// Fase 3 = critério de parada
		while (continueProcess()) {

			// Fase 4 = Seleção (Aplicar Cruzamento e Mutação)
			Selection selecao = new Selection();
			population = selecao.applySelection(population, scrTable, pathTableScripts);
			eliteIndividuals=selecao.eliteIndividuals;
			// Repete-se Fase 2 = Avaliação da população
			evalFunction.setEliteIndividuals(eliteIndividuals);
			population = evalFunction.evalPopulation(population, this.generations, scrTable);
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true)
				population.fillAllCommands(pathTableScripts);
			
			//Remove the unused commands
			if(ConfigurationsGA.removeRules==true)
				population.chooseusedCommands(pathUsedCommands);
//		    Iterator it = population.getUsedCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		        //it.remove(); // avoids a ConcurrentModificationException
//		    }
			//Remove used commands from all commands
			if(ConfigurationsGA.removeRules==true)
				population.removeCommands(scrTable);

			// atualiza a geração
			updateGeneration();

			System.out.println("Log - Generation = " + this.generations);
			f0.println("Log - Generation = " + this.generations);
			population.printWithValue(f0);
			f0.flush();
			
			if(ConfigurationsGA.UCB1==true)
			{
				Log_Facade.shrinkRewardTable();
				System.out.println("call shrink");
			}
		}
		
		f0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return population;
	}

	private boolean resetPopulation(Population population2) {
		if (ConfigurationsGA.RESET_ENABLED) {
			if (population2.isPopulationValueZero()) {
				System.out.println("Population reset!");
				return true;
			}
		}
		return false;
	}

	private void updateGeneration() {
		this.generations++;
	}

	private boolean continueProcess() {
		switch (ConfigurationsGA.TYPE_CONTROL) {
		case 0:
			return hasTime();

		case 1:
			return hasGeneration();

		default:
			return false;
		}

	}

	private boolean hasGeneration() {
		if (this.generations < ConfigurationsGA.QTD_GENERATIONS) {
			return true;
		}
		return false;
	}

	/**
	 * Função que inicia o contador de tempo para o critério de parada
	 */
	protected void resetControls() {
		this.timeInicial = Instant.now();
		this.generations = 0;
	}

	protected boolean hasTime() {
		Instant now = Instant.now();

		Duration duracao = Duration.between(timeInicial, now);

		// System.out.println( "Horas " + duracao.toMinutes());

		if (duracao.toHours() < ConfigurationsGA.TIME_GA_EXEC) {
			return true;
		} else {
			return false;
		}

	}
	
	public String recoverScriptGenotype(String portfolioIds)
	{
		String portfolioGenotype;
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        portfolioIds = portfolioIds.replaceAll("\\s+","");
        String[] itens = portfolioIds.replace("[", "").replace("]", "").split(",");

        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }
        
        portfolioGenotype=buildScriptsTable(pathTableScripts).get(BigDecimal.valueOf(iScriptsAi1.get(0)));
       
		return portfolioGenotype;
	}
	
    public HashMap<BigDecimal, String> buildScriptsTable(String pathTableScripts) {
    	HashMap<BigDecimal, String> scriptsTable = new HashMap<>();
        String line="";
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "ScriptsTable.txt"))) {
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
        } catch(Exception e){
            System.out.println(line);
            System.out.println(e);
        }

        return scriptsTable;
    }
	
}
