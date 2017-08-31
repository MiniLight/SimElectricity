package simelectricity.essential.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.item.ISimpleTexture;
import rikka.librikka.item.ItemBase;
import simelectricity.api.SEAPI;

public class ItemVitaTea extends ItemBase implements ISimpleTexture {
    public ItemVitaTea() {
        super("cell_vita", false);

    }

    @Override
    public void beforeRegister() {
        setCreativeTab(SEAPI.SETab);
    }

    @Override
    public String getIconName(int damage) {
        return "cell_vita";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
