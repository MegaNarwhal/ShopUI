package us.blockbox.shopui.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static us.blockbox.shopui.ShopInventory.getShopMenu;
import static us.blockbox.shopui.locale.ShopMessage.Message.OPEN_FAILED;
import static us.blockbox.shopui.locale.ShopMessage.Message.PLAYER_PERMISSION_INSUFFICIENT;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

public class CommandShop implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
		if(!(sender instanceof Player)){
			return true;
		}
		if(!sender.hasPermission("shopui.command.shop")){
			sender.sendMessage(getMessage(PLAYER_PERMISSION_INSUFFICIENT));
			return true;
		}
		Player p = (Player)sender;

		final Inventory menu = getShopMenu(p);
		if(menu == null){
			sender.sendMessage(getMessage(OPEN_FAILED));
			return true;
		}
		p.openInventory(menu);
		return true;
	}
}
