package info.openmultinet.ontology.translators.yang.vnfd;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation.Sharing;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResourceBuilder;


/**
 * This class is used to build the virtual network function descriptor instance
 *
 */
public class VnfdBuilderHelper {
	
	
	public GeneralInformation createGeneralInfo(String nameInfo, String description, String vendor, short version,
			Sharing sharing) {
		GeneralInformationBuilder infoBuilder = new GeneralInformationBuilder();
		infoBuilder.setName(nameInfo);
		infoBuilder.setDescription(description);
		infoBuilder.setVendor(vendor);
		infoBuilder.setVersion(version);
		infoBuilder.setSharing(sharing);
		return infoBuilder.build();
	}
	
	
	public VirtualizationDeploymentUnit createVirtualizationDeploymentUnit(String imageRef, RequireResource reqRes,
			Integer index, VirtualizationDeploymentUnitBuilder vdu) {
		vdu.setImageRef(imageRef);
		vdu.setRequireResource(reqRes);
		vdu.setIndex(index);
		vdu.setKey(new VirtualizationDeploymentUnitKey(index));
		return vdu.build();
	}

	public ExternalInterface createExternalInterface(String nameInterf, ExternalInterfaceBuilder interf) {
		interf.setKey(new ExternalInterfaceKey(nameInterf));
		interf.setName(nameInterf);
		return interf.build();
	}

	public RequireResource createRequiredResources(Integer unit, BigInteger diskSize, BigInteger memorySize,
			RequireResourceBuilder res) {
		res.setCPUUnit(unit);
		res.setDiskSize(diskSize);
		res.setMemorySize(memorySize);
		return res.build();
		
	}
}
