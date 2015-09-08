package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit;


/**
 * VNF deployment information.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnfd.yang</i>):
 * <pre>
 * container deploy-information {
 *     list virtualization-deployment-unit {
 *         key "index"
 *         leaf index {
 *             type uint16;
 *         }
 *         container require-resource {
 *             leaf CPU-unit {
 *                 type uint16;
 *             }
 *             leaf memory-size {
 *                 type uint64;
 *             }
 *             leaf disk-size {
 *                 type uint64;
 *             }
 *         }
 *         leaf image-ref {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnfd/VNF-descriptor/deploy-information</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformationBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformationBuilder
 */
public interface DeployInformation
    extends
    ChildOf<VNFDescriptor>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformation>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnfd","2014-01-03","deploy-information");;

    /**
     * Used to store the deployment parameters of VNF.
     */
    List<VirtualizationDeploymentUnit> getVirtualizationDeploymentUnit();

}

