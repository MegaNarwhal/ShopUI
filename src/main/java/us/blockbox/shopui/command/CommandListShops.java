package us.blockbox.shopui.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.blockbox.shopui.ISubCommand;
import us.blockbox.shopui.ShopCategory;
import us.blockbox.shopui.ShopConfig;

import java.util.Map;

//Created 11/28/2016 11:14 PM
public class CommandListShops implements ISubCommand{

	private final CommandSender console = Bukkit.getConsoleSender();
	private final ShopConfig config = ShopConfig.getInstance();

	@Override
	public boolean onCommand(CommandSender sender,String[] args){
/*		if(!sender.hasPermission("shopui.command.list")){
			sender.sendMessage(ShopMessage.PLAYER_PERMISSION_INSUFFICIENT.getMsg());
			return true;
		}*/
		if(config.getShopCategories().isEmpty()){
			sender.sendMessage("No shop categories configured.");
			return true;
		}
		sender.sendMessage(ShopConfig.getPrefix() + "Enabled categories:");
		if(sender instanceof Player){
			final Player p = (Player)sender;
			for(final Map.Entry<String,ShopCategory> cat : config.getShopCategories().entrySet()){
				final String id = cat.getValue().getShopId();
				final String name = cat.getKey();
				Bukkit.getServer().dispatchCommand(console,"tellraw " + p.getName() + " {\"text\":\"- " + name + " (" + id + ")\",\"color\":\"white\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/shopui add " + id + " \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to add item to " + name + "\"}]}}}");
				//new FancyMessage("- " + name + " (" + id + ")").suggest("/shopadd " + id + " ").tooltip("Add an item to " + name).send(p);
			}
		}else{
			for(final Map.Entry<String,ShopCategory> cat : config.getShopCategories().entrySet()){
				sender.sendMessage("- " + cat.getKey() + ChatColor.GRAY + " (" + cat.getValue().getShopId() + ")");
			}
		}
		return true;
	}
}
