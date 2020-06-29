package startup.exchange.com.OrderbookManagement;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
	//CopyOnWriteArraySet<String> tickImage;
	//CopyOnWriteArraySet<Map.Entry<Session, String>> userDealStatus;
	DealMatcher matcher;
	//Thread dispatchWorker;
	
	private final OrderbookManagementRepository userRepository;
	private final TansactionRepository transactionRepository;
	
	public Orderbook(final OrderbookManagementRepository user_, final TansactionRepository trancsaction_)
	{
		userRepository = user_;
		transactionRepository  = trancsaction_;
		connections = new ConcurrentHashMap<String, ServerEndPoint>();
		matcher = new DealMatcher();
//		Runnable t = () -> {
//			if (!pendingItems.isEmpty())
//			{
//				for (Map.Entry<String, Object> item: pendingItems)
//				{
//					if (item.getKey().contentEquals("broadcast"))
//					{
//						for(ServerEndPoint s: connections)
//						{
//							//s.session send message
//						}
//					}
//					else if (item.getKey().contains("userportfilio"))
//					{
//						Map.Entry<Session, String> value = (Map.Entry<Session, String>)item.getValue();
//						//value.session send message
//					}
//				}
//				pendingItems.clear();
//			}
//			
//			try {
//				while(pendingItems.isEmpty())
//					Thread.currentThread().wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		};
//		dispatchWorker = new Thread(t);
//		dispatchWorker.start();
	}
	
	public void AddConnection(String userid_, ServerEndPoint e) 
	{
		connections.put(userid_, e);
		Optional<UserPortfolio> portfolio = userRepository.findById(userid_);
		if (portfolio.isPresent())
		{
			Gson gson = new Gson();
			String userorders = gson.toJson(portfolio.get().getOrders());
			try {
				e.session.getBasicRemote().sendText(userorders);
			} catch (IOException e1) {
				_log.error("fail to update {} with orders {}",userid_, userorders);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
		
		TransactionOrder o = ParseToOrder(message_);
		o.setId(userid_ + "_" + o.getId()); 
		transactionRepository.save(o);
		
		List<UserOrder> l = matcher.MatchDeal(o);
		Gson gson = new Gson();
		for (UserOrder r: l)
		{
			String userid = r.getId().split("_")[0];
			ServerEndPoint s = connections.get(userid);
			if (s.session.isOpen())
			{
				s.session.getBasicRemote().sendText(gson.toJson(r));
			}
			//update to DB
			
		}
		BroadCast();
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
			Gson gson = new Gson();
			for(ServerEndPoint s : connections.values())
			{
				if (s.session.isOpen())
				{
					s.session.getBasicRemote().sendText(gson.toJson(orders));
				}
			}
		}
	}
}
