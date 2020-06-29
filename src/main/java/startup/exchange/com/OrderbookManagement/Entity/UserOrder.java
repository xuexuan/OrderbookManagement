package startup.exchange.com.OrderbookManagement.Entity;


import lombok.Data;

@Data
public class UserOrder {

	public void CopyFromTransaction(TransactionOrder order_)
	{
		side = order_.side;
		pendingamount = order_.amount;
		doneamount = 0;
		price = order_.price;
		status = order_.status;
		id = order_.id;
		unixtimestamp = order_.unixtimestamp;
	}
	
	String side;
	double pendingamount;
	double doneamount;
	double price;
	String status;
	String id;
	long unixtimestamp;
}
