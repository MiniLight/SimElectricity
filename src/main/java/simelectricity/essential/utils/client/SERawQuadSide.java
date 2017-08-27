package simelectricity.essential.utils.client;

import com.google.common.primitives.Ints;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.awt.*;

public class SERawQuadSide {
    public static BakedQuad bake(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            TextureAtlasSprite texture) {

        EnumFacing side = SEBakedQuadHelper.getFacingFromVertexes(
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3,
                x4, y4, z4);

        float u = texture.getIconWidth();
        float v = texture.getIconHeight();

        return new BakedQuad(Ints.concat(
                SEBakedQuadHelper.vertexToInts(x1, y1, z1, Color.WHITE.getRGB(), texture, 0, 0),
                SEBakedQuadHelper.vertexToInts(x2, y2, z2, Color.WHITE.getRGB(), texture, 0, v),
                SEBakedQuadHelper.vertexToInts(x3, y3, z3, Color.WHITE.getRGB(), texture, u, v),
                SEBakedQuadHelper.vertexToInts(x4, y4, z4, Color.WHITE.getRGB(), texture, u, 0)
        ), 0, side, texture);
    }
}
