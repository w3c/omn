package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * General information of a VNF.
 * 
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />(Source path: <i>META-INF\yang\ietf-vnfd.yang</i>):
 * <pre>
 * container general-information {
 *     leaf name {
 *         type string;
 *     }
 *     leaf description {
 *         type string;
 *     }
 *     leaf vendor {
 *         type string;
 *     }
 *     leaf version {
 *         type uint8;
 *     }
 *     leaf sharing {
 *         type enumeration;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>ietf-vnfd/VNF-descriptor/general-information</i>
 * 
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformationBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformationBuilder
 */
public interface GeneralInformation
    extends
    ChildOf<VNFDescriptor>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>
{


    /**
     * The enumeration built-in type represents values from a set of assigned names.
     */
    public enum Sharing {
        /**
         * The VNF could not be shared by more than
        one consumer.
         */
        NonSharing(0),
        
        /**
         * The VNF could be shared, such as virtual
        STB is shared by more than one 
         * consumer.
         */
        Sharing(1)
        ;
    
    
        int value;
        static java.util.Map<java.lang.Integer, Sharing> valueMap;
    
        static {
            valueMap = new java.util.HashMap<>();
            for (Sharing enumItem : Sharing.values())
            {
                valueMap.put(enumItem.value, enumItem);
            }
        }
    
        private Sharing(int value) {
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
         * @return corresponding Sharing item
         */
        public static Sharing forValue(int valueArg) {
            return valueMap.get(valueArg);
        }
    }

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:ietf:params:xml:ns:yang:ietf-vnfd","2014-01-03","general-information");;

    /**
     * the name of this VNF.
     */
    java.lang.String getName();
    
    /**
     * description of this VNF.
     */
    java.lang.String getDescription();
    
    /**
     * the vendor generating this VNF.
     */
    java.lang.String getVendor();
    
    /**
     * the version number.
     */
    java.lang.Short getVersion();
    
    /**
     * The flag shows whether the VNF could be
    shared by more than one consumer.
     */
    Sharing getSharing();

}

