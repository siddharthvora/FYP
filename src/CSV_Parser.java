
import java.io.IOException;


public class CSV_Parser 
{

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Population myPop = new Population(500, true);
		Individual indiv;
		
		/*for(int i=0;i<499;i++)
		{
			indiv = myPop.getIndividual(i);
			System.out.println(indiv.profit[0]+" "+indiv.buysellcount[0]);
		}*/
		
		
		myPop.generate_Roulette_Wheel();
		
	
	}
}
