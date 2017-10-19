package simelectricity.essential.grid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.Utils;
import rikka.librikka.math.Vec3f;
import simelectricity.api.node.ISEGridNode;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;
import simelectricity.essential.client.grid.accessory.PoleAccessoryRendererDispatcher;

public abstract class TilePowerPole3 extends TilePowerPoleBase {
	protected BlockPos accessory;
	public int facing;
    
	protected boolean acceptAccessory(TileEntity accessory) {
		return accessory instanceof TileCableJoint;
	}
	
    @Override
    public boolean canConnect(@Nullable BlockPos to) {
    	if (to == null)
    		return this.neighbor1 == null || this.neighbor2 == null || this.accessory == null;
    	
    	TileEntity te = world.getTileEntity(to);
    	if (acceptAccessory(te)) {
    		return this.accessory == null;
    	} else {
    		return this.neighbor1 == null || this.neighbor2 == null;
    	}
    }
    
    @Override
    public void onGridNeighborUpdated() {
        this.neighbor1 = null;
        this.neighbor2 = null;
        this.accessory = null;
        
        for (ISEGridNode node: this.gridNode.getNeighborList()) {
        	TileEntity tile = world.getTileEntity(node.getPos());
        	if (acceptAccessory(tile)) {
        		this.accessory = node.getPos();
        	} else if (neighbor1 == null){
        		neighbor1 = node.getPos();
        	} else {
        		neighbor2 = node.getPos();
        	}
        }
        
        markTileEntityForS2CSync();
    }
    
    //////////////////////////////
    /////TileEntity
    //////////////////////////////
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
		this.facing = nbt.getInteger("facing");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("facing", this.facing);
        return super.writeToNBT(nbt);
    }

    /////////////////////////////////////////////////////////
    ///Sync
    /////////////////////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(NBTTagCompound nbt) {
        super.prepareS2CPacketData(nbt);
        nbt.setInteger("facing", this.facing);
        Utils.saveToNbt(nbt, "accessory", this.accessory);
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void onSyncDataFromServerArrived(NBTTagCompound nbt) {
		this.facing = nbt.getInteger("facing");
		this.accessory = Utils.posFromNbt(nbt, "accessory");
        super.onSyncDataFromServerArrived(nbt);
    }

    public static abstract class Pole10Kv extends TilePowerPole3 {
        public static class Type0 extends Pole10Kv {
            @Override
            @Nonnull
            @SideOnly(Side.CLIENT)
            protected PowerPoleRenderHelper createRenderHelper() {
            	final TilePowerPole3 pole = this;
                PowerPoleRenderHelper helper = new PowerPoleRenderHelper(world, pos, facing, 1, 3) {
                	@Override
                	public void onUpdate() {
                		PoleAccessoryRendererDispatcher.render(pole, accessory);
                	}
                };
                helper.addInsulatorGroup(0, 0.5F, 0,
                        helper.createInsulator(0, 1.2F, 0, 0.55F, -0.74F),
                        helper.createInsulator(0, 1.2F, 0, 1.5F, 0),
                        helper.createInsulator(0, 1.2F, 0, 0.55F, 0.74F)
                );
                return helper;
            }
        }
        
        public static class Type1 extends Pole10Kv {
            @Override
            @Nonnull
            @SideOnly(Side.CLIENT)
            protected PowerPoleRenderHelper createRenderHelper() {
            	final TilePowerPole3 pole = this;
                int rotation = facing;
                PowerPoleRenderHelper helper = new PowerPoleRenderHelper(world, pos, rotation, 2, 3) {
                    @Override
                    public void onUpdate() {
                    	PoleAccessoryRendererDispatcher.render(pole, accessory);
                    	
                    	if (this.connectionInfo.size() == 2) {
                            PowerPoleRenderHelper.ConnectionInfo[] connection1 = connectionInfo.getFirst();
                            PowerPoleRenderHelper.ConnectionInfo[] connection2 = connectionInfo.getLast();

                            Vec3f pos = new Vec3f(
                                    0.5F + this.pos.getX(),
                                    this.pos.getY() + 1.5F,
                                    0.5F + this.pos.getZ()
                            );


        					this.addExtraWire(connection1[1].fixedFrom, pos, -0.4F);
        					this.addExtraWire(pos, connection2[1].fixedFrom, -0.4F);
                            if (PowerPoleRenderHelper.hasIntersection(
                                    connection1[0].fixedFrom, connection2[0].fixedFrom,
                                    connection1[2].fixedFrom, connection2[2].fixedFrom)) {
        						this.addExtraWire(connection1[0].fixedFrom, connection2[2].fixedFrom, 0.5F);
        						this.addExtraWire(connection1[2].fixedFrom, connection2[0].fixedFrom, 0.5F);
        					} else {
        						this.addExtraWire(connection1[0].fixedFrom, connection2[0].fixedFrom, 0.5F);
        						this.addExtraWire(connection1[2].fixedFrom, connection2[2].fixedFrom, 0.5F);
                            }
                        }
                    }
                };
                helper.addInsulatorGroup(-0.6F, 0.9F, 0,
                        helper.createInsulator(0.5F, 1.2F, -0.05F, 0.1F, -0.74F),
                        helper.createInsulator(0.5F, 1.2F, -0.05F, 0.9F, 0),
                        helper.createInsulator(0.5F, 1.2F, -0.05F, 0.1F, 0.74F)
                );
                helper.addInsulatorGroup(0.6F, 0.9F, 0,
                        helper.createInsulator(0.5F, 1.2F, 0.05F, 0.1F, -0.74F),
                        helper.createInsulator(0.5F, 1.2F, 0.05F, 0.9F, 0),
                        helper.createInsulator(0.5F, 1.2F, 0.05F, 0.1F, 0.74F)
                );
                return helper;
            }
        }
    }
    
    public static class Pole415vType0 extends TilePowerPole3 {
        @Override
        @Nonnull
        @SideOnly(Side.CLIENT)
        protected PowerPoleRenderHelper createRenderHelper() {
            PowerPoleRenderHelper helper = new PowerPoleRenderHelper(this.world, this.pos, this.facing, 1, 4);
            helper.addInsulatorGroup(0, 0.55F, 0,
                    helper.createInsulator(0, 1.2F, 0, 0.3F, -0.9F),
                    helper.createInsulator(0, 1.2F, 0, 0.3F, -0.45F),
                    helper.createInsulator(0, 1.2F, 0, 0.3F, 0.45F),
                    helper.createInsulator(0, 1.2F, 0, 0.3F, 0.9F)
            );
            return helper;
        }
    }
}
