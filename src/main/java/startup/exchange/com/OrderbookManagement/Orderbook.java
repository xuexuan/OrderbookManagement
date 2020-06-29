package startup.exchange.com.OrderbookManagement;

import java.io.IOException;
import java.util.ArrayList;
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
		
		String userorders;
		Gson gson = new Gson();
		if (portfolio.isPresent())
		{
			userorders = gson.toJson(portfolio.get().getOrders());
		}
		else
		{
			UserPortfolio userport = new UserPortfolio();
			userport.setTotal_amount(1000000.0);
			userport.setId(userid_);
			userRepository.save(userport);
			userorders = gson.toJson(userport);
		}
		
		try {
			//personal account
			e.session.getBasicRemote().sendText(userorders);
			//market tick
			e.session.getBasicRemote().sendText(gson.toJson(matcher.GetTop10Orders()));
		} catch (IOException e1) {
			_log.error("fail to update {} with orders {}",userid_, userorders);
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
		
		//start match best orders
		List<UserOrder> l = matcher.MatchDeal(o);
		
		//get matched order infomation,
		//ready to update back to client
		//and backup into DB
		Gson gson = new Gson();
		List<UserPortfolio> users = new ArrayList<UserPortfolio>();
		for (UserOrder r: l)
		{
			String userid = r.getId().split("_")[0];
			ServerEndPoint s = connections.get(userid);
			if (s.session.isOpen())
			{
				s.session.getBasicRemote().sendText(gson.toJson(r));
			}
			
			//replace orders inside MongoDB
			Optional<UserPortfolio> tmp_user = userRepository.findById(userid);
			if (tmp_user.isPresent())
			{
				Boolean bFound = false;
				for (UserPortfolio p: users)
				{
					if (p.getId() == tmp_user.get().getId())
					{
						bFound = true;
						break;
					}
				}
				if (bFound == false)
				{
					users.add(tmp_user.get());	
				}
				
				double total_doneamount = 0;
				for(UserOrder u: tmp_user.get().getOrders())
				{
					if (userid + "_" + u.getId() == r.getId())
					{
						u.setDoneamount(r.getDoneamount());
						u.setPrice(r.getPrice());
						u.setSide(r.getSide());
						u.setStatus(r.getStatus());
						u.setUnixtimestamp(r.getUnixtimestamp());
						total_doneamount = u.getDoneamount();
					}
				}
				//calculate the rest amount;
				tmp_user.get().setTotal_amount(tmp_user.get().getTotal_amount() - total_doneamount);
			}
		}
		
		BroadCast();
		
		//can be delay, so can handle inside other thread
		for (UserPortfolio u: users)
		{
			userRepository.save(u);
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
