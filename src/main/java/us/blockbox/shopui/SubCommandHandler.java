package us.blockbox.shopui;

import java.util.HashMap;
import java.util.Set;

//Created 11/30/2016 2:56 AM
public class SubCommandHandler{ //todo allow this to be reused
	private static SubCommandHandler ourInstance = new SubCommandHandler();

	public static SubCommandHandler getInstance(){
		return ourInstance;
	}

	private static final HashMap<String,ISubCommand> subCommandMap = new HashMap<>();

	private SubCommandHandler(){
	}

	public ISubCommand getSubCommand(String s){
		return subCommandMap.get(s);
	}

	void addSubCommand(String s,ISubCommand subCommand){
		subCommandMap.put(s,subCommand);
	}

	public Set<String> getSubCommands(){
		return subCommandMap.keySet();
	}
}
