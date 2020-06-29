package startup.exchange.com.OrderbookManagement.Entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;



@Data
@Document(collection = "userporfolio")
public class UserPortfolio {

	String id;
	List<UserOrder> orders;
}
