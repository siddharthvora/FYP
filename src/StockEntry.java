import java.util.Date;


public class StockEntry 
{
	String Company;
	Date date;
	int purchaseprice;
	int quantity;
	
	@SuppressWarnings("deprecation")
	StockEntry(String Company, String date, int purchaseprice, int quantity)
	{
		this.Company=Company;
		this.date=new Date(date);
		this.purchaseprice=purchaseprice;
		this.quantity=quantity;
	}
}
