package principal;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

public class RunTestsSetCover {
	
	private final static String pathTableScriptsInit = System.getProperty("user.dir").concat("/TableInitialPortfolio/");
	//private static final String pathTableScriptsInit = "TableInitialPortfolio/";
	private static final String pathTable = System.getProperty("user.dir").concat("/Table/");
	private final static String pathLogsBestPortfolios = System.getProperty("user.dir").concat("/TrackingPortfolios/TrackingPortfolios.txt");


	public static void main(String[] args) {

	
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
		RunScriptByState sc = new RunScriptByState();
		
		RunSetCoverCalculation scCalculation = new RunSetCoverCalculation(sc.dataH);
		List<Integer> setCover=scCalculation.getSetCover();
		curriculumportfolio=setCover.toString();
		
		
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
