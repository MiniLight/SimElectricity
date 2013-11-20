package simElectricity;

import simElectricity.API.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class Item_UltimateMultimeter extends Item{
	public Item_UltimateMultimeter(int id) {
		super(id);
		maxStackSize = 1;
		setHasSubtypes(true);
		setUnlocalizedName("Item_UltimateMultimeter");
		setMaxDamage(256);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
    	itemIcon=par1IconRegister.registerIcon("simElectricity:Item_UltimateMultimeter");
    }
    
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
    	if((world.getBlockTileEntity(x, y, z) instanceof IBaseComponent)&(!world.isRemote)){
    		IBaseComponent te=(IBaseComponent) world.getBlockTileEntity(x, y, z);
    		//Print out information here
    		if (te instanceof IEnergyTile){
    			IEnergyTile ps=(IEnergyTile) te;
    			
    			player.sendChatToPlayer(ChatMessageComponent.createFromText(
    					"Output voltage: "+String.valueOf(ps.getOutputVoltage())
    					));
    			player.sendChatToPlayer(ChatMessageComponent.createFromText(
    					"Internal Resistance: "+String.valueOf(ps.getInternalResistance())
    					));
    		}
    		
    		if(te instanceof IConductor){
    			IConductor c=(IConductor) te;
    			
    		}
    		
    		player.sendChatToPlayer(ChatMessageComponent.createFromText("resistance: "+String.valueOf(te.getResistance())));    		
    		
    		return true;
    	}else{
    		return false;
    	}
        
    }
}
