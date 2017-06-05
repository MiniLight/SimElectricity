package simelectricity.api.tile;

import simelectricity.api.ISEPlaceable;
import simelectricity.api.node.ISESubComponent;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * ISETile is a container, it is able to support up to 6 ISESubComponent. TileEntities implement this interface
 * <p/>
 * Can be used to make transformers, regulators, diodes, switches, generators and loads
 */
public interface ISETile extends ISEPlaceable{
	/**
	 * @return An array of directions that can be used to connect to {link}ISESubComponent
	 */
	public ForgeDirection[] getValidDirections();
	
	public ISESubComponent getComponent(ForgeDirection side);
}
