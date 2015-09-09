package info.openmultinet.ontology.translators.yang.vnfI;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_domain_nfv;

import java.util.List;
import java.util.logging.Logger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
/**
 * 
 * This class builds the the virtual network function instance out of the omn-domain-nfv ontology
 *
 */
public class OMN2Vnfi
extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(OMN2Vnfi.class.getName());
	long id;
	String vnfdName;
	Action opAction;
	IpAddress destIp;
	String cPUunit;
	String diskSize;
	String memorySize;

	public VNFInstance getInstanceModel(Model model)
			throws InvalidModelException {
		extractModelInfo(model);
		return buildInstance();

	}

	private VNFInstance buildInstance() {

		VnfIBuilderHelper builder = new VnfIBuilderHelper();
		VNFInstance mig = builder.MigrationInstanceCreator(destIp, id, vnfdName, opAction);
		return mig;
	}

	private void extractModelInfo(Model model)
			throws InvalidModelException {

		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type, Omn_domain_nfv.VnfInstance).toList();
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();
		if (group.hasProperty(Omn_domain_nfv.hasID)) {
			id = group.getProperty(Omn_domain_nfv.hasID).getObject().asLiteral().getLong();
		}
		if (group.hasProperty(Omn_domain_nfv.hasVnfdName)) {
			vnfdName = group.getProperty(Omn_domain_nfv.hasVnfdName).getObject().asLiteral().toString();
		}

		if (group.hasProperty(Omn_domain_nfv.hasOperation)) {
			Statement operation = group.getProperty(Omn_domain_nfv.hasOperation);
			String action = operation.getProperty(Omn_domain_nfv.hasAction).getObject().asLiteral().toString();
			if (action.equals("ScaleUp")) {
				opAction = Action.ScaleUp;
			} else if (action.equals("ScaleDown")) {
				opAction = Action.ScaleDown;
			} else if (action.equals("Start")) {
				opAction = Action.Start;
			} else if (action.equals("Stop")) {
				opAction = Action.Stop;
			} else if (action.equals("Pause")) {
				opAction = Action.Pause;
			} else {
				opAction = Action.Migrate;
			}
			Statement parameter = operation.getProperty(Omn_domain_nfv.hasParameter);
			if (parameter.getResource().hasProperty(Omn_domain_nfv.migratesTo)) {
				Statement migration = parameter.getProperty(Omn_domain_nfv.migratesTo);
				String ipAddr = migration.getProperty(Omn_domain_nfv.destIp).getObject().asLiteral().toString();
				Ipv4Address ipv4 = new Ipv4Address(ipAddr);
				destIp = new IpAddress(ipv4);

			} else {
				Statement scale = parameter.getProperty(Omn_domain_nfv.hasScale);
				cPUunit = scale.getProperty(Omn_domain_nfv.hasCPUunit).getObject().asLiteral().toString();
				diskSize = scale.getProperty(Omn_domain_nfv.hasDiskSize).getObject().asLiteral().toString();
				memorySize = scale.getProperty(Omn_domain_nfv.hasMemorySize).getObject().asLiteral().toString();
			}
		}

	}
	
}
