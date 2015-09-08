package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.Action;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * case migration {
 *     leaf destination-location {
 *         type ip-address;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnf-instance/VNF-instance/operation/parameter/action/migration</i>
 */
public interface Migration
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action.Migration>,
    Action
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnf-instance","2014-01-03","migration");;

    /**
     * The ip address of the destination location.
     */
    IpAddress getDestinationLocation();

}

