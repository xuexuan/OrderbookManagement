package startup.exchange.com.OrderbookManagement.Entity;

import javax.websocket.Session;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "transaction")
public class TransactionOrder {

	public TransactionOrder()
	{
		
	}
	
	public TransactionOrder(TransactionOrder o_)
	{
		this.side = o_.side;
		this.amount = o_.amount;
		this.price = o_.price;
		this.status = o_.status;
		this.id = o_.id;
		this.unixtimestamp = o_.unixtimestamp;
	}
	
	String side;
	double amount;
	double price;
	String status;
	String id;
	long unixtimestamp;
	Session session;
}
