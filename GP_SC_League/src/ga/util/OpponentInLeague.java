package ga.util;

import java.math.BigDecimal;
import java.util.Map.Entry;

import ga.model.Chromosome;

public class OpponentInLeague {
	private Entry<Chromosome, BigDecimal> entryChromosome;
	private String typePopulation;
	private int generationEliteNumber;
	
	public OpponentInLeague(Entry<Chromosome, BigDecimal> entryChromosome, String typePopulation, int generationEliteNumber)
	{
		this.entryChromosome=entryChromosome;
		this.typePopulation=typePopulation;
		this.generationEliteNumber=generationEliteNumber;
	}

	public Entry<Chromosome, BigDecimal> getEntryChromosome() {
		return entryChromosome;
	}

	public String getTypePopulation() {
		return typePopulation;
	}

	public void setEntryChromosome(Entry<Chromosome, BigDecimal> entryChromosome) {
		this.entryChromosome = entryChromosome;
	}

	public void setTypePopulation(String typePopulation) {
		this.typePopulation = typePopulation;
	}

	public int getGenerationEliteNumber() {
		return generationEliteNumber;
	}

	public void setGenerationEliteNumber(int generationEliteNumber) {
		this.generationEliteNumber = generationEliteNumber;
	}
	
	
}


