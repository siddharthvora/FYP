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
import java.util.LinkedList;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;




public class Individual 
{
	byte gene[]=new byte[13];
	public LinkedList<Day> stocklist= new LinkedList<>();
	int buysellcount[]=new int[10];
	double profit[]=new double[10];
	double costprice, fitness;
	int buycount;
	Signal signal = new Signal();
	
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
			gene[4]=(byte) (Math.random()*255);
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
		for(int stockno=0;stockno<1;stockno++) //make 10
		{
			//reading xls file
			FileInputStream file = new FileInputStream(new File("testing.xls"));
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
			System.out.println(profit[stockno]+" "+buysellcount[stockno]);
			display();
		}
		}
		catch(Exception e)
		{
			System.out.println();
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
		}
		System.out.println();
	}
}
