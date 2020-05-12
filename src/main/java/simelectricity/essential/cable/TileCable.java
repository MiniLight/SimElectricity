package simelectricity.essential.cable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants.NBT;
import rikka.librikka.Utils;
import simelectricity.api.ISEEnergyNetUpdateHandler;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISESimulatable;
import simelectricity.api.tile.ISECableTile;
import simelectricity.essential.api.ISEGenericCable;
import simelectricity.essential.api.ISEIuminousCoverPanelHost;
import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.api.coverpanel.*;
import simelectricity.essential.common.SEEnergyTile;

public class TileCable extends SEEnergyTile implements ISEGenericCable, ISEIuminousCoverPanelHost, ISECableTile, ISEEnergyNetUpdateHandler {
    public boolean emitRedstoneSignal;
    /**
     * Accessible from client
     */
    public byte lightLevel;
    private final ISESimulatable node = SEAPI.energyNetAgent.newCable(this, false);
    private int color;
    private double resistance = 10;
    private volatile double voltage;
    private final boolean[] connections = new boolean[6];
    private final ISECoverPanel[] installedCoverPanels = new ISECoverPanel[6];

    ////////////////////////////////////////
    //Private functions
    ////////////////////////////////////////
    private ListNBT coverPanelsToNBT() {
        ListNBT tagList = new ListNBT();
        for (int i = 0; i < this.installedCoverPanels.length; i++) {
            ISECoverPanel coverPanel = this.installedCoverPanels[i];
            if (coverPanel != null) {
                CompoundNBT tag = new CompoundNBT();
                tag.putInt("side", i);
                SEEAPI.coverPanelRegistry.saveToNBT(coverPanel, tag);
                tagList.add(tag);
            }
        }
        return tagList;
    }

