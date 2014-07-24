package simElectricity.API.EnergyTile;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * This interface can represent a energy source or a energy sink
 * <p/>
 * e.g. a generator or a machine should implement this interface!
 * <p/>
 * See {@link simElectricity.API.EnergyTile.ICircuitComponent} for information about
 * {@link simElectricity.API.EnergyTile.ICircuitComponent#getOutputVoltage() getOutputVoltage()};
 */
public interface IEnergyTile extends ICircuitComponent {
    /**
     * Return a side that is designed to interact with energyNet
     */
    ForgeDirection getFunctionalSide();

    /**
     * Called when the functional side is going to be set
     */
    void setFunctionalSide(ForgeDirection newFunctionalSide);

    /**
     * Usually called by the wrench, to determine set or not
     */
    boolean canSetFunctionalSide(ForgeDirection newFunctionalSide);
}