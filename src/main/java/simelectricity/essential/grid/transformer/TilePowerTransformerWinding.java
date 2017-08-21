package simelectricity.essential.grid.transformer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISEGridNode;
import simelectricity.api.node.ISESimulatable;
import simelectricity.api.tile.ISEGridTile;
import simelectricity.essential.client.grid.ISEPowerPole;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;
import simelectricity.essential.common.SEEnergyTile;
import simelectricity.essential.common.multiblock.ISEMultiBlockTile;
import simelectricity.essential.common.multiblock.MultiBlockTileInfo;
import simelectricity.essential.utils.Utils;

public abstract class TilePowerTransformerWinding extends SEEnergyTile implements ISEMultiBlockTile, ISEGridTile, ISEPowerPole{
	protected MultiBlockTileInfo mbInfo;
	
	@SideOnly(Side.CLIENT)
	private EnumFacing facing;
	@SideOnly(Side.CLIENT)
	private boolean mirrored;
	@SideOnly(Side.CLIENT)
    private PowerPoleRenderHelper renderHelper;
    private BlockPos neighbor = null;
    
	//////////////////////////////
	/////TileEntity
	//////////////////////////////
	@SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 100000;
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public AxisAlignedBB getRenderBoundingBox(){
    	return INFINITE_EXTENT_AABB;
    }
	
	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		mbInfo = new MultiBlockTileInfo(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		mbInfo.saveToNBT(nbt);
		return super.writeToNBT(nbt);
	}
	
	/////////////////////////////////////////////////////////
	///Sync
	/////////////////////////////////////////////////////////
	@Override
	public void prepareS2CPacketData(NBTTagCompound nbt) {
		Utils.saveToNbt(nbt, "neighbor", neighbor);
		Utils.saveToNbt(nbt, "facing", mbInfo.facing);
		nbt.setBoolean("mirrored", mbInfo.mirrored);
	}
	
	@Override
	@SideOnly(value = Side.CLIENT)
	public void onSyncDataFromServerArrived(NBTTagCompound nbt) {
		this.neighbor = Utils.posFromNbt(nbt, "neighbor");
		this.facing = Utils.facingFromNbt(nbt, "facing");
		this.mirrored = nbt.getBoolean("mirrored");

		this.updateRenderInfo();
		
		if (neighbor != null) {
			TileEntity neighborTile = world.getTileEntity(this.neighbor);
			if (neighborTile instanceof ISEPowerPole)
				((ISEPowerPole)neighborTile).updateRenderInfo();
		}

		super.onSyncDataFromServerArrived(nbt);
	}
	
	//////////////////////////////
	/////ISEMultiBlockTile
	//////////////////////////////
	@Override
	public MultiBlockTileInfo getMultiBlockTileInfo() {
		return this.mbInfo;
	}
	
	@Override
	public void onStructureCreating(MultiBlockTileInfo mbInfo) {
		this.mbInfo = mbInfo;
		this.markDirty();
		
		this.gridNode = SEAPI.energyNetAgent.newGridNode(pos, 3);
		SEAPI.energyNetAgent.attachGridNode(world, gridNode);
	}

	@Override
	public void onStructureCreated() {}

	@Override
	public void onStructureRemoved() {
		SEAPI.energyNetAgent.detachGridNode(world, gridNode);
	}
	
	//////////////////////////////
	/////ISEGridTile
	//////////////////////////////
    private ISEGridNode gridNode = null;    
	@Override
	public void setGridNode(ISEGridNode gridObj) {this.gridNode = gridObj;}

	@Override
	public ISEGridNode getGridNode() {return gridNode;}

	@Override
	public void onGridNeighborUpdated() {
		this.neighbor = null;
		f:for (ISESimulatable neighbor : gridNode.getNeighborList()){
			if (neighbor instanceof ISEGridNode){
				ISEGridNode gridNode = (ISEGridNode) neighbor;
				this.neighbor = gridNode.getPos().toImmutable();
				break f;
			}
		}
		
		this.markTileEntityForS2CSync();
	}
	
	public boolean canConnect() {
		return neighbor == null;
	}
	
	/////////////////////////////////////////////////////////
	///ITransmissionTower
	/////////////////////////////////////////////////////////
	@Override
	@SideOnly(Side.CLIENT)
	public void updateRenderInfo() {
		getRenderHelper().updateRenderData(neighbor);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public PowerPoleRenderHelper getRenderHelper() {
        //Create renderHelper on client side
        if (world.isRemote){
			if (renderHelper == null) {
				renderHelper = new PowerPoleRenderHelper(world, pos, facing, mirrored, 1, 3);
				renderHelper.addInsulatorGroup(0.6F, 1.45F, 0F, 
						renderHelper.createInsulator(0, 1, 1.17F, 1),
						renderHelper.createInsulator(0, 1, 1.3F, 0),
						renderHelper.createInsulator(0, 1, 1.17F, -2));
			}
        	return renderHelper;
        }else{
        	return null;
        }
	}
	
	public static class Primary extends TilePowerTransformerWinding {
		@Override
		public void onStructureCreated() {
			super.onStructureCreated();
			BlockPos pos = mbInfo.getPartPos(EnumBlockType.Secondary.offset);
			Secondary secondaryTile = (Secondary) world.getTileEntity(pos);
			SEAPI.energyNetAgent.makeTransformer(world, this.getGridNode(), secondaryTile.getGridNode(), 1, 0.5);
		}
	}
	
	public static class Secondary extends TilePowerTransformerWinding {

	}
}
