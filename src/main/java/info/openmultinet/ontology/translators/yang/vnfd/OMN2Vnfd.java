package info.openmultinet.ontology.translators.yang.vnfd;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_domain_nfv;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This class builds the the virtual network function descriptor out of the omn-domain-nfv ontology
 *
 */
public class OMN2Vnfd
extends AbstractConverter {
	
	private static final Logger LOG = Logger.getLogger(OMN2Vnfd.class.getName());
	private String nameExternalInterf;
	private String nameGeneralInfo;
	private String description;
	private String vendor;
	private String version;
	private String sharing;
	private String cPUunit;
	private String diskSize;
	private String memorySize;
	private String index;
	private String imageRef;
	private Sharing isSharing;
	
	public VNFDescriptor getDescriptor(Model model)
			throws InvalidModelException {
		VnfdBuilderHelper builder = new VnfdBuilderHelper();
		List<ExternalInterface> listInterf = new ArrayList<ExternalInterface>();
		ExternalInterfaceBuilder interf = new ExternalInterfaceBuilder();
		VirtualizationDeploymentUnitBuilder vdu = new VirtualizationDeploymentUnitBuilder();
		RequireResourceBuilder res = new RequireResourceBuilder();
		List<VirtualizationDeploymentUnit> vdubList = new ArrayList<VirtualizationDeploymentUnit>();
		extractModelInfo(model, builder, vdu, res, vdubList, listInterf, interf);
		return buildInstance(builder, vdubList, listInterf);
		
	}

	private VNFDescriptor buildInstance(VnfdBuilderHelper builder, List<VirtualizationDeploymentUnit> vdubList,
			List<ExternalInterface> listInterf) {
		
		DeployInformationBuilder deployInf = new DeployInformationBuilder();

		if (sharing.equals("Sharing")) {
			isSharing = Sharing.Sharing;
		} else {
			isSharing = Sharing.NonSharing;
		}
		
		deployInf.setVirtualizationDeploymentUnit(vdubList);
		DeployInformation deploy = deployInf.build();
		GeneralInformation info = builder.createGeneralInfo(nameGeneralInfo, description, vendor,
				Short.valueOf(version), isSharing);
		
		VNFDescriptorBuilder vnfdb = new VNFDescriptorBuilder();
		vnfdb.setGeneralInformation(info);
		vnfdb.setDeployInformation(deploy);
		vnfdb.setExternalInterface(listInterf);
		
		return vnfdb.build();
	}

	private void extractModelInfo(Model model, VnfdBuilderHelper builder, VirtualizationDeploymentUnitBuilder vdu,
			RequireResourceBuilder res, List<VirtualizationDeploymentUnit> vdubList,
			List<ExternalInterface> listInterf, ExternalInterfaceBuilder interf)
					throws InvalidModelException {
		
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type, Omn_domain_nfv.VnfDescriptor).toList();
		
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();
		if (group.hasProperty(Omn_domain_nfv.hasExternalInterfaces)) {
			extractExternalInterfacesInfo(model, group, listInterf, interf, builder);
		}
		if (group.hasProperty(Omn_domain_nfv.hasGeneralInfo)) {
			extractGeneralInfo(model, group);
		}
		if (group.hasProperty(Omn_domain_nfv.hasDeployInfo)) {
			extractDeployInfo(model, group, builder, vdubList, vdu, res);
		}

	}
	
	private void extractDeployInfo(Model model, Resource group, VnfdBuilderHelper builder,
			List<VirtualizationDeploymentUnit> vdubList, VirtualizationDeploymentUnitBuilder vdu,
			RequireResourceBuilder res) {

		Statement deployInfo = group.getProperty(Omn_domain_nfv.hasDeployInfo);
		StmtIterator resourceIterator = deployInfo.getResource().listProperties(Omn_domain_nfv.hasVDU);
		while (resourceIterator.hasNext()) {
			Resource vduInfo = resourceIterator.next().getResource();

			index = vduInfo.getProperty(Omn_domain_nfv.hasIndex).getObject().asLiteral().toString();
			imageRef = vduInfo.getProperty(Omn_domain_nfv.hasImageRef).getObject().asLiteral().toString();
			Statement reqRes = vduInfo.getProperty(Omn_domain_nfv.hasRequiredResources);
			cPUunit = reqRes.getProperty(Omn_domain_nfv.hasCPUunit).getObject().asLiteral().toString();
			diskSize = reqRes.getProperty(Omn_domain_nfv.hasDiskSize).getObject().asLiteral().toString();
			memorySize = reqRes.getProperty(Omn_domain_nfv.hasMemorySize).getObject().asLiteral().toString();

			RequireResource req = builder.createRequiredResources(Integer.valueOf(cPUunit),
					BigInteger.valueOf(Long.valueOf(diskSize)), BigInteger.valueOf(Long.valueOf(memorySize)), res);

			VirtualizationDeploymentUnit vdub = builder.createVirtualizationDeploymentUnit(imageRef, req,
					Integer.valueOf(index), vdu);

			vdubList.add(vdub);
		}
	}
	
	private void extractGeneralInfo(Model model, Resource group) {
		Statement generalInfo = group.getProperty(Omn_domain_nfv.hasGeneralInfo);
		nameGeneralInfo = generalInfo.getProperty(Omn_domain_nfv.hasName).getObject().asLiteral().toString();
		description = generalInfo.getProperty(Omn_domain_nfv.hasDescription).getObject().asLiteral().toString();
		vendor = generalInfo.getProperty(Omn_domain_nfv.hasVendor).getObject().asLiteral().toString();
		version = generalInfo.getProperty(Omn_domain_nfv.hasVersion).getObject().asLiteral().toString();
		sharing = generalInfo.getProperty(Omn_domain_nfv.isSharing).getObject().asLiteral().toString();
		
	}
	
	private void extractExternalInterfacesInfo(Model model, Resource group, List<ExternalInterface> listInterf,
			ExternalInterfaceBuilder interf, VnfdBuilderHelper builder) {
		StmtIterator resourceIterator = group.listProperties(Omn_domain_nfv.hasExternalInterfaces);
		while (resourceIterator.hasNext()) {
			Resource nodeResource = resourceIterator.next().getResource();
			nameExternalInterf = nodeResource.getProperty(Omn_domain_nfv.hasName).getObject().asLiteral().toString();
			ExternalInterface external = builder.createExternalInterface(nameExternalInterf, interf);
			listInterf.add(external);

		}
	}
}
