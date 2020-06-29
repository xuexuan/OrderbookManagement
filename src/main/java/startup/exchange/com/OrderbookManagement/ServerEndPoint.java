package startup.exchange.com.OrderbookManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


//dispatch userid by IP
@Component
@ServerEndpoint(value="/demo/basecoin/{userid}")
public class ServerEndPoint {

	Logger _log = LoggerFactory.getLogger(ServerEndPoint.class);
	
	Session session;
	String userid;
	List<String> userPortfolio = new ArrayList<String>();
	private static ApplicationContext applicationContext;
	
	public static void SetApplicationContext(ApplicationContext context_)
	{
		ServerEndPoint.applicationContext = context_;
	}
	
	private Orderbook _orderbook;
	
	@OnOpen
	public void onOpen(@PathParam("userid") String userid_, Session session_) throws IOException{
		_log.info("user {} request connection", userid_);
		session = session_;
		userid = userid_;
		_orderbook = applicationContext.getBean(Orderbook.class);
		_orderbook.AddConnection(userid_, this);
		//get list from DB
		//send list to client
	}
	
	@OnMessage
	public void onMessage(Session session, String message)  throws IOException, EncodeException{
		//insert transaction into DB
		_log.info("user {} request order {}", userid, message);
		_orderbook.HandleTransistion(session, userid, message);
		//update deal status into user DB
		//update userPortfolio in cache.
	}
	
	@OnClose
    public void onClose(Session session) throws IOException {
		_log.info("user {} close connection", userid);
		_orderbook.RemoveConnection(this);
	}
	
	@OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
		try {
			onClose(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			_log.error("close due to error");
			e.printStackTrace();
		}
    }
}
