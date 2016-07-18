/*
 * Copyright (C) 2014 SimElectricity
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package simElectricity.Common.EnergyNet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import simElectricity.API.EnergyTile.ISEGridTile;
import simElectricity.API.EnergyTile.ISEPlaceable;
import simElectricity.API.Events.*;
import simElectricity.Common.ConfigManager;
import simElectricity.Common.SEUtils;

public class EnergyNetEventHandler {
    public EnergyNetEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        WorldData.onWorldUnload(event.world);
    }

    @SubscribeEvent
    public void tick(WorldTickEvent event) {
        if (event.phase != Phase.START)
            return;
        if (event.side != Side.SERVER)
            return;

        WorldData.getEnergyNetForWorld(event.world).onTick();
    }

    //Energy net --------------------------------------------------------------------------------------------------------------
    @SubscribeEvent
    public void onTileAttach(TileAttachEvent event) {
        TileEntity te = event.te;
        if (!te.getWorldObj().blockExists(te.xCoord, te.yCoord, te.zCoord)) {
            SEUtils.logInfo(te + " is added to the energy net too early!, abort!");
            return;
        }

        if (te.isInvalid()) {
            SEUtils.logInfo("Invalid tileentity " + te + " is trying to attach to the energy network, aborting");
            return;
        }
        
        if (te.getWorldObj().isRemote) {
            SEUtils.logInfo("Client tileentity " + te + " is requesting, abort!");
            return;
        }
        
        World world = event.te.getWorldObj();
        if (te instanceof ISEGridTile) {
        	EnergyNetDataProvider grid = EnergyNetDataProvider.get(world);
        	grid.onGridTilePresent(event.te);
        	
        	if (ConfigManager.showEnergyNetInfo)
        		SEUtils.logInfo("GridTile assosiated with GridObject at "+String.valueOf(event.te.xCoord)+","+String.valueOf(event.te.yCoord)+","+String.valueOf(event.te.zCoord));
        }
        
        if (te instanceof ISEPlaceable) {
	        EnergyNet energyNet = WorldData.getEnergyNetForWorld(world);
	        energyNet.addTileEntity(te);
	
	        if (ConfigManager.showEnergyNetInfo)
	            SEUtils.logInfo("Tileentity " + te + " has attached to the energy network!");        
        }
        



        //SEUtils.logInfo("Unacceptable tileentity " + te + " is trying to attach to the energy network, aborting");
    }

    @SubscribeEvent
    public void onTileDetach(TileDetachEvent event) {
        TileEntity te = event.te;

        if (te.getWorldObj().isRemote) {
            SEUtils.logInfo("Client tileentity " + te + " is requesting, abort!");
            return;
        }

        World world = event.te.getWorldObj();
        if (te instanceof ISEGridTile) {
        	EnergyNetDataProvider grid = EnergyNetDataProvider.get(world);
        	grid.onGridTileInvalidate(event.te);
        	
        	if (ConfigManager.showEnergyNetInfo)
        		SEUtils.logInfo("GridTile destroyed at"+String.valueOf(event.te.xCoord)+","+String.valueOf(event.te.yCoord)+","+String.valueOf(event.te.zCoord));
        }
        
        if (te instanceof ISEPlaceable) {
	        EnergyNet energyNet = WorldData.getEnergyNetForWorld(world);
	        energyNet.removeTileEntity(te);        	
	        if (ConfigManager.showEnergyNetInfo)
	            SEUtils.logInfo("Tileentity " + te + " has detached from the energy network!");
        }
    }

    @SubscribeEvent
    public void onTileRejoin(TileRejoinEvent event) {
        TileEntity te = event.energyTile;

        if (te.getWorldObj().isRemote) {
            SEUtils.logInfo("Client tileentity " + te + " is requesting, abort!");
            return;
        }

        WorldData.getEnergyNetForWorld(te.getWorldObj()).rejoinTileEntity(te);

        if (ConfigManager.showEnergyNetInfo)
            SEUtils.logInfo("Tileentity " + te + " has rejoined the energy network!");
    }

    @SubscribeEvent
    public void onTileChange(TileChangeEvent event) {
        TileEntity te = event.energyTile;

        if (te.getWorldObj().isRemote) {
            SEUtils.logInfo("Client tileentity " + te + " is requesting, aborting");
            return;
        }

        WorldData.getEnergyNetForWorld(te.getWorldObj()).markForUpdate(te);

        if (ConfigManager.showEnergyNetInfo)
            SEUtils.logInfo("Tileentity " + te + " causes the energy network to update!");
    }    
    
    @SubscribeEvent
    public void onGridObjectAttach(GridObjectAttachEvent event) {    
    	EnergyNetDataProvider grid = EnergyNetDataProvider.get(event.world);
    	GridNode obj = grid.addGridObject(event.x, event.y, event.z, event.type);
    	
    	if (!ConfigManager.showEnergyNetInfo)
    		return;
    	
    	if (obj == null){
    		SEUtils.logInfo("Fail to attach gridObject at " +String.valueOf(event.x)+","+String.valueOf(event.y)+","+String.valueOf(event.z));
    	}else{
    		SEUtils.logInfo("GridObject attached at " +obj.getIDString()+", type " + obj.type);
    	}
    }
    
    
    @SubscribeEvent
    public void onGridObjectDetach(GridObjectDetachEvent event) {    
    	EnergyNetDataProvider grid = EnergyNetDataProvider.get(event.world);
    	GridNode obj = grid.getGridObjectAtCoord(event.x, event.y, event.z);
    	
    	if (obj == null){
    		if (ConfigManager.showEnergyNetInfo)
    			SEUtils.logInfo("Fail to detach gridObject at " +String.valueOf(event.x)+","+String.valueOf(event.y)+","+String.valueOf(event.z));
    	}else{
    		if (ConfigManager.showEnergyNetInfo)
    			SEUtils.logInfo("GridObject detached at " +obj.getIDString()+", type " + obj.type);
    		grid.removeGridObject(obj);
    	}
    	
    	
    }
    
    @SubscribeEvent
    public void onGridConnection(GridConnectionEvent event) {    
    	EnergyNetDataProvider grid = EnergyNetDataProvider.get(event.world);
    	GridNode obj1 = grid.getGridObjectAtCoord(event.x1, event.y1, event.z1);
    	GridNode obj2 = grid.getGridObjectAtCoord(event.x2, event.y2, event.z2);
    	

    	
    	if (ConfigManager.showEnergyNetInfo){
        	if (obj1 == null || obj2 == null){
        		SEUtils.logInfo("Fail to build grid connection between " +String.valueOf(event.x1)+","+String.valueOf(event.y1)+","+String.valueOf(event.z1)+" and "
						+String.valueOf(event.x2)+","+String.valueOf(event.y2)+","+String.valueOf(event.z2));
        	}else{
        		SEUtils.logInfo("Grid connection built between " +String.valueOf(event.x1)+","+String.valueOf(event.y1)+","+String.valueOf(event.z1)+" and "
						+String.valueOf(event.x2)+","+String.valueOf(event.y2)+","+String.valueOf(event.z2));
        	}
    	}
    	
    	if (obj1 != null && obj2 != null)
    		grid.addConnection(obj1, obj2, event.resistance);
    }
    
    @SubscribeEvent
    public void onGridDisconnectionEvent(GridDisconnectionEvent event) {    
    	EnergyNetDataProvider grid = EnergyNetDataProvider.get(event.world);
    	GridNode obj1 = grid.getGridObjectAtCoord(event.x1, event.y1, event.z1);
    	GridNode obj2 = grid.getGridObjectAtCoord(event.x2, event.y2, event.z2);
    	

    	
    	if (ConfigManager.showEnergyNetInfo){
        	if (obj1 == null || obj2 == null){
        		SEUtils.logInfo("Fail to remove grid connection between " +String.valueOf(event.x1)+","+String.valueOf(event.y1)+","+String.valueOf(event.z1)+" and "
						+String.valueOf(event.x2)+","+String.valueOf(event.y2)+","+String.valueOf(event.z2));
        	}else{
        		SEUtils.logInfo("Grid connection removed between " +String.valueOf(event.x1)+","+String.valueOf(event.y1)+","+String.valueOf(event.z1)+" and "
						+String.valueOf(event.x2)+","+String.valueOf(event.y2)+","+String.valueOf(event.z2));
        	}
    	}
    	
    	if (obj1 != null && obj2 != null)
    		grid.removeConnection(obj1, obj2);
    }   
}
