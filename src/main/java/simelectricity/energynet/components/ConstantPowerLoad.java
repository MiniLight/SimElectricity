package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;

import simelectricity.api.components.ISEConstantPowerLoadData;
import simelectricity.api.node.ISESubComponent;

public class ConstantPowerLoad extends SEComponent.Tile<ISEConstantPowerLoadData> implements ISESubComponent, ISEConstantPowerLoadData{
	public double pRated, rMin, rMax;
	public boolean enabled;
	
	public ConstantPowerLoad(ISEConstantPowerLoadData dataProvider, TileEntity te){
		super(dataProvider, te);
	}

	@Override
	public void updateComponentParameters() {
		this.pRated = dataProvider.getRatedPower();
		this.rMin = dataProvider.getMinimumResistance();
		this.rMax = dataProvider.getMaximumResistance();
		this.enabled = dataProvider.isEnabled();
	}

	@Override
	public double getRatedPower() {
		return pRated;
	}

	@Override
	public double getMinimumResistance() {
		return rMin;
	}

	@Override
	public double getMaximumResistance() {
		return rMax;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
