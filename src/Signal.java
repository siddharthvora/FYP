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
	LinkedList<Double> SMAofpDM = new LinkedList<Double>();
	LinkedList<Double> SMAofnDM = new LinkedList<Double>();
	LinkedList<Double> SMAofTR = new LinkedList<Double>();
	LinkedList<Double> SMAofADX_modulus = new LinkedList<Double>();
	
	
	boolean flag_SMA=false;
	boolean flag_DI_buy=false;
	boolean flag_DI_sell=false;
	
	double current_SMA;
	double lamdaforDI, lamdaforADX;
	double current_EMApDM, previous_EMApDM, current_EMAnDM, previous_EMAnDM;
	double current_EMA_TR, previous_EMA_TR;
	
	double previous_pDI, current_pDI;
	double previous_nDI, current_nDI;
	
	double ADX;
	double ADX_modulus;
	double current_EMA_ADX_modulus, previous_EMA_ADX_modulus;
	
	int n1,n2,n3; ///made global
	Iterator<Row> initalizeQueue(Iterator<Row> it, Individual individual)
	{
		n1 = individual.get_gene(0); //number of days for SMA
		n2 = individual.get_gene(2); //number of days for +DI, -DI, TR
		n3 = individual.get_gene(4); //number of days for ADX
		Day day;
		int temp1=0, temp2=0, temp3=0; //temp1 for SMA, temp2 for +DI, -DI, TR, temp3 for ADX
		
		lamdaforDI = 2.0/(n2+1);
		lamdaforADX = 2.0/(n3+1);
		
		while(it.hasNext())
		{
			day=individual.getDay(it);
			if(temp1<n1)
			{
				SMA.add(day.Open);
				current_SMA+=day.Open;
				temp1++;
			}
			else if(temp1==n1)
			{
				current_SMA/=n1;
				current_SMA = update_Queue(SMA, current_SMA, day.Open, n1);
				temp1++;
			}
			else
			{
				if( (day.date).isEqual(new DateTime(2001,1,4,0,0)) )
					return it;
				current_SMA = update_Queue(SMA, current_SMA, day.Open, n1);
				temp1++;
			}
			
			
			if(temp2<n2)
			{
				SMAofpDM.add(day.pDM);
				SMAofnDM.add(day.nDM);
				SMAofTR.add(day.TR);
				
				current_EMApDM+=day.pDM;
				current_EMAnDM+=day.nDM;
				current_EMA_TR+=day.TR;
				
				temp2++;
				
			}
			else if(temp2==n2)
			{
				current_EMApDM /= n2;
				current_EMApDM = update_Queue(SMAofpDM, current_EMApDM, day.pDM, n2);
							
				current_EMAnDM /= n2;
				current_EMAnDM = update_Queue(SMAofnDM, current_EMAnDM, day.nDM, n2);
				
				current_EMA_TR /= n2;
				current_EMA_TR = update_Queue(SMAofTR, current_EMA_TR, day.TR, n2);
								
				temp2++;
			}
			else
			{
				if( (day.date).isEqual(new DateTime(2000,1,6,0,0)) )
					return it;
				
				update_DI(day);
				
				if(temp3<n3)
				{
					SMAofADX_modulus.add(ADX_modulus);
					current_EMA_ADX_modulus += ADX_modulus;
					temp3++;
				}
				else if(temp3==n3)
				{
					current_EMA_ADX_modulus/=n3;
					current_EMA_ADX_modulus = update_Queue(SMAofADX_modulus, current_EMA_ADX_modulus, ADX_modulus, n3);					
					temp3++;
				}
				else
				{
					update_ADX();
					temp3++;
				}
				
				temp2++;
			}
					
		}
		return it;
	}
	
	double update_Queue(LinkedList<Double> list, double current_value, double value_to_append, int n)
	{
		double temp = list.pop();
		current_value -= temp/n;
		current_value += value_to_append/n;
		list.add(value_to_append);
		return current_value;
	}
	
	void update_DI(Day day)
	{
		previous_EMApDM = current_EMApDM;
		previous_EMAnDM = current_EMAnDM;
		previous_EMA_TR = current_EMA_TR;
		previous_pDI = current_pDI;
		previous_nDI = current_nDI;
		
		current_EMApDM = lamdaforDI*day.pDM + (1-lamdaforDI)*previous_EMApDM; //EMA(+DM)
		current_EMAnDM = lamdaforDI*day.nDM + (1-lamdaforDI)*previous_EMAnDM; //EMA(-DM)
		current_EMA_TR = lamdaforDI*day.TR + (1-lamdaforDI)*previous_EMA_TR; //ATR
		
		current_pDI = 100*current_EMApDM/current_EMA_TR; //+DI
		current_nDI = 100*current_EMAnDM/current_EMA_TR; //-DI
		
		ADX_modulus = Math.abs((current_pDI-current_nDI)/(current_pDI+current_nDI));
	}
	
	void update_ADX()
	{
		previous_EMA_ADX_modulus = current_EMA_ADX_modulus;
		current_EMA_ADX_modulus = lamdaforADX*ADX_modulus + (1-lamdaforADX)*previous_EMA_ADX_modulus;
		ADX = 100*current_EMA_ADX_modulus;		
	}
	
	boolean evaluate_sellsignal(Day day , Individual individual , int stockno)
	{
		LinkedList<Day> stocklist = individual.stocklist;
		if(!stocklist.isEmpty())
		{
			int max_days_to_hold = individual.get_gene(8);
			Day first = stocklist.getFirst();
			DateTime stock_purchase_day = first.date;
			DateTime current_day = day.date;
			current_day.minusDays(max_days_to_hold);
			if(current_day.isEqual(stock_purchase_day))
			{
				stocklist.removeFirst();
				individual.costprice -= first.Open;
				individual.profit[stockno] += day.Open - first.Open;
				individual.buycount--;
				individual.buysellcount[stockno]++;
			}
		}
		
		double ans=0;
		
		//SMA
		if(day.Open<current_SMA)
			ans += individual.get_gene(1)/100.0; //threshold for sma signal. confirm later
		
		//DI
		if(current_nDI > current_pDI)
		{
			if(!flag_DI_sell) {
				flag_DI_sell = true;
				ans += individual.get_gene(5)/100.0;
			}
		}
		else {
			flag_DI_sell = false;
		}
		
		//ADX
		if(ADX > 25)
			ans += individual.get_gene(7)/100.0;
		
		double sell_threshold=individual.get_gene(11)/100.0;
		if(ans>=sell_threshold)
			return true;
		return false;
	}
	
	boolean evaluate_buysignal(Day day, Individual individual)
	{
		double ans = 0;
		
		//SMA
		if(day.Open > current_SMA)
		{
			if(!flag_SMA) {
				flag_SMA = true;
				ans += individual.get_gene(1)/100.0;
			}
		}
		else {
			flag_SMA = false;
		}
		update_Queue(SMA, current_SMA, day.Open, n1);
		
		//DI
		if(current_pDI > current_nDI) //assuming buy when +di becomes greater than -di
		{
			if(!flag_DI_buy) {
				flag_DI_buy = true;
				ans += individual.get_gene(5)/100.0;
			}
		}		
		else {
			flag_DI_buy = false;
		}
		
		//ADX
		if(ADX>25)
			ans += individual.get_gene(7)/100.0;
		
		update_DI(day);
		update_ADX();
		
		double buy_threshold = individual.get_gene(10)/100.0;
		if(ans>=buy_threshold)
			return true;
		return false;
	}
}
