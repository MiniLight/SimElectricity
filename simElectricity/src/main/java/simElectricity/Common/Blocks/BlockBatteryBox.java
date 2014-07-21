package simElectricity.Common.Blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Common.Blocks.BlockStandardGenerator;
import simElectricity.API.Util;
import simElectricity.Common.Blocks.TileEntity.TileBatteryBox;

import java.util.Random;

public class BlockBatteryBox extends BlockStandardGenerator {
    private IIcon[] iconBuffer = new IIcon[6];

    public BlockBatteryBox() {
        super();
        setHardness(2.0F);
        setResistance(5.0F);
        setBlockName("BatteryBox");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister r) {
        iconBuffer[0] = r.registerIcon("simElectricity:SolarPanel_Bottom");
        iconBuffer[1] = r.registerIcon("simElectricity:SolarPanel_Bottom");
        iconBuffer[2] = r.registerIcon("simElectricity:SolarPanel_Front");
        iconBuffer[3] = r.registerIcon("simElectricity:SolarPanel_Side");
        iconBuffer[4] = r.registerIcon("simElectricity:SolarPanel_Side");
        iconBuffer[5] = r.registerIcon("simElectricity:SolarPanel_Side");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (!(te instanceof TileBatteryBox))
            return iconBuffer[0];

        int iconIndex = Util.getTextureOnSide(side, ((TileBatteryBox) te).getFunctionalSide());

        if (((TileBatteryBox) te).getFunctionalSide() == ForgeDirection.DOWN) {
            if (iconIndex == 3) {
                iconIndex = 1;
            } else if (iconIndex == 1) {
                iconIndex = 3;
            }
        }

        return iconBuffer[iconIndex];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return iconBuffer[Util.getTextureOnSide(side, ForgeDirection.WEST)];
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote)
            return;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileBatteryBox))
            return;
        Util.updateTileEntityFunctionalSide(te);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (world.isRemote)
            return;

        //Server side only!
        TileEntity te = world.getTileEntity(x, y, z);
        Util.updateTileEntityFunctionalSide(te);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileBatteryBox();
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }
}
