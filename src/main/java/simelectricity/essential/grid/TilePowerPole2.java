package simelectricity.essential.grid;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.math.MathAssitant;
import rikka.librikka.math.Vec3f;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;

public class TilePowerPole2 extends TilePowerPoleBase {
    @SideOnly(Side.CLIENT)
    public boolean isType0() {
        return this.getBlockMetadata() >> 3 == 0;
    }
	
    @Override
    @SideOnly(Side.CLIENT)
    public BlockPos getAccessoryPos() {
    	return null;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    protected PowerPoleRenderHelper createRenderHelper() {
        PowerPoleRenderHelper helper;
        int rotation = (this.getBlockMetadata() & 3) * 2;

        if (this.isType0()) {
            helper = new PowerPoleRenderHelper(this.pos, rotation, 2, 3) {
                @Override
                public void onUpdate() {
                    if (this.connectionInfo.size() < 2)
                        return;

                    PowerPoleRenderHelper.ConnectionInfo[] connection1 = this.connectionInfo.getFirst();
                    PowerPoleRenderHelper.ConnectionInfo[] connection2 = connectionInfo.getLast();

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
            helper.addInsulatorGroup(-0.25F, 0.125F, 0,
                    helper.createInsulator(2, 3, -0.25F, 0.125F, -4.5F),
                    helper.createInsulator(2, 3, -0.25F, 0.125F, 0),
                    helper.createInsulator(2, 3, -0.25F, 0.125F, 4.5F)
            );
            helper.addInsulatorGroup(0.25F, 0.125F, 0,
                    helper.createInsulator(2, 3, 0.25F, 0.125F, -4.5F),
                    helper.createInsulator(2, 3, 0.25F, 0.125F, 0),
                    helper.createInsulator(2, 3, 0.25F, 0.125F, 4.5F)
            );
        } else {
            helper = new PowerPoleRenderHelper(this.pos, rotation, 1, 3);
            helper.addInsulatorGroup(0, 0.125F - 1.95F, 0F,
                    helper.createInsulator(0, 3, 0, -2F, -4.5F),
                    helper.createInsulator(0, 3, 0, -2F, 0F),
                    helper.createInsulator(0, 3, 0, -2F, 4.5F)
            );
        }

        return helper;
    }
}
