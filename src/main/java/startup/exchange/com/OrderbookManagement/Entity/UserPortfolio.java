package startup.exchange.com.OrderbookManagement.Entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;



@Data
@Document(collection = "user_portfolio")
public class UserPortfolio {

	String id;
	Double total_amount;
	List<UserOrder> orders;
}
