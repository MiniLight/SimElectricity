package simElectricity.Common.Blocks.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import simElectricity.API.Energy;
import simElectricity.API.IEnergyNetUpdateHandler;
import simElectricity.API.Common.TileEntitySE;
import simElectricity.API.EnergyTile.ITransformer;

public class TileRegulator extends TileEntitySE implements ITransformer, IEnergyNetUpdateHandler{
    public ForgeDirection inputSide = ForgeDirection.NORTH, outputSide = ForgeDirection.SOUTH;
    
    public Primary primary = new ITransformer.Primary(this);
    public Secondary secondary = new ITransformer.Secondary(this);
    
    public float ratio = 10, outputResistance = 0.1F;
    public float outputVoltage = 230;
    
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        ratio = tagCompound.getFloat("ratio");
        outputResistance = tagCompound.getFloat("outputResistance");
        inputSide = ForgeDirection.getOrientation(tagCompound.getByte("inputSide"));
        outputSide = ForgeDirection.getOrientation(tagCompound.getByte("outputSide"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setFloat("ratio", ratio);
        tagCompound.setFloat("outputResistance", outputResistance);
        tagCompound.setByte("inputSide", (byte) inputSide.ordinal());
        tagCompound.setByte("outputSide", (byte) outputSide.ordinal());
    }
    
	@Override
	public boolean attachToEnergyNet() {
		return true;
	}

    @Override
    public float getResistance() {
        return outputResistance;
    }

    @Override
    public float getRatio() {
        return ratio;
    }

    @Override
    public ForgeDirection getPrimarySide() {
        return inputSide;
    }

    @Override
    public ForgeDirection getSecondarySide() {
        return outputSide;
    }

    @Override
    public ITransformerWinding getPrimary() {
        return primary;
    }

    @Override
    public ITransformerWinding getSecondary() {
        return secondary;
    }

    
    float aError = 0;
	@Override
	public void onEnergyNetUpdate() {
		double p = 0.01, i = 0.0000001;
		double vo = Energy.getVoltage(secondary);
		double error = outputVoltage - vo;
		if (Math.abs(error) > 1 && 1< Energy.getVoltage(primary)){
			ratio -= -p * error;
			ratio += i * aError;
			aError += error;
			Energy.postTileChangeEvent(this);
			System.out.println(vo);
		} else {
			aError = 0;
		}
		
		if (ratio >25.5){
			ratio =25.5F;
		}
		if (ratio <0){
			ratio = 0.00001F;
		}
	}
	
}
