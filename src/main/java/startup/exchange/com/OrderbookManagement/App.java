package startup.exchange.com.OrderbookManagement;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@SpringBootApplication
public class App 
{
	 public static void main( String[] args )
	    {
		 	ConfigurableApplicationContext configApp = SpringApplication.run(App.class, args);
		 	ServerEndPoint.SetApplicationContext(configApp);
	    }
	    
	    @Bean
	    public ServerEndpointExporter serverEndpointExporter(){
	        return new ServerEndpointExporter();
	    }
}
