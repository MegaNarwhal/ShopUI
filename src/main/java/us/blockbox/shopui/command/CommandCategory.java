package us.blockbox.shopui.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.blockbox.shopui.ISubCommand;
import us.blockbox.shopui.ShopConfig;

//Created 11/30/2016 2:27 AM
public class CommandCategory implements ISubCommand{

	private static final ShopConfig config = ShopConfig.getInstance();

	@Override
	public boolean onCommand(CommandSender sender,String[] args){
		if(args.length < 2){
			showUsage(sender);
			return false;
		}
		String name = "";
		for(int i = 1; i < args.length; i++){
			name += args[i] + (i < args.length - 1 ? " " : "");
		}
		ItemStack held;
		if(sender instanceof Player){
			held = ((Player)sender).getInventory().getItemInMainHand().clone();
			if(held == null || held.getType() == Material.AIR){
				held = new ItemStack(Material.STONE);
			}
			held.setAmount(1);
		}else{
			held = new ItemStack(Material.STONE);
		}
		if(config.addCategory(args[0],name,held)){
			sender.sendMessage(ShopConfig.getPrefix() + "Category " + args[0] + " created.");
		}else{
			sender.sendMessage(ShopConfig.getPrefix() + "Category creation failed.");
		}
		return true;
	}

	private static void showUsage(final CommandSender sender){
		sender.sendMessage("/shopui create <id> <name>");
	}
}
