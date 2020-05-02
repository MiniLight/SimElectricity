package simelectricity.essential;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rikka.librikka.gui.AutoGuiHandler;
import rikka.librikka.model.CodeBasedModel;
import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.cable.BlockCable;
import simelectricity.essential.cable.BlockWire;
import simelectricity.essential.client.cable.CableModel;
import simelectricity.essential.client.cable.WireModel;
import simelectricity.essential.client.coverpanel.LedPanelRender;
import simelectricity.essential.client.coverpanel.SupportRender;
import simelectricity.essential.client.coverpanel.VoltageSensorRender;
import simelectricity.essential.client.semachine.SEMachineModel;
import simelectricity.essential.client.semachine.SocketRender;
import simelectricity.essential.common.semachine.SEMachineBlock;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.Side;
//import rikka.librikka.model.loader.AdvancedModelLoader;
//import simelectricity.essential.client.cable.CableStateMapper;
//import simelectricity.essential.client.coverpanel.SupportRender;
import simelectricity.essential.client.grid.pole.CableJointModel;
//import simelectricity.essential.client.grid.FastTESRPowerPole;
//import simelectricity.essential.client.grid.GridStateMapper;
//import simelectricity.essential.client.grid.pole.FastTESRPowerPole2;
//import simelectricity.essential.client.grid.pole.FastTESRPowerPole3;
//import simelectricity.essential.client.grid.pole.FastTESRPowerPoleBottom;
//import simelectricity.essential.client.grid.pole.FastTESRPowerPoleTop;
//import simelectricity.essential.client.grid.transformer.FastTESRPowerTransformer;
//import simelectricity.essential.client.semachine.SEMachineStateMapper;
//import simelectricity.essential.client.semachine.SocketRender;
//import simelectricity.essential.grid.BlockPowerPoleBottom;
//import simelectricity.essential.grid.TilePoleBranch;
//import simelectricity.essential.grid.TilePowerPole;
//import simelectricity.essential.grid.TilePowerPole2;
//import simelectricity.essential.grid.TilePowerPole3;
//import simelectricity.essential.grid.TilePowerPole3.Pole10Kv;
//import simelectricity.essential.grid.TilePowerPole3.Pole415vType0;
//import simelectricity.essential.grid.transformer.TileDistributionTransformer;
//import simelectricity.essential.grid.transformer.TilePowerTransformerPlaceHolder;
//import simelectricity.essential.grid.transformer.TilePowerTransformerWinding.Primary;
//import simelectricity.essential.grid.transformer.TilePowerTransformerWinding.Secondary;
import simelectricity.essential.grid.BlockCableJoint;

