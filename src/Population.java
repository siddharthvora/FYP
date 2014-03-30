import java.io.IOException;
import java.util.Arrays;
//comment
public class Population {

	Individual[] individuals;
	double totalFitness=0;
	double[] roulette_wheel;
	int profitabe_individuals_in_population;
	private static final int tournamentSize = 5;

    public Population(int populationSize, boolean initialise) throws NumberFormatException, IOException {
        individuals = new Individual[populationSize];
        // Initialise population
        if (initialise) {
            // Loop and create individuals
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.random_intialize();
                newIndividual.weight_normalize();
                newIndividual.evaluateFitness();
                if(newIndividual.fitness>=0)
                	totalFitness += newIndividual.fitness;
                saveIndividual(i, newIndividual);
            }
        }
    }

    /* Getters */
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.fitness <= getIndividual(i).fitness) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }
    
    // Save individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }
    
    public int size() {
        return individuals.length;
    }

    public void generate_Roulette_Wheel()
    {
    	boolean flag=false;
        Arrays.sort(individuals);
    	roulette_wheel = new double[size()];
    	roulette_wheel[0]=((getIndividual(0).fitness)/totalFitness);

    	for(int i=1; i<size(); i++)
    	{
    		if(flag) {
    			roulette_wheel[i]=9999; //infinite
    			continue;
    		}
    		Individual individual = getIndividual(i);
    		double individual_fitness = individual.fitness;
    		if(individual_fitness <= 0)
    		{
    			roulette_wheel[i-1]=1;
    			roulette_wheel[i]=9999;
    			flag=true;
    			profitabe_individuals_in_population=i;
    			continue;
    		}
    		roulette_wheel[i]=roulette_wheel[i-1]+(individual_fitness/totalFitness); 		    		
    	}
    	if(!flag)
    		profitabe_individuals_in_population=size();
    	
    	
    }

    public Individual[] RouletteWheelSelection()
    {
    	Individual indiv[] = new Individual[2];
    	double random = Math.random();
    	int index1 = search(random);
    	int index2;
    	indiv[0] = getIndividual(index1);
    	
    	do
    	{
    		random = Math.random();
    		index2 = search(random);
    	}while(index2==index1);
    	indiv[1] = getIndividual(index2);
    	return indiv;    	    	
    }
    
    private int search(double value)
    {
    	int i;
    	for(i=0;i<profitabe_individuals_in_population;i++)
    	{
    		if(roulette_wheel[i]>=value)
    			break;
    	}
    	return i;
    }
    
    public void pop_display()
    {
    	for(int i=0;i<size();i++)
    	{
    		individuals[i].display();
    	}
    }
    
    public Individual tournamentSelection(Population pop) throws NumberFormatException, IOException {
        // Create a tournament population
        Population tournament = new Population(tournamentSize, false);
        // For each place in the tournament get a random individual
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        // Get the fittest
        Individual fittest = tournament.getFittest();
        return fittest;
    }
    
 }
