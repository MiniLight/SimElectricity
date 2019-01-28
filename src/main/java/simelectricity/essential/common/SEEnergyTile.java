package simelectricity.essential.common;

import rikka.librikka.tileentity.TileEntityBase;
import simelectricity.api.SEAPI;

public abstract class SEEnergyTile extends TileEntityBase {
    protected boolean isAddedToEnergyNet;

    /**
     * Called just before joining the energyNet, do some initialization here </p>
     * Should not use onLoad() in client anyway!
     */
    @Override
    public void onLoad() {
        if (!this.world.isRemote && !this.isAddedToEnergyNet) {
            SEAPI.energyNetAgent.attachTile(this);
            isAddedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (!this.world.isRemote && this.isAddedToEnergyNet) {
            SEAPI.energyNetAgent.detachTile(this);
            isAddedToEnergyNet = false;
        }
    }

    @Override
    public void onChunkUnload() {
        this.invalidate();
    }

    /**
     * A helper function
     */
    protected final void updateTileParameter() {
        SEAPI.energyNetAgent.updateTileParameter(this);
    }

    protected final void updateTileConnection() {SEAPI.energyNetAgent.updateTileConnection(this); }
}
