package startup.exchange.com.OrderbookManagement.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import startup.exchange.com.OrderbookManagement.Entity.TransactionOrder;


@Repository
public interface TansactionRepository extends MongoRepository<TransactionOrder, String>{

}
