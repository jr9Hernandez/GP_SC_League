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

	private Population populationMainAgents;
	private Population populationMainExploiters;
	private Population populationLeagueExploiters;
	private Instant timeInicial;
	private int generations = 0;
	private ScriptsTable scrTableMainAgents;
	private ScriptsTable scrTableMainExploiters;
	private ScriptsTable scrTableLeagueExploiters;
	private HashMap<Chromosome, BigDecimal> eliteMainAgents=new HashMap<Chromosome, BigDecimal>();
	private HashMap<Chromosome, BigDecimal> eliteMainExploiters=new HashMap<Chromosome, BigDecimal>();
	private HashMap<Chromosome, BigDecimal> eliteLeagueExploiters=new HashMap<Chromosome, BigDecimal>();

	private final String pathTableMainAgents = System.getProperty("user.dir").concat("/TableMainAgents/");
	private final String pathTableMainExploiters = System.getProperty("user.dir").concat("/TableMainExploiters/");
	private final String pathTableLeagueExploiters = System.getProperty("user.dir").concat("/TableLeagueExploiters/");
	
	private final String pathLogsMainAgents = System.getProperty("user.dir").concat("/TrackingMainAgents/");
	private final String pathLogsMainExploiters = System.getProperty("user.dir").concat("/TrackingMainExploiters/");
	private final String pathLogsLeagueExploiters = System.getProperty("user.dir").concat("/TrackingLeagueExploiters/");
	
	private final String pathInitialPopulation = System.getProperty("user.dir").concat("/InitialPopulation/");
	
	private final String pathUsedCommandsMainAgents = System.getProperty("user.dir").concat("/commandsUsedMainAgents/");
	private final String pathUsedCommandsMainExploiters = System.getProperty("user.dir").concat("/commandsUsedMainExploiters/");
	private final String pathUsedCommandsLeagueExploiters = System.getProperty("user.dir").concat("/commandsUsedLeagueExploiters/");
	
	static int [] frequencyIdsRulesForUCB= new int[ConfigurationsGA.QTD_RULES];
	static int numberCallsUCB11=0;
	//private final String pathTableScripts = "/home/rubens/cluster/TesteNewGASG/Table/";

	/**
	 * Este metodo aplicará todas as fases do processo de um algoritmo Genético
	 * Fres
	 * @param evalFunction
	 *            Será a função de avaliação que desejamos utilizar
	 */
	public ArrayList<Population> run(RoundRobinEliteandSampleEval evalFunction, String scriptsSetCover, HashSet<String> booleansUsed) {
		
		//Array for saving each population
		ArrayList<Population> league=new ArrayList<Population>();
		
		// Creating the table of scripts
		scrTableMainAgents = new ScriptsTable(pathTableMainAgents);
		scrTableMainExploiters = new ScriptsTable(pathTableMainExploiters);
		scrTableLeagueExploiters = new ScriptsTable(pathTableLeagueExploiters);
		//do {
			if(ConfigurationsGA.portfolioSetCover)
			{
				scrTableMainAgents = scrTableMainAgents.generateScriptsTableFromSetCover(ConfigurationsGA.SIZE_TABLE_SCRIPTS,scriptsSetCover,booleansUsed,curriculumportfolio);
				scrTableMainExploiters = scrTableMainExploiters.generateScriptsTableFromSetCover(ConfigurationsGA.SIZE_TABLE_SCRIPTS,scriptsSetCover,booleansUsed,curriculumportfolio);
				scrTableLeagueExploiters = scrTableLeagueExploiters.generateScriptsTableFromSetCover(ConfigurationsGA.SIZE_TABLE_SCRIPTS,scriptsSetCover,booleansUsed,curriculumportfolio);
			}
			else
			{
				if(!ConfigurationsGA.recoverTable)
				{
					scrTableMainAgents = scrTableMainAgents.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
					scrTableMainExploiters = scrTableMainExploiters.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
					scrTableLeagueExploiters = scrTableLeagueExploiters.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
				}
				else
				{
					scrTableMainAgents = scrTableMainAgents.generateScriptsTableRecover();
					scrTableMainExploiters = scrTableMainExploiters.generateScriptsTableRecover();
					scrTableLeagueExploiters = scrTableLeagueExploiters.generateScriptsTableRecover();
				}
			}
		   //}while(scrTable.checkDiversityofTypes());
		scrTableMainAgents.setCurrentSizeTable(scrTableMainAgents.getScriptTable().size());
		scrTableMainExploiters.setCurrentSizeTable(scrTableMainExploiters.getScriptTable().size());
		scrTableLeagueExploiters.setCurrentSizeTable(scrTableLeagueExploiters.getScriptTable().size());

		PrintWriter fMainAgents;
		PrintWriter fMainExploiters;
		PrintWriter fLeagueExploiters;
		try {
			fMainAgents = new PrintWriter(new FileWriter(pathLogsMainAgents+"Tracking.txt"));
			fMainExploiters = new PrintWriter(new FileWriter(pathLogsMainExploiters+"Tracking.txt"));
			fLeagueExploiters = new PrintWriter(new FileWriter(pathLogsLeagueExploiters+"Tracking.txt"));

//		do {
			// Fase 1 = gerar a população inicial
			if(!ConfigurationsGA.curriculum)
			{
				populationMainAgents = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTableMainAgents);
				populationMainExploiters = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTableMainExploiters);
				populationLeagueExploiters = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTableLeagueExploiters);
			}
			else
			{
				populationMainAgents = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTableMainAgents, pathInitialPopulation);
				populationMainExploiters = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTableMainExploiters, pathInitialPopulation);
				populationLeagueExploiters = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTableLeagueExploiters, pathInitialPopulation);
			}			
			populationMainAgents.setIdTypePopulation("MainAgents");
			populationMainExploiters.setIdTypePopulation("MainExploiters");
			populationLeagueExploiters.setIdTypePopulation("LeagueExploiters");

			// Fase 2 = avalia a população
			evalFunction.setEliteMainAgents(eliteMainAgents);
			evalFunction.setEliteMainExploiters(eliteMainExploiters);
			evalFunction.setEliteLeagueExploiters(eliteLeagueExploiters);
			
