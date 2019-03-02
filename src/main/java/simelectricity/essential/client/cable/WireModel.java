package simelectricity.essential.client.cable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import rikka.librikka.model.CodeBasedModel;
import rikka.librikka.model.quadbuilder.IRawModel;
import rikka.librikka.model.quadbuilder.RawQuadCube;
import rikka.librikka.model.quadbuilder.RawQuadGroup;
import rikka.librikka.properties.UnlistedPropertyRef;
import simelectricity.essential.api.ISEGenericWire;
import simelectricity.essential.cable.BlockWire;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class WireModel extends CodeBasedModel {
    private final ResourceLocation insulatorTextureLoc, conductorTextureLoc;
    private TextureAtlasSprite insulatorTexture, conductorTexture;
    public final float thickness;

    public WireModel(String domain, String name, float thickness) {
        this.thickness = thickness;
//        this.insulatorTextureLoc = this.registerTexture(domain + ":blocks/" + name + "_insulator");    // We just want to bypass the ModelBakery
//        this.conductorTextureLoc = this.registerTexture(domain + ":blocks/" + name + "_conductor");    // and load our texture
        this.insulatorTextureLoc = this.registerTexture(domain + ":blocks/cable/essential_cable_aluminum_thin_insulator");    // We just want to bypass the ModelBakery
        this.conductorTextureLoc = this.registerTexture(domain + ":blocks/cable/essential_cable_aluminum_thin_conductor");    // and load our texture
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.conductorTexture;
    }

    @Override
    protected void bake(Function<ResourceLocation, TextureAtlasSprite> textureRegistry) {
        this.conductorTexture = textureRegistry.apply(conductorTextureLoc);
        this.insulatorTexture = textureRegistry.apply(insulatorTextureLoc);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState blockState,
                                    @Nullable EnumFacing cullingSide, long rand) {
        List<BakedQuad> quads = new ArrayList<BakedQuad>();

        TileEntity te = UnlistedPropertyRef.get(blockState);

        if (!(te instanceof ISEGenericWire))
            return quads;

        ISEGenericWire wireTile = (ISEGenericWire) te;

        if (cullingSide == null) {
            //Render center & branches in SOLID layer
            if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID) {
                for (EnumFacing wire_side: EnumFacing.VALUES) {
                    byte numOfCon = 0;
                    EnumFacing conSide = EnumFacing.DOWN;

                    TextureAtlasSprite[] centerTexture = {this.insulatorTexture, this.insulatorTexture,
                            this.insulatorTexture, this.insulatorTexture,
                            this.insulatorTexture, this.insulatorTexture};

                    RawQuadGroup group = new RawQuadGroup();

                    for (EnumFacing direction : EnumFacing.VALUES) {
                        if (wireTile.getWireParam(wire_side).hasBranchOnSide(direction)) {
                            group.add(genBranch(direction, wireTile.getWireParam(direction).hasBranchOnSide(wire_side)));
                            centerTexture[direction.ordinal()] = null;
                            conSide = direction;
                            numOfCon++;
                        }
                    }

                    if (numOfCon > 0) {
                        if (numOfCon == 1) {
                            centerTexture[conSide.getOpposite().ordinal()] = this.conductorTexture;
                        }

                        // Center
                        RawQuadCube cube = new RawQuadCube(thickness, thickness, thickness, centerTexture);
                        cube.translateCoord(0.5F, 0.5F - thickness / 2, 0.5F);
                        group.add(cube);
                    }

                    translateGroupCoord(wire_side, group);

                    group.bake(quads);
                }
            }

            // Corners
            for (EnumFacing[] pair: BlockWire.corners) {
                EnumFacing wire_side = pair[0];
                EnumFacing to = pair[1];

                if (
                        wireTile.getWireParam(wire_side).hasBranchOnSide(to) &&
                        wireTile.getWireParam(to).hasBranchOnSide(wire_side)
                        ) {
                    // Corner - interior
                    RawQuadCube cube = genCorner(to, false);
                    translateGroupCoord(wire_side, cube);
                    cube.bake(quads);
                }
            }


            for (EnumFacing side: EnumFacing.VALUES) {
                for (EnumFacing to : EnumFacing.VALUES) {
                    if (wireTile.hasExtConnection(side, to)) {
                        int index = BlockWire.cornerIdOf(side, to);
                        if (index < 0)
                            continue;

                        EnumFacing f0 = BlockWire.corners[index][0];
                        EnumFacing f1 = BlockWire.corners[index][1];

                        RawQuadCube cube = genCorner(to, false);
                        if (f0 == EnumFacing.UP)
                            cube.translateCoord(0, thickness, 0);
                        if (f0 == EnumFacing.DOWN)
                            cube.translateCoord(0, -thickness, 0);
                        if (f0 == EnumFacing.NORTH)
                            cube.translateCoord(0, 0, -thickness);
                        if (f0 == EnumFacing.SOUTH)
                            cube.translateCoord(0, 0, +thickness);
                        translateGroupCoord(f1, cube);

                        cube.bake(quads);
                    }

                }
            }
        }

        return quads;
    }

    private void translateGroupCoord(EnumFacing wire_side, IRawModel group) {
        switch (wire_side) {
            case DOWN:
                group.translateCoord(0, thickness / 2 - 0.5F , 0);
                break;
            case UP:
                group.translateCoord(0, 0.5F-thickness / 2 , 0);
                break;
            case NORTH:
                group.translateCoord(0, 0 , thickness / 2 - 0.5F);
                break;
            case SOUTH:
                group.translateCoord(0, 0 , 0.5F - thickness / 2);
                break;
            case WEST:
                group.translateCoord(thickness / 2 - 0.5F, 0 , 0);
                break;
            case EAST:
                group.translateCoord(0.5F - thickness / 2, 0 , 0);
                break;
        }
    }

    private RawQuadCube genCorner(EnumFacing branch, boolean displayConductor) {
        RawQuadCube cube = null;

        switch (branch) {
            case DOWN:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{displayConductor ? conductorTexture : insulatorTexture, null,
                                insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, -0.5F, 0);
                break;

            case UP:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{null, displayConductor ? conductorTexture : insulatorTexture,
                                insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, 0.5F - thickness, 0);
                break;

            case NORTH:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                displayConductor ? conductorTexture : insulatorTexture, null, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, -thickness / 2, -0.5F + thickness / 2);
                break;

            case SOUTH:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                null, displayConductor ? conductorTexture : insulatorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, -thickness / 2, 0.5F - thickness / 2);
                break;

            case WEST:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                insulatorTexture, insulatorTexture, displayConductor ? conductorTexture : insulatorTexture, null});
                cube.translateCoord(-0.5F + thickness / 2, -thickness / 2, 0);
                break;

            case EAST:
                cube = new RawQuadCube(thickness, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                insulatorTexture, insulatorTexture, null, displayConductor ? conductorTexture : insulatorTexture});
                cube.translateCoord(0.5F - thickness / 2, -thickness / 2, 0);
                break;
        }

        cube.translateCoord(0.5F, 0.5F, 0.5F);

        return cube;
    }

    private RawQuadCube genBranch(EnumFacing branch, boolean noCorner) {
        RawQuadCube cube = null;
        float yMax = noCorner ? 0.5F - thickness * 3 / 2 : 0.5F - thickness / 2;

        switch (branch) {
            case DOWN:
                cube = new RawQuadCube(thickness, yMax, thickness,
                        new TextureAtlasSprite[]{noCorner?null:conductorTexture, null,
                                insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, noCorner ? -0.5F + thickness : -0.5F, 0);
                break;

            case UP:
                cube = new RawQuadCube(thickness, yMax, thickness,
                        new TextureAtlasSprite[]{null, noCorner?null:conductorTexture,
                                insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, thickness / 2, 0);
                break;

            case NORTH:
                cube = new RawQuadCube(thickness, thickness, yMax,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                noCorner?null:conductorTexture, null, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, -thickness / 2, -0.25F - thickness / 4 + (noCorner ? thickness/2 : 0));
                break;

            case SOUTH:
                cube = new RawQuadCube(thickness, thickness, yMax,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                null, noCorner?null:conductorTexture, insulatorTexture, insulatorTexture});
                cube.translateCoord(0, -thickness / 2, 0.25F + thickness / 4 - (noCorner ? thickness/2 : 0));
                break;

            case WEST:
                cube = new RawQuadCube(yMax, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                insulatorTexture, insulatorTexture, noCorner?null:conductorTexture, null});
                cube.translateCoord(-0.25F - thickness / 4 + (noCorner ? thickness/2 : 0), -thickness / 2, 0);
                break;

            case EAST:
                cube = new RawQuadCube(yMax, thickness, thickness,
                        new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
                                insulatorTexture, insulatorTexture, null, noCorner?null:conductorTexture});
                cube.translateCoord(0.25F + thickness / 4 - (noCorner ? thickness/2 : 0), -thickness / 2, 0);
                break;
        }


        cube.translateCoord(0.5F, 0.5F, 0.5F);

        return cube;
    }
}
