package simelectricity.essential.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.IMetaProvider;
import rikka.librikka.IMetaBase;
import rikka.librikka.item.ItemBase;
import simelectricity.api.SEAPI;
import simelectricity.essential.api.coverpanel.ISECoverPanel;
import simelectricity.essential.coverpanel.LedPanel;
import simelectricity.essential.coverpanel.VoltageSensorPanel;

/**
 * The ItemMisc creates very simple and basic items
 * e.g. raw materials and things cannot be "used" by players
 * @author Rikka0w0
 */
public final class ItemMisc extends ItemBase implements IMetaProvider<IMetaBase> {
    public static enum ItemType implements IMetaBase {
    	ledpanel(LedPanel::new),
    	voltagesensor(VoltageSensorPanel::new),
    	facade(null),
    	facade_hollow(null);
    	
    	public final Supplier<ISECoverPanel> constructor;
    	ItemType(Supplier<ISECoverPanel> constructor) {
    		this.constructor = constructor;
    	}
    }
    
    public final ItemType itemType;
    private ItemMisc(ItemType itemType) {
        super("item_" + itemType.name(), (new Item.Properties())
        		.group(SEAPI.SETab));
        this.itemType = itemType;
    }
    
    @Override
	public IMetaBase meta() {
		return itemType;
	}
    
    public static ItemMisc[] create() {
    	ItemMisc[] ret = new ItemMisc[ItemType.values().length];
    	for (ItemType meta: ItemType.values()) {
    		ret[meta.ordinal()] = new ItemMisc(meta);
    	}
    	return ret;
    }
    
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (this.itemType != ItemType.facade && this.itemType != ItemType.facade_hollow)
			return super.onItemUse(context);
		BlockState blockstate = context.getWorld().getBlockState(context.getPos());
		
		if (!blockstate.isSolid())
			return super.onItemUse(context);
		
		CompoundNBT bsNBT = NBTUtil.writeBlockState(blockstate);
		context.getItem().getOrCreateTag().put("facade_blockstate", bsNBT);
		
		return ActionResultType.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (this.itemType != ItemType.facade && this.itemType != ItemType.facade_hollow || !stack.hasTag()) 
			return;

		BlockState blockstate = NBTUtil.readBlockState(stack.getTag().getCompound("facade_blockstate"));
		if (!blockstate.isAir())
			tooltip.add(blockstate.getBlock().getNameTextComponent());
	}
}
