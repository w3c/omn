package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitKey;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * Used to store the deployment parameters of VNF.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnfd.yang</i>):
 * <pre>
 * list virtualization-deployment-unit {
 *     key "index"
 *     leaf index {
 *         type uint16;
 *     }
 *     container require-resource {
 *         leaf CPU-unit {
 *             type uint16;
 *         }
 *         leaf memory-size {
 *             type uint64;
 *         }
 *         leaf disk-size {
 *             type uint64;
 *         }
 *     }
 *     leaf image-ref {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnfd/VNF-descriptor/deploy-information/virtualization-deployment-unit</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitBuilder@see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitKey
 */
public interface VirtualizationDeploymentUnit
    extends
    ChildOf<DeployInformation>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>,
    Identifiable<VirtualizationDeploymentUnitKey>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnfd","2014-01-03","virtualization-deployment-unit");;

    /**
     * the VDU index.
     */
    java.lang.Integer getIndex();
    
    /**
     * The required source for the VNF.
     */
    RequireResource getRequireResource();
    
    /**
     * the software image associated with the VNF.
     */
    java.lang.String getImageRef();
    
    /**
     * Returns Primary Key of Yang List Type
     */
    VirtualizationDeploymentUnitKey getKey();

}

