/*
 * Copyright (C) 2014 SimElectricity
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package simElectricity.Common;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import simElectricity.API.SEAPI;
import simElectricity.Common.SEUtils;

public class ConfigManager {

    public static Configuration config;

    /**
     * Enable Optimized Nodes
     */
    public static boolean showEnergyNetInfo;   
    public static String matrixSolver;
    public static int precision;
    public static int maxIteration;
    public static int shuntPN;

    @SideOnly(Side.CLIENT)
    public static int parabolaRenderSteps;
    
    public static void init(FMLPreInitializationEvent event) {
        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
            syncConfig(event.getSide().isClient());
        }
    }

    private static void syncConfig(boolean isClient) {
        showEnergyNetInfo = config.get(Configuration.CATEGORY_GENERAL, "ShowEnergyNetInfo", false, "Display energy net information, such as joining/leaving/changing").getBoolean();
        // matrixSolver = config.get(Configuration.CATEGORY_GENERAL, "MatrixSolver", "QR", "Which algorithms is used for solving matrix(QR is much more effective than Gaussian, options: QR, Gaussian)").getString();
        matrixSolver = config.getString("MatrixSolver", Configuration.CATEGORY_GENERAL, "QR", "Which algorithms is used for solving matrix(QR is much more effective than Gaussian.).Options: QR, Gaussian", new String[] { "QR", "Gaussian" });
        precision = config.get(Configuration.CATEGORY_GENERAL, "Precision", 3, "The maximum allowed error from simulation, 3 means 3 decimal places").getInt();
        maxIteration = config.get(Configuration.CATEGORY_GENERAL, "Max iteration", 50, "The Maximum number of iteration per tick").getInt();
        shuntPN = config.get(Configuration.CATEGORY_GENERAL, "Shunt resistance per PN", 1000000000, "The resistance connected between every PN junction").getInt();
        SEAPI.ratioSE2IC = config.get(Configuration.CATEGORY_GENERAL, "Convertion ratio between SE and IC", 10, "SE Power / this ratio = IC Power").getInt();
        
        
        //Client-only configurations
        if (isClient){
            parabolaRenderSteps = config.get(Configuration.CATEGORY_GENERAL, "ParabolaRenderSteps", 12, "Decides how smooth the parabola cable is(must be a even number!Client ONLY!)").getInt();
        }
        
        if (config.hasChanged())
            config.save();
        
    }

    //This function is supposed to be called by a client with GUI only!
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(SEUtils.MODID))
            syncConfig(true);
    }
}
