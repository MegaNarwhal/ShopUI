package us.blockbox.shopui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import us.blockbox.shopui.listener.CommandShopPreProcessListener;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;
import static us.blockbox.shopui.ShopUI.plugin;

//Created 11/20/2016 2:54 AM
public class ShopConfig{

	private Logger log = plugin.getLogger();
	private FileConfiguration categoryConfig;
	private File categoryConfigFile = new File(plugin.getDataFolder(),"shops.yml");
	public Map<String,List<ShopItem>> shopItems = new HashMap<>();
	public Map<String,ShopCategory> shopCategories = new LinkedHashMap<>();
	private boolean debug = false;
	private boolean updaterEnabled = false;

	public boolean debugEnabled(){
		return debug;
	}

	private static ShopConfig ourInstance = new ShopConfig();

	public static ShopConfig getInstance(){
		return ourInstance;
	}

	private ShopConfig(){
	}

	void loadConfig(){
		if(!categoryConfigFile.isFile()){
			plugin.saveResource("shops.yml",false);
		}
		categoryConfig = YamlConfiguration.loadConfiguration(categoryConfigFile);
		plugin.saveDefaultConfig();
		final FileConfiguration config = plugin.getConfig();
		final File shopDir = new File(plugin.getDataFolder(),"shops/");
		if(!shopDir.exists() && !shopDir.isDirectory()){
			plugin.saveResource("shops/test.yml",false);
		}
		debug = config.getBoolean("debug",false);
		if(debug){
			log.info("Debugging is enabled!");
		}
		if(config.isSet("updatechecker")){
			updaterEnabled = config.getBoolean("updatechecker");
		}else{
			config.set("updatechecker",true);
			updaterEnabled = true;
			plugin.saveConfig();
		}
		shopItems.clear();
		shopCategories.clear();
		for(final String s : categoryConfig.getKeys(false)){
			parseCategory(s);
			parseShop(s);
		}
		if(config.getBoolean("forceshopcommand")){
			log.info("Forcing shop command.");
			getServer().getPluginManager().registerEvents(new CommandShopPreProcessListener(),plugin);
		}
	}

	public boolean addCategory(String id,String name,ItemStack itemStack){
		id = id.toLowerCase();
		if(categoryConfig.contains(id)){
			if(debug){
				log.info("ID " + id + " is already present in shops.yml.");
			}
			return false;
		}
		Map<String,String> opts = new HashMap<>();
		opts.put("item",itemStack.getType().toString() + (itemStack.getDurability() != 0 ? (":" + itemStack.getDurability()) : ""));
		opts.put("name",name);
		if(debug){
			log.info("Creating new category.");
			log.info("item: " + opts.get("item"));
			log.info("name: " + opts.get("name"));
		}
		categoryConfig.set(id,opts);
		try{
			categoryConfig.save(categoryConfigFile);
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		final File f = new File(plugin.getDataFolder(),"shops/" + id + ".yml");
		if(debug){
			log.info("Saving to " + f.getName());
		}
		if(!f.isFile() || !f.exists()){
			try{
				if(!f.createNewFile()){
					log.severe("Failed to create new category config file: " + f.getName());
					return false;
				}
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
//todo more debug output
	private void parseCategory(String s){
		String shopName = ChatColor.translateAlternateColorCodes('&',categoryConfig.getString(s + ".name",s));
		if(debug){
			log.info("Loading category " + shopName + " (ID: " + s + ")");
		}
		shopCategories.put(ChatColor.stripColor(shopName),new ShopCategory(s,shopName,parseItemInfo(categoryConfig.getString(s + ".item",null),1)));
	}

	private void parseShop(final String shop){
		final File f = new File(plugin.getDataFolder(),"shops/" + shop + ".yml");
		if(!f.isFile() || !f.exists()){
			log.warning("File " + f.getName() + " not found, skipping.");
			return;
		}
		FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(f);
		List<ShopItem> shopItemStacks = new ArrayList<>();
		for(final String key : shopConfig.getKeys(false)){

			final Object item = shopConfig.get(key + ".item");
			final int quantity = shopConfig.getInt(key + ".quantity",1);

			ItemStack stack = null;

			switch(item.getClass().getSimpleName()){
				case "ItemStack":{
					//todo figure out how to check material before deserializing
					stack = ((ItemStack)item).clone();
					stack.setAmount(quantity);
					break;
				}
				case "String":{
					stack = parseItemInfo((String)item,quantity);
					break;
				}
				default:{
					log.warning("Unrecognized item info type.");
					break;
				}
			}
			if(stack != null){
				if(debug){
					log.info("Adding " + stack.getType() + (stack.getDurability() == 0 ? "" : ":" + stack.getDurability()) + " x" + stack.getAmount() + " to " + shop + ".");
				}
				double priceBuy = shopConfig.getDouble(key + ".buy",1000.0D);
				double priceSell = shopConfig.getDouble(key + ".sell",0.0D);
				if(priceSell > priceBuy){
					log.warning("Sell price greater than buy price for item " + stack.getType().toString() + " x" + quantity + (stack.getItemMeta().hasDisplayName() ? (" (" + stack.getItemMeta().getDisplayName() + ")") : "") + " in shop " + shop + ", setting sell price to buy price.");
					priceSell = priceBuy;
				}
				shopItemStacks.add(new ShopItem(stack,priceBuy,priceSell,quantity));
//				log.info(stack.toString());
			}
		}
		shopItems.put(shop,shopItemStacks);
	}

	@Deprecated
	public boolean addItem(final String shop,final String name,final Object itemStack,final double priceBuy,final double priceSell){
		return addItem(shop,name,itemStack,priceBuy,priceSell,1);
	}

	public boolean addItem(final String shop,final String name,final Object itemStack,final double priceBuy,final double priceSell,int quantity){
		final File f = new File(plugin.getDataFolder(),"shops/" + shop + ".yml");
		if(!f.isFile() || !f.exists()){
			log.warning("File " + f.getName() + " not found, skipping.");
			return false;
		}
		FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(f);
		if(shopConfig.contains(name)){
			return false;
		}

		final Map<String,Object> map = new LinkedHashMap<>();
		map.put("item",itemStack);
		map.put("buy",priceBuy);
		map.put("sell",priceSell);
		if(quantity != 1){
			map.put("quantity",quantity);
		}

		shopConfig.set(name,map);
		try{
			shopConfig.save(f);
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private ItemStack parseItemInfo(String string,int quantity){
		final String[] iData = string.split(":");
		Material material = Material.matchMaterial(iData[0]);
		if(material == null){
			log.info("Invalid material " + iData[0] + ", defaulting to dirt.");
			return new ItemStack(Material.DIRT,1);
		}
		ItemStack stack = new ItemStack(material,quantity);
		if(iData.length > 1){
			short dataVal = 0;
			try{
				dataVal = Short.parseShort(iData[1]);
			}catch(NumberFormatException ex){
				log.warning("Invalid data value, defaulting to 0.");
			}
			if(dataVal > 0){
				stack.setDurability(dataVal);
			}
		}
		return stack;
	}

	public boolean isUpdaterEnabled(){
		return updaterEnabled;
	}
}