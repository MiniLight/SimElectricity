package simelectricity.extension.facades;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import simelectricity.essential.api.ISECoverPanelFactory;
import simelectricity.essential.api.coverpanel.ISECoverPanel;

public class TECoverFactory implements ISECoverPanelFactory{
    @Override
    public boolean acceptItemStack(ItemStack itemStack) {
        Item item = itemStack.getItem();

        return item.getUnlocalizedName().startsWith("item.thermaldynamics.cover");
    }

    @Override
    public ISECoverPanel fromItemStack(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt.hasKey("Meta", 1) && nbt.hasKey("Block", 8)) {
            String blockName = nbt.getString("Block");
            Block block = Block.getBlockFromName(blockName);
            int meta = nbt.getByte("Meta");

            return new TEFacadePanel(block.getStateFromMeta(meta), itemStack);
        }
        return null;
    }

    @Override
    public boolean acceptNBT(NBTTagCompound nbt) {
        return nbt.getString("coverPanelType").equals("TEFacade");
    }

    @Override
    public ISECoverPanel fromNBT(NBTTagCompound nbt) {
        return new TEFacadePanel(nbt);
    }
}
