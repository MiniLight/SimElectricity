package simelectricity.essential.client.semachine;

import java.util.List;

import simelectricity.essential.Essential;
import simelectricity.essential.common.semachine.ISESocketProvider;
import simelectricity.essential.utils.client.SERawQuadCube;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SocketRender {	
	private static TextureAtlasSprite[] icons;
	
	public SocketRender(){
		this.icons = new TextureAtlasSprite[ISESocketProvider.numOfSockets];
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
    public void stitcherEventPre(TextureStitchEvent.Pre event) {
		//Register textures
		TextureMap map = event.getMap();

		for (int i=0; i<icons.length; i++)
			icons[i] = map.registerSprite(new ResourceLocation(Essential.modID + ":blocks/sockets/" + i));
	}
	
	public static void getBaked(List<BakedQuad> list, int[] iconIndex){		
		TextureAtlasSprite[] textures = new TextureAtlasSprite[6];
		for (int side=0; side<6; side++){
			int i = iconIndex[side];
			if (i >= icons.length)
				i = 0;
			
			textures[side] = i<0 ? null : icons[i];
		}
		
		SERawQuadCube cube = new SERawQuadCube(1.0001F, 1.0001F, 1.0001F, textures);
		cube.translateCoord(0.5F, 0, 0.5F);
		cube.bake(list);
	}
}