//			populationMainAgents = evalFunction.evalPopulation(populationMainAgents, this.generations, scrTableMainAgents);	
//			populationMainExploiters = evalFunction.evalPopulation(populationMainExploiters, this.generations, scrTableMainExploiters);	
//			populationLeagueExploiters = evalFunction.evalPopulation(populationLeagueExploiters, this.generations, scrTableLeagueExploiters);	
			
//			population.printWithValue(f0);
//			System.out.println("sep");
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true)
			{
				populationMainAgents.fillAllCommands(pathTableMainAgents);
				populationMainExploiters.fillAllCommands(pathTableMainExploiters);
				populationLeagueExploiters.fillAllCommands(pathTableLeagueExploiters);
			}
//		    Iterator it = population.getAllCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		    }
			//Choose the used commands
			if(ConfigurationsGA.removeRules==true)
			{
				populationMainAgents.chooseusedCommands(pathUsedCommandsMainAgents);
				populationMainExploiters.chooseusedCommands(pathTableMainExploiters);
				populationLeagueExploiters.chooseusedCommands(pathTableLeagueExploiters);
			}
				
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
			{
				populationMainAgents.removeCommands(scrTableMainAgents);
				populationMainExploiters.removeCommands(scrTableMainExploiters);
				populationLeagueExploiters.removeCommands(scrTableLeagueExploiters);
			}
			
//		    Iterator it2 = population.getAllCommandsperGeneration().entrySet().iterator();
//		    while (it2.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it2.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		    }
			
			System.out.println("Log - Generation = " + this.generations);
			fMainAgents.println("Log - Generation = " + this.generations);
			fMainAgents.println("population Main Agents ");
			populationMainAgents.printWithValue(fMainAgents);
			fMainAgents.flush();
			
			System.out.println("Log - Generation = " + this.generations);
			fMainExploiters.println("Log - Generation = " + this.generations);
			fMainExploiters.println("population Main Exploiters ");
			populationMainExploiters.printWithValue(fMainExploiters);
			fMainExploiters.flush();
			
			System.out.println("Log - Generation = " + this.generations);
			fLeagueExploiters.println("Log - Generation = " + this.generations);
			fLeagueExploiters.println("population League Exploiters ");
			populationLeagueExploiters.printWithValue(fLeagueExploiters);
			fLeagueExploiters.flush();

			
