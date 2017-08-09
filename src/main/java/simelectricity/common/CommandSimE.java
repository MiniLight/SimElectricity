package simelectricity.common;

import simelectricity.SimElectricity;
import simelectricity.energynet.EnergyNet;
import simelectricity.energynet.EnergyNetAgent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CommandSimE extends CommandBase{

	@Override
	public String getName() {
		return "sime";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sime (info | refresh) [dimensionID]";
	}

	private static void info(ICommandSender sender, int dim){
		World world = DimensionManager.getWorld(dim);
		EnergyNet energyNet = EnergyNetAgent.getEnergyNetForWorld(world);
		sender.sendMessage(new TextComponentString("-----------------------------------"));
		sender.sendMessage(new TextComponentString("EnergyNet for dimension " + dim + ":"));
		for(String s: energyNet.info())
			sender.sendMessage(new TextComponentString(s));
	}
	
	private static void refresh(ICommandSender sender, int dim){
		EnergyNet energyNet = EnergyNetAgent.getEnergyNetForWorld(
				DimensionManager.getWorld(dim));
		energyNet.reFresh();
		sender.sendMessage(new TextComponentString("EnergyNet for dimension " + dim + " has been refreshed!"));		
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0){
			sender.sendMessage(new TextComponentString("SimElectricity Version: " + SimElectricity.version));
			info(sender, sender.getEntityWorld().provider.getDimension());
		}else if (args[0].equalsIgnoreCase("info")){
			if (args.length == 1){
				info(sender, sender.getEntityWorld().provider.getDimension());
			}else{
				info(sender, Integer.valueOf(args[1]));
			}
		}else if (args[0].equalsIgnoreCase("refresh")){
			if (args.length == 1){
				refresh(sender, sender.getEntityWorld().provider.getDimension());
			}else{
				refresh(sender, Integer.valueOf(args[1]));
			}
		}
	}
}
