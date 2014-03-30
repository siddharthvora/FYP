
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class CSV_Parser 
{

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		final int no_of_fit_individuals=50;
		Individual fittest_individuals[] = new Individual[no_of_fit_individuals];
		int generationCount = 0;
		
		Population myPop = new Population(500, true);
		Operator op = new Operator();		
		Individual fittest = null;
		
		for(int i=0 ; i < no_of_fit_individuals ; i++)
			fittest_individuals[i] = myPop.getIndividual(i);
		
		for(;generationCount<=10;generationCount++)
        {
            
            System.out.println("Generation: " + generationCount + " Fittest: " );
            fittest = myPop.getFittest();
            fittest.display();
            
            //for(int i=0; i<myPop.size(); i++)
            	//myPop.getIndividual(i).display();
            
            //br.readLine();
            
            
            myPop = op.evolvePopulation(myPop);
            boolean already_exist = false;
            int j=0;
            for(int i=0; i<no_of_fit_individuals; i++)
            {
            	already_exist = false;
            	if(myPop.getIndividual(j).fitness>fittest_individuals[i].fitness)
            	{
            		for(int x=0; x<no_of_fit_individuals; x++)
            			if(myPop.getIndividual(j).isEqualto(fittest_individuals[x]))
            			{
            				j++; i--;
            				already_exist=true;
            			}
            		if(!already_exist)
            		{
	            		for(int k=no_of_fit_individuals-1;k>i;k--)
	            			fittest_individuals[k]=fittest_individuals[k-1];
	            		fittest_individuals[i]=myPop.getIndividual(j);
	            		j++;
            		}
            	}
            }
        }
        
		Arrays.sort(fittest_individuals);
		for(int i=0 ; i < no_of_fit_individuals ; i++)
			fittest_individuals[i].display();
		/*
		fittest_individuals[0].evaluateFitnesson_OutOfSampleData();
		System.out.println("fittest in training out of sample");
		fittest_individuals[0].display();
		*/
		System.out.println("Run on validation data");
		for(int i=0 ; i < no_of_fit_individuals ; i++)
			fittest_individuals[i].evaluateFitnesson_ValidationData();
		
		Arrays.sort(fittest_individuals);
		
		for(int i=0 ; i < no_of_fit_individuals ; i++)	
			fittest_individuals[i].display();
		
		//Arrays.sort(fittest_individuals);
		System.out.println("Run on Testing data");
		
		for(int i=0; i<20; i++)
		{
			fittest_individuals[i].evaluateFitnesson_OutOfSampleData();
			System.out.println();
	        fittest_individuals[i].display();
		}
		
        /*System.out.println("\nSolution found!");
        System.out.println("Generation: " + generationCount);
        System.out.println("Genes:");
        System.out.println(individual.fitness);*/
	}
}

/*Population myPop = new Population(500, true);
Individual indiv;
myPop.generate_Roulette_Wheel();
indiv = myPop.getFittest();
System.out.println(indiv.fitness + " " + indiv.buysellcount[0]);
indiv.display();*/

/*Individual indiv = new Individual();
indiv.gene[0]=(byte) 129;
indiv.gene[1]=32;
indiv.gene[2]=(byte) 42;
indiv.gene[3]=85;
indiv.gene[4]=(byte)28;
indiv.gene[5]=115;
indiv.gene[6]=64;
indiv.gene[7]=5;
indiv.gene[8]=18;
indiv.gene[9]=(byte) 136;
indiv.gene[10]=(byte)54;
indiv.gene[11]=38;
indiv.gene[12]=(byte)181;

indiv.evaluateFitness();
System.out.println(indiv.fitness + " " + indiv.buysellcount[0]);*/