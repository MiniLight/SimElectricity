package simelectricity.essential.client.grid.accessory;

import rikka.librikka.math.Vec3f;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;

public class AR415VType0CableJoint implements ISEAccessoryRenderer {
	public final static ISEAccessoryRenderer instance = new AR415VType0CableJoint();
	private AR415VType0CableJoint() {}

	@Override
	public void renderConnection(PowerPoleRenderHelper current, PowerPoleRenderHelper neighbor) {
		Vec3f to0 = neighbor.groups[0].insulators[0].realPos;
		Vec3f to1 = neighbor.groups[0].insulators[1].realPos;
		Vec3f to2 = neighbor.groups[0].insulators[2].realPos;
		Vec3f to3 = neighbor.groups[0].insulators[3].realPos;
		
		Vec3f from0 = current.groups[0].insulators[0].realPos;
		Vec3f from1 = current.groups[0].insulators[1].realPos;
		Vec3f from2 = current.groups[0].insulators[2].realPos;
		Vec3f from3 = current.groups[0].insulators[3].realPos;
		
		float tension1 = -0.3F;
		float tension2 = -0.2F;
		
		if (PowerPoleRenderHelper.hasIntersection(from0, to0, from3, to3)) {
			current.addExtraWire(from0, to3, tension1);
			current.addExtraWire(from3, to0, tension1);
			current.addExtraWire(from1, to2, tension2);
			current.addExtraWire(from2, to1, tension2);
		} else {
			current.addExtraWire(from0, to0, tension1);
			current.addExtraWire(from1, to1, tension2);
			current.addExtraWire(from2, to2, tension2);
			current.addExtraWire(from3, to3, tension1);
		}
	}
}
