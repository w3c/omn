package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource} instances.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource
 */
public class RequireResourceBuilder {

    private java.lang.Integer _cPUUnit;
    private static List<Range<BigInteger>> _cPUUnit_range;
    private BigInteger _diskSize;
    private static List<Range<BigInteger>> _diskSize_range;
    private BigInteger _memorySize;
    private static List<Range<BigInteger>> _memorySize_range;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> augmentation = new HashMap<>();

    public RequireResourceBuilder() {
    } 

    public RequireResourceBuilder(RequireResource base) {
        this._cPUUnit = base.getCPUUnit();
        this._diskSize = base.getDiskSize();
        this._memorySize = base.getMemorySize();
        if (base instanceof RequireResourceImpl) {
            RequireResourceImpl _impl = (RequireResourceImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public java.lang.Integer getCPUUnit() {
        return _cPUUnit;
    }
    
    public BigInteger getDiskSize() {
        return _diskSize;
    }
    
    public BigInteger getMemorySize() {
        return _memorySize;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public RequireResourceBuilder setCPUUnit(java.lang.Integer value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value);
            boolean isValidRange = false;
            for (Range<BigInteger> r : _cPUUnit_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _cPUUnit_range));
            }
        }
        this._cPUUnit = value;
        return this;
    }
    public static List<Range<BigInteger>> _cPUUnit_range() {
        if (_cPUUnit_range == null) {
            synchronized (RequireResourceBuilder.class) {
                if (_cPUUnit_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(65535L)));
                    _cPUUnit_range = builder.build();
                }
            }
        }
        return _cPUUnit_range;
    }
    
    public RequireResourceBuilder setDiskSize(BigInteger value) {
        if (value != null) {
            BigInteger _constraint = value;
            boolean isValidRange = false;
            for (Range<BigInteger> r : _diskSize_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _diskSize_range));
            }
        }
        this._diskSize = value;
        return this;
    }
    public static List<Range<BigInteger>> _diskSize_range() {
        if (_diskSize_range == null) {
            synchronized (RequireResourceBuilder.class) {
                if (_diskSize_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, new BigInteger("18446744073709551615")));
                    _diskSize_range = builder.build();
                }
            }
        }
        return _diskSize_range;
    }
    
    public RequireResourceBuilder setMemorySize(BigInteger value) {
        if (value != null) {
            BigInteger _constraint = value;
            boolean isValidRange = false;
            for (Range<BigInteger> r : _memorySize_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _memorySize_range));
            }
        }
        this._memorySize = value;
        return this;
    }
    public static List<Range<BigInteger>> _memorySize_range() {
        if (_memorySize_range == null) {
            synchronized (RequireResourceBuilder.class) {
                if (_memorySize_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, new BigInteger("18446744073709551615")));
                    _memorySize_range = builder.build();
                }
            }
        }
        return _memorySize_range;
    }
    
    public RequireResourceBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public RequireResource build() {
        return new RequireResourceImpl(this);
    }

    private static final class RequireResourceImpl implements RequireResource {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource.class;
        }

        private final java.lang.Integer _cPUUnit;
        private final BigInteger _diskSize;
        private final BigInteger _memorySize;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> augmentation = new HashMap<>();

        private RequireResourceImpl(RequireResourceBuilder base) {
            this._cPUUnit = base.getCPUUnit();
            this._diskSize = base.getDiskSize();
            this._memorySize = base.getMemorySize();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public java.lang.Integer getCPUUnit() {
            return _cPUUnit;
        }
        
        @Override
        public BigInteger getDiskSize() {
            return _diskSize;
        }
        
        @Override
        public BigInteger getMemorySize() {
            return _memorySize;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_cPUUnit == null) ? 0 : _cPUUnit.hashCode());
            result = prime * result + ((_diskSize == null) ? 0 : _diskSize.hashCode());
            result = prime * result + ((_memorySize == null) ? 0 : _memorySize.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource other = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource)obj;
            if (_cPUUnit == null) {
                if (other.getCPUUnit() != null) {
                    return false;
                }
            } else if(!_cPUUnit.equals(other.getCPUUnit())) {
                return false;
            }
            if (_diskSize == null) {
                if (other.getDiskSize() != null) {
                    return false;
                }
            } else if(!_diskSize.equals(other.getDiskSize())) {
                return false;
            }
            if (_memorySize == null) {
                if (other.getMemorySize() != null) {
                    return false;
                }
            } else if(!_memorySize.equals(other.getMemorySize())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                RequireResourceImpl otherImpl = (RequireResourceImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("RequireResource [");
            boolean first = true;
        
            if (_cPUUnit != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_cPUUnit=");
                builder.append(_cPUUnit);
             }
            if (_diskSize != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_diskSize=");
                builder.append(_diskSize);
             }
            if (_memorySize != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_memorySize=");
                builder.append(_memorySize);
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
