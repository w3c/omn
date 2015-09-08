package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit;


/**
 * The required source for the VNF.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnfd.yang</i>):
 * <pre>
 * container require-resource {
 *     leaf CPU-unit {
 *         type uint16;
 *     }
 *     leaf memory-size {
 *         type uint64;
 *     }
 *     leaf disk-size {
 *         type uint64;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnfd/VNF-descriptor/deploy-information/virtualization-deployment-unit/require-resource</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResourceBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResourceBuilder
 */
public interface RequireResource
    extends
    ChildOf<VirtualizationDeploymentUnit>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnfd","2014-01-03","require-resource");;

    /**
     * The virtual CPU unit numbers
     */
    java.lang.Integer getCPUUnit();
    
    /**
     * The virtual memory size, unit:KByte.
     */
    BigInteger getMemorySize();
    
    /**
     * The virtual disk size, unit:MByte.
     */
    BigInteger getDiskSize();

}

