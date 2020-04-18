package util;

import java.math.BigDecimal;
import java.util.HashMap;

import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;

public class AddingHashM {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		jbalvin();

	}
	
	public static void jbalvin() {
		
		String calimanjaro="(j)";
		if(calimanjaro.contains("j"))
		{
			System.out.println("ich");
		}
		System.out.println("och");
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
		
		Chromosome tChom=new Chromosome();;
		Chromosome tChomalt=new Chromosome();;

		
//		while (newChromosomes.size()<ConfigurationsGA.SIZE_POPULATION) {
//			//gerar o novo cromossomo com base no tamanho
//			tChom = new Chromosome();
//			tChomalt = new Chromosome();
//			for (int j = 0; j < 1; j++) {
//				tChom.addGene(5);
//			}
//			for (int j = 0; j < 1; j++) {
//				tChomalt.addGene(5);
//			}
//			System.out.println("first "+tChom);
//			System.out.println("second "+tChomalt);
//
//			newChromosomes.put(tChom, BigDecimal.ZERO);
//		}

	}

}
