package SetCoverSampling;

public class IndividualFitnessSetCover {
	
	private String individual;
	private int fitness;
	
	public IndividualFitnessSetCover(String individual, int fitness)
	{
		this.individual=individual;
		this.fitness=fitness;
	}
	public IndividualFitnessSetCover()
	{
		
	}

	/**
	 * @return the individual
	 */
	public String getIndividual() {
		return individual;
	}

	/**
	 * @return the fitness
	 */
	public int getFitness() {
		return fitness;
	}

	/**
	 * @param individual the individual to set
	 */
	public void setIndividual(String individual) {
		this.individual = individual;
	}

	/**
	 * @param fitness the fitness to set
	 */
	public void setFitness(int fitness) {
		this.fitness = fitness;
	}

}
