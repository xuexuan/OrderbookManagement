package startup.exchange.com.OrderbookManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.server.ServerEndpointConfig;

public class SocketServerConfiguration extends ServerEndpointConfig.Configurator implements ApplicationContextAware
{
    private static volatile BeanFactory context;
	Logger _log = LoggerFactory.getLogger(SocketServerConfiguration.class);

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException
    {
         return context.getBean(clazz);
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		SocketServerConfiguration.context = applicationContext;
	}
	
//	@Override
//	public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
//		_log.info(request.getHeaders().toString());
//		_log.info("user ip: "+ getIPAddress(config));
//	}
	
//	private String getIPAddress(ServerEndpointConfig config) {
//		String rtnVal = null;
		
//		InetSocketAddress remoteAddress = (InetSocketAddress)config.getUserProperties().get(REMOTE_ADDRESS);
//		if (remoteAddress != null) {
//			rtnVal = remoteAddress.getAddress().getHostAddress();
//		}
		
//		return rtnVal;
//	}
}
