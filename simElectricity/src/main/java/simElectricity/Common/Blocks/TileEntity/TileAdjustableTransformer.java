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
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Energy;
import simElectricity.API.Common.TileEntitySE;
import simElectricity.API.EnergyTile.ISESubComponent;
import simElectricity.API.EnergyTile.ISETile;
import simElectricity.API.EnergyTile.ISETransformerPrimary;
import simElectricity.API.EnergyTile.ISETransformerSecondary;
import simElectricity.API.INetworkEventHandler;

public class TileAdjustableTransformer extends TileEntitySE implements ISETile, INetworkEventHandler {
	public class TPrimary implements ISETransformerPrimary{
		private ISETransformerSecondary _sec;
		private TileAdjustableTransformer _par;
		
		public TPrimary(TileAdjustableTransformer parent){
			_sec = new TSecondary(this);
			_par = parent;
		}
		
		@Override
		public ISETransformerSecondary getSecondary() {
			return _sec;
		}

		@Override
		public double getRatio() {
			return _par.ratio;
		}

		@Override
		public double getResistance() {
			return _par.outputResistance;
		}
		
	}
	
	public class TSecondary implements ISETransformerSecondary{
		private ISETransformerPrimary _pri;
		
		public TSecondary(ISETransformerPrimary primary){
			_pri = primary;
		}
		
		@Override
		public ISETransformerPrimary getPrimary() {
			return _pri;
		}
		
	}
	
    public TPrimary primary = new TPrimary(this);
    
    public ForgeDirection primarySide = ForgeDirection.NORTH, secondarySide = ForgeDirection.SOUTH;
    public float ratio = 10, outputResistance = 1;

    @Override
	public boolean attachToEnergyNet(){
    	return true;
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
	public void onFieldUpdate(String[] fields, Object[] values) {
		//Handling on server side
		if (!worldObj.isRemote){
			for (String s:fields){
		        if (s.contains("primarySide") || s.contains("secondarySide")) {
		            Energy.postTileRejoinEvent(this);
		            worldObj.notifyBlockChange(xCoord, yCoord, zCoord, 
		            		worldObj.getBlock(xCoord, yCoord, zCoord));
		        } else if (s.contains("outputResistance") || s.contains("ratio")) {
		            Energy.postTileChangeEvent(this);
		        }				
			}

		}
	}

	@Override
	public void addNetworkFields(List fields) {

	}

	@Override
	public int getNumberOfComponents() {
		return 2;
	}

	@Override
	public ForgeDirection[] getValidDirections() {
		return new ForgeDirection[]{primarySide, secondarySide};
	}

	@Override
	public ISESubComponent getComponent(ForgeDirection side) {
		if (side == primarySide)
			return primary;
		if (side == secondarySide)
			return primary.getSecondary();
		return null;
	}
	
	public ForgeDirection getPrimarySide(){
		return primarySide;
	}
	
	public ForgeDirection getSecondarySide(){
		return secondarySide;
	}
}
