package simelectricity.essential.common.semachine;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;
import rikka.librikka.Utils;
import simelectricity.api.ISEWrenchable;
import simelectricity.api.ISESidedFacing;
import simelectricity.api.SEAPI;
import simelectricity.api.components.ISEComponentParameter;
import simelectricity.api.node.ISESubComponent;
import simelectricity.api.tile.ISETile;
import simelectricity.essential.common.SEEnergyTile;

public abstract class SESinglePortMachine<T extends ISEComponentParameter> extends SEEnergyTile implements ISESidedFacing, ISEWrenchable, ISETile, ISEComponentParameter {
    protected Direction functionalSide = Direction.SOUTH;
    protected final ISESubComponent circuit = SEAPI.energyNetAgent.newComponent(this, this);
    protected final T cachedParam = (T) circuit;

    ///////////////////////////////////
    /// TileEntity
    ///////////////////////////////////
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

        this.functionalSide = Utils.facingFromNbt(tagCompound, "functionalSide");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        Utils.saveToNbt(tagCompound, "functionalSide", this.functionalSide);
        return super.write(tagCompound);
    }

    @Override
    public Direction getFacing() {
        return getBlockState().get(BlockStateProperties.FACING);
    }

    ///////////////////////////////////
    /// ISESidedFacing
    ///////////////////////////////////
    @Override
    public void setFacing(Direction newFacing) {
    	world.setBlockState(pos, getBlockState().with(BlockStateProperties.FACING, newFacing));
    }

    @Override
    public boolean canSetFacing(Direction newFacing) {
        return true;
    }


    ///////////////////////////////////
    /// ISEWrenchable
    ///////////////////////////////////
    @Override
    public void onWrenchAction(Direction side, boolean isCreativePlayer) {
        this.SetFunctionalSide(side);
    }

    @Override
    public boolean canWrenchBeUsed(Direction side) {
        return true;
    }

    /////////////////////////////////////////////////////////
    ///Sync
    /////////////////////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(CompoundNBT nbt) {
        super.prepareS2CPacketData(nbt);

        Utils.saveToNbt(nbt, "functionalSide", this.functionalSide);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onSyncDataFromServerArrived(CompoundNBT nbt) {
        this.functionalSide = Utils.facingFromNbt(nbt, "functionalSide");

        // Flag 1 - update Rendering Only!
        this.markForRenderUpdate();

        super.onSyncDataFromServerArrived(nbt);
    }

    /////////////////////////////////////////////////////////
    ///ISETile
    /////////////////////////////////////////////////////////
    @Override
    public ISESubComponent getComponent(Direction side) {
        return side == this.functionalSide ? this.circuit : null;
    }

    /////////////////////////////////////////////////////////
    ///Utils
    /////////////////////////////////////////////////////////
    public void SetFunctionalSide(Direction side) {
        this.functionalSide = side;

        markTileEntityForS2CSync();
        this.world.notifyNeighborsOfStateChange(this.pos, getBlockState().getBlock());

        if (isAddedToEnergyNet)
            SEAPI.energyNetAgent.updateTileConnection(this);
    }
    
    protected void collectModelData(ModelDataMap.Builder builder) {
    	builder.withInitial(ISESocketProvider.prop, (ISESocketProvider) this);
    }
}
