package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;
import simelectricity.api.components.ISEVoltageSourceData;
import simelectricity.api.node.ISESubComponent;

public class VoltageSource extends SEComponent.Tile<ISEVoltageSourceData> implements ISESubComponent{
	public double v, r;
	
	public VoltageSource(ISEVoltageSourceData dataProvider, TileEntity te){
		super(dataProvider, te);
	}

	@Override
	public void updateComponentParameters() {
		this.v = dataProvider.getOutputVoltage();
		this.r = dataProvider.getResistance();
	}
}