//		} while (resetPopulation(populationMainAgents) || resetPopulation(populationMainExploiters) || 
//				resetPopulation(populationLeagueExploiters));

		resetControls();
		// Fase 3 = critério de parada
		while (continueProcess()) {

			// Fase 4 = Seleção (Aplicar Cruzamento e Mutação)
			Selection selecaoMainAgents = new Selection();
			Selection selecaoMainExploiters = new Selection();
			Selection selecaoLeagueExploiters = new Selection();
			
			populationMainAgents = selecaoMainAgents.applySelection(populationMainAgents, scrTableMainAgents, pathTableMainAgents);
			populationMainExploiters = selecaoMainExploiters.applySelection(populationMainExploiters, scrTableMainExploiters, pathTableMainExploiters);
			populationLeagueExploiters = selecaoLeagueExploiters.applySelection(populationLeagueExploiters, scrTableLeagueExploiters, pathTableLeagueExploiters);
			eliteMainAgents=selecaoMainAgents.eliteIndividuals;
			eliteMainExploiters=selecaoMainExploiters.eliteIndividuals;
			eliteLeagueExploiters=selecaoLeagueExploiters.eliteIndividuals;
			// Repete-se Fase 2 = Avaliação da população
//			evalFunction.setEliteMainAgents(eliteMainAgents);
//			evalFunction.setEliteMainExploiters(eliteMainExploiters);
//			evalFunction.setEliteLeagueExploiters(eliteLeagueExploiters);
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true)
			{
				populationMainAgents.fillAllCommands(pathTableMainAgents);
				populationMainExploiters.fillAllCommands(pathTableMainExploiters);
				populationLeagueExploiters.fillAllCommands(pathTableLeagueExploiters);
			}
			
			//Remove the unused commands
			if(ConfigurationsGA.removeRules==true)
			{
				populationMainAgents.chooseusedCommands(pathUsedCommandsMainAgents);
				populationMainExploiters.chooseusedCommands(pathTableMainExploiters);
				populationLeagueExploiters.chooseusedCommands(pathTableLeagueExploiters);
			}
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
			{
				populationMainAgents.removeCommands(scrTableMainAgents);
				populationMainExploiters.removeCommands(scrTableMainExploiters);
				populationLeagueExploiters.removeCommands(scrTableLeagueExploiters);
			}

			// atualiza a geração
			updateGeneration();

			System.out.println("Log - Generation = " + this.generations);
			fMainAgents.println("Log - Generation = " + this.generations);
			fMainAgents.println("population Main Agents ");
			populationMainAgents.printWithValue(fMainAgents);
			fMainAgents.flush();
			
			System.out.println("Log - Generation = " + this.generations);
			fMainExploiters.println("Log - Generation = " + this.generations);
			fMainExploiters.println("population Main Exploiters ");
			populationMainExploiters.printWithValue(fMainExploiters);
			fMainExploiters.flush();
			
			System.out.println("Log - Generation = " + this.generations);
			fLeagueExploiters.println("Log - Generation = " + this.generations);
			fLeagueExploiters.println("population League Exploiters ");
			populationLeagueExploiters.printWithValue(fLeagueExploiters);
			fLeagueExploiters.flush();
			
			if(ConfigurationsGA.UCB1==true)
			{
				Log_Facade.shrinkRewardTable();
				System.out.println("call shrink");
			}
		}
		
		fMainAgents.close();
		fMainExploiters.close();
		fLeagueExploiters.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		league.add(populationMainAgents);
		league.add(populationMainExploiters);
		league.add(populationLeagueExploiters);

		return league;
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
	
//	public String recoverScriptGenotype(String portfolioIds)
//	{
//		String portfolioGenotype;
//        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
//        portfolioIds = portfolioIds.replaceAll("\\s+","");
//        String[] itens = portfolioIds.replace("[", "").replace("]", "").split(",");
//
//        for (String element : itens) {
//            iScriptsAi1.add(Integer.decode(element));
//        }
//        
//        portfolioGenotype=buildScriptsTable(pathTableScripts).get(BigDecimal.valueOf(iScriptsAi1.get(0)));
//       
//		return portfolioGenotype;
//	}
	
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