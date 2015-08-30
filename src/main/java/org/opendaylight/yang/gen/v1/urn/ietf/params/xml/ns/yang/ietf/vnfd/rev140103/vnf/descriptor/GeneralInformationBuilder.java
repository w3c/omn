package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation.Sharing;
import java.util.HashMap;
import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.List;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation
 */
public class GeneralInformationBuilder {

    private java.lang.String _description;
    private java.lang.String _name;
    private Sharing _sharing;
    private java.lang.String _vendor;
    private java.lang.Short _version;
    private static List<Range<BigInteger>> _version_range;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> augmentation = new HashMap<>();

    public GeneralInformationBuilder() {
    } 

    public GeneralInformationBuilder(GeneralInformation base) {
        this._description = base.getDescription();
        this._name = base.getName();
        this._sharing = base.getSharing();
        this._vendor = base.getVendor();
        this._version = base.getVersion();
        if (base instanceof GeneralInformationImpl) {
            GeneralInformationImpl _impl = (GeneralInformationImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public java.lang.String getDescription() {
        return _description;
    }
    
    public java.lang.String getName() {
        return _name;
    }
    
    public Sharing getSharing() {
        return _sharing;
    }
    
    public java.lang.String getVendor() {
        return _vendor;
    }
    
    public java.lang.Short getVersion() {
        return _version;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public GeneralInformationBuilder setDescription(java.lang.String value) {
        this._description = value;
        return this;
    }
    
    public GeneralInformationBuilder setName(java.lang.String value) {
        this._name = value;
        return this;
    }
    
    public GeneralInformationBuilder setSharing(Sharing value) {
        this._sharing = value;
        return this;
    }
    
    public GeneralInformationBuilder setVendor(java.lang.String value) {
        this._vendor = value;
        return this;
    }
    
    public GeneralInformationBuilder setVersion(java.lang.Short value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value);
            boolean isValidRange = false;
            for (Range<BigInteger> r : _version_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _version_range));
            }
        }
        this._version = value;
        return this;
    }
    public static List<Range<BigInteger>> _version_range() {
        if (_version_range == null) {
            synchronized (GeneralInformationBuilder.class) {
                if (_version_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(255L)));
                    _version_range = builder.build();
                }
            }
        }
        return _version_range;
    }
    
    public GeneralInformationBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public GeneralInformation build() {
        return new GeneralInformationImpl(this);
    }

    private static final class GeneralInformationImpl implements GeneralInformation {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation.class;
        }

        private final java.lang.String _description;
        private final java.lang.String _name;
        private final Sharing _sharing;
        private final java.lang.String _vendor;
        private final java.lang.Short _version;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> augmentation = new HashMap<>();

        private GeneralInformationImpl(GeneralInformationBuilder base) {
            this._description = base.getDescription();
            this._name = base.getName();
            this._sharing = base.getSharing();
            this._vendor = base.getVendor();
            this._version = base.getVersion();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public java.lang.String getDescription() {
            return _description;
        }
        
        @Override
        public java.lang.String getName() {
            return _name;
        }
        
        @Override
        public Sharing getSharing() {
            return _sharing;
        }
        
        @Override
        public java.lang.String getVendor() {
            return _vendor;
        }
        
        @Override
        public java.lang.Short getVersion() {
            return _version;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_description == null) ? 0 : _description.hashCode());
            result = prime * result + ((_name == null) ? 0 : _name.hashCode());
            result = prime * result + ((_sharing == null) ? 0 : _sharing.hashCode());
            result = prime * result + ((_vendor == null) ? 0 : _vendor.hashCode());
            result = prime * result + ((_version == null) ? 0 : _version.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation other = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation)obj;
            if (_description == null) {
                if (other.getDescription() != null) {
                    return false;
                }
            } else if(!_description.equals(other.getDescription())) {
                return false;
            }
            if (_name == null) {
                if (other.getName() != null) {
                    return false;
                }
            } else if(!_name.equals(other.getName())) {
                return false;
            }
            if (_sharing == null) {
                if (other.getSharing() != null) {
                    return false;
                }
            } else if(!_sharing.equals(other.getSharing())) {
                return false;
            }
            if (_vendor == null) {
                if (other.getVendor() != null) {
                    return false;
                }
            } else if(!_vendor.equals(other.getVendor())) {
                return false;
            }
            if (_version == null) {
                if (other.getVersion() != null) {
                    return false;
                }
            } else if(!_version.equals(other.getVersion())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                GeneralInformationImpl otherImpl = (GeneralInformationImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("GeneralInformation [");
            boolean first = true;
        
            if (_description != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_description=");
                builder.append(_description);
             }
            if (_name != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_name=");
                builder.append(_name);
             }
            if (_sharing != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_sharing=");
                builder.append(_sharing);
             }
            if (_vendor != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_vendor=");
                builder.append(_vendor);
             }
            if (_version != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_version=");
                builder.append(_version);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
