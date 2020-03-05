package principal;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import SetCoverSampling.DataRecollection;
import SetCoverSampling.GameSampling;
import SetCoverSampling.RunSampling;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.PreSelection;
import ga.util.RunGA;
import ga.util.Evaluation.RatePopulation;
import ga.util.Evaluation.RoundRobinEliteandSampleEval;
import ga.util.Evaluation.RoundRobinEliteandSampleIterativeEval;
import ga.util.Evaluation.RoundRobinEval;
import ga.util.Evaluation.RoundRobinIterativeEval;
import ga.util.Evaluation.RoundRobinSampleEval;
import ga.util.Evaluation.SetCoverEval;
import ga.util.Evaluation.FixedScriptedEval;

public class RunTestsGP {
	
	private final static String pathTableScriptsInit = System.getProperty("user.dir").concat("/TableInitialPortfolio/");
	//private static final String pathTableScriptsInit = "TableInitialPortfolio/";
	private static final String pathTable = System.getProperty("user.dir").concat("/Table/");
	private final static String pathLogsBestPortfolios = System.getProperty("user.dir").concat("/TrackingPortfolios/TrackingPortfolios.txt");


	public static void main(String[] args) {
		//classe com as configurações = ConfigurationsGA
		
		//teste de inicialização de população
		//Population p = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION);
		//p.print();
		//p.printWithValue();
		//p = Population.getInitialPopulation(new Integer(100));
		//p.print();
		
		//################################################################33
		//sandBOX
		//Population p = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION);
		//p.print();
		//RatePopulation fEval = new RoundRobinEval();
		
		
		//p = fEval.evalPopulation(p,1);
		
		//System.out.println("\n Pos avaliação");
		//p.printWithValue();
		
		//################################################################33
	
		//RatePopulation fEval;
		String curriculumportfolio="empty";
		
		File logsBestPortfolios=new File(pathLogsBestPortfolios);
		GameSampling.deleteFolder(logsBestPortfolios);
		
		//Here we play with a search-based algorithm and save the path
		try {
			RunSampling sampling=new RunSampling(0,pathTableScriptsInit,curriculumportfolio);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		for(int i=0;i<ConfigurationsGA.LOOPS_SELFPLAY;i++)
		{
			
		//* 
		//aplicando o Algoritmo Genético
		//criei uma classe para controlar a execução do GA.
		RunGA ga = new RunGA(curriculumportfolio);
		
		//escolhemos uma função de avaliação
		//RatePopulation fEval = new RoundRobinEval();
		//RatePopulation fEval = new RoundRobinSampleEval();
		RoundRobinEliteandSampleEval fEval = new RoundRobinEliteandSampleEval();
		//RatePopulation fEval = new RoundRobinIterativeEval();
		//RoundRobinEliteandSampleIterativeEval fEval = new RoundRobinEliteandSampleIterativeEval();
		//fEval = new SetCoverEval();
		
		//rodamos o GA
		Population popFinal = ga.run(fEval,"", new HashSet<String>());
		
		//popFinal.printWithValue();
		
		//here we save the best portfolio of this loop of the algorithm
		
		
		//Here we chose the best individual
		HashMap<Chromosome, BigDecimal> elite=(HashMap<Chromosome, BigDecimal>)PreSelection.sortByValueBest(popFinal.getChromosomes());
		for (Chromosome ch : elite.keySet()) {
			
			ArrayList<Integer> Genes=(ArrayList<Integer>) ch.getGenes().clone();
			curriculumportfolio=Genes.toString();
			
		}
		
		//Fase 7 - finalizar 
		fEval.finishProcess();
		
		//Here we play with a search-based algorithm and save the path
		try {
			RunSampling sampling=new RunSampling(i,pathTable,curriculumportfolio);
		} catch (IOException er) {
			// TODO Auto-generated catch block
			er.printStackTrace();
		}
		
		
		}
		
		
		 
	
	}

}
