package simelectricity.essential.client;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

/**
 * An alternative to ISBRH in 1.7.10 and previous
 * @author Rikka0_0
 */
public abstract class CodeBasedModel implements IModel, IBakedModel {
	/**
	 * @param texture file path, including domain
	 * @return a key which can be used to retrieve the corresponding TextureAtlasSprite (like IIcon)
	 */
	protected ResourceLocation registerTexture(String texture) {
		ResourceLocation resLoc = new ResourceLocation(texture);
		textures.add(resLoc);
		return resLoc;
	}
	
	protected abstract void onTextureRegistered(Function<ResourceLocation, TextureAtlasSprite> registry);
	////////////////////////////////////////////////////////////////////////
	private final Set<ResourceLocation> textures = Sets.newHashSet();
	////////////////
	/// IModel
	////////////////
	@Override
	public final Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of();
	}

	@Override
	public final Collection<ResourceLocation> getTextures() {
		return ImmutableSet.copyOf(textures);
	}

	@Override
	public final IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}
	
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		this.onTextureRegistered(bakedTextureGetter);
		return this;
	}

	/////////////////
	/// IBakedModel
	/////////////////
	@Override
	public boolean isAmbientOcclusion() {return false;}
	
	@Override
	public boolean isGui3d() {return false;}
	
	@Override
	public boolean isBuiltInRenderer() {return false;}
	
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {return ItemCameraTransforms.DEFAULT;}
	
	@Override
	public ItemOverrideList getOverrides() {return ItemOverrideList.NONE;}
}
