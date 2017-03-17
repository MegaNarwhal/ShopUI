package us.blockbox.shopui.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.blockbox.shopui.ISubCommand;
import us.blockbox.shopui.ShopConfig;

import static us.blockbox.shopui.ShopUI.prefix;
import static us.blockbox.shopui.locale.ShopMessage.Message.COMMAND_ADD_FAILED;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

//Created 11/22/2016 7:13 PM
public class CommandHeldItemAdd implements ISubCommand{

	private ShopConfig config = ShopConfig.getInstance();

	@Override
	public boolean onCommand(CommandSender sender,String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(prefix + "You must be a player.");
			return true;
		}
/*		if(!sender.hasPermission("shopui.command.shopadd")){
			sender.sendMessage(PLAYER_PERMISSION_INSUFFICIENT.getMsg());
			return true;
		}*/
		if(args.length < 3){ //New requirements should be <shop> <buy> <sell> [quantity] [itemname]
			showUsage(sender);
			return false;
		}
		final Player p = ((Player)sender);
		ItemStack held = p.getInventory().getItemInMainHand();
		if(held == null || held.getType() == Material.AIR){
			sender.sendMessage(prefix + "You must be holding an item.");
			return true;
		}

		final double priceBuy;
		final double priceSell;
		if(isAcceptable(args[1]) && isAcceptable(args[2])){
			priceBuy = Double.parseDouble(args[1]);
			priceSell = Double.parseDouble(args[2]);
		}else{
			sender.sendMessage(prefix + "Prices may not have more than 2 decimal places.");
			return true;
		}

		if(priceSell > priceBuy){
			sender.sendMessage(prefix + "Failed to add item. The sell price may not be greater than the buy price.");
			return true;
		}


		String itemName;
		int amount = 1;
		if(args.length > 3){
			try{
				amount = Integer.parseInt(args[3]);
				if(args.length > 4) //Did they use [0]<shop> [1]<buy> [2]<sell> [3][quantity] [4]<itemname>? Then arg 4 is their item name
					itemName = args[4];
				else
					itemName = held.getType().toString(); //They used [0]<shop> [1]<buy> [2]<sell> [3][quantity] so we will set the item name to the held item's type
			}catch(NumberFormatException ex){
				amount = 1;
				itemName = args[3]; //arg[3] isn't an integer, they must've skipped the quantity and put the item name: [0]<shop> [1]<buy> [2]<sell> [3]<itemname>
			}
		}else{
			amount = held.getAmount();
			itemName = held.getType().toString(); //They didn't set a quantity or an item name, let's default this to the held item's type again [0]<shop> [1]<buy> [2]<sell>
		}

		if(held.hasItemMeta()){
			if(config.addItem(args[0], itemName,held,priceBuy,priceSell,amount)){
				sender.sendMessage(prefix + "Complex item added to " + args[0] + ".");
			}else{
				sender.sendMessage(getMessage(COMMAND_ADD_FAILED));
			}
		}else{
			final String simpleItem = held.getType().toString() + ((held.getDurability() != 0) ? (":" + held.getDurability()) : "");
			if(config.addItem(args[0], itemName, simpleItem, priceBuy, priceSell, amount)){
				sender.sendMessage(prefix + "Simple item added to " + args[0] + ".");
			}else{
				sender.sendMessage(getMessage(COMMAND_ADD_FAILED));
			}
		}
		return true;
	}

	private static boolean isAcceptable(final String string){
		final String[] s = string.split("\\.");
		return (s.length == 1 || (s.length == 2 && s[1].length() <= 2));
	}

	private static void showUsage(final CommandSender sender){
		sender.sendMessage(prefix + "/shopui add <shop> <buy> <sell> [quantity] [itemname]");
	}
}
