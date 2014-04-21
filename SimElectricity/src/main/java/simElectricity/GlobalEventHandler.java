package simElectricity;

import simElectricity.API.IBaseComponent;
import simElectricity.API.TileAttachEvent;
import simElectricity.API.TileChangeEvent;
import simElectricity.API.TileDetachEvent;
import simElectricity.API.TileRejoinEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class GlobalEventHandler {
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		WorldData.onWorldUnload(event.world);
	}
	
	@SubscribeEvent
	public void tick(WorldTickEvent event){
		if(event.phase!=Phase.START)
			return;
		if(event.side!=Side.SERVER)
			return;	
		
		EnergyNet.onTick(event.world);
	}
	
	//Energy net --------------------------------------------------------------------------------------------------------------
	@SubscribeEvent
	public void onAttachEvent(TileAttachEvent event) {
		TileEntity te=event.energyTile;
		if(!te.getWorldObj().blockExists(te.xCoord, te.yCoord, te.zCoord)){
			System.out.println(te+ " is added to the energy net too early!, aborting");
			return;			
		}
		
		if(te.isInvalid()){
			System.out.println("Invalid tileentity " + te+ " is trying to attach to the energy network, aborting");
			return;			
		}
		
		if (!(te instanceof IBaseComponent)) {
			System.out.println("Unacceptable tileentity " + te+ " is trying to attach to the energy network, aborting");
			return;
		}
		
		EnergyNet.getForWorld(te.getWorldObj()).addTileEntity(te);
		
		System.out.println("Tileentity " + te
				+ " is attached to the energy network!");
	}

	@SubscribeEvent
	public void onTileDetach(TileDetachEvent event) {
		TileEntity te=event.energyTile;
		
		EnergyNet.getForWorld(te.getWorldObj()).removeTileEntity(te);
		
		System.out.println("Tileentity " + te + " is detach from the energy network!");
	}
	
	@SubscribeEvent
	public void onTileRejoin(TileRejoinEvent event) {
		TileEntity te=event.energyTile;
		
		EnergyNet.getForWorld(te.getWorldObj()).rejoinTileEntity(te);
		
		System.out.println("Tileentity " + te + " is rejoined from the energy network!");
	}		

	@SubscribeEvent
	public void onTileChange(TileChangeEvent event) {
		TileEntity te=event.energyTile;
		
		EnergyNet.getForWorld(te.getWorldObj()).markForUpdate(te);
		
		System.out.println("Tileentity " + te + " cause the energy network to update!");
	}
}
