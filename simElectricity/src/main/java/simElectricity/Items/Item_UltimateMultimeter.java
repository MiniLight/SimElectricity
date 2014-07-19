package simElectricity.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import simElectricity.API.Energy;
import simElectricity.API.EnergyTile.*;
import simElectricity.API.Util;

public class Item_UltimateMultimeter extends Item {
    public Item_UltimateMultimeter() {
        super();
        maxStackSize = 1;
        setHasSubtypes(true);
        setUnlocalizedName("Item_UltimateMultimeter");
        setMaxDamage(256);
        setCreativeTab(Util.SETab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister r) {
        itemIcon = r.registerIcon("simElectricity:Item_UltimateMultimeter");
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof ITransformer && (!(world.isRemote))) {
            ITransformer transformer = (ITransformer) tile;
            ITransformer.ITransformerWinding primary = transformer.getPrimary();
            ITransformer.ITransformerWinding secondary = transformer.getSecondary();

            Util.chat(player, "-----------------------");
            Util.chat(player, "Transformer");
            Util.chat(player, "Primary Voltage: " + String.valueOf(Energy.getVoltage(primary)));
            Util.chat(player, "Secondary Voltage: " + String.valueOf(Energy.getVoltage(secondary)));

            float secondaryResistance = transformer.getResistance();
            float secondaryCurrent = (transformer.getRatio() * Energy.getVoltage(primary) - Energy.getVoltage(secondary)) / secondaryResistance;
            Util.chat(player, "Secondary current: " + String.valueOf(secondaryCurrent));
            Util.chat(player, "Power loss: " + String.valueOf(secondaryCurrent * secondaryCurrent * secondaryResistance));
        }

        if ((tile instanceof IBaseComponent || tile instanceof IComplexTile) && (!(world.isRemote))) {
            IBaseComponent te = null;
            if (tile instanceof IBaseComponent) {
                te = (IBaseComponent) tile;
            } else if (tile instanceof IComplexTile) {
                te = ((IComplexTile) tile).getCircuitComponent(Util.getPlayerSight(player).getOpposite());
            }

            if (te == null)
                return false;

            String tileType = "Unknown";
            float outputVoltage = 0;


            Util.chat(player, "-----------------------");
            if (te instanceof ICircuitComponent) {
                ICircuitComponent ps = (ICircuitComponent) te;
                if (((ICircuitComponent) te).getOutputVoltage() == 0)
                    tileType = "Energy Sink";
                else {
                    tileType = "Energy Source";
                    outputVoltage = ((ICircuitComponent) te).getOutputVoltage();
                }

                if (!(te instanceof IEnergyTile))
                    tileType += "(SubComponent)";
            }

            if (te instanceof IConductor) {
                IConductor c = (IConductor) te;
                tileType = "Energy Conductor";
            }

            //Print out information here
            Util.chat(player, "Type: " + tileType);
            if (te instanceof IEnergyTile)
                Util.chat(player, "FunctionalSide: " + ((IEnergyTile) te).getFunctionalSide().toString());

            if (te instanceof ICircuitComponent && outputVoltage > 0)
                Util.chat(player, "Internal resistance: " + String.valueOf(te.getResistance()) + "\u03a9");
            else
                Util.chat(player, "Resistance: " + String.valueOf(te.getResistance()) + "\u03a9");

            if (te instanceof ICircuitComponent) {
                Util.chat(player, "Current: " + String.valueOf(Energy.getCurrent((ICircuitComponent) te, tile.getWorldObj())) + "A");
                Util.chat(player, "Power rate: " + String.valueOf(Energy.getPower((ICircuitComponent) te, tile.getWorldObj())) + "W");
            }
            Util.chat(player, "Voltage: " + String.valueOf(Energy.getVoltage(te, tile.getWorldObj())) + "V");
            if (outputVoltage > 0) { //Energy Source
                Util.chat(player, "Internal voltage: " + String.valueOf(outputVoltage) + "V");
                Util.chat(player, "Output rate: " + String.valueOf(outputVoltage * Energy.getCurrent((ICircuitComponent) te, tile.getWorldObj())) + "W");
            }

            return true;
        } else {
            return false;
        }
    }
}
