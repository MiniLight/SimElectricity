package simelectricity.essential.machines.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.tileentity.INamedContainerProvider2;
import simelectricity.api.ISEEnergyNetUpdateHandler;
import simelectricity.api.SEAPI;
import simelectricity.api.components.ISESwitch;
import simelectricity.essential.common.semachine.ISE2StateTile;
import simelectricity.essential.common.semachine.SETwoPortMachine;
import simelectricity.essential.machines.gui.ContainerSwitch;

public class TileSwitch extends SETwoPortMachine<ISESwitch> implements 
		ISESwitch, ISE2StateTile, ISEEnergyNetUpdateHandler, INamedContainerProvider2 {
    public double current;

    public double resistance = 0.001;
    public double maxCurrent = 10;
    public boolean isOn;

    /////////////////////////////////////////////////////////
    ///TileEntity
    /////////////////////////////////////////////////////////
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

        this.resistance = tagCompound.getDouble("resistance");
        this.maxCurrent = tagCompound.getDouble("maxCurrent");
        this.isOn = tagCompound.getBoolean("isOn");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.putDouble("resistance", this.resistance);
        tagCompound.putDouble("maxCurrent", this.maxCurrent);
        tagCompound.putBoolean("isOn", this.isOn);

        return super.write(tagCompound);
    }

    /////////////////////////////////////////////////////////
    ///ISEEnergyNetUpdateHandler
    /////////////////////////////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
        if (this.cachedParam.isOn()) {
            this.current = this.input.getCurrentMagnitude();
        } else {
            this.current = 0;
        }

        if (this.current > this.maxCurrent) {
            setSwitchStatus(false);
        }
    }

    /////////////////////////////////////////////////////////
    ///ISESwitchData
    /////////////////////////////////////////////////////////
    @Override
    public boolean isOn() {
        return this.isOn;
    }

    @Override
    public double getResistance() {
        return this.resistance;
    }

    /////////////////////////////////////////////////////////
    ///Sync
    /////////////////////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(CompoundNBT nbt) {
        super.prepareS2CPacketData(nbt);

        nbt.putBoolean("isOn", this.isOn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onSyncDataFromServerArrived(CompoundNBT nbt) {
        this.isOn = nbt.getBoolean("isOn");

        markForRenderUpdate();

        super.onSyncDataFromServerArrived(nbt);
    }

    ///////////////////////////////////
    /// ISESocketProvider
    ///////////////////////////////////
    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSocketIconIndex(Direction side) {
        if (side == this.inputSide)
            return 2;
        else if (side == this.outputSide)
            return 4;
        else
            return -1;
    }

    ///////////////////////////////////
    /// Utils
    ///////////////////////////////////
    public void setSwitchStatus(boolean isOn) {
    	if (this.isOn != isOn) {
            this.isOn = isOn;
            this.setSecondState(isOn);
    	}
        SEAPI.energyNetAgent.updateTileParameter(this);

        markTileEntityForS2CSync();
    }

    ///////////////////////////////////
    /// INamedContainerProvider
    ///////////////////////////////////
	@Override
	public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
		return new ContainerSwitch(this, windowId);
	}
}
