import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;

/*
 *  Date time value initialised to 4th January 2000 for now. change that to 2001 later
 */

public class Signal
{
	LinkedList<Double> SMA = new LinkedList<>();
	LinkedList<Double> SMAforDI = new LinkedList<>();
	double current_SMA;
	double current_SMAforDI;
	Iterator<Row> initalizeQueue(Iterator<Row> it, Individual individual)
	{
		int n1=individual.get_gene(0);
		int n2=individual.get_gene(2); //change according to di
		Day day;
		int temp1=0, temp2=0;
		while(it.hasNext())
		{
			day=individual.getDay(it);
			if(temp1<n1)
			{
				SMA.add(day.Open);
				temp1++;
				current_SMA+=day.Open;
			}
			else if(temp1==n1)
			{
				current_SMA-=SMA.pop();
				current_SMA+=day.Open;
				current_SMA/=n1;
				SMA.add(day.Open);
				temp1++;
			}
			else
			{
				if( (day.date).isEqual(new DateTime(2000,1,4,0,0)) )
					return it;
				double temp=SMA.pop();
				current_SMA-=temp/n1;
				current_SMA+=day.Open/n1;
				SMA.add(day.Open);
				temp1++;
			}
			
			
			if(temp2<n2)
			{
				SMAforDI.add(day.Open);
				temp2++;
				current_SMAforDI+=day.Open;
			}
			else if(temp2==n2)
			{
				current_SMAforDI -= SMAforDI.pop();
				current_SMAforDI += day.Open;
				current_SMAforDI /= n2;
				SMAforDI.add(day.Open);	
				temp2++;
			}
			else
			{
				if( (day.date).isEqual(new DateTime(2000,1,4,0,0)) )
					return it;
				double temp=SMAforDI.pop();
				current_SMAforDI -= temp/n2;
				current_SMAforDI += day.Open/n2;
				SMAforDI.add(day.Open);
				temp2++;
			}			
		}
		return it;
	}

	boolean evaluate_sellsignal(Day day)
	{
		boolean SMA_result = day.Open>current_SMA;
		
		
		return false;//change this
		
	}
	
	
}
