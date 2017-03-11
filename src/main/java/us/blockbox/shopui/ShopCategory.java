package us.blockbox.shopui;

import org.bukkit.inventory.ItemStack;

public class ShopCategory{ //todo use uilib
	private final String shopId;
	private final String shopNameColored;
	private final ItemStack itemStack;

	public ShopCategory(String shopId,String shopNameColored,ItemStack itemStack){
		this.shopId = shopId;
		this.shopNameColored = shopNameColored;
		this.itemStack = itemStack;
	}

	public String getShopId(){
		return shopId;
	}

	public ItemStack getItemStack(){
		return itemStack;
	}

	public String getShopNameColored(){
		return shopNameColored;
	}
}
