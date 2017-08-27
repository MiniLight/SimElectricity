package simelectricity.essential.grid.transformer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import simelectricity.api.node.ISEGridNode;
import simelectricity.essential.common.SETileEntity;
import simelectricity.essential.common.multiblock.ISEMultiBlockTile;
import simelectricity.essential.common.multiblock.MultiBlockTileInfo;
import simelectricity.essential.utils.Utils;

public class TilePowerTransformerPlaceHolder extends SETileEntity implements ISEMultiBlockTile {
    //To minimize network usage, mbInfo will not be send to blocks other than the Render block
    protected MultiBlockTileInfo mbInfo;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.mbInfo = new MultiBlockTileInfo(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.mbInfo.saveToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override
    public MultiBlockTileInfo getMultiBlockTileInfo() {
        return mbInfo;
    }

    @Override
    public void onStructureCreating(MultiBlockTileInfo mbInfo) {
        this.mbInfo = mbInfo;
        markDirty();
    }

    @Override
    public void onStructureCreated() {
    }

    @Override
    public void onStructureRemoved() {
    }

    public static class Primary extends TilePowerTransformerPlaceHolder {
        public ISEGridNode getPrimaryTile() {
            BlockPos pos = this.mbInfo.getPartPos(EnumBlockType.Primary.offset);
            TileEntity te = this.world.getTileEntity(pos);
            return te instanceof TilePowerTransformerWinding.Primary ?
                    ((TilePowerTransformerWinding.Primary) te).getGridNode() : null;
        }

        public boolean canConnect() {
            BlockPos pos = this.mbInfo.getPartPos(EnumBlockType.Primary.offset);
            TileEntity te = this.world.getTileEntity(pos);
            return te instanceof TilePowerTransformerWinding && ((TilePowerTransformerWinding) te).canConnect();
        }
    }

    public static class Secondary extends TilePowerTransformerPlaceHolder {
        public ISEGridNode getSecondaryTile() {
            BlockPos pos = this.mbInfo.getPartPos(EnumBlockType.Secondary.offset);
            TileEntity te = this.world.getTileEntity(pos);
            return te instanceof TilePowerTransformerWinding.Secondary ?
                    ((TilePowerTransformerWinding.Secondary) te).getGridNode() : null;
        }

        public boolean canConnect() {
            BlockPos pos = this.mbInfo.getPartPos(EnumBlockType.Secondary.offset);
            TileEntity te = this.world.getTileEntity(pos);
            return te instanceof TilePowerTransformerWinding && ((TilePowerTransformerWinding) te).canConnect();
        }
    }

    public static class Render extends TilePowerTransformerPlaceHolder {
        @SideOnly(Side.CLIENT)
        private EnumFacing facing;
        @SideOnly(Side.CLIENT)
        private boolean mirrored;

        @Override
        public void prepareS2CPacketData(NBTTagCompound nbt) {
            Utils.saveToNbt(nbt, "facing", this.mbInfo.facing);
            nbt.setBoolean("mirrored", this.mbInfo.mirrored);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void onSyncDataFromServerArrived(NBTTagCompound nbt) {
            facing = Utils.facingFromNbt(nbt, "facing");
            mirrored = nbt.getBoolean("mirrored");

            markForRenderUpdate();

            super.onSyncDataFromServerArrived(nbt);
        }

        public EnumFacing getFacing() {
            if (this.world.isRemote)
                return this.facing;
            else
                return this.mbInfo.facing;
        }

        public boolean isMirrored() {
            if (this.world.isRemote)
                return this.mirrored;
            else
                return this.mbInfo.mirrored;
        }
    }
}
