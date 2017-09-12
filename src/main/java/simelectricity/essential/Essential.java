package simelectricity.essential;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import rikka.librikka.AutoGuiHandler;
import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.cable.CableWatchEventHandler;
import simelectricity.essential.coverpanel.CoverPanelRegistry;
import simelectricity.essential.coverpanel.SECoverPanelFactory;
import simelectricity.essential.utils.network.MessageContainerSync;


@Mod(modid = Essential.modID, name = "SimElectricity Essential", dependencies = "required-after:simelectricity")
public class Essential {
    public static final String modID = "sime_essential";

    @SidedProxy(clientSide = "simelectricity.essential.ClientProxy", serverSide = "simelectricity.essential.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Essential.modID)
    public static Essential instance;

    public SimpleNetworkWrapper networkChannel;

    /**
     * PreInitialize
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemRegistry.registerItems();
        BlockRegistry.registerBlocks();

        SEEAPI.coverPanelRegistry = new CoverPanelRegistry();

        networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel(modID);
        networkChannel.registerMessage(MessageContainerSync.HandlerClient.class, MessageContainerSync.class, 0, Side.CLIENT);
        networkChannel.registerMessage(MessageContainerSync.HandlerServer.class, MessageContainerSync.class, 1, Side.SERVER);

        proxy.preInit();
    }

    /**
     * Initialize
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BlockRegistry.registerTileEntities();

        proxy.init();

        MinecraftForge.EVENT_BUS.register(new CableWatchEventHandler());

        //Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new AutoGuiHandler());
    }

    /**
     * PostInitialize
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        SEEAPI.coverPanelRegistry.registerCoverPanelFactory(new SECoverPanelFactory());
    }
}
