/**
 *
 */
package info.openmultinet.ontology.translators.yang.vnfd;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_domain_nfv;

import java.util.logging.Logger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This class constructs the ontology corresponding to the virtual network function descriptor
 *
 */
public class Vnfd2OMN
		extends AbstractConverter {
	
	private static final Logger LOG = Logger.getLogger(Vnfd2OMN.class.getName());
	static VnfdInstanceExample exple = new VnfdInstanceExample();
	static VNFDescriptor desc = exple.vnfdInstance();

	public static VNFDescriptor getDesc() {
		return desc;
	}

	public static void setDesc(VNFDescriptor desc) {
		Vnfd2OMN.desc = desc;
	}

	public static Model getModel() {
		final Model model = ModelFactory.createDefaultModel();
		final Resource nfvd = model.createResource(AbstractConverter.NAMESPACE + "vnfdExapmle");
		nfvd.addProperty(RDF.type, Omn_domain_nfv.VnfDescriptor);
		addGeneralInfo(nfvd, model, desc);

		int index = 0;
		while (index < exple.getListInterf().size()) {

			addExternalInterfaces(nfvd, model, index);
			index++;
		}
		
		final Resource res = model.createResource();
		nfvd.addProperty(Omn_domain_nfv.hasDeployInfo, res);
		int i = 0;
		while (i < exple.getVdubList().size()) {

			addVduInfo(model, i, res);
			i++;
		}
		
		Parser.setCommonPrefixes(model);
		return model;
	}
	
	private static void addGeneralInfo(Resource nfvd, Model model, VNFDescriptor desc2) {
		nfvd.addProperty(
				Omn_domain_nfv.hasGeneralInfo,
				model.createResource().addProperty(Omn_domain_nfv.hasName, desc.getGeneralInformation().getName())
				.addProperty(Omn_domain_nfv.hasDescription, desc.getGeneralInformation().getDescription())
				.addProperty(Omn_domain_nfv.hasVendor, desc.getGeneralInformation().getVendor())
				.addProperty(Omn_domain_nfv.hasVersion, desc.getGeneralInformation().getVersion().toString())
				.addProperty(Omn_domain_nfv.isSharing, desc.getGeneralInformation().getSharing().toString()));
		
	}
	
	private static void addVduInfo(Model model, Integer index, Resource res) {
		
		res = res.addProperty(
				Omn_domain_nfv.hasVDU,
				model.createResource()
				.addProperty(Omn_domain_nfv.hasIndex, exple.getVdubList().get(index).getIndex().toString())
				.addProperty(
						Omn_domain_nfv.hasRequiredResources,
						model.createResource()
						.addProperty(
								Omn_domain_nfv.hasCPUunit,
								exple.getVdubList().get(index).getRequireResource().getCPUUnit()
								.toString())
								.addProperty(
										Omn_domain_nfv.hasMemorySize,
										exple.getVdubList().get(index).getRequireResource().getMemorySize()
										.toString())
										.addProperty(
												Omn_domain_nfv.hasDiskSize,
												exple.getVdubList().get(index).getRequireResource().getDiskSize()
												.toString()))
												.addProperty(Omn_domain_nfv.hasImageRef, exple.getVdubList().get(index).getImageRef()));

	}

	public static void addExternalInterfaces(Resource nfvd, Model model, Integer index) {
		nfvd.addProperty(Omn_domain_nfv.hasExternalInterfaces,
				model.createResource().addProperty(Omn_domain_nfv.hasName, exple.getListInterf().get(index).getName()));
	}
	
	// protected static String getRDFNamespace(String namespace) {
	// if (!(namespace.endsWith("/") || namespace.endsWith("#"))) {
	// namespace = namespace.concat("#");
	// }
	// return namespace;
	// }
	
}
