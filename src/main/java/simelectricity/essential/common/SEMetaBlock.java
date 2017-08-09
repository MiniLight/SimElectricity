package simelectricity.essential.common;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public abstract class SEMetaBlock extends SEBlock{
	public SEMetaBlock(String unlocalizedName, Material material, Class<? extends SEItemBlock> itemBlockClass) {
		super(unlocalizedName, material, itemBlockClass);
		
		propertyMeta = (IProperty<Integer>) this.getBlockState().getProperty("meta");
		this.setDefaultState(getDefaultState(this.blockState.getBaseState()));
	}

	/**
	 * @return when implementing your own cable, please make sure to return correct number!
	 */
	protected abstract int getNumOfSubTypes();
	
	///////////////////////////////
	///BlockStates
	///////////////////////////////
	@Override
	protected final BlockStateContainer createBlockState(){
		ArrayList<IUnlistedProperty> unlisted = new ArrayList();
		
		createUnlistedProperties(unlisted);
		
		IProperty[] propertyArray = new IProperty[] {
				PropertyInteger.create("meta", 0 , getNumOfSubTypes()-1)
				};
		IUnlistedProperty[] unlistedArray = unlisted.toArray(new IUnlistedProperty[unlisted.size()]);
		
		if (unlisted.isEmpty()){
			return new BlockStateContainer(this, propertyArray);
		}else{
			return new ExtendedBlockState(this, propertyArray, unlistedArray);
		}
	}

	/**
	 * Override this to add more unlisted properties
	 * @param properties
	 */
	protected void createUnlistedProperties(ArrayList<IUnlistedProperty> properties){}
	
	public final IProperty<Integer> propertyMeta;
	
	/**
	 * Before the initialization is done, propertyMeta is null,
	 * @return @NonNullable propertyMeta
	 */
	public final IProperty<Integer> getPropertyMeta(){
		if (propertyMeta == null)
			return (IProperty<Integer>) this.blockState.getProperty("meta");
		return propertyMeta;
	}
	
	private IBlockState getDefaultState(IBlockState baseState){
		return baseState.withProperty(propertyMeta, 0);
	}
	
	/**
	 * This gets called before during the initialization, propertyMeta is not ready yet
	 */
	@Override
    public final IBlockState getStateFromMeta(int meta){		
        return super.getDefaultState().withProperty(getPropertyMeta(), meta & 15);
    }
	
	@Override
    public final int getMetaFromState(IBlockState state){
		int meta = state.getValue(getPropertyMeta());
		meta = meta & 15;
		return meta;
    }
	
	@Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos){		
		int meta = this.getMetaFromState(state);
		return state.withProperty(propertyMeta, meta);
    }
	
	@Override
    public int damageDropped(IBlockState state){
        return state.getValue(propertyMeta);
    }
}