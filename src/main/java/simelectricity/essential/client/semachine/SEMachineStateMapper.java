package simelectricity.essential.client.semachine;

import simelectricity.essential.common.semachine.SEMachineBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SEMachineStateMapper extends StateMapperBase{
	public final static String VPATH = "virtual/blockstates/semachine";
	public final String domain;
	
	public SEMachineStateMapper(String domain){
		this.domain = domain;
	}
	
	@Override
	protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
		Block block = state.getBlock();
		
		if (block instanceof ISESidedTextureBlock){
			ISESidedTextureBlock stBlock = (ISESidedTextureBlock) block;
			
			String modelName = stBlock.getModelNameFrom(state);
			boolean has2State = stBlock.hasSecondState(state);
			
			String varStr = (has2State ? "2" : "1") + modelName;
			
			ModelResourceLocation res = new ModelResourceLocation(
					this.domain + ":" + VPATH,
					varStr
					);
			return res;
		}
		
		
		return null;
	}

	public static boolean accepts(String resPath){
		return resPath.startsWith(VPATH);
	}
	
	public static IModel loadModel(String domain, String resPath, String variantStr) throws Exception {
		boolean has2State = variantStr.startsWith("2");
		variantStr = variantStr.substring(1);
		IModel model = new SEMachineRawModel(domain, variantStr, has2State);
		return model;
	}
	
	public void register(SEMachineBlock block){
		ModelLoader.setCustomStateMapper(block, this);
		
		ItemBlock itemBlock = block.getItemBlock();
		for (int meta: block.propertyMeta.getAllowedValues()){
			IBlockState blockState = block.getStateFromMeta(meta);
			ModelResourceLocation res = this.getModelResourceLocation(blockState);
			ModelLoader.setCustomModelResourceLocation(itemBlock, meta, res);
		}
	}
}
