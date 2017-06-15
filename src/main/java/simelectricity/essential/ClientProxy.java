package simelectricity.essential;

import cpw.mods.fml.client.registry.ClientRegistry;

import simelectricity.Templates.Client.Render.RenderTransmissionTower;

import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.cable.render.RenderBlockCable;
import simelectricity.essential.grid.BlockTransmissionTowerTop;
import simelectricity.essential.grid.BlockTransmissionTowerBottom;
import simelectricity.essential.grid.TileTransmissionTower;
import simelectricity.essential.grid.render.RenderTransmissionTowerTop;
import simelectricity.essential.grid.render.RenderTransmissionTowerBottom;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy{	
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
		BlockCable.renderID = (new RenderBlockCable()).getRenderId();
		RenderBlockCable.bakeCableModel(BlockRegistry.blockCable);
		
		BlockTransmissionTowerTop.renderID = (new RenderTransmissionTowerTop()).getRenderId();
		ClientRegistry.bindTileEntitySpecialRenderer(TileTransmissionTower.class, new RenderTransmissionTower());
	
		BlockTransmissionTowerBottom.renderID = (new RenderTransmissionTowerBottom()).getRenderId();
	}
}
