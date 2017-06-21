package simelectricity.essential.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class ContainerNoInventory<TYPE extends TileEntity> extends Container {
	protected TYPE tileEntity;
	
	@Override
	public ItemStack slotClick(int p_75144_1_, int p_75144_2_, int p_75144_3_, EntityPlayer p_75144_4_){
		return null;
	}
	
	public ContainerNoInventory(TileEntity tileEntity){
		this.tileEntity = (TYPE) tileEntity;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public abstract void detectAndSendChanges();
}
