package simelectricity.essential.client.grid.pole;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.model.codebased.CodeBasedModel;
import simelectricity.essential.grid.BlockPowerPoleBottom;

@SideOnly(Side.CLIENT)
public class PowerPoleBottomRawModel extends CodeBasedModel {
    private final ResourceLocation texture;
    private TextureAtlasSprite particle;

    public PowerPoleBottomRawModel() {
        texture = this.registerTexture("sime_essential:render/transmission/metal");
    }

    @Override
    public void bake(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    	this.particle = bakedTextureGetter.apply(texture);
    	
    	for (int facing=0; facing<8; facing++) {
            int rotation = 0;
            switch (facing) {
                case 1:
                    rotation = 0;
                    break;
                case 3:
                    rotation = 90;
                    break;
                case 5:
                    rotation = 180;
                    break;
                case 7:
                    rotation = 270;
                    break;

                case 2:
                    rotation = 45;
                    break;
                case 4:
                    rotation = 135;
                    break;
                case 6:
                    rotation = 225;
                    break;
                case 0:
                    rotation = 315;
                    break;
            }

            Vec3i offset = BlockPowerPoleBottom.getCenterBoxOffset(facing);
            LinkedList<BakedQuad> quads = new LinkedList();
            Models.renderTower0Bottom(this.particle).rotateAroundY(rotation).transform(0.5 + offset.getX(), 0, 0.5 + offset.getZ()).bake(quads);
            FastTESRPowerPoleBottom.bakedModel[facing] = quads;
    	}
    }

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return ImmutableList.of();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particle;
	}
}
