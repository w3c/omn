package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterface;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformation;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.IetfVnfdData;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * A configuration template that describes a VNF.
 * 
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />(Source path: <i>META-INF\yang\ietf-vnfd.yang</i>):
 * <pre>
 * container VNF-descriptor {
 *     container general-information {
 *         leaf name {
 *             type string;
 *         }
 *         leaf description {
 *             type string;
 *         }
 *         leaf vendor {
 *             type string;
 *         }
 *         leaf version {
 *             type uint8;
 *         }
 *         leaf sharing {
 *             type enumeration;
 *         }
 *     }
 *     container deploy-information {
 *         list virtualization-deployment-unit {
 *             key "index"
 *             leaf index {
 *                 type uint16;
 *             }
 *             container require-resource {
 *                 leaf CPU-unit {
 *                     type uint16;
 *                 }
 *                 leaf memory-size {
 *                     type uint64;
 *                 }
 *                 leaf disk-size {
 *                     type uint64;
 *                 }
 *             }
 *             leaf image-ref {
 *                 type string;
 *             }
 *         }
 *     }
 *     list external-interface {
 *         key "name"
 *         leaf name {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnfd/VNF-descriptor</i>
 * 
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptorBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptorBuilder
 */
public interface VNFDescriptor
    extends
    ChildOf<IetfVnfdData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnfd","2014-01-03","VNF-descriptor");;

    /**
     * General information of a VNF.
     */
    GeneralInformation getGeneralInformation();
    
    /**
     * VNF deployment information.
     */
    DeployInformation getDeployInformation();
    
    /**
     * The interface connected to other VNF.
     */
    List<ExternalInterface> getExternalInterface();

}

