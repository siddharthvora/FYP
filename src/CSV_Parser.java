
import java.io.IOException;

import org.joda.time.DateTime;


public class CSV_Parser {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DateTime dt = new DateTime(2000,1,4,0,0);
		System.out.println(dt);
		Individual i = new Individual();
		i.gene[0]=5;
		i.gene[2]=3;
		i.evaluateFitness();
	}
		
		

}
