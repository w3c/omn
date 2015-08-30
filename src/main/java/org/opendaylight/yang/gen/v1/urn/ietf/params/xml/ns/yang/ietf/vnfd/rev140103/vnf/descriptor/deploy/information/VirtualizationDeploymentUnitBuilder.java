package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.virtualization.deployment.unit.RequireResource;
import java.util.HashMap;
import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.List;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit
 */
public class VirtualizationDeploymentUnitBuilder {

    private java.lang.String _imageRef;
    private java.lang.Integer _index;
    private static List<Range<BigInteger>> _index_range;
    private VirtualizationDeploymentUnitKey _key;
    private RequireResource _requireResource;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> augmentation = new HashMap<>();

    public VirtualizationDeploymentUnitBuilder() {
    } 

    public VirtualizationDeploymentUnitBuilder(VirtualizationDeploymentUnit base) {
        if (base.getKey() == null) {
            this._key = new VirtualizationDeploymentUnitKey(
                base.getIndex()
            );
            this._index = base.getIndex();
        } else {
            this._key = base.getKey();
            this._index = _key.getIndex();
        }
        this._imageRef = base.getImageRef();
        this._requireResource = base.getRequireResource();
        if (base instanceof VirtualizationDeploymentUnitImpl) {
            VirtualizationDeploymentUnitImpl _impl = (VirtualizationDeploymentUnitImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public java.lang.String getImageRef() {
        return _imageRef;
    }
    
    public java.lang.Integer getIndex() {
        return _index;
    }
    
    public VirtualizationDeploymentUnitKey getKey() {
        return _key;
    }
    
    public RequireResource getRequireResource() {
        return _requireResource;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public VirtualizationDeploymentUnitBuilder setImageRef(java.lang.String value) {
        this._imageRef = value;
        return this;
    }
    
    public VirtualizationDeploymentUnitBuilder setIndex(java.lang.Integer value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value);
            boolean isValidRange = false;
            for (Range<BigInteger> r : _index_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _index_range));
            }
        }
        this._index = value;
        return this;
    }
    public static List<Range<BigInteger>> _index_range() {
        if (_index_range == null) {
            synchronized (VirtualizationDeploymentUnitBuilder.class) {
                if (_index_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(65535L)));
                    _index_range = builder.build();
                }
            }
        }
        return _index_range;
    }
    
    public VirtualizationDeploymentUnitBuilder setKey(VirtualizationDeploymentUnitKey value) {
        this._key = value;
        return this;
    }
    
    public VirtualizationDeploymentUnitBuilder setRequireResource(RequireResource value) {
        this._requireResource = value;
        return this;
    }
    
    public VirtualizationDeploymentUnitBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public VirtualizationDeploymentUnit build() {
        return new VirtualizationDeploymentUnitImpl(this);
    }

    private static final class VirtualizationDeploymentUnitImpl implements VirtualizationDeploymentUnit {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit.class;
        }

        private final java.lang.String _imageRef;
        private final java.lang.Integer _index;
        private final VirtualizationDeploymentUnitKey _key;
        private final RequireResource _requireResource;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> augmentation = new HashMap<>();

        private VirtualizationDeploymentUnitImpl(VirtualizationDeploymentUnitBuilder base) {
            if (base.getKey() == null) {
                this._key = new VirtualizationDeploymentUnitKey(
                    base.getIndex()
                );
                this._index = base.getIndex();
            } else {
                this._key = base.getKey();
                this._index = _key.getIndex();
            }
            this._imageRef = base.getImageRef();
            this._requireResource = base.getRequireResource();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public java.lang.String getImageRef() {
            return _imageRef;
        }
        
        @Override
        public java.lang.Integer getIndex() {
            return _index;
        }
        
        @Override
        public VirtualizationDeploymentUnitKey getKey() {
            return _key;
        }
        
        @Override
        public RequireResource getRequireResource() {
            return _requireResource;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_imageRef == null) ? 0 : _imageRef.hashCode());
            result = prime * result + ((_index == null) ? 0 : _index.hashCode());
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((_requireResource == null) ? 0 : _requireResource.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit other = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit)obj;
            if (_imageRef == null) {
                if (other.getImageRef() != null) {
                    return false;
                }
            } else if(!_imageRef.equals(other.getImageRef())) {
                return false;
            }
            if (_index == null) {
                if (other.getIndex() != null) {
                    return false;
                }
            } else if(!_index.equals(other.getIndex())) {
                return false;
            }
            if (_key == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if(!_key.equals(other.getKey())) {
                return false;
            }
            if (_requireResource == null) {
                if (other.getRequireResource() != null) {
                    return false;
                }
            } else if(!_requireResource.equals(other.getRequireResource())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                VirtualizationDeploymentUnitImpl otherImpl = (VirtualizationDeploymentUnitImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("VirtualizationDeploymentUnit [");
            boolean first = true;
        
            if (_imageRef != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_imageRef=");
                builder.append(_imageRef);
             }
            if (_index != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_index=");
                builder.append(_index);
             }
            if (_key != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_key=");
                builder.append(_key);
             }
            if (_requireResource != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_requireResource=");
                builder.append(_requireResource);
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
