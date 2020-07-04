package startup.exchange.com.OrderbookManagement;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import startup.exchange.com.OrderbookManagement.Entity.TransactionOrder;
import startup.exchange.com.OrderbookManagement.Entity.UserOrder;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
//    	DealMatcher matcher = new DealMatcher();
//    	TransactionOrder order = new TransactionOrder();
//    	List<UserOrder> users = matcher.MatchDeal(order);
//    	
        assertTrue( true );
    }
}
