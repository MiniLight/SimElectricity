package simElectricity.Common.Blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Common.Blocks.BlockContainerSE;
import simElectricity.API.Energy;
import simElectricity.API.Util;
import simElectricity.Common.Blocks.TileEntity.TileWire;
import simElectricity.Common.Items.ItemBlocks.ItemBlockWire;

import java.util.List;
import java.util.Random;

public class BlockWire extends BlockContainerSE {

    public static final String[] subNames = { "CopperCable_Thin", "CopperCable_Medium", "CopperCable_Thick" };
    public static final float[] resistanceList = { 0.27F, 0.09F, 0.03F };
    public static final float[] collisionWidthList = { 0.12F, 0.22F, 0.32F };
    public static final float[] renderingWidthList = { 0.1F, 0.2F, 0.3F };

    public IIcon[] iconBuffer = new IIcon[subNames.length];


    //Initialize Block
    public BlockWire() {
        super(Material.circuits);
        setHardness(0.2F);
        setResistance(5.0F);
        setBlockName("Wire");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float f1, float f2, float f3) {
        if (player.isSneaking())
            return false;
    	        
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (!(tileEntity instanceof TileWire))
        	return false;
        
        TileWire wire = (TileWire) tileEntity;
        
    	if (player.getCurrentEquippedItem() !=null){
    		ItemStack stack = player.getCurrentEquippedItem();
    		if (stack.getItem() == Items.dye){
    			if (!world.isRemote){
    				wire.color = stack.getItemDamage() + 1;           //Set the color
    				Energy.postTileRejoinEvent(tileEntity);           //Reconnect the wire to the energy network
    				Util.updateTileEntityField(tileEntity, "color");  //Update the field color to every client within the dimension
    				onBlockPlacedBy(world, x, y, z, player, null);    //Update rests to clients
    			}
    			
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            Util.scheduleBlockUpdate(world.getTileEntity(x, y, z));
            //updateRenderSides();
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(x, y, z);
        ((TileWire) te).updateSides();                  //Update information about rendering
        Util.updateTileEntityField(te, "renderSides");  //Synchronize the field to clients

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS){ //Update neighbors
        	updateRenderSides(world.getTileEntity(x + direction.offsetX, y + direction.offsetY, z+direction.offsetZ));
        }
    }

    void updateRenderSides(TileEntity te) {
        if (te instanceof TileWire) {
            ((TileWire) te).updateSides();
            Util.updateTileEntityField(te, "renderSides");
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote)
            return;
        ((TileWire) world.getTileEntity(x, y, z)).updateSides();
        Util.updateTileEntityField(world.getTileEntity(x, y, z), "renderSides");
    }


    // Rendering

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(1F, 0.6F, 1F, 0.0F, 0.6F, 0.0F);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity) {
        super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
        TileWire tile = (TileWire) world.getTileEntity(x, y, z);
        float WIDTH = collisionWidthList[world.getBlockMetadata(x, y, z)];

        float minA = 0.5F - WIDTH, maxA = 0.5F + WIDTH;
        float minX = 0.5F - WIDTH,
                minY = 0.5F - WIDTH,
                minZ = 0.5F - WIDTH,
                maxX = 0.5F + WIDTH,
                maxY = 0.5F + WIDTH,
                maxZ = 0.5F + WIDTH;

        //X方向
        boolean[] arr = tile.renderSides;

        if (arr[5])
            maxA = 1.0F;
        if (arr[4])
            minA = 0.0F;
        if (arr[5] || arr[4]) {
            setBlockBounds(minA, minY, minZ, maxA, maxY, maxZ);
            super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
        }

        if (arr[3])
            maxA = 1.0F;
        else maxA = 0.5F + WIDTH;
        if (arr[2])
            minA = 0.0F;
        else minA = 0.5F - WIDTH;
        if (arr[3] || arr[2]) {
            setBlockBounds(minX, minY, minA, maxX, maxY, maxA);
            super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
        }

        if (arr[1])
            maxA = 1.0F;
        else maxA = 0.5F + WIDTH;
        if (arr[0])
            minA = 0.0F;
        else minA = 0.5F - WIDTH;
        if (arr[1] || arr[0]) {
            setBlockBounds(minX, minA, minZ, maxX, maxA, maxZ);
            super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
        }
    }


    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float WIDTH = collisionWidthList[world.getBlockMetadata(x, y, z)];
        boolean[] possibleConnections = ((TileWire) world.getTileEntity(x, y, z)).renderSides;

        minX = 0.5 - WIDTH;
        minY = 0.5 - WIDTH;
        minZ = 0.5 - WIDTH;
        maxX = 0.5 + WIDTH;
        maxY = 0.5 + WIDTH;
        maxZ = 0.5 + WIDTH;

        if (possibleConnections[0])
            minY = 0;

        if (possibleConnections[1])
            maxY = 1;

        if (possibleConnections[2])
            minZ = 0;

        if (possibleConnections[3])
            maxZ = 1;

        if (possibleConnections[4])
            minX = 0;

        if (possibleConnections[5])
            maxX = 1;
    }

    //This will tell minecraft not to render any side of our cube.
    @Override
    public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int i, int j, int k, int l) {
        return false;
    }

    //And this tell it that you can see through this block, and neighbor blocks should be rendered.
    @Override
    public boolean isOpaqueCube() {
        return false;
    }


    //Multi block stuff starts

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister r) {
        for (int i = 0; i < subNames.length; i++) {
            iconBuffer[i] = r.registerIcon("simElectricity:Wiring/" + subNames[i]);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int blockMeta = world.getBlockMetadata(x, y, z);
        return iconBuffer[blockMeta];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return iconBuffer[meta];
    }

    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List subItems) {
        for (int ix = 0; ix < subNames.length; ix++) {
            subItems.add(new ItemStack(this, 1, ix));
        }
    }

    @Override
    public Block setBlockName(String name) {
        GameRegistry.registerBlock(this, ItemBlockWire.class, name);
        return super.setBlockName(name);
    }

    @Override
    public boolean shouldRegister() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        return new TileWire(meta);
    }
}
