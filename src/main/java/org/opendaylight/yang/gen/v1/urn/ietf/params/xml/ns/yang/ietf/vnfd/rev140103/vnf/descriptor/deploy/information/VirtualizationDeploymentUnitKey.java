package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnit;


public class VirtualizationDeploymentUnitKey
 implements Identifier<VirtualizationDeploymentUnit> {
    private static final long serialVersionUID = 4599768986293979512L; 
    final private java.lang.Integer _index;
    
    public VirtualizationDeploymentUnitKey(java.lang.Integer _index) {
        this._index = _index;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public VirtualizationDeploymentUnitKey(VirtualizationDeploymentUnitKey source) {
        this._index = source._index;
    }


    public java.lang.Integer getIndex() {
        return _index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_index == null) ? 0 : _index.hashCode());
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VirtualizationDeploymentUnitKey other = (VirtualizationDeploymentUnitKey) obj;
        if (_index == null) {
            if (other._index != null) {
                return false;
            }
        } else if(!_index.equals(other._index)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.vnf.descriptor.deploy.information.VirtualizationDeploymentUnitKey.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_index != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_index=");
            builder.append(_index);
         }
        return builder.append(']').toString();
    }



}
