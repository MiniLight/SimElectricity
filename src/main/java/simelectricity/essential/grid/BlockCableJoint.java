package simelectricity.essential.grid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISEGridNode;
import simelectricity.api.tile.ISEGridTile;
import simelectricity.essential.api.ISEHVCableConnector;
import simelectricity.essential.common.SEBlock;
import simelectricity.essential.common.SEItemBlock;

public class BlockCableJoint extends SEBlock implements ITileEntityProvider, ISEHVCableConnector{
	///////////////////
	/// Initialize
	///////////////////
	public BlockCableJoint() {
		super("essential_cable_joint", Material.circuits, ItemBlock.class);
	}

	@Override
	public void beforeRegister() {
		this.isBlockContainer = true;
		this.setCreativeTab(SEAPI.SETab);
	}
	
	public static class ItemBlock extends SEItemBlock{
		public ItemBlock(Block block) {super(block);}
		
		@Override
		@Deprecated
	    @SideOnly(Side.CLIENT)
	    public IIcon getIconFromDamage(int damage){
			return ((BlockCableJoint)field_150939_a).inventoryTexture;
		}
		
	    @Override
	    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata){
	        int facing = 8 - MathHelper.floor_double((player.rotationYaw) * 8.0F / 360.0F + 0.5D) & 7;
	        
	        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, facing);
	    }
	}

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
        return false;
    }
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileCableJoint();
	}
	
	////////////////////////////////////
	/// Rendering
	////////////////////////////////////
	@Deprecated
	private IIcon inventoryTexture;
    //This will tell minecraft not to render any side of our cube.
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int i, int j, int k, int l) {
        return false;
    }

    //And this tell it that you can see through this block, and neighbor blocks should be rendered.
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isNormalCube() {
		return false;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 0;
	}
    
	@Deprecated
	public static int renderID = 0; 	//Definition has changed from 1.8
	@Override
    public int getRenderType()
    {
        return renderID;
    }
	
	@Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister r) {
    	this.inventoryTexture = r.registerIcon("sime_essential:transmission/essential_cable_joint");
    }
	
	//////////////////////////////////////
	/////Item drops and Block activities
	//////////////////////////////////////
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        if (world.isRemote)
            return; 
        
        SEAPI.energyNetAgent.attachGridObject(world, SEAPI.energyNetAgent.newGridNode(x, y, z, (byte)0));
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
    	TileEntity te = world.getTileEntity(x, y, z);	//Do this before the tileEntity is removed!
    	if (te instanceof ISEGridTile)
    		SEAPI.energyNetAgent.detachGridObject(world, ((ISEGridTile) te).getGridNode());
    	
        super.breakBlock(world, x, y, z, block, meta);
    }
    
	//////////////////////////////////////
	/// ISEHVCableConnector
	//////////////////////////////////////
	@Override
	public boolean canHVCableConnect(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileCableJoint)
			return ((TileCableJoint) te).canConnect();
		else
			return false;
	}

	@Override
	public ISEGridNode getGridNode(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof ISEGridTile)
			return ((ISEGridTile) te).getGridNode();
		
		return null;
	}
}
