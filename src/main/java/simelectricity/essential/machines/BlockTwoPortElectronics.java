package simelectricity.essential.machines;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.Utils;
import simelectricity.essential.Essential;
import simelectricity.essential.client.semachine.ISESidedTextureBlock;
import simelectricity.essential.common.semachine.SEMachineBlock;
import simelectricity.essential.common.semachine.SETwoPortMachine;
import simelectricity.essential.machines.tile.TileAdjustableTransformer;
import simelectricity.essential.machines.tile.TileCurrentSensor;
import simelectricity.essential.machines.tile.TileDiode;
import simelectricity.essential.machines.tile.TileSwitch;

import javax.annotation.Nullable;

public class BlockTwoPortElectronics extends SEMachineBlock implements ISESidedTextureBlock {
    public static String subNames[] = {"adjustable_transformer", "current_sensor", "diode", "switch"};

    ///////////////////////////////
    ///Block Properties
    ///////////////////////////////
    public BlockTwoPortElectronics() {
        super("essential_two_port_electronics", BlockTwoPortElectronics.subNames);
    }

    @Override
    protected int getMetaUpperBound() {
        return BlockTwoPortElectronics.subNames.length;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (meta) {
            case 0:
                return new TileAdjustableTransformer();
            case 1:
                return new TileCurrentSensor();
            case 2:
                return new TileDiode();
            case 3:
                return new TileSwitch();
        }
        return null;
    }

    ///////////////////////////////
    ///ISESidedTextureBlock
    ///////////////////////////////
    @Override
    @SideOnly(Side.CLIENT)
    public String getModelNameFrom(IBlockState blockState) {
        int meta = blockState.getValue(propertyMeta);
        return "electronics_" + BlockTwoPortElectronics.subNames[meta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasSecondState(IBlockState state) {
        int meta = getMetaFromState(state);

        return meta == 3;
    }

    //////////////////////////////////////
    /////Item drops and Block activities
    //////////////////////////////////////
    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this.itemBlock, 1, getMetaFromState(world.getBlockState(pos)));
    }

    @Override
    protected boolean isSecondState(TileEntity te) {
        return te instanceof TileSwitch && !((TileSwitch) te).isOn;
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking())
            return false;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSwitch) {
            TileSwitch tileSwitch = (TileSwitch) te;
            if (tileSwitch.getFacing() == facing) {
                if (tileSwitch.isOn) {
                    world.playSound(player, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
                } else {
                    world.playSound(player, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
                }
                if (!world.isRemote)
                    tileSwitch.setSwitchStatus(!tileSwitch.isOn);
                return true;
            }
        }

        if (!world.isRemote)
            player.openGui(Essential.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof SETwoPortMachine) {
            EnumFacing sight = Utils.getPlayerSight(placer);
            ((SETwoPortMachine) te).setFacing(sight.getOpposite());

            if (te instanceof TileSwitch)
                ((SETwoPortMachine) te).setFunctionalSide(EnumFacing.UP, EnumFacing.DOWN);
            else
                ((SETwoPortMachine) te).setFunctionalSide(sight.getOpposite(), sight);
        }
    }

    ///////////////////////
    ///Redstone
    ///////////////////////
    @Override
    public boolean canProvidePower(IBlockState state) {
        int meta = getMetaFromState(state);
        return meta == 1;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);

        return !(te instanceof TileCurrentSensor);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);

        return te instanceof TileCurrentSensor;

    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileCurrentSensor)
            return ((TileCurrentSensor) te).emitRedstoneSignal ? 15 : 0;

        return 0;
    }
}
