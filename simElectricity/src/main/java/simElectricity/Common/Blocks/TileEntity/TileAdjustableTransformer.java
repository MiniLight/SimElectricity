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

package simElectricity.Common.Blocks.TileEntity;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Energy;
import simElectricity.API.EnergyTile.ITransformer;
import simElectricity.API.INetworkEventHandler;
import simElectricity.API.Util;

public class TileAdjustableTransformer extends TileEntity implements ITransformer, INetworkEventHandler {
    public Primary primary = new ITransformer.Primary(this);
    public Secondary secondary = new ITransformer.Secondary(this);
    protected boolean isAddedToEnergyNet = false;

    public ForgeDirection primarySide = ForgeDirection.NORTH, secondarySide = ForgeDirection.SOUTH;
    public float ratio = 10, outputResistance = 1;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!worldObj.isRemote && !isAddedToEnergyNet) {
            Energy.postTileAttachEvent(this);
            this.isAddedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate() {
        if (!worldObj.isRemote & isAddedToEnergyNet) {
            Energy.postTileDetachEvent(this);
            this.isAddedToEnergyNet = false;
        }

        super.invalidate();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        ratio = tagCompound.getFloat("ratio");
        outputResistance = tagCompound.getFloat("outputResistance");
        primarySide = ForgeDirection.getOrientation(tagCompound.getByte("primarySide"));
        secondarySide = ForgeDirection.getOrientation(tagCompound.getByte("secondarySide"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setFloat("ratio", ratio);
        tagCompound.setFloat("outputResistance", outputResistance);
        tagCompound.setByte("primarySide", (byte) primarySide.ordinal());
        tagCompound.setByte("secondarySide", (byte) secondarySide.ordinal());
    }

    @Override
    public float getResistance() {
        return outputResistance;
    }

    @Override
    public float getRatio() {
        return ratio;
    }

    @Override
    public ForgeDirection getPrimarySide() {
        return primarySide;
    }

    @Override
    public ForgeDirection getSecondarySide() {
        return secondarySide;
    }

    @Override
    public ITransformerWinding getPrimary() {
        return primary;
    }

    @Override
    public ITransformerWinding getSecondary() {
        return secondary;
    }

	@Override
	public void onFieldUpdate(String[] fields, Object[] values, boolean isClient) {
		if (isClient){
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}else{
			for (String s:fields){
		        if (s.contains("primarySide") || s.contains("secondarySide")) {
		            Energy.postTileRejoinEvent(this);
		            Util.updateNetworkFields(this);
		        } else if (s.contains("outputResistance") || s.contains("ratio")) {
		            Energy.postTileChangeEvent(this);
		        }				
			}

		}
	}

	@Override
	public void addNetworkFields(List fields) {
		fields.add("primarySide");
		fields.add("secondarySide");
		
        worldObj.notifyBlockChange(xCoord, yCoord, zCoord, 
        		worldObj.getBlock(xCoord, yCoord, zCoord));
	}
}
