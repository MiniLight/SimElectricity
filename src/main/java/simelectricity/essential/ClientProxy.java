package simelectricity.essential;

import cpw.mods.fml.client.registry.ClientRegistry;


import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.cable.render.RenderBlockCable;
import simelectricity.essential.grid.BlockCableJoint;
import simelectricity.essential.grid.BlockTransmissionTowerTop;
import simelectricity.essential.grid.BlockTransmissionTowerBottom;
import simelectricity.essential.grid.TileCableJoint;
import simelectricity.essential.grid.TileTransmissionTower;
import simelectricity.essential.grid.render.BlockRenderCableJoint;
import simelectricity.essential.grid.render.TileRenderTransmissionTower;
import simelectricity.essential.grid.render.BlockRenderTransmissionTowerTop;
import simelectricity.essential.grid.render.BlockRenderTransmissionTowerBottom;
import simelectricity.essential.grid.render.TileRenderTranmissionTowerBase;
import simelectricity.essential.machines.gui.GuiAdjustableResistor;
import simelectricity.essential.machines.gui.GuiAdjustableTransformer;
import simelectricity.essential.machines.gui.GuiDiode;
import simelectricity.essential.machines.gui.GuiQuantumGenerator;
import simelectricity.essential.machines.gui.GuiSwitch;
import simelectricity.essential.machines.gui.GuiVoltageMeter;
import simelectricity.essential.machines.gui.GuiVoltageRegulator;
import simelectricity.essential.machines.render.BlockRenderMachine;
import simelectricity.essential.machines.tile.TileAdjustableResistor;
import simelectricity.essential.machines.tile.TileAdjustableTransformer;
import simelectricity.essential.machines.tile.TileDiode;
import simelectricity.essential.machines.tile.TileQuantumGenerator;
import simelectricity.essential.machines.tile.TileSwitch;
import simelectricity.essential.machines.tile.TileVoltageMeter;
import simelectricity.essential.machines.tile.TileVoltageRegulator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy{
	@Override
	public EntityPlayer getClientPlayer(){
		return Minecraft.getMinecraft().thePlayer;
	}
	
	@Override
	public World getClientWorld(){
		return Minecraft.getMinecraft().theWorld;
	}
	
	@Override
	public Object getClientThread() {
		return Minecraft.getMinecraft();
	}
	
	@Override
	public void registerRenders() {
		//Cable
		BlockCable.renderID = (new RenderBlockCable()).getRenderId();
		RenderBlockCable.bakeCableModel(BlockRegistry.blockCable);
		
		//Transmission Tower
		BlockTransmissionTowerTop.renderID = (new BlockRenderTransmissionTowerTop()).getRenderId();
		ClientRegistry.bindTileEntitySpecialRenderer(TileTransmissionTower.class, new TileRenderTransmissionTower());
		BlockTransmissionTowerBottom.renderID = (new BlockRenderTransmissionTowerBottom()).getRenderId();
		
		//Cable Joint
		BlockCableJoint.renderID = (new BlockRenderCableJoint()).getRenderId();
		ClientRegistry.bindTileEntitySpecialRenderer(TileCableJoint.class, new TileRenderTranmissionTowerBase());
		
		BlockRegistry.blockElectronics.renderID = (new BlockRenderMachine()).getRenderId();
		BlockRegistry.blockTwoPortElectronics.renderID = BlockRegistry.blockElectronics.renderID;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		Container container = BlockRegistry.getContainer(te, player);
		
		if (te instanceof TileVoltageMeter)
			return new GuiVoltageMeter(container);
		if (te instanceof TileQuantumGenerator)
			return new GuiQuantumGenerator(container);
		if (te instanceof TileAdjustableResistor)
			return new GuiAdjustableResistor(container);
		
		if (te instanceof TileAdjustableTransformer)
			return new GuiAdjustableTransformer(container);
		if (te instanceof TileVoltageRegulator)
			return new GuiVoltageRegulator(container);
		if (te instanceof TileDiode)
			return new GuiDiode(container);
		if (te instanceof TileSwitch)
			return new GuiSwitch(container);
		
		return null;
	}
}
