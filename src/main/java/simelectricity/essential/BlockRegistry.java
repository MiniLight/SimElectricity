package simelectricity.essential;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.registry.GameRegistry;
import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.cable.TileCable;
import simelectricity.essential.grid.BlockCableJoint;
import simelectricity.essential.grid.BlockTransmissionTowerCollisionBox;
import simelectricity.essential.grid.BlockTransmissionTowerTop;
import simelectricity.essential.grid.BlockTransmissionTowerBottom;
import simelectricity.essential.grid.TileCableJoint;
import simelectricity.essential.grid.TileTransmissionTower;
import simelectricity.essential.machines.BlockElectronics;
import simelectricity.essential.machines.BlockTwoPortElectronics;
import simelectricity.essential.machines.gui.ContainerAdjustableResistor;
import simelectricity.essential.machines.gui.ContainerAdjustableTransformer;
import simelectricity.essential.machines.gui.ContainerQuantumGenerator;
import simelectricity.essential.machines.gui.ContainerVoltageMeter;
import simelectricity.essential.machines.tile.TileAdjustableResistor;
import simelectricity.essential.machines.tile.TileAdjustableTransformer;
import simelectricity.essential.machines.tile.TileIncandescentLamp;
import simelectricity.essential.machines.tile.TileQuantumGenerator;
import simelectricity.essential.machines.tile.TileVoltageMeter;

//@GameRegistry.ObjectHolder(SETemplate.MODID)
public class BlockRegistry {
	public static BlockCable blockCable;
	
	public static BlockTransmissionTowerTop transmissionTowerTop;
	public static BlockTransmissionTowerBottom transmissionTowerBottom;
	public static BlockTransmissionTowerCollisionBox transmissionTowerCollisionBox;
	public static BlockCableJoint cableJoint;
	
	public static BlockElectronics blockElectronics;
	public static BlockTwoPortElectronics blockTwoPortElectronics;
	
	public static void registerBlocks(){
		blockCable = new BlockCable();
		
		transmissionTowerTop = new BlockTransmissionTowerTop();
		transmissionTowerBottom = new BlockTransmissionTowerBottom();
		transmissionTowerCollisionBox = new BlockTransmissionTowerCollisionBox();
		cableJoint = new BlockCableJoint();
		
		blockElectronics = new BlockElectronics();
		blockTwoPortElectronics = new BlockTwoPortElectronics();
	}
	
	public static void registerTileEntities(){
		GameRegistry.registerTileEntity(TileCable.class, "SEECable");
		GameRegistry.registerTileEntity(TileTransmissionTower.class, "SEETransmissionTower");
		GameRegistry.registerTileEntity(TileCableJoint.class, "SEECableJoint");
		
		GameRegistry.registerTileEntity(TileVoltageMeter.class, "SEEVoltageMeter");
		GameRegistry.registerTileEntity(TileQuantumGenerator.class, "SEEQuantumGenerator");
		GameRegistry.registerTileEntity(TileAdjustableResistor.class, "SEEAdjustableResistor");
		GameRegistry.registerTileEntity(TileIncandescentLamp.class, "SEEIncandescentLamp");
		
		GameRegistry.registerTileEntity(TileAdjustableTransformer.class, "SEEAdjustableTransformer");
	}
	
	public static Container getContainer(TileEntity te, EntityPlayer player){
		if (te instanceof TileVoltageMeter)
			return new ContainerVoltageMeter(te);
		if (te instanceof TileQuantumGenerator)
			return new ContainerQuantumGenerator(te);
		if (te instanceof TileAdjustableResistor)
			return new ContainerAdjustableResistor(te);
		
		if (te instanceof TileAdjustableTransformer)
			return new ContainerAdjustableTransformer(te);
		
		return null;
	}
}
