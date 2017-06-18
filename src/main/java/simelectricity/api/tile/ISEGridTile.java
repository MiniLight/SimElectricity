package simelectricity.api.tile;

import simelectricity.api.node.ISEGridNode;

/**
 * TileEntities can implement this interface
 */
public interface ISEGridTile{
    /**
     * This function will be called by the grid manager when a ISEGridObject is going to associate with the ISEGridTile
     * <p/>
     * Make sure you store the instance of ISEGridObject in this function
     */
	void setGridNode(ISEGridNode gridObj);
	
	/**
	 * @return the GridNode instance from setGridNode()
	 */
	ISEGridNode getGridNode();
	
    /**
     * This function will be called as soon as the neighbor list of its ISEGridObject has changed
     * <p/>
     * E.g. Connection established, map loading, Connection removed...
     */
	void onGridNeighborUpdated();
	
    /**
     * Called by HV cable items, to decide if this ISEGridObject can be connected
     */
	boolean canConnect();
}