package simelectricity.essential.api;

import net.minecraft.util.EnumFacing;
import simelectricity.api.tile.ISECableTile;

public interface ISEGenericCable extends ISECoverPanelHost, ISECableTile {
    /**
     * Called on the server side, when it is necessary to send the client about the modified cable connection status
     */
    void onCableRenderingUpdateRequested();

    /**
     * Called by cable render (may be custom implementation) to
     * determine if the cable block has connection on the given side
     * @param side
     * @return ture if electrically connected
     */
    boolean connectedOnSide(EnumFacing side);
}
