package startup.exchange.com.OrderbookManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import startup.exchange.com.OrderbookManagement.Entity.UserOrder;
import startup.exchange.com.OrderbookManagement.Entity.UserPortfolio;
import startup.exchange.com.OrderbookManagement.Repository.OrderbookManagementRepository;

public class AccountPortfolio {

	Logger _log = LoggerFactory.getLogger(AccountPortfolio.class);
	private final OrderbookManagementRepository userrepo;
	
	//optimize by thread
	//private CopyOnWriteArrayList<UserPortfolio> pendingAccounts;
	//private Thread worker;
	
	public AccountPortfolio(OrderbookManagementRepository repo_)
	{
		userrepo = repo_;
		//pendingAccounts = new CopyOnWriteArrayList<UserPortfolio>();
	}
	
	public String GetInitialPortfolio(String userid_)
	{
		Optional<UserPortfolio> portfolio = userrepo.findById(userid_);
		
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
			userrepo.save(userport);
			userorders = gson.toJson(userport);
		}
		
		_log.info("userportfolio query {}", userorders);
		return userorders;
	}
	
	
	public List<UserPortfolio> UpdatePortfolio(UserOrder neworder_, List<UserOrder> orders_)
	{
		//get matched order infomation
		//and backup into DB
		
		//Insert new order
		List<UserPortfolio> users = new ArrayList<UserPortfolio>();
		
		Optional<UserPortfolio> n = userrepo.findById(neworder_.getId().split("_")[0]);
		if (n.isPresent() && !neworder_.getStatus().contentEquals("cancel"))
		{
			if (n.get().getOrders() != null)
			{
				n.get().getOrders().add(neworder_);	
			}
			else
			{
				List<UserOrder> l = new ArrayList<UserOrder>();
				l.add(neworder_);
				n.get().setOrders(l);
			}
			users.add(n.get());
		}
				
		
		for (UserOrder r: orders_)
		{
			String userid = r.getId().split("_")[0];
			
			UserPortfolio currentuser = null;
			for (UserPortfolio p: users)
			{
				if (p.getId().contentEquals(userid))
				{
					currentuser = p;
					break;
				}
			}
			
			if (currentuser == null)
			{
				Optional<UserPortfolio> tmp_user = userrepo.findById(userid);
				if (tmp_user.isPresent())
				{
					currentuser = tmp_user.get();
					users.add(currentuser);
				}
			}
			
			
			if (currentuser != null)
			{
				if (currentuser.getOrders() == null)
				{
					List<UserOrder> o = new ArrayList<UserOrder>();
					o.add(r);
					currentuser.setOrders(o);
				}
				else
				{
					double total_doneamount = 0;
					Boolean bMatch = false;
					for(UserOrder u: currentuser.getOrders())
					{
						if (u.getId().contentEquals(r.getId()))
						{
							u.setPendingamount(r.getPendingamount());
							u.setDoneamount(u.getDoneamount() + r.getDoneamount());
							u.setPrice(r.getPrice());
							u.setSide(r.getSide());
							u.setStatus(r.getStatus());
							u.setUnixtimestamp(r.getUnixtimestamp());
							if(u.getSide().contentEquals("buy"))
								total_doneamount -= u.getDoneamount();
							else
								total_doneamount += u.getDoneamount();
							bMatch = true;
							break;
						}
					}
					if (!bMatch)
					{
						currentuser.getOrders().add(r);
					}
					//calculate the rest amount;
					currentuser.setTotal_amount(currentuser.getTotal_amount() + total_doneamount);
				}
			}
		}
		
		//can be delay, so can handle inside other thread
		for (UserPortfolio u: users)
		{
			userrepo.save(u);
		}
		return users;
	}
}
