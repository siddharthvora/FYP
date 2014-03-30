/*
 * 1. Simple Moving Average (n,w) (0,1)
 * 2. +Di, -Di				(n,w) (2,3)
 * 3. ADX					(n,w) (4,5)
 * 4. RSI					(n,w) (6,7)
 * 5. ADI					(w)   (8)
 * 6. Max Days to Hold		(n)   (9)
 * 7. Minimum profit to		(%profit) (10) 
 *    sell stock
 * 
 * 8. threshold for buy signal (11)
 * 9. threshold for sell signal (12)
 * n=255, w=0-100 for first 5 genes
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;

import java.util.concurrent.CopyOnWriteArrayList;


public class Individual implements Comparable<Individual>
{
	byte gene[]=new byte[13];
	public List<Day> stocklist= new CopyOnWriteArrayList<>();
	
	int buysellcount[]=new int[10];
	int buysellcount_training[];
	int buysellcount_validation[];
	
	double profit[]=new double[10];
	double profit_training[];
	double profit_validation[];
	
	Boolean flag=false;
	double costprice, fitness;
	int buycount;
	Signal signal = new Signal();
	int stockno;
	DateTime start_of_trading_period = new DateTime(2006,01,3,0,0); //added for outofsample period
	
	int buysellcount_2=0;
	/*String stocks[]= { "Asahi","Nippon","Nissin","Oji",
						"Shimizu","Sumitomo Chemical","Sumitomo Heavy"
						, "Sumitomo" , "Toshiba" , "Toyota" };*/
	
	
	public void random_intialize()
	{
		do {
			gene[0]=(byte) (Math.random()*255);
		}while(gene[0]==0);
		
		gene[1]=(byte) (Math.random()*255);
		
		do {
			gene[2]=(byte) (Math.random()*255);
		}while(gene[2]==0);
		
		gene[3]=(byte) (Math.random()*255);
		
		do {
			gene[4]=(byte) (Math.random()*127);
		}while(gene[4]==0);
		
		gene[5]=(byte) (Math.random()*255);
		
		do {
			gene[6]=(byte) (Math.random()*255);
		}while(gene[6]==0);
		
		gene[6]=(byte) (Math.random()*255);
		gene[7]=(byte) (Math.random()*255);
		gene[8]=(byte) (Math.random()*255);
		gene[9]=(byte) (Math.random()*255);	
		gene[10]=(byte) (Math.random()*255);
		gene[11]=(byte) (Math.random()*255);
		gene[12]=(byte) (Math.random()*255);
		
	}
	
	public void weight_normalize()
	{
		double ans;
		int w1,w2,w3,w4,w5;
		w1 = get_gene(1);
		w2 = get_gene(3);
		w3 = get_gene(5);
		w4 = get_gene(7);
		w5 = get_gene(8);
		ans = (w1 + w2 + w3 + w4 + w5)/255.0;
		gene[1] = (byte) (Math.round(w1/ans));
		gene[3] = (byte) (Math.round(w2/ans));
		gene[5] = (byte) (Math.round(w3/ans));
		gene[7] = (byte) (Math.round(w4/ans));
		gene[8] = (byte) (Math.round(w5/ans));
	}
	
	public void gene_check()
	{
		if(gene[0]==0) gene[0] = 1;
		if(gene[2]==0) gene[2] = 1;
		if(gene[4]==0) gene[4] = 1;
		if(gene[6]==0) gene[6] = 1;
		if(gene[4]>127) gene[4] = 127;
	}
	
	public int get_gene(int i)
	{
		int x; 
		x = gene[i] & 0xFF;
		return x;
	}
	
	public void evaluateFitness() throws NumberFormatException, IOException
	{
		try
		{
		for(stockno=0;stockno<1;stockno++) //make 10
		{
			//reading xls file
			//FileInputStream file = new FileInputStream(new File(stocks[stockno] + "(Training).xls"));
			FileInputStream file = new FileInputStream(new File("S&P"+ "(Training).xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheetAt(0);
					
			Iterator<Row> rowIterator = sheet.iterator();
			rowIterator.next(); //for headings of column
			rowIterator.next(); //for first row as it does not contain other data
			
			rowIterator=signal.initalizeQueue(rowIterator, this);
			Day day=null;
			while(rowIterator.hasNext())
			{
				day=getDay(rowIterator);
				boolean sell_signal = signal.evaluate_sellsignal(day, this, stockno);
				if(sell_signal)
				{
					sell_stocks(day,stockno);
				}
				
				boolean buy_signal=signal.evaluate_buysignal(day, this);
				if(buy_signal)
				{
					buy_stock(day,stockno);
				}
				
			}
			sell_stocks(day, stockno);
			buycount=0;
			costprice=0;
			fitness +=profit[stockno];
			flag=true;
			file.close();
			//System.out.println(profit[stockno]+" "+buysellcount[stockno]);
			//display();
		}
		}
		catch(Exception e)
		{
			System.out.println(stockno);
			display();
			throw e;
			
			//System.exit(0);
		}
	}
	
	public void evaluateFitnesson_ValidationData() throws NumberFormatException, IOException
	{
		profit_training = new double[10];
		buysellcount_training = new int[10];
		for(int i=0; i<10; i++)
		{
			profit_training[i] = profit[i];
			profit[i] = 0;
			
			buysellcount_training[i] = buysellcount[i];
			buysellcount[i]=0;
		}
		fitness=0;
		try
		{
		for(stockno=0;stockno<1;stockno++) //make 10
		{
			//reading xls file
			buysellcount_2=0;
			FileInputStream file = new FileInputStream(new File("S&P" + "(Validation).xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheetAt(0);
					
			Iterator<Row> rowIterator = sheet.iterator();
			rowIterator.next(); //for headings of column
			rowIterator.next(); //for first row as it does not contain other data
			
			signal = new Signal();
			start_of_trading_period = new DateTime(1996,01,8,0,0);
			
			rowIterator=signal.initalizeQueue(rowIterator, this);
			Day day=null;
			while(rowIterator.hasNext())
			{
				day=getDay(rowIterator);
				boolean sell_signal = signal.evaluate_sellsignal(day, this, stockno);
				if(sell_signal)
				{
					sell_stocks(day,stockno);
				}
				
				boolean buy_signal=signal.evaluate_buysignal(day, this);
				if(buy_signal)
				{
					buy_stock(day,stockno);
				}
				
			}
			sell_stocks(day, stockno);
			buycount=0;
			costprice=0;
			fitness +=profit[stockno];
			flag=true;
			file.close();
			//System.out.println(profit[stockno]+" "+buysellcount[stockno]);
			//display();
		}
		}
		catch(Exception e)
		{
			System.out.println(stockno);
			display();
			throw e;
			
			//System.exit(0);
		}
	}
	
	public void evaluateFitnesson_OutOfSampleData() throws NumberFormatException, IOException
	{
		buysellcount_2=0;
		profit_validation = new double[10];
		buysellcount_validation = new int[10];
		for(int i=0; i<10; i++)
		{
			profit_validation[i] = profit[i];
			profit[i] = 0;
			
			buysellcount_validation[i] = buysellcount[i];
			buysellcount[i]=0;
		}
		fitness=0;
		try
		{
		for(stockno=0;stockno<1;stockno++) //make 10
		{
			//reading xls file
			FileInputStream file = new FileInputStream(new File("S&P"+ "(Testing).xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheetAt(0);
					
			Iterator<Row> rowIterator = sheet.iterator();
			rowIterator.next(); //for headings of column
			rowIterator.next(); //for first row as it does not contain other data
			
			signal = new Signal();
			start_of_trading_period = new DateTime(2012,01,9,0,0);
			
			rowIterator=signal.initalizeQueue(rowIterator, this);
			Day day=null;
			while(rowIterator.hasNext())
			{
				day=getDay(rowIterator);
				boolean sell_signal = signal.evaluate_sellsignal(day, this, stockno);
				if(sell_signal)
				{
					sell_stocks(day,stockno);
				}
				
				boolean buy_signal=signal.evaluate_buysignal(day, this);
				if(buy_signal)
				{
					buy_stock(day,stockno);
				}
				
			}
			sell_stocks(day, stockno);
			buycount=0;
			costprice=0;
			fitness +=profit[stockno];
			flag=true;
			file.close();
			//System.out.println(profit[stockno]+" "+buysellcount[stockno]);
			//display();
		}
		}
		catch(Exception e)
		{
			System.out.println(stockno);
			display();
			throw e;
			
			//System.exit(0);
		}
	}
	
	void sell_stocks(Day day,int i)
	{
		profit[i]+=day.Open*buycount-costprice;
		buysellcount[i]+=buycount;
		buycount=0;
		costprice=0;
		stocklist.clear();
	}
	
	void buy_stock(Day day, int i)
	{
		stocklist.add(day);
		costprice += day.Open;
		buycount++;
	}
	
	Day getDay(Iterator<Row> rowIterator)
	{
		Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
		Day day = new Day();
		
		Cell cell=cellIterator.next();
		day.date = new DateTime(cell.getDateCellValue());
		
		cell=cellIterator.next();
		day.Open = cell.getNumericCellValue();
		
		cell = cellIterator.next();
		day.High = cell.getNumericCellValue();
		
		cell = cellIterator.next();
		day.Low = cell.getNumericCellValue();
		
		cell=cellIterator.next();	
		day.Close = cell.getNumericCellValue();    
		
		cell=cellIterator.next();
		day.Volume = (int)cell.getNumericCellValue();
		
		cell=cellIterator.next();// for upmove
		cell=cellIterator.next();// for downmove
		
		cell=cellIterator.next();
		day.pDM = cell.getNumericCellValue();
		
		cell=cellIterator.next();
		day.nDM = cell.getNumericCellValue();
		
		cell=cellIterator.next();
		day.TR = cell.getNumericCellValue();
		
		cell=cellIterator.next();
		day.Gain = cell.getNumericCellValue();
		
		cell=cellIterator.next();
		day.Loss = cell.getNumericCellValue();
		
		day.day_number = cell.getRowIndex();
		
		return day;
		
	}
	
	void display()
	{
		for(int i=0;i<13;i++)
		{
			System.out.print((gene[i] & 0xFF)+" ");
			if(i==12) System.out.print(fitness);
		}
		System.out.print(" " + buysellcount_2 + " " + buysellcount[0] );
		System.out.println();
	}

	@Override
	public int compareTo(Individual arg0) {
		// TODO Auto-generated method stub
		if((arg0.fitness-this.fitness)>0)
			return 1;
		else if((arg0.fitness-this.fitness)<0)
			return -1;
		return 0;
	}
	
	String toBinary()
	{
	    StringBuilder sb = new StringBuilder(gene.length * Byte.SIZE);
	    for( int i = 0; i < Byte.SIZE * gene.length; i++ )
	        sb.append((gene[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
	    return sb.toString();
	}
	
	void fromBinary(String s )
	{
		char[] chars = s.toCharArray();
		int p=0;
		for(int i=0;i<13;i++)
		{
			int k = 0;
			for(int j=7;j>=0;j--)
			{
				k += ((chars[p++])=='0'?0:1)*Math.pow(2,j);
			}
			gene[i] = (byte) k;
		}
	}

	boolean isEqualto(Individual individual)
	{
		for(int i=0; i<13; i++)
		{
			if(gene[i] != individual.gene[i])
				return false;
		}
		return true;
	}
}
