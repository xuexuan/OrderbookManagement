package startup.exchange.com.OrderbookManagement.Entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;



@Data
@Document(collection = "user_porfolio")
public class UserPortfolio {

	String id;
	List<UserOrder> orders;
}
