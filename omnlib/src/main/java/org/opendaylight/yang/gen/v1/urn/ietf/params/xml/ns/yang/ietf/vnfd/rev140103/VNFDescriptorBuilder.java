package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.DeployInformation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.GeneralInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.ExternalInterface;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor} instances.
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor
 */
public class VNFDescriptorBuilder {

    private DeployInformation _deployInformation;
    private List<ExternalInterface> _externalInterface;
    private GeneralInformation _generalInformation;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> augmentation = new HashMap<>();

    public VNFDescriptorBuilder() {
    } 

    public VNFDescriptorBuilder(VNFDescriptor base) {
        this._deployInformation = base.getDeployInformation();
        this._externalInterface = base.getExternalInterface();
        this._generalInformation = base.getGeneralInformation();
        if (base instanceof VNFDescriptorImpl) {
            VNFDescriptorImpl _impl = (VNFDescriptorImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public DeployInformation getDeployInformation() {
        return _deployInformation;
    }
    
    public List<ExternalInterface> getExternalInterface() {
        return _externalInterface;
    }
    
    public GeneralInformation getGeneralInformation() {
        return _generalInformation;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public VNFDescriptorBuilder setDeployInformation(DeployInformation value) {
        this._deployInformation = value;
        return this;
    }
    
    public VNFDescriptorBuilder setExternalInterface(List<ExternalInterface> value) {
        this._externalInterface = value;
        return this;
    }
    
    public VNFDescriptorBuilder setGeneralInformation(GeneralInformation value) {
        this._generalInformation = value;
        return this;
    }
    
    public VNFDescriptorBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public VNFDescriptor build() {
        return new VNFDescriptorImpl(this);
    }

    private static final class VNFDescriptorImpl implements VNFDescriptor {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor.class;
        }

        private final DeployInformation _deployInformation;
        private final List<ExternalInterface> _externalInterface;
        private final GeneralInformation _generalInformation;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> augmentation = new HashMap<>();

        private VNFDescriptorImpl(VNFDescriptorBuilder base) {
            this._deployInformation = base.getDeployInformation();
            this._externalInterface = base.getExternalInterface();
            this._generalInformation = base.getGeneralInformation();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public DeployInformation getDeployInformation() {
            return _deployInformation;
        }
        
        @Override
        public List<ExternalInterface> getExternalInterface() {
            return _externalInterface;
        }
        
        @Override
        public GeneralInformation getGeneralInformation() {
            return _generalInformation;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_deployInformation == null) ? 0 : _deployInformation.hashCode());
            result = prime * result + ((_externalInterface == null) ? 0 : _externalInterface.hashCode());
            result = prime * result + ((_generalInformation == null) ? 0 : _generalInformation.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor other = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor)obj;
            if (_deployInformation == null) {
                if (other.getDeployInformation() != null) {
                    return false;
                }
            } else if(!_deployInformation.equals(other.getDeployInformation())) {
                return false;
            }
            if (_externalInterface == null) {
                if (other.getExternalInterface() != null) {
                    return false;
                }
            } else if(!_externalInterface.equals(other.getExternalInterface())) {
                return false;
            }
            if (_generalInformation == null) {
                if (other.getGeneralInformation() != null) {
                    return false;
                }
            } else if(!_generalInformation.equals(other.getGeneralInformation())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                VNFDescriptorImpl otherImpl = (VNFDescriptorImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("VNFDescriptor [");
            boolean first = true;
        
            if (_deployInformation != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_deployInformation=");
                builder.append(_deployInformation);
             }
            if (_externalInterface != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_externalInterface=");
                builder.append(_externalInterface);
             }
            if (_generalInformation != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_generalInformation=");
                builder.append(_generalInformation);
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
