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

package simElectricity.Templates.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.IEnergyNetUpdateHandler;
import simElectricity.API.ISEConnectable;
import simElectricity.API.SEAPI;
import simElectricity.API.DataProvider.ISEJunctionData;
import simElectricity.API.EnergyTile.ISESimulatable;
import simElectricity.API.EnergyTile.ISESubComponent;
import simElectricity.API.Tile.ISECableTile;
import simElectricity.API.Tile.ISETile;
import simElectricity.Templates.Common.TileEntitySE;
import simElectricity.Templates.Common.TileEntityTwoPort;
import simElectricity.Templates.Utils.IGuiSyncHandler;
import simElectricity.Templates.Utils.Utils;

import java.util.LinkedList;
import java.util.List;

public class TileSwitch extends TileEntityTwoPort implements ISEJunctionData, ISEConnectable, IEnergyNetUpdateHandler, IGuiSyncHandler {	
	ISESubComponent junction = (ISESubComponent) SEAPI.energyNetAgent.newComponent(this);
	public double current=0F;
    
    public double resistance = 0.005F;
    public double maxCurrent = 1F;
    public boolean isOn = false;

	/////////////////////////////////////////////////////////
	///TileEntity
	/////////////////////////////////////////////////////////
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        resistance = tagCompound.getDouble("resistance");
        maxCurrent = tagCompound.getDouble("maxCurrent");
        isOn = tagCompound.getBoolean("isOn");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setDouble("resistance", resistance);
        tagCompound.setDouble("maxCurrent", maxCurrent);
        tagCompound.setBoolean("isOn", isOn);
    }

	/////////////////////////////////////////////////////////
	///ISEConnectable
	/////////////////////////////////////////////////////////
    @Override
    public boolean canConnectOnSide(ForgeDirection side) {
        return side == inputSide || side == outputSide;
    }

	/////////////////////////////////////////////////////////
	///IEnergyNetUpdateHandler
	/////////////////////////////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
    	current = SEAPI.energyNetAgent.getCurrentMagnitude(this.junction);
        if (current > maxCurrent) {
            isOn = false;
            SEAPI.energyNetAgent.reattachTile(this);
            
            this.markTileEntityForS2CSync();
        }
    }

	/////////////////////////////////////////////////////////
	///ISETile
	/////////////////////////////////////////////////////////
	@Override
	public int getNumberOfComponents() {
		return 1;
	}

	@Override
	public ISESubComponent getComponent(ForgeDirection side) {
		return (side == inputSide || side == outputSide) ? junction : null;
	}

	
	/////////////////////////////////////////////////////////
	///ISEJunctionData
	/////////////////////////////////////////////////////////
	@Override
	public void getNeighbors(List<ISESimulatable> list) {
        if (isOn) {
            TileEntity neighbor = Utils.getTileEntityonDirection(this, inputSide);

            if (neighbor instanceof ISECableTile)
                list.add(((ISECableTile) neighbor).getNode());

            neighbor = Utils.getTileEntityonDirection(this, outputSide);

            if (neighbor instanceof ISECableTile)
            	list.add(((ISECableTile) neighbor).getNode());
        }
	}

	@Override
	public double getResistance(ISESimulatable neighbor) {
		return resistance / 2;
	}
	
	/////////////////////////////////////////////////////////
	///Sync
	/////////////////////////////////////////////////////////
	@Override
	public void prepareS2CPacketData(NBTTagCompound nbt){	
		super.prepareS2CPacketData(nbt);
		
		nbt.setBoolean("isOn", isOn);
	}
	
	@Override
	public void onSyncDataFromServerArrived(NBTTagCompound nbt){	
		isOn = nbt.getBoolean("isOn");

		super.onSyncDataFromServerArrived(nbt);
	}

	/////////////////////////////////////////////////////////
	///IGuiSyncHandler
	/////////////////////////////////////////////////////////
	@Override
	public void onGuiEvent(byte eventID, Object[] data) {
		if (eventID == IGuiSyncHandler.EVENT_FACING_CHANGE){
			byte button = (Byte) data[0];
			ForgeDirection selectedDirection = (ForgeDirection) data[1];
			
		    if (button == 0) {        //Left key
		        if (outputSide == selectedDirection)
		            outputSide = inputSide;
		        inputSide = selectedDirection;
		    } else if (button == 1) { //Right key
		        if (inputSide == selectedDirection)
		            inputSide = outputSide;
		        outputSide = selectedDirection;
	        }

            SEAPI.energyNetAgent.reattachTile(this);
			this.markTileEntityForS2CSync();
			this.worldObj.notifyBlockChange(xCoord, yCoord, zCoord, null);
			return;
		}
		
		//EVENT_BUTTON_CLICK
		boolean isCtrlDown = (Boolean) data[0];
		byte button = (Byte) data[1];
		
		double resistance = this.resistance;
		double maxCurrent = this.maxCurrent;
		boolean isOn = this.isOn;
		boolean reattch = false;
		
        switch (button) {
        case 0:
            if (isCtrlDown)
                resistance -= 1;
            else
                resistance -= 0.1;
            break;
        case 1:
            if (isCtrlDown)
                resistance -= 0.001;
            else
                resistance -= 0.01;
            break;
        case 2:
            if (isCtrlDown)
                resistance += 0.001;
            else
                resistance += 0.01;
            break;
        case 3:
            if (isCtrlDown)
                resistance += 1;
            else
                resistance += 0.1;
            break;

        case 4:
            if (isCtrlDown)
                maxCurrent -= 100;
            else
                maxCurrent -= 10;
            break;
        case 5:
            if (isCtrlDown)
                maxCurrent -= 0.1;
            else
                maxCurrent -= 1;
            break;
        case 6:
            if (isCtrlDown)
                maxCurrent += 0.1;
            else
                maxCurrent += 1;
            break;
        case 7:
            if (isCtrlDown)
                maxCurrent += 100;
            else
                maxCurrent += 10;
            break;
        case 8:
        	reattch = true;
        	isOn = !isOn;
        	break;
        default:
        }

	    if (resistance < 0.001)
	        resistance = 0.001F;
	    if (resistance > 100)
	        resistance = 100;
	    
	    if (maxCurrent < 0.1)
	        maxCurrent = 0.1F;
	    if (maxCurrent > 1000)
	        maxCurrent = 1000;
			
	    this.resistance = resistance;
	    this.maxCurrent = maxCurrent;
	    this.isOn = isOn;
	    
	    
	    if (reattch)
	    	SEAPI.energyNetAgent.reattachTile(this);
	    else
	    	SEAPI.energyNetAgent.markTileForUpdate(this);
	    //onEnergyNetUpdate();		//Check trip-off
	    this.markTileEntityForS2CSync();
	}
}
