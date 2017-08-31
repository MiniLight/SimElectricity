package simelectricity.essential.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import rikka.librikka.Utils;
import rikka.librikka.item.ISimpleTexture;
import rikka.librikka.item.ItemBase;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISEGridNode;
import simelectricity.essential.api.ISEHVCableConnector;

import java.util.HashMap;
import java.util.Map;

public class ItemHighVoltageCable extends ItemBase implements ISimpleTexture {
    private static final String[] subNames = {"copper", "aluminum"};
    private static final double[] resistivityList = {0.1, 0.2};
    private final Map<EntityPlayer, BlockPos> lastCoordinates;

    public ItemHighVoltageCable() {
        super("essential_hv_cable", true);
        lastCoordinates = new HashMap<EntityPlayer, BlockPos>();
    }

    private static boolean numberOfConductorMatched(ISEGridNode node1, ISEGridNode node2) {
        if (node1.numOfParallelConductor() == 0 || node2.numOfParallelConductor() == 0)
            return true;
        return node1.numOfParallelConductor() == node2.numOfParallelConductor();
    }

    @Override
    public void beforeRegister() {
        setCreativeTab(SEAPI.SETab);
    }

    @Override
    public String[] getSubItemUnlocalizedNames() {
        return ItemHighVoltageCable.subNames;
    }

    @Override
    public String getIconName(int damage) {
        return "hvcable_" + ItemHighVoltageCable.subNames[damage];
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);

        if (itemStack.getItem() != this) {
            itemStack = player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
            if (itemStack.getItem() != this)
                return EnumActionResult.FAIL;
        }

        if (world.isRemote)
            return EnumActionResult.SUCCESS;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Block block = world.getBlockState(pos).getBlock();

        if (!(block instanceof ISEHVCableConnector))
            return EnumActionResult.SUCCESS;

        ISEHVCableConnector connector1 = (ISEHVCableConnector) block;

        if (!this.lastCoordinates.containsKey(player))
            this.lastCoordinates.put(player, null);

        BlockPos lastCoordinate = this.lastCoordinates.get(player);

        if (lastCoordinate == null) {    //First selection
            if (connector1.canHVCableConnect(world, pos)) {
                lastCoordinate = new BlockPos(pos);
                Utils.chatWithLocalization(player, "chat.sime_essential:powerpole_selected");
            } else {
                Utils.chatWithLocalization(player, "chat.sime_essential:powerpole_connection_denied");
            }

            this.lastCoordinates.put(player, lastCoordinate);
        } else {
            Block neighbor = world.getBlockState(lastCoordinate).getBlock();

            if (neighbor instanceof ISEHVCableConnector) {
                ISEHVCableConnector connector2 = (ISEHVCableConnector) neighbor;
                ISEGridNode node1 = (ISEGridNode) connector1.getNode(world, pos);
                ISEGridNode node2 = (ISEGridNode) connector2.getNode(world, new BlockPos(lastCoordinate));

                if (node1 == node2) {
                    Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_recursive_connection"));
                } else if (!connector1.canHVCableConnect(world, pos)) {
                    Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_current_selection_invalid"));
                } else if (!connector2.canHVCableConnect(world, lastCoordinate)) {
                    Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_last_selection_invalid"));
                } else if (!ItemHighVoltageCable.numberOfConductorMatched(node1, node2)) {
                    Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_type_mismatch"));
                } else {
                    double distance = node1.getPos().distanceSq(node2.getPos());
                    if (distance < 5 * 5) {
                        Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_too_close"));
                    } else if (distance > 200 * 200) {
                        Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_too_far"));
                    } else {
                        double resistance = distance * ItemHighVoltageCable.resistivityList[itemStack.getItemDamage()];    //Calculate the resistance
                        if (node1 != null && node2 != null &&
                                SEAPI.energyNetAgent.isNodeValid(world, node1) &&
                                SEAPI.energyNetAgent.isNodeValid(world, node2)) {

                            SEAPI.energyNetAgent.connectGridNode(world, node1, node2, resistance);
                        }
                    }
                }
            } else {
                Utils.chatWithLocalization(player, I18n.translateToLocal("chat.sime_essential:powerpole_current_selection_invalid"));
            }

            this.lastCoordinates.put(player, null);
        }

        return EnumActionResult.SUCCESS;
    }
}
