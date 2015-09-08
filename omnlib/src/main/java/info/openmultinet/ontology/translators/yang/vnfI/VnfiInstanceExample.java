package info.openmultinet.ontology.translators.yang.vnfI;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;

/**
 * This class corresponds to two example of virtual netwok function instances: 
 * one corresponding to the scale action and the other one corresponding to the migration action
 *
 */
public class VnfiInstanceExample {
	
	VnfIBuilderHelper builder = new VnfIBuilderHelper();
	
	public VnfIBuilderHelper getBuilder() {
		return builder;
	}
	
	public void setBuilder(VnfIBuilderHelper builder) {
		this.builder = builder;
	}

	public VNFInstance migrateInstance() {
		
		Ipv4Address ipv4 = new Ipv4Address("192.85.120.75");
		IpAddress destIp = new IpAddress(ipv4);
		VNFInstance vnfi = builder
				.MigrationInstanceCreator(
						destIp,
						10L,
						" storage ",
						org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action.Migrate);
		return vnfi;
		
	}

	public VNFInstance scaleInstance() {
         
		long  id = 520L;
		BigInteger diskSize = new BigInteger("18446744073709");
		BigInteger memorySize = new BigInteger("184467440737");
		VNFInstance vnfi = builder
				.ScaleInstanceCreator(
						diskSize,
						memorySize,
						10,
						id,
						"storage",
						org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action.ScaleUp);

		return vnfi;
		
	}
}
