package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.lang.model.util.Elements;

import SetCoverSampling.DataRecollection;
import SetCoverSampling.GameSampling;
import SetCoverSampling.RunSampling;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.PreSelection;
import ga.util.RunGA;
import ga.util.RunScriptByState;
import ga.util.Evaluation.RatePopulation;
import ga.util.Evaluation.RoundRobinEliteandSampleEval;
import ga.util.Evaluation.RoundRobinEliteandSampleIterativeEval;
import ga.util.Evaluation.RoundRobinEval;
import ga.util.Evaluation.RoundRobinIterativeEval;
import ga.util.Evaluation.RoundRobinSampleEval;
import ga.util.Evaluation.SetCoverEval;
import setcoverCalculation.RunSetCoverCalculation;
import ga.util.Evaluation.FixedScriptedEval;

public class RunTests_SetCover_GP {
	
	private final static String pathTableScriptsInit = System.getProperty("user.dir").concat("/TableInitialPortfolio/");
	//private static final String pathTableScriptsInit = "TableInitialPortfolio/";
	private static final String pathTableMainAgents = System.getProperty("user.dir").concat("/TableMainAgents/");
	private static final String pathTableMainExploiters = System.getProperty("user.dir").concat("/TableMainExploiters/");
	private static final String pathTableLeagueExploiters = System.getProperty("user.dir").concat("/TableLeagueExploiters/");
	private final static String pathLogsBestPortfolios = System.getProperty("user.dir").concat("/TrackingPortfolios/TrackingPortfolios.txt");
	private final static String dirPathPlayer = System.getProperty("user.dir").concat("/logs_game/logs_states/");
	private final static String pathFixedTrace = System.getProperty("user.dir").concat("/FixedTrace/FixedTrace.txt");
	
	public static void main(String[] args) {

	
		String curriculumportfolio="empty";
		
		File logsBestPortfolios=new File(pathLogsBestPortfolios);
		GameSampling.deleteFolder(logsBestPortfolios);
		
		File file=new File(dirPathPlayer);
		GameSampling.deleteFolder(file);
		
		if(!ConfigurationsGA.fixedTrace)
		{
			//Here we play with a search-based algorithm and save the path
			for(int i=0;i<ConfigurationsGA.numberOfTraces;i++)
			{
				try {
					RunSampling sampling=new RunSampling(0,pathTableScriptsInit,curriculumportfolio);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		

		for(int i=1;i<ConfigurationsGA.LOOPS_SELFPLAY;i++)
		{
			
		String scriptsSetCover="";
		HashSet<String> booleansUsedRedefined=new HashSet<>();
		if(!ConfigurationsGA.fixedTrace)
		{
		//SC
			RunScriptByState sc = new RunScriptByState();
		
			RunSetCoverCalculation scCalculation = new RunSetCoverCalculation(sc.dataH);
			List<Integer> setCover=scCalculation.getSetCover();
			scriptsSetCover=setCover.toString();
			booleansUsedRedefined=sc.booleansUsed;
		}
		
		if(Files.exists(Paths.get(pathTableMainAgents+"ScriptsTable.txt"))) { 
			Path source = Paths.get(pathTableMainAgents+"ScriptsTable.txt");
			try {
				Files.move(source, source.resolveSibling("ScriptsTable"+i+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(Files.exists(Paths.get(pathTableMainExploiters+"ScriptsTable.txt"))) { 
			Path source = Paths.get(pathTableMainExploiters+"ScriptsTable.txt");
			try {
				Files.move(source, source.resolveSibling("ScriptsTable"+i+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(Files.exists(Paths.get(pathTableLeagueExploiters+"ScriptsTable.txt"))) { 
			Path source = Paths.get(pathTableLeagueExploiters+"ScriptsTable.txt");
			try {
				Files.move(source, source.resolveSibling("ScriptsTable"+i+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//* 
		//applying the GP
		RunGA ga = new RunGA(curriculumportfolio);
		
		//escolhemos uma função de avaliação
		//RatePopulation fEval = new RoundRobinEval();
		//RatePopulation fEval = new RoundRobinSampleEval();
		RoundRobinEliteandSampleEval fEval = new RoundRobinEliteandSampleEval();
		//RatePopulation fEval = new RoundRobinIterativeEval();
		//RoundRobinEliteandSampleIterativeEval fEval = new RoundRobinEliteandSampleIterativeEval();
		//fEval = new SetCoverEval();
		
		//rodamos o GA
		
		if(ConfigurationsGA.fixedTrace)
		{
			File arqTour = new File(pathFixedTrace);

			try {
				FileReader arq = new FileReader(arqTour);
				BufferedReader bf = new BufferedReader(arq);

				scriptsSetCover = bf.readLine();
				String booleansUsedLine= bf.readLine();
				String [] parts=booleansUsedLine.split("\\s+");
				
				for(String element:parts)
				{	
					if(element.substring(element.length() - 1).equals(","))
						element=element.substring(0, element.length() - 1);
					booleansUsedRedefined.add(element.trim());
				}
				arq.close();

			} catch (Exception e) {
				System.out.println(e.toString());
			}

		}
		
		System.out.println("format final commands: "+scriptsSetCover);
		
		System.out.println("format final booleans: "+booleansUsedRedefined.toString());
		ArrayList<Population> popFinal = ga.run(fEval,scriptsSetCover,booleansUsedRedefined);
		
		//popFinal.printWithValue();
		
		//Here we chose the best individual
		
		
//      uncooment from here for enabling loops of traces
//		HashMap<Chromosome, BigDecimal> elite=(HashMap<Chromosome, BigDecimal>)PreSelection.sortByValueBest(popFinal.getChromosomes());
//		for (Chromosome ch : elite.keySet()) {
//			
//			ArrayList<Integer> Genes=(ArrayList<Integer>) ch.getGenes().clone();
//			curriculumportfolio=Genes.toString();
//			
//		}		
//				
//		//Here we play with a search-based algorithm and save the path
//		try {
//			RunSampling sampling=new RunSampling(i,pathTable,curriculumportfolio);
//		} catch (IOException er) {
//			// TODO Auto-generated catch block
//			er.printStackTrace();
//		}
//		
//		curriculumportfolio=ga.recoverScriptGenotype(curriculumportfolio).trim();
		
		
		}
		
		
		 
	
	}

}
