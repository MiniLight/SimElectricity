package simElectricity.Blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Util;
import simElectricity.mod_SimElectricity;

import java.util.Random;

public class BlockVoltageMeter extends BlockContainer {
	private IIcon[] iconBuffer = new IIcon[6];
	
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i1, float f1, float f2, float f3){
    	TileEntity te = world.getTileEntity(x, y, z);
    	
    	if(player.isSneaking())
    		return false;
    	
    	player.openGui(mod_SimElectricity.instance, 0, world, x, y, z);
    	return true;
    }
    
    public BlockVoltageMeter() {
		super(Material.rock);
		setHardness(2.0F);
		setResistance(5.0F);
		setBlockName("VoltageMeter");
		setCreativeTab(Util.SETab);
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister r){
    	iconBuffer[0] = r.registerIcon("simElectricity:VoltageMeter_Side");
    	iconBuffer[1] = r.registerIcon("simElectricity:VoltageMeter_Side");
    	iconBuffer[2] = r.registerIcon("simElectricity:VoltageMeter_Front");
    	iconBuffer[3] = r.registerIcon("simElectricity:VoltageMeter_Back");
    	iconBuffer[4] = r.registerIcon("simElectricity:VoltageMeter_Side");
    	iconBuffer[5] = r.registerIcon("simElectricity:VoltageMeter_Side");
    }

	
    @SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess world, int x,int y, int z, int side) {
    	int blockMeta = world.getBlockMetadata(x, y, z);
    	TileEntity te=world.getTileEntity(x, y, z);
    	
    	if(!(te instanceof TileVoltageMeter))
    		return iconBuffer[0];
    	
    	//System.out.println(((TileVoltageMeter)te).getFunctionalSide());
    	return iconBuffer[Util.getTextureOnSide(side, ((TileVoltageMeter)te).getFacing())];
	}
	
    @SideOnly(Side.CLIENT)
   	@Override
   	public IIcon getIcon(int side, int meta) {
    	return iconBuffer[Util.getTextureOnSide(side, ForgeDirection.WEST)];
   	}
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {       
        TileEntity te = world.getTileEntity(x, y, z);
        
        if (!(te instanceof TileVoltageMeter))
        	return;
        
        //Both for server and client
        ((TileVoltageMeter)te).setFacing(Util.getPlayerSight(player).getOpposite());
        ((TileVoltageMeter)te).setFunctionalSide(Util.getPlayerSight(player));
    }	
    
    @Override
    public void updateTick(World world, int x, int y, int z, Random p_149674_5_) {
     	if (world.isRemote)
    		return;    	
    	TileEntity te = world.getTileEntity(x, y, z);
    	if (!(te instanceof TileVoltageMeter))
    		return;
    	Util.updateTileEntityFacing(te);
    	Util.updateTileEntityFunctionalSide(te); 	
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
    	if (world.isRemote)
    		return;    	
    	
    	//Server side only!
    	TileEntity te = world.getTileEntity(x, y, z);
    	Util.updateTileEntityFacing(te);
    }
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {return new TileVoltageMeter();}
	
	@Override
	public int damageDropped(int par1) {return par1;}
}
