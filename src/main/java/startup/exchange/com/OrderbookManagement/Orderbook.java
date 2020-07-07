package startup.exchange.com.OrderbookManagement;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import startup.exchange.com.OrderbookManagement.Entity.TransactionOrder;
import startup.exchange.com.OrderbookManagement.Entity.UserOrder;
import startup.exchange.com.OrderbookManagement.Entity.UserPortfolio;
import startup.exchange.com.OrderbookManagement.Repository.OrderbookManagementRepository;
import startup.exchange.com.OrderbookManagement.Repository.TansactionRepository;


//order portfolio store in mongodb
//{
//	"id":"user_id",
//	"orders":[
//	          {
//	        	  "symbol":"basecpin",
//	        	  "side":"buy",
//	        	  "pending_amount":"1000",
//	        	  "done_amount":"0",
//	        	  "price":"96",
//	        	  "time_stamp":"12345566",
//	        	  "id":"order_id"
//	          }
//	          ]
//}

//transaction store in mongodb
//collection by coin type
//{
//	"id":"order_id",
//	"side":"buy",
//	"amount":"10000",
//	"price":"95",
//	"time_stamp":"12344565"
//}

@Service
public class Orderbook {

	Logger _log = LoggerFactory.getLogger(Orderbook.class);
	
	ConcurrentHashMap<String, ServerEndPoint> connections;
	DealMatcher matcher;
	//can be handle in other thread
	AccountPortfolio accPort;
	
	private final TansactionRepository transactionRepository;
	
	public Orderbook(final OrderbookManagementRepository user_, final TansactionRepository trancsaction_)
	{
		transactionRepository  = trancsaction_;
		accPort = new AccountPortfolio(user_);
		connections = new ConcurrentHashMap<String, ServerEndPoint>();
		matcher = new DealMatcher();
	}
	
	public void AddConnection(String userid_, ServerEndPoint e) 
	{
		connections.put(userid_, e);
		String userorders = accPort.GetInitialPortfolio(userid_);
		
		try {
			//personal account
			e.session.getBasicRemote().sendText(userorders);
			Gson gson = new Gson();
			//market tick
			e.session.getBasicRemote().sendText(gson.toJson(matcher.GetTop10Orders()));
		} catch (IOException e1) {
			//_log.error("fail to update {} with orders {}",userid_, userorders);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private TransactionOrder ParseToOrder(String message_) throws JsonParseException {
		TransactionOrder r = new TransactionOrder();
		try {
			Gson gson = new Gson();
			r =  gson.fromJson(message_, TransactionOrder.class);
		}
		catch(JsonParseException e)
		{
			_log.error("fail to parse order meessage: "+ message_);
		}
		return r;
	}
	
	//here need to handle multiple thread
	public Boolean HandleTransistion(Session session_, String userid_, String message_) throws IOException {
		
		//save transaction into DB
		TransactionOrder o = ParseToOrder(message_);
		o.setId(userid_ + "_" + o.getId()); 
		transactionRepository.save(o);
		
		_log.info("Insert: " +message_);
		//start match best orders
		List<UserOrder> l = matcher.MatchDeal(o);
		
		//update tick orders and final price
		BroadCast();
		
		//async call
		UserOrder neworder = new UserOrder();
		neworder.CopyFromTransaction(o);
		List<UserPortfolio> updatedUser = accPort.UpdatePortfolio(neworder, l);
		Gson gson = new Gson();
		for(UserPortfolio u: updatedUser)
		{
			ServerEndPoint s = connections.get(u.getId().split("_")[0]);
			synchronized(s.session)
			{
				if (s.session.isOpen())
				{
					s.session.getBasicRemote().sendText(gson.toJson(u));
				}	
			}
		}
		return true;
	}
	
	public void RemoveConnection(ServerEndPoint e)
	{
		connections.remove(e.userid);
	}
	
	private void BroadCast() throws IOException
	{
		synchronized(this) {
			List<TransactionOrder> orders = matcher.GetTop10Orders();
			Double price = matcher.GetCurrentPrice();
			_log.info("Current price: "+ price);
			Gson gson = new Gson();
			for(ServerEndPoint s : connections.values())
			{
				synchronized(s.session)
				{
					if (s.session.isOpen())
					{
						s.session.getBasicRemote().sendText(gson.toJson(orders));
						s.session.getBasicRemote().sendText(gson.toJson(price));
					}	
				}
			}
		}
	}
}
