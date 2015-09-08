package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.IetfVnfInstanceData;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * VNF instance.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * container VNF-instance {
 *     leaf id {
 *         type uint32;
 *     }
 *     leaf VNFD-name {
 *         type string;
 *     }
 *     container operation {
 *         leaf action {
 *             type enumeration;
 *         }
 *         container parameter {
 *             choice action {
 *                 case migration {
 *                     leaf destination-location {
 *                         type ip-address;
 *                     }
 *                 }
 *                 case scale {
 *                     leaf CPU-unit {
 *                         type uint16;
 *                     }
 *                     leaf memory-size {
 *                         type uint64;
 *                     }
 *                     leaf disk-size {
 *                         type uint64;
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnf-instance/VNF-instance</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstanceBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstanceBuilder
 */
public interface VNFInstance
    extends
    ChildOf<IetfVnfInstanceData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnf-instance","2014-01-03","VNF-instance");;

    /**
     * the instance id.
     */
    java.lang.Long getId();
    
    /**
     * the name of VNF descriptor.
     */
    java.lang.String getVNFDName();
    
    /**
     * Performing an operation on VNF.
     */
    Operation getOperation();

}

