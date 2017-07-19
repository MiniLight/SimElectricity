package simelectricity.essential.extensions.buildcraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import simelectricity.essential.api.ISECoverPanel;
import simelectricity.essential.api.ISECoverPanelRender;
import simelectricity.essential.extensions.buildcraft.client.BCFacadeRender;

public class BCFacadePanel implements ISECoverPanel{
	private final boolean isHollow;
	private final int meta;
	private final Block block;
	private final ItemStack itemStack;
	
	public BCFacadePanel(NBTTagCompound nbt) {
		this.isHollow = nbt.getBoolean("isHollow");
		this.meta = nbt.getByte("meta");
		
		int blockID = nbt.getInteger("blockID");
		this.block = Block.getBlockById(blockID);
		
		this.itemStack = ItemStack.loadItemStackFromNBT(nbt);
		if (itemStack != null)
			itemStack.stackSize = 1;
	}
	
	public BCFacadePanel(boolean isHollow, int meta, Block block, ItemStack itemStack){
		this.isHollow = isHollow;
		this.meta = meta;
		this.block = block;
		
		if (itemStack == null){
			this.itemStack = null;
		}else{
			itemStack.stackSize = 1;
			this.itemStack = itemStack;
		}
			
	}

	@Override
	public boolean isHollow() {return isHollow;}

	public int getBlockMeta() {return meta;}

	public Block getBlock() {return block;}

	@Override
	public void toNBT(NBTTagCompound nbt) {
		nbt.setString("coverPanelType", "BCFacade");
		
		nbt.setBoolean("isHollow", isHollow);
		nbt.setInteger("meta", meta);
		nbt.setInteger("blockID", Block.getIdFromBlock(block));
				
		if (itemStack != null)
			itemStack.writeToNBT(nbt);
	}

	@Override
	public ItemStack getCoverPanelItem() {
		return itemStack.copy();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ISECoverPanelRender getCoverPanelRender() {
		return BCFacadeRender.instance;
	}
}
