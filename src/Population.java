import java.io.IOException;
import java.util.Arrays;
//comment
public class Population {

	Individual[] individuals;
	double totalFitness=0;
	double[] roulette_wheel;

    public Population(int populationSize, boolean initialise) throws NumberFormatException, IOException {
        individuals = new Individual[populationSize];
        // Initialise population
        if (initialise) {
            // Loop and create individuals
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.random_intialize();
                newIndividual.evaluateFitness();
                if(newIndividual.fitness>=0)
                	totalFitness += newIndividual.fitness;
                saveIndividual(i, newIndividual);
            }
            Arrays.sort(individuals);
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
        //Arrays.sort(individuals);
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
    		if(individual_fitness == 0)
    		{
    			roulette_wheel[i-1]=1;
    			roulette_wheel[i]=9999;
    			flag=true;
    			continue;
    		}
    		roulette_wheel[i]=roulette_wheel[i-1]+(individual_fitness/totalFitness); 		    		
    	}
    	
    	for(int i=1; i<size(); i++)
    	{
        	System.out.println(roulette_wheel[i]);
    	}
    	
    }
}
