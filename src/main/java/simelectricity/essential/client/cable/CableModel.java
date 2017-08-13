package simelectricity.essential.client.cable;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import simelectricity.essential.api.ISEGenericCable;
import simelectricity.essential.api.client.ISECoverPanelRender;
import simelectricity.essential.api.coverpanel.ISECoverPanel;
import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.utils.client.SERawQuadCube;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class CableModel implements IPerspectiveAwareModel {
	private final TextureAtlasSprite insulatorTexture, conductorTexture;
	private final float thickness;
	
	private final List<BakedQuad>[] branches = new LinkedList[6];
	
	public CableModel(TextureAtlasSprite insulatorTexture,
			TextureAtlasSprite conductorTexture, float thickness) {
		this.insulatorTexture = insulatorTexture;
		this.conductorTexture = conductorTexture;
		this.thickness = thickness;
		
		//Bake branches
		final List<BakedQuad> branchDown = new LinkedList<BakedQuad>();
		final List<BakedQuad> branchUp = new LinkedList<BakedQuad>();
		final List<BakedQuad> branchNorth = new LinkedList<BakedQuad>();
		final List<BakedQuad> branchSouth = new LinkedList<BakedQuad>();
		final List<BakedQuad> branchWest = new LinkedList<BakedQuad>();
		final List<BakedQuad> branchEast = new LinkedList<BakedQuad>();
		
		SERawQuadCube cube = new SERawQuadCube(thickness, 0.5F-thickness/2, thickness, 
				new TextureAtlasSprite[]{conductorTexture, null,
				insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
		cube.translateCoord(0, -0.5F, 0);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchDown);
		
		cube = new SERawQuadCube(thickness, 0.5F-thickness/2, thickness, 
				new TextureAtlasSprite[]{null, conductorTexture,
				insulatorTexture, insulatorTexture, insulatorTexture, insulatorTexture});
		cube.translateCoord(0, thickness/2, 0);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchUp);
		
		cube = new SERawQuadCube(thickness, thickness, 0.5F-thickness/2, 
				new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
				conductorTexture, null, insulatorTexture, insulatorTexture});
		cube.translateCoord(0, -thickness/2, -0.25F - thickness/4);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchNorth);
		
		cube = new SERawQuadCube(thickness, thickness, 0.5F-thickness/2, 
				new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
				null, conductorTexture, insulatorTexture, insulatorTexture});
		cube.translateCoord(0, -thickness/2, 0.25F + thickness/4);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchSouth);
		
		cube = new SERawQuadCube(0.5F-thickness/2, thickness, thickness, 
				new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
				insulatorTexture, insulatorTexture, conductorTexture, null});
		cube.translateCoord(-0.25F - thickness/4, -thickness/2, 0);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchWest);
		
		cube = new SERawQuadCube(0.5F-thickness/2, thickness, thickness, 
				new TextureAtlasSprite[]{insulatorTexture, insulatorTexture,
				insulatorTexture, insulatorTexture, null, conductorTexture});
		cube.translateCoord(0.25F + thickness/4, -thickness/2, 0);
		cube.translateCoord(0.5F, 0.5F, 0.5F);
		cube.bake(branchEast);
		
		branches[0] = branchDown;
		branches[1] = branchUp;
		branches[2] = branchNorth;
		branches[3] = branchSouth;
		branches[4] = branchWest;
		branches[5] = branchEast;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}
	@Override
	public boolean isGui3d() {
		return false;
	}
	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return conductorTexture;
	}
	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}
	
	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;	//I'm not sure what this thing does QAQ, only know this prevents crashing 233
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		// If the parent model isn't an IPerspectiveAware, we'll need to generate the correct matrix ourselves using the
	    //  ItemCameraTransforms.

	    ItemCameraTransforms itemCameraTransforms = this.getItemCameraTransforms();
	    ItemTransformVec3f itemTransformVec3f = itemCameraTransforms.getTransform(cameraTransformType);
	    TRSRTransformation tr = new TRSRTransformation(itemTransformVec3f);
	    Matrix4f mat = null;
	    if (tr != null) { // && tr != TRSRTransformation.identity()) {
	    	mat = tr.getMatrix();
	    }
	    
	    // The TRSRTransformation for vanilla items have blockCenterToCorner() applied, however handlePerspective
	    //  reverses it back again with blockCornerToCenter().  So we don't need to apply it here.

	    return Pair.of(this, mat);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState blockState,
			@Nullable EnumFacing uselessside, long rand) {

		List<BakedQuad> quads = new LinkedList<BakedQuad>();
		
	    if (!(blockState instanceof IExtendedBlockState))
	    	//Normally this should not happen, just in case, to prevent crashing
	    	return ImmutableList.of();
	    
	    IExtendedBlockState exBlockState = (IExtendedBlockState)blockState;
	    WeakReference<ISEGenericCable> ref = exBlockState.getValue(BlockCable.propertyTile);
        ISEGenericCable cable = ref==null ? null : ref.get();
        
        if (cable == null)
        	return ImmutableList.of();
		
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		
		//Render center & branches in SOLID layer
		if (layer == BlockRenderLayer.SOLID) {
		    byte numOfCon = 0;
		    EnumFacing conSide = EnumFacing.DOWN;
		    
		    TextureAtlasSprite[] centerTexture = new TextureAtlasSprite[]
		    		{insulatorTexture, insulatorTexture,
		    		insulatorTexture, insulatorTexture, 
		    		insulatorTexture, insulatorTexture};
			
			for (EnumFacing direction: EnumFacing.VALUES){
				if (cable.connectedOnSide(direction)){
					quads.addAll(branches[direction.ordinal()]);
					centerTexture[direction.ordinal()] = null;
					conSide = direction;
					numOfCon++;
				}
			}
			
			if (numOfCon == 1){
				centerTexture[conSide.getOpposite().ordinal()] = conductorTexture;
			}
			
			SERawQuadCube cube = new SERawQuadCube(thickness, thickness, thickness, centerTexture);
			cube.translateCoord(0.5F, 0.5F-thickness/2, 0.5F);
			cube.bake(quads);
		}

		//CoverPanel can be rendered in any layer
		for (EnumFacing side: EnumFacing.VALUES){
			ISECoverPanel coverPanel = cable.getCoverPanelOnSide(side);
			if (coverPanel != null) {
				ISECoverPanelRender render = coverPanel.getCoverPanelRender();
				if (render != null)
					render.renderCoverPanel(coverPanel, side, quads);
			}
				
		}
				
		return quads;
	}
}
