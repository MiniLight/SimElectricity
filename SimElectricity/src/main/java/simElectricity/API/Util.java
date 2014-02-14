package simElectricity.API;

import simElectricity.EnergyNet;
import simElectricity.mod_SimElectricity;
import simElectricity.Network.PacketTileEntityFieldUpdate;
import net.minecraft.tileentity.TileEntity;

public class Util {
	public static float getPower(IEnergyTile Tile){
		if(Tile.getOutputVoltage()>0){//Energy Source
			return ((Tile.getOutputVoltage()-getVoltage(Tile))*(Tile.getOutputVoltage()-getVoltage(Tile)))/Tile.getResistance(); 
		}else{//Energy Sink
			return getVoltage(Tile)*getVoltage(Tile)/Tile.getResistance();    				
		}
	}

	public static float getCurrent(IEnergyTile Tile){
		if(Tile.getOutputVoltage()>0){//Energy Source
			return (Tile.getOutputVoltage()-getVoltage(Tile))/Tile.getResistance(); 
		}else{//Energy Sink
			return getVoltage(Tile)/Tile.getResistance();    				
		}
	}
	
	public static float getVoltage(IBaseComponent Tile){
		if (EnergyNet.getForWorld(((TileEntity)Tile).getWorldObj()).voltageCache.containsKey(Tile))
			return EnergyNet.getForWorld(((TileEntity)Tile).getWorldObj()).voltageCache.get(Tile);
		else
			return 0;
	}
	
	public static void updateTileEntityField(TileEntity te,String field){
		mod_SimElectricity.instance.packetPipeline.sendToAll(new PacketTileEntityFieldUpdate(te,field));
	}
}