    private void coverPanelsFromNBT(ListNBT tagList) {
        for (int i = 0; i < 6; i++)
			this.installedCoverPanels[i] = null;

        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT tag = tagList.getCompound(i);
            int side = tag.getInt("side");
            if (side > -1 && side < this.installedCoverPanels.length) {
                ISECoverPanel coverPanel = SEEAPI.coverPanelRegistry.fromNBT(tag);
				this.installedCoverPanels[side] = coverPanel;

                if (coverPanel != null)
                    coverPanel.setHost(this, Direction.byIndex(side));
            }
        }
    }

    public void setResistanceOnPlace(double resistance) {
        this.resistance = resistance;
    }

    ////////////////////////////////////////
    //ISEGenericCable
    ////////////////////////////////////////
    @Override
    public void onRenderingUpdateRequested() {
        //Update connection
        Direction[] dirs = Direction.values();
        for (int i = 0; i < 6; i++) {
			this.connections[i] = SEAPI.energyNetAgent.canConnectTo(this, dirs[i]);
        }

        //Initiate Server->Client synchronization
		this.markTileEntityForS2CSync();
    }

    @Override
    public boolean connectedOnSide(Direction side) {
        return this.connections[side.ordinal()];
    }

    @Override
    public ISECoverPanel getSelectedCoverPanel(PlayerEntity player) {
        Block block = this.getBlockType();
        if (block instanceof BlockCable) {
            RayTraceResult result = ((BlockCable) block).rayTrace(this.world, this.pos, player);

            if (result.subHit < 7 || result.subHit > 12)
                return null;    //The player is not looking at any installed cover panel

            Direction side = Direction.byIndex(result.subHit - 7);
            return this.installedCoverPanels[side.ordinal()];
        }
        return null;
    }

    @Override
    public ISECoverPanel getCoverPanelOnSide(Direction side) {
        return this.installedCoverPanels[side.ordinal()];
    }

    @Override
    public boolean canInstallCoverPanelOnSide(Direction side, ISECoverPanel coverPanel) {
        return this.installedCoverPanels[side.ordinal()] == null;
    }

    @Override
    public void installCoverPanel(Direction side, ISECoverPanel coverPanel) {
		this.installedCoverPanels[side.ordinal()] = coverPanel;
        coverPanel.setHost(this, side);

        if (!coverPanel.isHollow()) {
            //If the cover panel is not hollow, it may block some connection
            if (this.connectedOnSide(side))
                SEAPI.energyNetAgent.updateTileConnection(this);
        }

        if (coverPanel instanceof ISEElectricalCoverPanel)
            ((ISEElectricalCoverPanel) coverPanel).onPlaced(this.voltage);

        if (coverPanel instanceof ISEElectricalLoadCoverPanel)
            SEAPI.energyNetAgent.updateTileConnection(this);

        world.notifyNeighborsOfStateChange(pos, getBlockType());

		this.onRenderingUpdateRequested();
    }

    @Override
    public boolean removeCoverPanel(ISECoverPanel coverPanel, boolean dropItem) {
        if (coverPanel == null)
            return false;

        //Look for that panel and remove it
        Direction side = null;
        for (Direction facing : Direction.values()) {
            if (this.installedCoverPanels[facing.ordinal()] == coverPanel)
                side = facing;
        }

        if (side == null)
            return false;

        //Remove the panel
		this.installedCoverPanels[side.ordinal()] = null;

        if (coverPanel instanceof ISEElectricalLoadCoverPanel || !coverPanel.isHollow())
            SEAPI.energyNetAgent.updateTileConnection(this);

		this.onLightValueUpdated();

		this.onRenderingUpdateRequested();

        world.notifyNeighborsOfStateChange(pos, getBlockType());

        //Spawn an item entity for player to pick up
        if (dropItem)
            Utils.dropItemIntoWorld(this.world, this.pos, coverPanel.getDroppedItemStack());

        return true;
    }

    ///////////////////////////////////////
    ///TileEntity
    ///////////////////////////////////////
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB bb = TileEntity.INFINITE_EXTENT_AABB;
        bb = new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1));
        return bb;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

		this.color = tagCompound.getInt("color");
		this.resistance = tagCompound.getDouble("resistance");
		this.coverPanelsFromNBT(tagCompound.getList("coverPanels", NBT.TAG_COMPOUND));
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.putInt("color", this.color);
        tagCompound.putDouble("resistance", this.resistance);
        tagCompound.put("coverPanels", this.coverPanelsToNBT());

        return super.write(tagCompound);
    }

    ///////////////////////////////////////
    ///ISECableTile
    ///////////////////////////////////////
    @Override
    public void setColor(int newColor) {
        this.color = newColor;
        this.updateTileConnection();
        this.onRenderingUpdateRequested();
        world.notifyNeighborsOfStateChange(this.pos, getBlockType());
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public double getResistance() {
        return this.resistance;
    }

    @Override
    public ISESimulatable getNode() {
        return this.node;
    }

    @Override
    public boolean canConnectOnSide(Direction direction) {
        ISECoverPanel coverPanel = this.getCoverPanelOnSide(direction);
        if (coverPanel == null)
            return true;
        else
            return coverPanel.isHollow();
    }

    @Override
    public boolean isGridLinkEnabled() {
        return false;
    }

    @Override
    public boolean hasShuntResistance() {
        boolean hasShuntResistance = false;
        for (ISECoverPanel coverPanel : installedCoverPanels)
            hasShuntResistance |= coverPanel instanceof ISEElectricalLoadCoverPanel;
        return hasShuntResistance;
    }

    @Override
    public double getShuntResistance() {
        double shuntConductance = 0;
        for (ISECoverPanel coverPanel : installedCoverPanels)
            if (coverPanel instanceof ISEElectricalLoadCoverPanel)
                shuntConductance += 1.0D / ((ISEElectricalLoadCoverPanel) coverPanel).getResistance();

        return 1.0D / shuntConductance;
    }

    ////////////////////////////////////////
    //Server->Client sync
    ////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(CompoundNBT nbt) {
        super.prepareS2CPacketData(nbt);

        byte bc = 0x00;
        if (this.connections[0]) bc |= 1;
        if (this.connections[1]) bc |= 2;
        if (this.connections[2]) bc |= 4;
        if (this.connections[3]) bc |= 8;
        if (this.connections[4]) bc |= 16;
        if (this.connections[5]) bc |= 32;

        nbt.putByte("connections", bc);
        nbt.putInt("color", color);

        nbt.put("coverPanels", this.coverPanelsToNBT());

        nbt.putByte("lightLevel", this.lightLevel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onSyncDataFromServerArrived(CompoundNBT nbt) {
        byte connectionsBinary = nbt.getByte("connections");

		connections[0] = (connectionsBinary & 1) > 0;
		connections[1] = (connectionsBinary & 2) > 0;
		connections[2] = (connectionsBinary & 4) > 0;
		connections[3] = (connectionsBinary & 8) > 0;
		connections[4] = (connectionsBinary & 16) > 0;
		connections[5] = (connectionsBinary & 32) > 0;
		this.color = nbt.getInt("color");

		this.coverPanelsFromNBT(nbt.getList("coverPanels", NBT.TAG_COMPOUND));

        byte lightLevel = nbt.getByte("lightLevel");
        if (this.lightLevel != lightLevel) {
            this.lightLevel = lightLevel;
            //Detect change & proceed
			this.world.getLightManager().checkBlock(this.pos);
            //world.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);	//checkLightFor
        }

        // Flag 1 - update Rendering Only!
		this.markForRenderUpdate();
    }

    ////////////////////////////////////////
    //ISEEnergyNetUpdateHandler
    ////////////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
		this.voltage = this.node.getVoltage();

        for (ISECoverPanel coverPanel : installedCoverPanels) {
            if (coverPanel instanceof ISEElectricalCoverPanel)
                ((ISEElectricalCoverPanel) coverPanel).onEnergyNetUpdate(this.voltage);
        }
    }

    /////////////////////////////////
    ///ISEIuminousCoverPanelHost
    /////////////////////////////////
    @Override
    public void onLightValueUpdated() {
        byte lightLevel = 0;
        for (ISECoverPanel coverPanel : installedCoverPanels) {
            if (coverPanel instanceof ISEIuminousCoverPanel) {
                byte ll = ((ISEIuminousCoverPanel) coverPanel).getLightValue();
                if (ll > lightLevel)
                    lightLevel = ll;
            }
        }

        if (this.lightLevel != lightLevel) {
            this.lightLevel = lightLevel;
			markTileEntityForS2CSync();
        }
    }
    
    protected Block getBlockType() {
    	return this.getBlockState().getBlock();
    }
    
    @Override
    protected void collectModelData(ModelDataMap.Builder builder) {
    	builder.withInitial(prop, this);
    }
}
