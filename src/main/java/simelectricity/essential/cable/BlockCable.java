package simelectricity.essential.cable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import simelectricity.api.SEAPI;
import simelectricity.essential.BlockRegistry;
import simelectricity.essential.Essential;
import simelectricity.essential.api.ISECoverPanelHost;
import simelectricity.essential.api.ISEGenericCable;
import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.api.coverpanel.ISECoverPanel;
import simelectricity.essential.api.coverpanel.ISEGuiCoverPanel;
import simelectricity.essential.api.coverpanel.ISERedstoneEmitterCoverPanel;
import simelectricity.essential.common.ISESubBlock;
import simelectricity.essential.common.SEBlock;
import simelectricity.essential.common.SEItemBlock;
import simelectricity.essential.common.SEMetaBlock;
import simelectricity.essential.utils.MatrixTranformations;
import simelectricity.essential.utils.Utils;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * The collision & ray trace is inspired by BuildCraft
 * @author Rikka0_0
 */
public class BlockCable extends SEMetaBlock implements ITileEntityProvider, ISESubBlock{
	public BlockCable() {
		this("essential_cable", Material.GLASS, ItemBlockCable.class, 
				new String[]{"copper_thin", "copper_medium", "copper_thick"},
				new double[]{0.22, 0.32, 0.42},
				new double[]{0.1, 0.01, 0.001},
				TileCable.class);
		setHardness(0.2F);		
	}
	
	/**
	 * @return when implementing your own cable, please make sure to return correct number!
	 */
	@Override
	protected int getNumOfSubTypes(){
		return 3;
	}	
	
	///////////////////////////////
	/// Cable Properties
	///////////////////////////////
	public final String[] subNames;
	public final double[] thickness;
	public final double[] resistances;
	
	private final Class<? extends TileCable> tileEntityClass;
	
	///////////////////////////////
	///Block Properties
	///////////////////////////////
	protected BlockCable(String name, Material material, Class<? extends SEItemBlock> itemBlockClass,
							String[] cableTypes, double[] thicknessList, double[] resistanceList, Class<? extends TileCable> tileEntityClass) {
		super(name, material, itemBlockClass);
		this.subNames = cableTypes;
		this.thickness = thicknessList;
		this.resistances = resistanceList;
		this.tileEntityClass = tileEntityClass;
	}
	
