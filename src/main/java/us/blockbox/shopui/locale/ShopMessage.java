package us.blockbox.shopui.locale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.blockbox.shopui.ShopUI;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ShopMessage{
	private static ShopUI plugin = ShopUI.getInstance();
	private static File messageFile = new File(plugin.getDataFolder(),"messages.yml");

	private ShopMessage(){
	}

	public enum Message{
		COMMAND_ADD_FAILED,
		OPEN_FAILED,
		PLAYER_INVENTORY_FULL,
		PLAYER_MONEY_INSUFFICIENT,
		PLAYER_PERMISSION_INSUFFICIENT
	}

	private static Map<Message,String> messages = new HashMap<>();

	public static void loadMessages(){
		if(!messageFile.exists() || !messageFile.isFile()){
			plugin.saveResource("messages.yml",false);
		}
		final FileConfiguration c = YamlConfiguration.loadConfiguration(messageFile);
		for(final Message m : Message.values()){
			messages.put(m,c.getString(m.name()));
		}
	}

	public static String getMessage(Message message){
		return messages.get(message);
	}
}

