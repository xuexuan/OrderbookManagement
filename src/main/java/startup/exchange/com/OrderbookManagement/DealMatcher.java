package startup.exchange.com.OrderbookManagement;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import startup.exchange.com.OrderbookManagement.Entity.TransactionOrder;
import startup.exchange.com.OrderbookManagement.Entity.UserOrder;


//Id   Side    Time   Qty   Price   Qty    Time   Side  
//---+------+-------+-----+-------+-----+-------+------
//#3                        20.30   200   09:05   SELL  
//#1                        20.30   100   09:01   SELL  
//#2                        20.25   100   09:03   SELL
//#7   BUY    10:11   250   20.35
//#5   BUY    09:08   200   20.20                       
//#4   BUY    09:06   100   20.15                       
//#6   BUY    09:09   200   20.15


public class DealMatcher {
	Logger _log = LoggerFactory.getLogger(Orderbook.class);
	
	List<TransactionOrder> buylist;
	List<TransactionOrder> selllist;
	
	
	public synchronized List<UserOrder> MatchDeal(TransactionOrder o_) {
		
		if (o_.getStatus().contentEquals("cancel"))
		{
			o_.setStatus("cancel");
			for(TransactionOrder r: buylist)
			{
				if (r.getId() == o_.getId())
				{
					buylist.remove(o_);
					break;
				}
			}
			
			for(TransactionOrder r: selllist)
			{
				if (r.getId() == o_.getId())
				{
					selllist.remove(o_);
					break;
				}
			}
		}
		
		o_.setStatus("pending");
		if (o_.getSide().contentEquals("buy"))
		{
			buylist.add(o_);
		}
		if (o_.getSide().contentEquals("sell"))
		{
			selllist.add(o_);
		}
		buylist.sort((TransactionOrder o1, TransactionOrder o2) ->{
			if (o1.getPrice() == o2.getPrice())
			{
				return (int) (o2.getUnixtimestamp() - o1.getUnixtimestamp());
			}
			else
			{
				return (int) (o2.getPrice() - o1.getPrice());
			}
		});
		
		selllist.sort((TransactionOrder o1, TransactionOrder o2) ->{
			if (o1.getPrice() == o2.getPrice())
			{
				return (int) (o1.getUnixtimestamp() - o2.getUnixtimestamp());
			}
			else
			{
				return (int) (o1.getPrice() - o2.getPrice());
			}
		});
		
		List<UserOrder> updateList = new ArrayList<UserOrder>();
		for (TransactionOrder b: buylist)
		{
			if (b.getPrice() < selllist.get(0).getPrice())
				break;
			
			for (TransactionOrder s: selllist)
			{
				int tmp_amount = (int) b.getAmount();
				if (s.getPrice() <= b.getPrice() && tmp_amount != 0)
				{
					if (s.getAmount() <= tmp_amount)
					{
						selllist.remove(s);
						UserOrder neworder = new UserOrder();
						neworder.CopyFromTransaction(s);
						neworder.setStatus("done");
						neworder.setPendingamount(0);
						neworder.setDoneamount(s.getAmount());
						updateList.add(neworder);
						tmp_amount -= s.getAmount();
					}
					else
					{
						UserOrder neworder = new UserOrder();
						neworder.CopyFromTransaction(s);
						neworder.setDoneamount(tmp_amount);
						neworder.setPendingamount(s.getAmount() - tmp_amount);
						neworder.setStatus("partial");
						updateList.add(neworder);
						tmp_amount = 0;
					}
					
					if (tmp_amount == 0)
					{
						buylist.remove(b);
						UserOrder neworder = new UserOrder();
						neworder.CopyFromTransaction(b);
						neworder.setDoneamount(b.getAmount());
						neworder.setPendingamount(0);
						neworder.setStatus("done");
						updateList.add(neworder);
					}
					else
					{
						UserOrder neworder = new UserOrder();
						neworder.CopyFromTransaction(b);
						neworder.setDoneamount(b.getAmount() - tmp_amount);
						neworder.setPendingamount(tmp_amount);
						neworder.setStatus("partial");
						updateList.add(neworder);
					}
				}
			}
		}
		
		return updateList;
	}
	
	public synchronized List<TransactionOrder> GetTop10Orders()
	{
		List<TransactionOrder> total = new ArrayList<TransactionOrder>();
		for (TransactionOrder o : buylist)
		{
			total.add(o);
		}
		
		for (TransactionOrder o : selllist)
		{
			total.add(o);
		}
		return total;
	}
}
