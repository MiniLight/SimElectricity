package simelectricity.essential.grid;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.math.MathAssitant;
import rikka.librikka.math.Vec3f;
import simelectricity.essential.BlockRegistry;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;

public class TilePowerPole2 extends TilePowerPoleBase {
    @OnlyIn(Dist.CLIENT)
    public boolean isType0() {
        return getBlockState().getBlock() == BlockRegistry.powerPole2[0];
    }
	
    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockPos getAccessoryPos() {
    	return null;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected PowerPoleRenderHelper createRenderHelper() {
        PowerPoleRenderHelper helper;
        int rotation = BlockPowerPole2.getFacingInt(getBlockState()) * 2;

        if (this.isType0()) {
            helper = new PowerPoleRenderHelper(this.pos, rotation, 2, 3) {
                @Override
                public void onUpdate() {
                    if (this.connectionList.size() < 2)
                        return;

                    PowerPoleRenderHelper.ConnectionInfo[] connection1 = this.connectionList.getFirst();
                    PowerPoleRenderHelper.ConnectionInfo[] connection2 = connectionList.getLast();

                    Vec3f pos = new Vec3f(
                            3.95F * MathHelper.sin(rotation / 180F * MathAssitant.PI) + 0.5F + this.pos.getX(),
                            this.pos.getY() + 23 - 18,
                            3.95F * MathHelper.cos(rotation / 180F * MathAssitant.PI) + 0.5F + this.pos.getZ()
                    );

                    this.addExtraWire(connection1[1].fixedFrom, connection2[1].fixedFrom, 2.5F);
                    if (PowerPoleRenderHelper.hasIntersection(
                            connection1[0].fixedFrom, connection2[0].fixedFrom,
                            connection1[2].fixedFrom, connection2[2].fixedFrom)) {
                        this.addExtraWire(connection1[0].fixedFrom, connection2[2].fixedFrom, 2.5F);
                        this.addExtraWire(connection1[2].fixedFrom, connection2[0].fixedFrom, 2.5F);
                    } else {
                        this.addExtraWire(connection1[0].fixedFrom, connection2[0].fixedFrom, 2.5F);
                        this.addExtraWire(connection1[2].fixedFrom, connection2[2].fixedFrom, 2.5F);
                    }
                }
            };
            helper.addInsulatorGroup(0, 0.125F, -0.25F,
                    helper.createInsulator(2, 3, -4.5F, 0.125F, -0.25F),
                    helper.createInsulator(2, 3, 0, 0.125F, -0.25F),
                    helper.createInsulator(2, 3, 4.5F, 0.125F, -0.25F)
            );
            helper.addInsulatorGroup(0, 0.125F, 0.25F,
                    helper.createInsulator(2, 3, -4.5F, 0.125F, 0.25F),
                    helper.createInsulator(2, 3, 0, 0.125F, 0.25F),
                    helper.createInsulator(2, 3, 4.5F, 0.125F, 0.25F)
            );
        } else {
            helper = new PowerPoleRenderHelper(this.pos, rotation, 1, 3);
            helper.addInsulatorGroup(0, 0.125F - 1.95F, 0F,
                    helper.createInsulator(0, 3, -4.5F, -2F, 0),
                    helper.createInsulator(0, 3, 0, -2F, 0F),
                    helper.createInsulator(0, 3, 4.5F, -2F, 0)
            );
        }

        return helper;
    }
}
