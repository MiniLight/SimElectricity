package simElectricity;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

public class ConfigManager {
    public static Configuration config;

    public static boolean optimizeNodes;


    public static void init(FMLPreInitializationEvent event) {

        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
            syncConfig();
        }

    }

    private static void syncConfig() {
        optimizeNodes = config.get(Configuration.CATEGORY_GENERAL, "Optimize_Nodes", true, "Enable Optimized Nodes.").getBoolean();

        if (config.hasChanged())
            config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(mod_SimElectricity.MODID))
            syncConfig();
    }
}
