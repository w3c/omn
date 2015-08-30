package info.openmultinet.ontology.translators.yang.vnfI;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.OperationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.Parameter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.ParameterBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.Action;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action.MigrationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.operation.parameter.action.ScaleBuilder;

/**
 * This class is used to create a virtual network function insance
 *
 */
public class VnfIBuilderHelper {

	ScaleBuilder scaleBuilder = new ScaleBuilder();
	MigrationBuilder migrationBuilder = new MigrationBuilder();

	public MigrationBuilder getMigrationBuilder() {
		return migrationBuilder;
	}

	public void setMigrationBuilder(MigrationBuilder migrationBuilder) {
		this.migrationBuilder = migrationBuilder;
	}

	public ScaleBuilder getScaleBuilder() {
		return scaleBuilder;
	}

	public void setScaleBuilder(ScaleBuilder scaleBuilder) {
		this.scaleBuilder = scaleBuilder;
	}

	public VNFInstance MigrationInstanceCreator(
			IpAddress destIp,
			long id,
			String vnfdName,
			org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action opAction) {
		Action migrate = migrationCreator(destIp);
		Parameter param = parameterCreator(migrate);
		Operation op = operationCreator(param, opAction);
		VNFInstanceBuilder vnfInstanceBuilder = new VNFInstanceBuilder();
		vnfInstanceBuilder.setId(id);
		vnfInstanceBuilder.setVNFDName(vnfdName);
		vnfInstanceBuilder.setOperation(op);
		vnfInstanceBuilder.build();
		return vnfInstanceBuilder.build();
	}

	private Action migrationCreator(IpAddress destIp) {
		migrationBuilder.setDestinationLocation(destIp);
		return migrationBuilder.build();
	}

	public VNFInstance ScaleInstanceCreator(
			BigInteger diskSize,
			BigInteger memorySize,
			Integer cpuUnit,
			long  id,
			String vnfdName,
			org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action opAction) {
		Action scale = actionCreator(diskSize, memorySize, cpuUnit);
		Parameter param = parameterCreator(scale);
		Operation op = operationCreator(param, opAction);
		VNFInstanceBuilder vnfInstanceBuilder = new VNFInstanceBuilder();
		vnfInstanceBuilder.setId(id);
		vnfInstanceBuilder.setVNFDName(vnfdName);
		vnfInstanceBuilder.setOperation(op);
		vnfInstanceBuilder.build();
		return vnfInstanceBuilder.build();
	}

	private Operation operationCreator(
			Parameter param,
			org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.vnf.instance.Operation.Action opAction) {
		OperationBuilder operation = new OperationBuilder();
		operation.setAction(opAction);
		operation.setParameter(param);
		return operation.build();
	}

	private Parameter parameterCreator(Action scale) {
		ParameterBuilder paramBuilder = new ParameterBuilder();
		paramBuilder.setAction(scale);
		return paramBuilder.build();
	}

	private Action actionCreator(BigInteger diskSize, BigInteger memorySize,
			Integer cpuUnit) {
		scaleBuilder.setCPUUnit(cpuUnit);
		scaleBuilder.setDiskSize(diskSize);
		scaleBuilder.setMemorySize(memorySize);
		return scaleBuilder.build();
	}
}
