package simElectricity.Blocks;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import simElectricity.API.Util;
import simElectricity.API.Common.ContainerBase;

public class ContainerAdjustableResistor extends ContainerBase{
    public ContainerAdjustableResistor(InventoryPlayer inventoryPlayer,	TileEntity te) {
		super(inventoryPlayer, te);
	}
	public int getPlayerInventoryStartIndex(){
    	return 27;
    }
    public int getPlayerInventoryEndIndex(){
    	return 36;
    }
    public int getTileInventoryStartIndex(){
    	return 0;
    }
    public int getTileInventoryEndIndex(){
    	return 27;
    }

	@Override
	public void init() {
        if(!tileEntity.getWorldObj().isRemote){
        	Util.updateTileEntityField(tileEntity, "resistance");
        }
	}
	
	@Override
    public void detectAndSendChanges()
    {
    	super.detectAndSendChanges();
    	Util.updateTileEntityField(tileEntity, "powerConsumed");
    	Util.updateTileEntityField(tileEntity, "power");
    }
}
