package simElectricity.EnergyNet.Components;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import simElectricity.API.DataProvider.ISEComponentDataProvider;
import simElectricity.API.EnergyTile.ISESubComponent;

public class RegulatorController extends SEComponent implements ISESubComponent {
	public RegulatorInput input;
	
	public RegulatorController(RegulatorInput input, TileEntity te){
		this.input = input;
		this.te = te;
	}

	@Override
	public ISESubComponent getComplement() {
		return input;
	}
}
