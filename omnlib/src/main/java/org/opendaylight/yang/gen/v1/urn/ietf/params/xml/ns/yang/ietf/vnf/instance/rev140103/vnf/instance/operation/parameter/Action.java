package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.QName;


/**
 * Different parameter with different action.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * choice action {
 *     case migration {
 *         leaf destination-location {
 *             type ip-address;
 *         }
 *     }
 *     case scale {
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
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnf-instance/VNF-instance/operation/parameter/action</i>
 */
public interface Action
    extends
    DataContainer
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnf-instance","2014-01-03","action");;


}

