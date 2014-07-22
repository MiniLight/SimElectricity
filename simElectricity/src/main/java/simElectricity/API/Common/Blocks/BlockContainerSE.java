package simElectricity.API.Common.Blocks;

import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import simElectricity.API.Common.Items.ItemBlockSE;
import simElectricity.API.Util;

/**
 * Basic SimElectricity Container Block
 *
 * @author <Meow J>
 */
public abstract class BlockContainerSE extends BlockContainer {

    public BlockContainerSE(Material material) {
        super(material);
        if (registerInCreativeTab())
            setCreativeTab(Util.SETab);
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
            dropItems(world, x, y, z);
            super.breakBlock(world, x, y, z, block, meta);
    } 

    /**
     * If this block has its own ItemBlock, just override this method and shouldRegister(set to false).
     *
     * @param name name of this block.
     *
     * @see simElectricity.Common.Blocks.BlockWire
     * @see simElectricity.Common.Items.ItemBlocks.ItemBlockWire
     */
    @Override
    public Block setBlockName(String name) {
        if (shouldRegister())
            GameRegistry.registerBlock(this, ItemBlockSE.class, name);
        return super.setBlockName(name);
    }

    public boolean registerInCreativeTab() {
        return true;
    }

    public boolean shouldRegister() {
        return true;
    }
    
    /**
     * Drop items inside the inventory
     * */    
	public static void dropItems(World world, int x, int y, int z){
	    Random rand = new Random();
	
	    TileEntity tileEntity = world.getTileEntity(x, y, z);
	    if (!(tileEntity instanceof IInventory)) {
	            return;
	    }
	    IInventory inventory = (IInventory) tileEntity;
	
	    for (int i = 0; i < inventory.getSizeInventory(); i++) {
	            ItemStack item = inventory.getStackInSlot(i);
	
	            if (item != null && item.stackSize > 0) {
	                    float rx = rand.nextFloat() * 0.8F + 0.1F;
	                    float ry = rand.nextFloat() * 0.8F + 0.1F;
	                    float rz = rand.nextFloat() * 0.8F + 0.1F;
	
	                    EntityItem entityItem = new EntityItem(world,
	                                    x + rx, y + ry, z + rz,
	                                    new ItemStack(item.getItem(), item.stackSize, item.getItemDamage()));
	
	                    if (item.hasTagCompound()) {
	                            entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
	                    }
	
	                    float factor = 0.05F;
	                    entityItem.motionX = rand.nextGaussian() * factor;
	                    entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
	                    entityItem.motionZ = rand.nextGaussian() * factor;
	                    world.spawnEntityInWorld(entityItem);
	                    item.stackSize = 0;
	            }
	    }
	}  
}
