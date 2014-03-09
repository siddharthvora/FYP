
import java.io.IOException;


public class CSV_Parser 
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Population myPop = new Population(5000, true);
		Individual indiv;
		indiv = myPop.getFittest();
		indiv.display();
		System.out.println("\n"+indiv.profit[0]+" "+indiv.buysellcount[0]);
		/*
		Individual i = new Individual();
		i.random_intialize();
		i.evaluateFitness();*/
	}
}
