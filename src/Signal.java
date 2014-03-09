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
	
	boolean flag_SMA=false;
	boolean flag_DI_buy=false;
	boolean flag_DI_sell=false;
	
	double current_SMA;
	double lamdaforDI, lamdaforADX, lamdaforRSI;
	double current_EMApDM, previous_EMApDM, current_EMAnDM, previous_EMAnDM;
	double current_EMA_TR, previous_EMA_TR;
	
	double previous_pDI, current_pDI;
	double previous_nDI, current_nDI;
	
	double ADX;
	double ADX_modulus;
	double current_EMA_ADX_modulus, previous_EMA_ADX_modulus;
	
	double RSI;
	double current_EMA_GAIN, previous_EMA_GAIN;
	double current_EMA_LOSS, previous_EMA_LOSS;
	
	int n1,n2,n3,n4; ///made global
	Iterator<Row> initalizeQueue(Iterator<Row> it, Individual individual)
	{
		n1 = individual.get_gene(0); //number of days for SMA
		n2 = individual.get_gene(2); //number of days for +DI, -DI, TR
		n3 = individual.get_gene(4); //number of days for ADX
		n4 = individual.get_gene(6); //number of days for RSI
		Day day;
		int temp1=0, temp2=0, temp3=0, temp4=0; //temp1 for SMA, temp2 for +DI, -DI, TR, temp3 for ADX, temp4 for RSI
		
		lamdaforDI = 2.0/(n2+1);
		lamdaforADX = 2.0/(n3+1);
		lamdaforRSI = 2.0/(n4+1);
		
		while(it.hasNext())
		{
			day=individual.getDay(it);
			
			if(temp1<n1)
			{
				SMA.add(day.Open);
				current_SMA+=day.Open;
				if(temp1==(n1-1))
					current_SMA/=n1;
				temp1++;
			}
			else
			{
				current_SMA = update_Queue(SMA, current_SMA, day.Open, n1);
				temp1++;
			}
			
			if(temp2<n2)
			{
				current_EMApDM+=day.pDM;
				current_EMAnDM+=day.nDM;
				current_EMA_TR+=day.TR;
				if(temp2==(n2-1))
				{
					current_EMApDM/=n2;
					current_EMAnDM/=n2;
					current_EMA_TR/=n2;
				}
				temp2++;	
			}
			else
			{				
				update_DI(day);
				
				if(temp3<n3)
				{
					current_EMA_ADX_modulus += ADX_modulus;
					if(temp3==(n3-1))
						current_EMA_ADX_modulus/=n3;
					temp3++;
				}
				else
				{
					update_ADX();
					temp3++;
				}
				temp2++;
			}
			
			if(temp4<n4)
			{
				current_EMA_GAIN += day.Gain;
				current_EMA_LOSS += day.Loss;
				if(temp4==(n4-1))
				{
					current_EMA_GAIN /= n4;
					current_EMA_LOSS /= n4;
				}
				temp4++;
			}
			else
			{
				update_RSI(day);
			}
			
			if( (day.date).isEqual(new DateTime(2000,12,29,0,0)) )
				return it;
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
	
	void update_RSI(Day day)
	{
		previous_EMA_GAIN = current_EMA_GAIN;
		previous_EMA_LOSS = current_EMA_LOSS;
		current_EMA_GAIN = lamdaforRSI*day.Gain + (1-lamdaforRSI)*previous_EMA_GAIN; //EMA(AVG_GAIN)
		current_EMA_LOSS = lamdaforRSI*day.Loss + (1-lamdaforRSI)*previous_EMA_LOSS; //EMA(AVG_LOSS)
		RSI=100-100/(1+(current_EMA_GAIN/current_EMA_LOSS));
	}
	
	boolean evaluate_sellsignal(Day day , Individual individual , int stockno)
	{
		LinkedList<Day> stocklist = individual.stocklist;
		if(!stocklist.isEmpty())
		{
			int max_days_to_hold = individual.get_gene(9);
			
			Day first = stocklist.getFirst();
			int stock_purchase_daynumber = first.day_number;
			int current_daynumber = day.day_number;
			if(current_daynumber - stock_purchase_daynumber == max_days_to_hold) //now equal should work
			{
				stocklist.removeFirst();
				individual.costprice -= first.Open;
				individual.profit[stockno] += day.Open - first.Open;
				individual.buycount--;
				individual.buysellcount[stockno]++;
			}
		}
		
		Iterator<Day> stocklist_iterator = stocklist.iterator();
		while(stocklist_iterator.hasNext())
		{
			Day bought_day = stocklist_iterator.next();
			double profit_percentage = (day.Open-bought_day.Open)/bought_day.Open;
			double min_profit_to_sell = individual.get_gene(10)/255.0;
			if(profit_percentage > min_profit_to_sell)
			{
				stocklist.remove(bought_day);
				individual.costprice -= bought_day.Open;
				individual.profit[stockno] += day.Open - bought_day.Open;
				individual.buycount--;
				individual.buysellcount[stockno]++;
			}
				
		}

		double ans=0.0;
		
		//SMA
		if(day.Open<current_SMA)
			ans += individual.get_gene(1)/255.0; //threshold for sma signal. confirm later
		
		//DI
		if(current_nDI > current_pDI)
		{
			if(!flag_DI_sell) {
				flag_DI_sell = true;
				ans += individual.get_gene(3)/255.0;
			}
		}
		else {
			flag_DI_sell = false;
		}
		
		//ADX
		if(ADX > 25)
			ans += individual.get_gene(5)/255.0;
		
		if(RSI < 30)
			ans += individual.get_gene(7)/255.0;
		
		double sell_threshold=individual.get_gene(12)/255.0;
		if(ans>=sell_threshold)
			return true;
		return false;
	}
	
	boolean evaluate_buysignal(Day day, Individual individual)
	{
		double ans=0.0;
		
		//SMA
		if(day.Open > current_SMA)
		{
			if(!flag_SMA) {
				flag_SMA = true;
				ans += individual.get_gene(1)/255.0;
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
				ans += individual.get_gene(3)/255.0;
			}
		}		
		else {
			flag_DI_buy = false;
		}
		
		//ADX
		if(ADX>25)
			ans += individual.get_gene(5)/255.0;
		
		if(RSI>70)
			ans += individual.get_gene(7)/255.0;
		
		update_DI(day);
		update_ADX();
		update_RSI(day);
		
		
		double buy_threshold = individual.get_gene(11)/255.0;
		if(ans>=buy_threshold)
			return true;
		return false;
	}
}