	@Override
	public void beforeRegister() {
		this.isBlockContainer = true;
		this.setCreativeTab(SEAPI.SETab);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileCable cable;
		try {
			cable = this.tileEntityClass.getConstructor().newInstance();
			if (!world.isRemote)	//createNewTileEntity is only called by server when the block is firstly placed
				cable.setResistanceOnPlace(resistances[meta]);
			return cable;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public String[] getSubBlockUnlocalizedNames() {
		return subNames;
	}
    
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side){
		return false;
    }
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side){
		TileEntity tile = world.getTileEntity(pos);
		
		return tile instanceof ISEGenericCable 
				? 
				((ISEGenericCable)tile).getCoverPanelOnSide(side) != null 
				:
				false;
	}
	
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        
        if (te instanceof TileCable){
        	return ((TileCable) te).lightLevel;
        }
        return 0;
    }

    //////////////////////////////////
	///CollisionBoxes
	//////////////////////////////////
	//Custom ray trace
	private static AxisAlignedBB getCoverPanelBoundingBox(EnumFacing side) {
		if (side == null)
			return null;
		
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0;
		bounds[0][1] = 1;
		// Y START - END
		bounds[1][0] = 0;
		bounds[1][1] = (float) ISECoverPanel.thickness;
		// Z START - END
		bounds[2][0] = 0;
		bounds[2][1] = 1;

		MatrixTranformations.transform(bounds, side);
		return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}
	
	private static AxisAlignedBB getCableBoundingBox(EnumFacing side, float thickness) {
		float min = 0.5F - thickness/2;
		float max = 0.5F + thickness/2;

		if (side == null) {
			return new AxisAlignedBB(min, min, min, max, max, max);
		}

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = 0;
		bounds[1][1] = min;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	//TODO: Custom Raytrace!!!!
	@Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB axisAlignedBB,
                                      List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isPistonMoving) {
		
		TileEntity te = world.getTileEntity(pos);
		
		if (!(te instanceof ISEGenericCable))
			return;
		
		int meta = state.getBlock().getMetaFromState(state);
		ISEGenericCable cable = (ISEGenericCable)te;
		
		double min = 0.5 - thickness[meta]/2;
		double max = 0.5 + thickness[meta]/2;
		
		//Center
		addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, min, min, max, max, max));
	
		//Branches
		if (cable.connectedOnSide(EnumFacing.DOWN))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, 0, min, max, max, max));
		
		if (cable.connectedOnSide(EnumFacing.UP))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, min, min, max, 1, max));
		
		if (cable.connectedOnSide(EnumFacing.NORTH))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, min, 0, max, max, max));
		
		if (cable.connectedOnSide(EnumFacing.SOUTH))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, min, min, max, max, 1));
				
		if (cable.connectedOnSide(EnumFacing.WEST))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, min, min, max, max, max));
		
		if (cable.connectedOnSide(EnumFacing.EAST))
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(min, min, min, 1, max, max));

		//Cover panel
		if (cable.getCoverPanelOnSide(EnumFacing.DOWN) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, 0, 0, 1, ISECoverPanel.thickness, 1));
		
		if (cable.getCoverPanelOnSide(EnumFacing.UP) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, 1 - ISECoverPanel.thickness, 0, 1, 1, 1));
		
		if (cable.getCoverPanelOnSide(EnumFacing.NORTH) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, 0, 0, 1, 1, ISECoverPanel.thickness));
		
		if (cable.getCoverPanelOnSide(EnumFacing.SOUTH) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, 0, 1 - ISECoverPanel.thickness, 1, 1, 1));
				
		if (cable.getCoverPanelOnSide(EnumFacing.WEST) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(0, 0, 0, ISECoverPanel.thickness, 1, 1));
		
		if (cable.getCoverPanelOnSide(EnumFacing.EAST) != null)
			addCollisionBoxToList(pos, axisAlignedBB, collidingBoxes, new AxisAlignedBB(1 - ISECoverPanel.thickness, 0, 0, 1, 1, 1));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		RayTraceResult trace = Minecraft.getMinecraft().objectMouseOver;	//TODO: not sure what this does!
			
		if (trace == null || trace.subHit < 0 || !pos.equals(trace.getBlockPos())) {
            // Perhaps we aren't the object the mouse is over
            return FULL_BLOCK_AABB;
        }
		
		TileEntity te = world.getTileEntity(pos);
		
		if (te instanceof ISEGenericCable){
			int meta = state.getBlock().getMetaFromState(state);
			ISEGenericCable cable = (ISEGenericCable)te;
			
			double x1,y1,z1, x2,y2,z2;
			x1= 0.5 - thickness[meta]/2;
			y1= x1;
			z1= x1;
			x2= 0.5 + thickness[meta]/2;
			y2= x2;
			z2= x2;
			
			if (cable.connectedOnSide(EnumFacing.DOWN))
				y1 = 0;
			
			if (cable.connectedOnSide(EnumFacing.UP))
				y2 = 1;
			
			if (cable.connectedOnSide(EnumFacing.NORTH))
				z1 = 0;
			
			if (cable.connectedOnSide(EnumFacing.SOUTH))
				z2 = 1;
					
			if (cable.connectedOnSide(EnumFacing.WEST))
				x1 = 0;
			
			if (cable.connectedOnSide(EnumFacing.EAST))
				x2 = 1;
			
			return new AxisAlignedBB(x1,y1,z1,x2,y2,z2).offset(pos).expand(0.01, 0.01, 0.01);
		}
		
		return null;
	}
	
	//////////////////////////////////////
	/////Item drops and Block activities
	////////////////////////////////////// 
	private boolean openGui(World world, BlockPos pos, EntityPlayer player, EnumFacing side){
		RayTraceResult trace = Minecraft.getMinecraft().objectMouseOver;	//TODO: not sure what this does!
		
		/*
		if (trace == null)
			return false; //This is not suppose to happen, but just in case!
			
		if (trace.hitCenter)
			return false;
		
		if (trace.sideHit == EnumFacing.UNKNOWN)
			return false;
		
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof ISECoverPanelHost){
			ISECoverPanelHost host = (ISECoverPanelHost) te;
			ISECoverPanel coverPanel = host.getCoverPanelOnSide(result.sideHit);
			
			if (coverPanel instanceof ISEGuiCoverPanel){
				player.openGui(Essential.instance, result.sideHit.ordinal(), world, x, y, z);
				return true;
			}
		}
		*/
		return false;	//TODO: need to be fixed!
	}
	
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ){
        TileEntity te = world.getTileEntity(pos);
        
        if (!(te instanceof ISEGenericCable))
        	return false;		//Normally this could not happen, but just in case!
               
        ItemStack itemStack = player.getHeldItemMainhand();
        if (itemStack == null)
        	return openGui(world, pos, player, side);
        
        if (itemStack.isEmpty())
        	return openGui(world, pos, player, side);
    
        ISEGenericCable cable = (ISEGenericCable) te;
                
        ISECoverPanel coverPanel = SEEAPI.coverPanelRegistry.fromItemStack(itemStack);
        if (coverPanel == null)
        	return openGui(world, pos, player, side);
        
    	if (cable.canInstallCoverPanelOnSide(side, coverPanel)){
        	if (!player.capabilities.isCreativeMode){
	        	itemStack.shrink(1);
	        	if (itemStack.isEmpty())
	        		itemStack = null;
        	}
        	
        	if (!world.isRemote){	//Handle on server side
        		cable.installCoverPanel(side, coverPanel);
        		world.notifyBlockUpdate(pos, state, state, 0x3);
        	}
        	return true;
    	}
        
    	return openGui(world, pos, player, side);
    }
	
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ISEGenericCable))
        	return;		//Normally this could not happen, but just in case!
    	
        ISEGenericCable cable = (ISEGenericCable) te;
        cable.onCableRenderingUpdateRequested();
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ISEGenericCable))
        	return;		//Normally this could not happen, but just in case!
        
        ISEGenericCable cable = (ISEGenericCable) te;
        cable.onCableRenderingUpdateRequested();
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
        boolean willHarvest) {
        //if (world.isRemote) {
        //    return false;
    	// }
        	//TODO:
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
	
	///////////////////////
	///Redstone
	///////////////////////	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		
		if (te instanceof TileCable){
			TileCable cable = (TileCable) te;
			ISECoverPanel coverPanel = cable.getCoverPanelOnSide(side);
			
			return coverPanel instanceof ISERedstoneEmitterCoverPanel;
		}
		
		return false;
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		
		if (te instanceof TileCable){
			TileCable cable = (TileCable) te;
			ISECoverPanel coverPanel = cable.getCoverPanelOnSide(side);
			
			return 	coverPanel instanceof ISERedstoneEmitterCoverPanel 
					?	(((ISERedstoneEmitterCoverPanel) coverPanel).isProvidingWeakPower()?15:0) 
					: 	0;
		}
		
		return 0;
	}
}
