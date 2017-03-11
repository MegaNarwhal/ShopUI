package us.blockbox.shopui;

import org.apache.commons.lang.Validate;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class ShopTransactionLogger{
	private static final ShopUI plugin = ShopUI.getInstance();
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private final File file;
	private final Queue<String> msgQueue = new LinkedList<>();
	private static final Set<ShopTransactionLogger> loggerSet = Collections.synchronizedSet(new HashSet<ShopTransactionLogger>());
	private static final int maxLines = 1000;

	ShopTransactionLogger(String fileName){
		this.file = chooseFile(fileName);
		ShopUI.log.info("Log file: " + file.getName());
		loggerSet.add(this);
	}

	void logToFile(String message){
		synchronized(msgQueue){
			msgQueue.add(dateFormat.format(System.currentTimeMillis()) + " " + message);
			if(msgQueue.size() < 50){
				return;
			}
		}
		flushQueue();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private boolean flushQueue(){
		synchronized(msgQueue){
			if(msgQueue.isEmpty()){
				return true;
			}
		}
		ShopUI.log.info("Flushing to file: " + getFile().getName());
		synchronized(file){
			if(!file.exists()){
				try{
					file.createNewFile();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			final PrintWriter pw;
			try{
				pw = new PrintWriter(new FileWriter(file,true));
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			while(!msgQueue.isEmpty()){
				final String msg = msgQueue.poll();
				if(msg != null){
					pw.println(msg);
				}
			}
			pw.flush();
			pw.close();
		}
		synchronized(msgQueue){
			return msgQueue.isEmpty();
		}
	}

	static void flushAllQueues(){
		for(final ShopTransactionLogger logger : loggerSet){
			logger.flushQueue();
		}
	}

	private File getFile(){
		return file;
	}

	private static int countLines(File file) throws IOException {
		try(InputStream is = new BufferedInputStream(new FileInputStream(file))){
			byte[] c = new byte[1024];
			int count = 0;
			int readChars;
			boolean empty = true;
			while((readChars = is.read(c)) != -1){
				empty = false;
				for(int i = 0; i < readChars; ++i){
					if(c[i] == '\n'){
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		}
	}

	private static File chooseFile(String fileName){
		Validate.notNull(fileName);
		final File testFile;
		final File firstFile = new File(plugin.getDataFolder(),fileName + "_0.txt");
		if(!plugin.getDataFolder().exists()){
			plugin.getDataFolder().mkdir();
			return firstFile;
		}
		final File[] dataFolderFiles = plugin.getDataFolder().listFiles();
		if(dataFolderFiles == null || dataFolderFiles.length == 0) return firstFile;
		final int i = pickHighest(fileName,dataFolderFiles);
		ShopUI.log.info("Highest log file number is " + i);
		if(i == -1){
			return firstFile;
		}else{
			testFile = new File(plugin.getDataFolder(),fileName + "_" + i + ".txt");
		}
		int lines = 0;
		try{
			lines = countLines(testFile);
		}catch(IOException e){
			e.printStackTrace();
		}
		if(lines > maxLines){
			ShopUI.log.info("Over " + maxLines + " lines, starting new file.");
			return new File(plugin.getDataFolder(),fileName + "_" + (i+1) + ".txt");
		}
		return testFile;
	}

	private static int pickHighest(String prefix,File... files){
		Validate.notNull(prefix);
		Validate.notNull(files);
		int i = -1;
		for(final File f : files){
			if(!f.getName().startsWith(prefix)){
				continue;
			}
			final String[] name = f.getName().split("_");
			final int num;
			try{
				num = Integer.parseInt(name[name.length - 1].replace(".txt",""));
			}catch(NumberFormatException ex){
				continue;
			}
			if(num > i){
				i = num;
			}
		}
		return i;
	}
}
