package simelectricity.essential.machines.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import rikka.librikka.container.ContainerSynchronizer;
import rikka.librikka.gui.AutoGuiHandler;
import simelectricity.essential.common.ContainerNoInvAutoSync;
import simelectricity.essential.machines.tile.TileRF2SE;
import simelectricity.essential.utils.network.ISEButtonEventHandler;

@AutoGuiHandler.Marker(GuiRF2SE.class)
public class ContainerRF2SE extends ContainerNoInvAutoSync<TileRF2SE> implements ISEButtonEventHandler {
    @ContainerSynchronizer.SyncField
    public double ratedOutputPower;
    @ContainerSynchronizer.SyncField
    public double voltage;
    @ContainerSynchronizer.SyncField
    public double actualOutputPower;
    @ContainerSynchronizer.SyncField
    public int bufferedEnergy;
    @ContainerSynchronizer.SyncField
    public int rfInputRateDisplay;

    // Server side
    public ContainerRF2SE(TileEntity tileEntity, int windowId) {
        super(tileEntity, windowId);
    }
    
    // Client side
    public ContainerRF2SE(int windowId, PlayerInventory inv, PacketBuffer data) {
    	this(null, windowId);
    }

    @Override
    public void onButtonPressed(int buttonID, boolean isCtrlPressed) {
        double ratedOutputPower = host.ratedOutputPower;

        switch (buttonID) {
            case 0:
                ratedOutputPower -= 100;
                break;
            case 1:
                ratedOutputPower -= 10;
                break;
            case 2:
                ratedOutputPower -= 1;
            case 3:
                ratedOutputPower += 1;
                break;
            case 4:
                ratedOutputPower += 10;
                break;
            case 5:
                ratedOutputPower += 100;
                break;
        }

        if (ratedOutputPower < 10)
            ratedOutputPower = 10;
        if (ratedOutputPower > TileRF2SE.bufferCapacity / 2)
            ratedOutputPower = TileRF2SE.bufferCapacity / 2;

        host.ratedOutputPower = ratedOutputPower;

        // SEAPI.energyNetAgent.updateTileParameter(this.host);
    }
}
