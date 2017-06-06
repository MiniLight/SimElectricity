package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;
import simelectricity.api.components.ISERegulatorData;
import simelectricity.api.node.ISESubComponent;

public class RegulatorInput extends SEComponent.Tile<ISERegulatorData> implements ISESubComponent{
	public double Vref, Ro, Dmax, Rc, A, Rs, Rdummy;
	public RegulatorOutput output;
	public RegulatorController controller;
	
	public RegulatorInput(ISERegulatorData dataProvider, TileEntity te){
		super(dataProvider, te);
		output = new RegulatorOutput(this, te);
		controller = new RegulatorController(this, te);
	}

	@Override
	public ISESubComponent getComplement() {
		return output;
	}

	@Override
	public void updateComponentParameters() {
		this.Vref = dataProvider.getRegulatedVoltage();
		this.Ro = dataProvider.getOutputResistance();
		this.Dmax = dataProvider.getDMax();
		this.Rc = dataProvider.getRc();
		this.A = dataProvider.getGain();
		this.Rs = dataProvider.getRs();
		this.Rdummy = dataProvider.getRDummyLoad();
		
	}
}
