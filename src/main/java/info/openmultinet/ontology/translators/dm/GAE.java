package info.openmultinet.ontology.translators.dm;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

@Api(name = "omnlib",
version = "v1",
namespace = @ApiNamespace(ownerDomain = "translator.open-multinet.info",
                           ownerName = "translator.open-multinet.info",
                           packagePath=""))
public class GAE extends DeliveryMechanism {

	private static final Logger LOG = Logger.getLogger(REST.class.getName());
    
    @ApiMethod(name = "hi")
	public MyBean test() {
    	LOG.log(Level.INFO, "test");
    	MyBean result = new MyBean();
    	result.setData("hi123");
		return result;
	}
    
    public class MyBean {

        private String myData;

        public String getData() {
            return myData;
        }

        public void setData(String data) {
            myData = data;
        }
    }

}
