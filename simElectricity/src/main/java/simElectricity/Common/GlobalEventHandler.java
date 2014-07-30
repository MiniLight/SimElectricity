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

package simElectricity.Common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;
import simElectricity.API.EnergyTile.IConductor;
import simElectricity.API.EnergyTile.IEnergyTile;
import simElectricity.API.ISidedFacing;
import simElectricity.API.IUpdateOnWatch;
import simElectricity.API.Util;

public class GlobalEventHandler {
    public GlobalEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    //When a player see the chunk, update facing, functionalside, wire rendering
    @SubscribeEvent
    public void onChunkWatchEvent(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);

        for (Object tileEntity : chunk.chunkTileEntityMap.values()) {
            TileEntity te = (TileEntity) tileEntity;
            if (te instanceof IEnergyTile)
                Util.updateTileEntityFunctionalSide(te);
            if (te instanceof ISidedFacing)
                Util.updateTileEntityFacing(te);
            if (te instanceof IConductor) {
            	Util.scheduleBlockUpdate(te);
            	//Send onNeighborBlockChange to IConductors, do reRender here
            	//te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord).onNeighborBlockChange(te.getWorldObj(),te.xCoord, te.yCoord, te.zCoord, null);
                //Be extremely careful when update something on the edge of the chunk
                if (te.xCoord % 16 == 0 || te.xCoord % 16 == 1 || te.xCoord % 16 == 15 ||
                        te.zCoord % 16 == 0 || te.zCoord % 16 == 1 || te.zCoord % 16 == 15)
                    te.getWorldObj().notifyBlocksOfNeighborChange(te.xCoord, te.yCoord, te.zCoord, null);
            }
            if (te instanceof IUpdateOnWatch)
                ((IUpdateOnWatch) te).onWatch();
        }
    }
}
