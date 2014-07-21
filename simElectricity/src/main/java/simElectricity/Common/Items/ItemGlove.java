package simElectricity.Common.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Common.Items.ItemSE;
import simElectricity.API.ISidedFacing;
import simElectricity.API.Util;

public class ItemGlove extends ItemSE {
    public ItemGlove() {
        super();
        maxStackSize = 1;
        setHasSubtypes(true);
        setUnlocalizedName("Glove");
        setMaxDamage(256);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister r) {
        itemIcon = r.registerIcon("simElectricity:Item_Glove");
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if ((world.getTileEntity(x, y, z) instanceof ISidedFacing) & (!world.isRemote)) {
            ISidedFacing te = (ISidedFacing) world.getTileEntity(x, y, z);
            ForgeDirection newFacing = Util.getPlayerSight(player).getOpposite();

            if (te.canSetFacing(newFacing)) {
                te.setFacing(newFacing);
                Util.updateTileEntityFacing((TileEntity) te);
                item.damageItem(1, player);
            }
            return true;
        } else {
            return false;
        }
    }
}
