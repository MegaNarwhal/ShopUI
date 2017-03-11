package us.blockbox.shopui.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import us.blockbox.shopui.ISubCommand;
import us.blockbox.shopui.ShopConfig;
import us.blockbox.shopui.SubCommandHandler;

import java.util.Arrays;

import static us.blockbox.shopui.locale.ShopMessage.Message.PLAYER_PERMISSION_INSUFFICIENT;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

public class CommandShopUI implements CommandExecutor{

	private static final SubCommandHandler sub = SubCommandHandler.getInstance();

	@Override
	public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
		if(!sender.hasPermission("shopui.command.manage")){
			sender.sendMessage(getMessage(PLAYER_PERMISSION_INSUFFICIENT));
			return true;
		}
		if(args.length > 0){
			ISubCommand subCommand = sub.getSubCommand(args[0].toLowerCase());
			if(subCommand != null){
				subCommand.onCommand(sender,Arrays.copyOfRange(args,1,args.length));
			}else{
/*				String argsConcatenated = "";
				for(int i = 0;i < args.length; i++){
					argsConcatenated += (args[i] + (i == args.length - 1 ? "" : " "));
				}
				subCommand = sub.getSubCommand(argsConcatenated);*/
				showHelp(sender);
			}
		}else{
			showHelp(sender);
		}
		return true;
	}

	private static void showHelp(final CommandSender sender){
		sender.sendMessage(ShopConfig.getPrefix() + "Subcommands: " + sub.getSubCommands().toString());
	}
}
