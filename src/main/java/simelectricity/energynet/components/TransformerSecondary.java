package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;
import simelectricity.api.components.ISETransformer;
import simelectricity.api.node.ISESubComponent;

public class TransformerSecondary extends SEComponent implements ISESubComponent<TransformerPrimary>, ISETransformer {
    private volatile TransformerPrimary primary;

    public TransformerSecondary(TransformerPrimary primary, TileEntity te) {
        this.primary = primary;
        this.te = te;
    }

    @Override
    public TransformerPrimary getComplement() {
        return this.primary;
    }

    @Override
    public double getRatio() {
        return this.primary.ratio;
    }

    @Override
    public double getInternalResistance() {
        return this.primary.rsec;
    }
}
