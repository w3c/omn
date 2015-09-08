package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.Action;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import java.math.BigInteger;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * case scale {
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
 * <i>ietf-vnf-instance/VNF-instance/operation/parameter/action/scale</i>
 */
public interface Scale
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action.Scale>,
    Action
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnf-instance","2014-01-03","scale");;

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

