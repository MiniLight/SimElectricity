package simelectricity.essential.client.grid.accessory;

import simelectricity.essential.client.grid.PowerPoleRenderHelper;

public interface ISEAccessoryRenderer {
	void renderConnection(PowerPoleRenderHelper pole, PowerPoleRenderHelper accessory);
}
