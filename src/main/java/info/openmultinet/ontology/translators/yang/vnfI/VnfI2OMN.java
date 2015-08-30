package info.openmultinet.ontology.translators.yang.vnfI;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_domain_nfv;

import java.util.logging.Logger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * This class constructs the ontology corresponding to the virtual network function instance
 *
 */
public class VnfI2OMN
		extends AbstractConverter {
	
	private static final Logger LOG = Logger.getLogger(VnfI2OMN.class.getName());
	final static Model model = ModelFactory.createDefaultModel();
	static VnfiInstanceExample instance = new VnfiInstanceExample();
	
	final static Resource nfvi = model.createResource(AbstractConverter.NAMESPACE + "storageInstance");
	
	public static void defaultModel(Resource nfvi, VNFInstance desc) {
		
		nfvi.addProperty(RDF.type, Omn_domain_nfv.VnfInstance);
		nfvi.addProperty(Omn_domain_nfv.hasID, desc.getId().toString());
		nfvi.addProperty(Omn_domain_nfv.hasVnfdName, desc.getVNFDName());
		desc.getOperation().getAction();
		
	}
	
	/**
	 * @return the omn-domain-nfv ontology for a virtual network function instance where the action coresponds to scale
	 */
	public static Model getScaleModel() {
		VNFInstance desc = instance.scaleInstance();
		VnfIBuilderHelper builder = instance.getBuilder();

		defaultModel(nfvi, desc);
		nfvi.addProperty(
				Omn_domain_nfv.hasOperation,
				model.createResource()
						.addProperty(Omn_domain_nfv.hasAction, desc.getOperation().getAction().toString())
						.addProperty(
								Omn_domain_nfv.hasParameter,
								model.createResource().addProperty(
										Omn_domain_nfv.hasScale,
										model.createResource()
												.addProperty(Omn_domain_nfv.hasMemorySize,
														builder.getScaleBuilder().getMemorySize().toString())
												.addProperty(Omn_domain_nfv.hasDiskSize,
														builder.getScaleBuilder().getDiskSize().toString())
												.addProperty(Omn_domain_nfv.hasCPUunit,
														builder.getScaleBuilder().getCPUUnit().toString()))));
		
		Parser.setCommonPrefixes(model);
		return model;
	}
	
	
	/**
	 * @return the omn-domain-nfv ontology for a virtual network function instance where the action coresponds to migration
	 */
	public static Model getMigrateModel() {
		VNFInstance desc = instance.migrateInstance();
		VnfIBuilderHelper builder = instance.getBuilder();

		defaultModel(nfvi, desc);
		nfvi.addProperty(
				Omn_domain_nfv.hasOperation,
				model.createResource()
				.addProperty(Omn_domain_nfv.hasAction, desc.getOperation().getAction().toString())
				.addProperty(
						Omn_domain_nfv.hasParameter,
						model.createResource().addProperty(
								Omn_domain_nfv.migratesTo,
								model.createResource().addProperty(
										Omn_domain_nfv.destIp,
										builder.getMigrationBuilder().getDestinationLocation().getIpv4Address()
										.getValue()))));
		Parser.setCommonPrefixes(model);
		return model;
	}
	
}
