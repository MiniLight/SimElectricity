package simelectricity.essential.grid;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.properties.Properties;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISESimulatable;
import simelectricity.api.tile.ISECableTile;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;

public class TileCableJoint extends TilePoleAccessory implements ISECableTile {
    private final ISESimulatable cableNode = SEAPI.energyNetAgent.newCable(this, true);
    
    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    protected PowerPoleRenderHelper createRenderHelper() {
        //Create renderHelper on client side
        int rotation = this.world.getBlockState(this.pos).getValue(Properties.facing3bit);
        PowerPoleRenderHelper renderHelper = new PowerPoleRenderHelper(this.world, this.pos, rotation, 1, 3);
        renderHelper.addInsulatorGroup(0.6F, 1.45F, 0F,
                renderHelper.createInsulator(0, 2, -0.3F, 1.17F, -0.95F),
                renderHelper.createInsulator(0, 2, 0.6F, 1.45F, 0F),
                renderHelper.createInsulator(0, 2, -0.3F, 1.17F, 0.95F));

        return renderHelper;
    }
    
    @Override
    public boolean canConnect(BlockPos toPos) {
    	if (toPos == null)
    		return this.host == null;
    	
    	TileEntity to = world.getTileEntity(toPos);
        return this.host == null && to instanceof TilePowerPole3.Pole10Kv;
    }
    
    /////////////////////////////////////////////////////////
    ///ISECableTile
    /////////////////////////////////////////////////////////
    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public double getResistance() {
        return 0.1;
    }

    @Override
    public ISESimulatable getNode() {
        return this.cableNode;
    }

    @Override
    public boolean canConnectOnSide(EnumFacing direction) {
        return direction == EnumFacing.DOWN;
    }

    @Override
    public boolean isGridLinkEnabled() {
        return true;
    }

    @Override
    public boolean hasShuntResistance() {
        return false;
    }

    @Override
    public double getShuntResistance() {
        return 0;
    }
}
