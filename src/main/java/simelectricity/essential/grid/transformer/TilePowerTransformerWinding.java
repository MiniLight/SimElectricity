package simelectricity.essential.grid.transformer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISEGridNode;
import simelectricity.api.node.ISESimulatable;
import simelectricity.api.tile.ISEGridTile;
import simelectricity.essential.common.SEEnergyTile;
import simelectricity.essential.common.multiblock.ISEMultiBlockTile;
import simelectricity.essential.common.multiblock.MultiBlockTileInfo;

public abstract class TilePowerTransformerWinding extends SEEnergyTile implements ISEMultiBlockTile, ISEGridTile{
	protected MultiBlockTileInfo mbInfo;
	
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
    private BlockPos neighbor = null;
    
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
