package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;
import simelectricity.api.components.ISESwitch;
import simelectricity.api.node.ISESubComponent;

public class SwitchB extends SEComponent implements ISESubComponent, ISESwitch {
    public SwitchA A;

    public SwitchB(SwitchA A, TileEntity te) {
        this.A = A;
        this.te = te;
    }

    @Override
    public ISESubComponent getComplement() {
        return this.A;
    }

    @Override
    public boolean isOn() {
        return this.A.isOn;
    }

    @Override
    public double getResistance() {
        return this.A.resistance;
    }
}
