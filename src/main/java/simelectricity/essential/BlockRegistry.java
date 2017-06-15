package simelectricity.essential;

import cpw.mods.fml.common.registry.GameRegistry;
import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.cable.TileCable;
import simelectricity.essential.grid.BlockTransmissionTowerCollisionBox;
import simelectricity.essential.grid.BlockTransmissionTowerTop;
import simelectricity.essential.grid.BlockTransmissionTowerBottom;
import simelectricity.essential.grid.TileTransmissionTower;

public class BlockRegistry {
	public static BlockCable blockCable;
	
	public static BlockTransmissionTowerTop transmissionTowerTop;
	public static BlockTransmissionTowerBottom transmissionTowerBottom;
	public static BlockTransmissionTowerCollisionBox transmissionTowerCollisionBox;
	
	public static void registerBlocks(){
		blockCable = new BlockCable();
		
		transmissionTowerTop = new BlockTransmissionTowerTop();
		transmissionTowerBottom = new BlockTransmissionTowerBottom();
		transmissionTowerCollisionBox = new BlockTransmissionTowerCollisionBox();
	}
	
	public static void registerTileEntities(){
		GameRegistry.registerTileEntity(TileCable.class, "TileCable");
		GameRegistry.registerTileEntity(TileTransmissionTower.class, "TileTransmissionTower");
	}	
}
