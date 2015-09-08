package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.Parameter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;


/**
 * Performing an operation on VNF.
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />(Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * container operation {
 *     leaf action {
 *         type enumeration;
 *     }
 *     container parameter {
 *         choice action {
 *             case migration {
 *                 leaf destination-location {
 *                     type ip-address;
 *                 }
 *             }
 *             case scale {
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
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnf-instance/VNF-instance/operation</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.OperationBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.OperationBuilder
 */
public interface Operation
    extends
    ChildOf<VNFInstance>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation>
{


    /**
     * The enumeration built-in type represents values from a set of assigned names.
     */
    public enum Action {
        /**
         * Start a VNF instance.
         */
        Start(0),
        
        /**
         * Stop a VNF instance.
         */
        Stop(1),
        
        /**
         * Pause a VNF instance.
         */
        Pause(2),
        
        /**
         * Pause a VNF instance.
         */
        Migrate(3),
        
        /**
         * Add resource to a VNF instance.
         */
        ScaleUp(4),
        
        /**
         * Add resource to a VNF instance.
         */
        ScaleDown(5)
        ;
    
    
        int value;
        static java.util.Map<java.lang.Integer, Action> valueMap;
    
        static {
            valueMap = new java.util.HashMap<>();
            for (Action enumItem : Action.values())
            {
                valueMap.put(enumItem.value, enumItem);
            }
        }
    
        private Action(int value) {
            this.value = value;
        }
        
        /**
         * @return integer value
         */
        public int getIntValue() {
            return value;
        }
    
        /**
         * @param valueArg
         * @return corresponding Action item
         */
        public static Action forValue(int valueArg) {
            return valueMap.get(valueArg);
        }
    }

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnf-instance","2014-01-03","operation");;

    /**
     * The operation on VNF
     */
    Action getAction();
    
    /**
     * The parameters that associated with scale procedure.
     */
    Parameter getParameter();

}

