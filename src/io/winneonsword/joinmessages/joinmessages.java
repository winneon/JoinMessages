package io.winneonsword.joinmessages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

public final class joinmessages extends JavaPlugin {
	
	joinmessages plugin;
	
	public File pluginFile;
	public File configFile;
	public FileConfiguration PluginJM;
	public FileConfiguration ConfigJM;
	
	public joinmessages(){
		
	}
	
	public final jm_listeners Listener = new jm_listeners(this);
	public final vanish_listener vanishListener = new vanish_listener(this);
	
	private final class updateCheck implements Runnable{
		private joinmessages plugin;
		
		public updateCheck(joinmessages plugin){
			this.plugin = plugin;
		}
		
		
		
		@Override
		public void run(){
			String pluginVersion = plugin.PluginJM.getString("version");
			try {
				// Credit to mbax for this version checker script. :)
				final URLConnection connection = new URL("https://raw.github.com/WinneonSword/Join-Messages/master/plugin.yml").openConnection();
				connection.setConnectTimeout(8000);
				connection.setReadTimeout(15000);
				connection.setRequestProperty("User-agent", "Join Messages");
				final LineNumberReader reader = new LineNumberReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				for (String version = null; (version = reader.readLine()) != null;){
					if (reader.getLineNumber() == 3){
						sb.append(version);
					}
				}
				String version = sb.toString();
				if (version != null){
					if (pluginVersion.contains("-a")){
						getLogger().info("You are using a ALPHA version!");
						getLogger().info("There will be bugs, so try to help by reporting them in an issue at:");
						getLogger().info("https://github.com/WinneonSword/Join-Messages");
						return;
					}
					if (pluginVersion.contains("-b")){
						getLogger().info("You are using a BETA version!");
						getLogger().info("This is slightly more stable than an ALPHA version, but still try to report bugs at:");
						getLogger().info("https://github.com/WinneonSword/Join-Messages");
						return;
					}
					if (!(pluginVersion.equals(version))){
						getLogger().info("Found a newer version of Join Messages available: " + version);
						getLogger().info("To download the newest version, go to: http://dev.bukkit.org/bukkit-plugins/join-messages/");
						return;
					}
					return;
				}
				reader.close();
				connection.getInputStream().close();
			} catch (final Exception e){
				
			}
			getLogger().severe("Could not check if plugin was up to date! Is the file missing, or is the server down?");
		}
	}
	
	private void copy(InputStream in, File file){
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void saveYMLs(){
		try {
			PluginJM.save(pluginFile);
			ConfigJM.save(configFile);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void loadYMLs(){
		try {
			PluginJM.load(pluginFile);
			ConfigJM.load(configFile);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void firstRun() throws Exception {
		if (!(pluginFile.exists())){
			pluginFile.getParentFile().mkdirs();
			copy(getResource("plugin.yml"), pluginFile);
		}
		if (!(configFile.exists())){
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
	}
	
	@Override
	public void onEnable(){
		pluginFile = new File(getDataFolder(), "plugin.yml");
		configFile = new File(getDataFolder(), "config.yml");
		try {
			firstRun();
		} catch (Exception e){
			e.printStackTrace();
		}
		PluginJM = new YamlConfiguration();
		ConfigJM = new YamlConfiguration();
		loadYMLs();
		getServer().getScheduler().runTaskTimerAsynchronously(this, new updateCheck(this), 40, 432000);
		Plugin VanishNoPacket = getServer().getPluginManager().getPlugin("VanishNoPacket");
		Plugin CMAPI = getServer().getPluginManager().getPlugin("CMAPI");
		if (CMAPI == null){
			getLogger().severe("CMAPI has not been found on your server! JoinMessages requires this!");
			getLogger().severe("Please take the CMAPI jar out of the ZIP JoinMessages came with and put that into your plugins folder!");
			getServer().getPluginManager().disablePlugin(this);
		}
		if (VanishNoPacket != null){
			getLogger().info("VanishNoPacket has been found! Hooking with VanishNoPacket...");
			getServer().getPluginManager().registerEvents(this.vanishListener, this);
			getLogger().info("Hooked with VanishNoPacket successfully!");
		}
		getServer().getPluginManager().registerEvents(this.Listener, this);
		getCommand("jm").setExecutor(new jm_command(this));
		getLogger().info("Join Messages has been enabled! Meow!");
		saveDefaultConfig();
	}
	
	@Override
	public void onDisable(){
		getLogger().info("Join Messages has been disabled! *cry*");
	}
}
