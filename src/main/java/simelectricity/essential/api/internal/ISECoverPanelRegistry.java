package simelectricity.essential.api.internal;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import simelectricity.essential.api.ISECoverPanelFactory;
import simelectricity.essential.api.coverpanel.ISECoverPanel;

public interface ISECoverPanelRegistry {
    void registerCoverPanelFactory(ISECoverPanelFactory factory);

    ISECoverPanel fromItemStack(ItemStack itemStack);

    ISECoverPanel fromNBT(CompoundNBT nbt);
}
