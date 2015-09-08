package info.openmultinet.ontology.translators.yang.vnfd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptorBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation.Sharing;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResourceBuilder;

/**
 * This class corresponds to an actual example of of a virtual network function instance
 *
 */
public class VnfdInstanceExample {

	private BigInteger diskSize = new BigInteger("18446744073709");
	private BigInteger memorySize = new BigInteger("184467440737");
	private short version = 10;
	private String nameInterf = "eth0";
	private String extInterf = "eth1";
	private Integer unit = 10;
	private String imageRef = "100";
	private Integer index = 10;
	private Integer index2 = 20;
	private String nameInfo = " storage ";
	private String description = "a storage VNF";
	private String vendor = "cisco";
	private Sharing sharing = Sharing.Sharing;
	
	VirtualizationDeploymentUnitBuilder vdu = new VirtualizationDeploymentUnitBuilder();

	List<VirtualizationDeploymentUnit> vdubList = new ArrayList<VirtualizationDeploymentUnit>();
	List<ExternalInterface> listInterf = new ArrayList<ExternalInterface>();
	
	public List<ExternalInterface> getListInterf() {
		return listInterf;
	}

	public void setListInterf(List<ExternalInterface> listInterf) {
		this.listInterf = listInterf;
	}

	public VNFDescriptor vnfdInstance() {
		
		VnfdBuilderHelper builder = new VnfdBuilderHelper();
		RequireResourceBuilder res = new RequireResourceBuilder();
		DeployInformationBuilder deployInf = new DeployInformationBuilder();
		
		ExternalInterfaceBuilder interf = new ExternalInterfaceBuilder();
		ExternalInterface external = builder.createExternalInterface(nameInterf, interf);
		ExternalInterface external2 = builder.createExternalInterface(extInterf, interf);
		listInterf.add(external);
		listInterf.add(external2);
		RequireResource req = builder.createRequiredResources(unit, diskSize, memorySize, res);
		
		VirtualizationDeploymentUnit vdub = builder.createVirtualizationDeploymentUnit(imageRef, req, index, vdu);
		VirtualizationDeploymentUnit vdub1 = builder.createVirtualizationDeploymentUnit(imageRef, req, index2, vdu);

		vdubList.add(vdub);
		vdubList.add(vdub1);
		deployInf.setVirtualizationDeploymentUnit(vdubList);
		DeployInformation deploy = deployInf.build();
		GeneralInformation info = builder.createGeneralInfo(nameInfo, description, vendor, version, sharing);

		VNFDescriptorBuilder vnfdb = new VNFDescriptorBuilder();
		vnfdb.setGeneralInformation(info);
		vnfdb.setDeployInformation(deploy);

		vnfdb.setExternalInterface(listInterf);
		return vnfdb.build();

	}
	
	public VirtualizationDeploymentUnitBuilder getVdu() {
		return vdu;
	}
	
	public void setVdu(VirtualizationDeploymentUnitBuilder vdu) {
		this.vdu = vdu;
	}
	
	public List<VirtualizationDeploymentUnit> getVdubList() {
		return vdubList;
	}
	
	public void setVdubList(List<VirtualizationDeploymentUnit> vdubList) {
		this.vdubList = vdubList;
	}
}
