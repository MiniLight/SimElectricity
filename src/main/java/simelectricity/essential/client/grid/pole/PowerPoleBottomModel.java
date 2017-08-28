package simelectricity.essential.client.grid.pole;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rikka.librikka.model.codebased.BlockRenderModel;
import simelectricity.essential.grid.BlockPowerPoleBottom;

import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PowerPoleBottomModel extends BlockRenderModel {
    private final TextureAtlasSprite texture;
    private final LinkedList<BakedQuad> quads;

    public PowerPoleBottomModel(int facing, TextureAtlasSprite texture) {
        this.texture = texture;
        quads = new LinkedList();

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
        Models.renderTower0Bottom(texture).rotateAroundY(rotation).transform(0.5 + offset.getX(), 0, 0.5 + offset.getZ()).bake(this.quads);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.texture;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID) {
            return ImmutableList.copyOf(this.quads);
        }
        return null;
    }
}