@Mod.EventBusSubscriber(modid = Essential.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistrationHandler {
    public static Map<BlockState, CodeBasedModel> dynamicModels = new HashMap<>();
	
	public static void registerTileEntityRenders() {
		ClientRegistry.bindTileEntityRenderer(BlockRegistry.ttb_tetype, TESR::new);
//        FastTESRPowerPole.register(Primary.class);
//        FastTESRPowerPole.register(Secondary.class);
//        FastTESRPowerPole.register(Pole10Kv.Type0.class);
//        FastTESRPowerPole.register(Pole415vType0.class);
//        
//        FastTESRPowerPole.register(TileDistributionTransformer.Pole10kV.class);
//        FastTESRPowerPole.register(TileDistributionTransformer.Pole415V.class);
//        
//        ClientRegistry.bindTileEntitySpecialRenderer(BlockPowerPoleBottom.Tile.class, FastTESRPowerPoleBottom.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePowerPole.class, FastTESRPowerPoleTop.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePowerPole2.class, FastTESRPowerPole2.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePowerPole3.Pole10Kv.Type1.class, FastTESRPowerPole3.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePoleBranch.Type10kV.class, FastTESRPowerPole3.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePoleBranch.Type415V.class, FastTESRPowerPole3.instance);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePowerTransformerPlaceHolder.Render.class, FastTESRPowerTransformer.instance);
	}
	
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {   	
    	for (CodeBasedModel dynamicModel: dynamicModels.values())
    		dynamicModel.onPreTextureStitchEvent(event);
    	
    	SocketRender.INSTANCE.onPreTextureStitchEvent(event);
    	SupportRender.INSTANCE.onPreTextureStitchEvent(event);
    	VoltageSensorRender.instance.onPreTextureStitchEvent(event);
    	LedPanelRender.instance.onPreTextureStitchEvent(event);
//    	FastTESRPowerPole.stitchTexture(map);
    }
    
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Post event) {
//    	event.getMap().getSprite(location)
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {  	
    	Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();
    	
		for (SEMachineBlock sem: BlockRegistry.blockElectronics)
			SEMachineModel.replace(registry, sem);
		for (SEMachineBlock sem: BlockRegistry.blockTwoPortElectronics)
			SEMachineModel.replace(registry, sem);
    	
    	dynamicModels.forEach((blockstate, dynamicModel) -> {
    		dynamicModel.onModelBakeEvent();
    		registry.put(BlockModelShapes.getModelLocation(blockstate), dynamicModel);
    	});
    	
    	SocketRender.INSTANCE.onModelBakeEvent();
    	SupportRender.INSTANCE.onModelBakeEvent();
    	VoltageSensorRender.instance.onModelBakeEvent();
    	LedPanelRender.instance.onModelBakeEvent();
    	

//      event.getModelRegistry().put(new ModelResourceLocation(), null);
//  	Minecraft.getInstance().getModelManager().getModel(location)
//  	net.minecraft.client.renderer.model.BlockModel
    }
    
    
    /*
     * Event order:
     * FMLClientSetupEvent
     * TextureStitchEvent.Pre
     * TextureStitchEvent.Post
     * ModelBakeEvent
     */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event){
        //Initialize client-side API
        SEEAPI.coloredBlocks = new LinkedList<Block>();
        Minecraft.getInstance().getBlockColors().register((blockstate, lightreader, pos, tintIndex) -> {
        	return Minecraft.getInstance().getBlockColors().getColor(lightreader.getBlockState(pos.down()), lightreader, pos, tintIndex);
        }, BlockRegistry.ttb);
        RenderTypeLookup.setRenderLayer(BlockRegistry.ttb, (layer)->true);
        
//    	SEEAPI.coloredBlocks.add(BlockRegistry.blockCable);
    	ClientRegistrationHandler.registerTileEntityRenders();
		
		// Was Block::getBlockLayer
		for (SEMachineBlock sem: BlockRegistry.blockElectronics) {
			RenderTypeLookup.setRenderLayer(sem, RenderType.getCutoutMipped());
		}
		for (SEMachineBlock sem: BlockRegistry.blockTwoPortElectronics) {
			RenderTypeLookup.setRenderLayer(sem, RenderType.getCutoutMipped());
		}
		
		for (BlockCable cable: BlockRegistry.blockCable) {
			RenderTypeLookup.setRenderLayer(cable, (layer)-> {
				return layer==RenderType.getSolid() || layer==RenderType.getCutoutMipped();
			}
			);
			
			cable.getStateContainer().getValidStates().forEach((blockstate) ->
				dynamicModels.put(blockstate, new CableModel(cable))
			);
		}
		for (BlockWire wire: BlockRegistry.blockWire) {
			RenderTypeLookup.setRenderLayer(wire, (layer)-> {
				return layer==RenderType.getSolid();
			}
			);
			
			wire.getStateContainer().getValidStates().forEach((blockstate) ->
				dynamicModels.put(blockstate, new WireModel(wire))
			);
		}

		BlockRegistry.cableJoint[BlockCableJoint.Type._10kv.ordinal()]
				.getStateContainer().getValidStates().forEach((blockstate) -> {
			dynamicModels.put(blockstate, new CableJointModel.Type10kV(blockstate));
		});
		BlockRegistry.cableJoint[BlockCableJoint.Type._415v.ordinal()]
				.getStateContainer().getValidStates().forEach((blockstate) -> {
			dynamicModels.put(blockstate, new CableJointModel.Type415V(blockstate));
		});
		
		// Register Gui
//		ScreenManager.registerFactory(BlockRegistry.cAdjustableResistor, GuiAdjustableResistor::new);
		for (Class<? extends Container> containerCls: BlockRegistry.registeredGuiContainers) {
			AutoGuiHandler.registerContainerGui(containerCls);
		}
		
//		new ResourceLocation("minecraft","elements")
//    	ModelLoaderRegistry.registerLoader(new ResourceLocation("librikka","virtual"), loader);
//		ModelLoaderRegistry.getModel("", deserializationContext, data)

		// Get unbaked model
//		IUnbakedModel adj = ModelLoader.instance().getUnbakedModel(new ResourceLocation(Essential.MODID, "block/electronics_adjustable_resistor"));
//		IUnbakedModel machine = ModelLoader.instance().getUnbakedModel(new ResourceLocation(Essential.MODID, "block/machine"));
//		adj = null;
		// ModelBakery public IUnbakedModel getUnbakedModel(ResourceLocation modelLocation)
	}	
}
