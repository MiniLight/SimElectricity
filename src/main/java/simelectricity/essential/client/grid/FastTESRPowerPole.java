package simelectricity.essential.client.grid;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import rikka.librikka.math.MathAssitant;
import rikka.librikka.math.Vec3f;
import rikka.librikka.model.quadbuilder.RawQuadCube;
import rikka.librikka.model.quadbuilder.RawQuadGroup;
import simelectricity.common.ConfigManager;

@SideOnly(Side.CLIENT)
public class FastTESRPowerPole<T extends TileEntity & ISEPowerPole> extends FastTESR<T> {	
    private static FastTESRPowerPole instance;
    private static TextureAtlasSprite texture;

    /**
     * Do not call this
     */
    public static void stitchTexture(TextureMap map) {
        texture = map.registerSprite(new ResourceLocation("sime_essential:render/transmission/hv_cable"));
    }
    
    public static <T extends TileEntity & ISEPowerPole> void register(Class<T> cls) {
        if (FastTESRPowerPole.instance == null)
        	FastTESRPowerPole.instance = new FastTESRPowerPole();

        ClientRegistry.bindTileEntitySpecialRenderer(cls, FastTESRPowerPole.instance);
    }

    public static RawQuadGroup renderParabolicCable(Object[] vertexAndTension, float thickness) {
    	return renderParabolicCable(vertexAndTension, thickness, texture);
    }
    
    /**
     * 
     * @param vertexesAndTension Vec3f, float, Vec3f, float, ..., Vec3f
     * @param thickness
     * @param texture
     * @return
     */
    public static RawQuadGroup renderParabolicCable(Object[] vertexesAndTension, float thickness, TextureAtlasSprite texture) {
    	RawQuadGroup ret = new RawQuadGroup();
    	Vec3f start = (Vec3f) vertexesAndTension[0];
    	for (int i=1; i<vertexesAndTension.length; i+=2) {
    		float tension = (float) vertexesAndTension[i];
    		Vec3f end = (Vec3f) vertexesAndTension[i+1];
    		ret.merge(renderParabolicCable(start, end, false, tension, thickness, texture));
    		start = end;
    	}
		return ret;
    }
    
    public static RawQuadGroup renderParabolicCable(Vec3f from, Vec3f to, boolean half, float tension, float thickness) {
    	return renderParabolicCable(from, to, half, tension, thickness, texture);
    }
    
	public static RawQuadGroup renderParabolicCable(Vec3f from, Vec3f to, boolean half, float tension, float thickness, TextureAtlasSprite texture) {
		RawQuadGroup ret = new RawQuadGroup();
		
		float steps = ConfigManager.parabolaRenderSteps;
		float length = from.distanceTo(to);
		float b = 4F * tension / length;
		float a = -b / length;
        float unitLength = length / steps;

        float x0, y0, x1, y1;

        for (int i = 0; i < steps / (half ? 2 : 1); i++) {
            x0 = i * unitLength;
            y0 = x0 * x0 * a + x0 * b;
            x1 = (i + 1) * unitLength;
            y1 = x1 * x1 * a + x1 * b;
            
            ret.add((new RawQuadCube(thickness, MathHelper.sqrt(unitLength*unitLength + (y1 - y0)*(y1 - y0)), thickness, texture))
            			.rotateAroundZ((float) Math.atan2(y0 - y1, unitLength) * 180F / MathAssitant.PI)
            			.translateCoord(y0, i * unitLength, 0)
            			);
        }
        
        ret.rotateToVec(from.x, from.y, from.z, to.x, to.y, to.z);
        ret.translateCoord(from.x, from.y, from.z);
        return ret;
	}
    
    @Override
    public boolean isGlobalRenderer(TileEntity te) {
        return true;
    }
    
    protected void bake(T te, PowerPoleRenderHelper helper) {
    	if (helper.extraWires.isEmpty() && helper.connectionInfo.isEmpty())
    		return;
    	
		BlockPos pos = helper.pos;  
        for (PowerPoleRenderHelper.ConnectionInfo[] connections : helper.connectionInfo) {
            for (PowerPoleRenderHelper.ConnectionInfo info : connections) {
            	RawQuadGroup group = renderParabolicCable(info.fixedFrom, info.fixedTo, true, info.tension, 0.06F);
            	group.translateCoord(-pos.getX(), -pos.getY(), -pos.getZ());
            	group.bake(helper.quadBuffer);
            }
        }
        
        for (PowerPoleRenderHelper.ExtraWireInfo wire : helper.extraWires) {
        	RawQuadGroup group = renderParabolicCable(wire.from, wire.to, false, wire.tension, 0.06F);
        	group.translateCoord(-pos.getX(), -pos.getY(), -pos.getZ());
        	group.bake(helper.quadBuffer);
        }
    }
    
	@Override
	public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		if (te.isInvalid())
			return;
		
		PowerPoleRenderHelper helper = te.getRenderHelper();
		
        if (helper == null)
            return;
              
        if (helper.needBake())
        	bake(te, helper);
        
		buffer.setTranslation(x, y, z);
		
		int i = 15728640;
		for (BakedQuad quad: helper.quadBuffer) {
			buffer.addVertexData(quad.getVertexData());
			buffer.putBrightness4(i, i, i, i);
			
			float diffuse = 1;
            if(quad.shouldApplyDiffuseLighting())
                diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(quad.getFace());

            buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
            buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
            buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
            buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
			
			buffer.putPosition(0, 0, 0);
		}
	}
}
