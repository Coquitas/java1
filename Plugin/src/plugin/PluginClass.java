package plugin;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class PluginClass extends JavaPlugin implements Listener {
	long tiempoUltimoGolpe = 0;
	long tiempoCooldown = 0;

	private ArrayList<Player> jugadores = new ArrayList<Player>();
	private ArrayList<Player> cazafantasmas = new ArrayList<Player>();
	private ArrayList<Player> fantasmas = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		getServer().broadcastMessage("on command se ejecuta");
		
		if(command.getName().equals("startgb")){
			//comprobar que el tama�o de la lista sea mayor de 3. seleccionar un j8ugador aleatorio y marcarlo como fantasma
			//coger el resto de los jugadores y meterlos en la lista de los cazafantasmas.
			// Math.random()*6 
			int random = (int) (Math.random()*jugadores.size());
			Player fantasma = jugadores.remove(random);
			if(!fantasmas.contains(random)){
				fantasmas.add(fantasma);
				kitFantasma(fantasma);
				getServer().broadcastMessage(ChatColor.DARK_RED + "El jugador " + ChatColor.GOLD + fantasma + ChatColor.DARK_RED + " ha muerto por vuestra culpa, y se quiere vengar de vosotros, tener cuidado.");

			}	
			cazafantasmas.addAll(jugadores);
			
			
			
			for (Player p : cazafantasmas){
				p.sendMessage(ChatColor.GREEN + "Tu objetivo como cazafantasma es sobrevivir a los ataques del fantasma, te hemod prporcionado una linterna que te ayudara a quitarte la ceguera unos 4 segundos, con ella podr�s pegar al fantasma.");
				p.sendMessage(ChatColor.AQUA + "Para utilizar la linterna comoiluminaci�n utiliza:" + ChatColor.GOLD + " Click Derecho" + ChatColor.AQUA + " para utilizarla como arma:" + ChatColor.GOLD + "Click Izquierdo");
				kitCazafantasma(p);
			}
			for (Player p : fantasmas) {
				p.sendMessage(ChatColor.GREEN + "Eres un fantasma y te tienes que vengar de tus compa�eros que no te salvaron, tienes un arma para matarles y tienes una ventaja, no tienes ceguera como ellos que la tienen, su arma es una linterna asi que ten cuidado.");
				kitFantasma(fantasma);
			}
			//cuando mate un fantasma a un gb que lo nmande a un fantasma.
			BukkitRunnable r = new BukkitRunnable() {
				
				@Override
				public void run() {
					
					for (Player p : cazafantasmas){
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,200,10000));
					}
				}
			};
			
			r.runTaskTimer(this, 1, 1);
			
			return true;
		}
		if(command.getName().equalsIgnoreCase("joingb")){
			
			if(sender instanceof Player){
				if(!jugadores.contains((Player)sender)){
					jugadores.add((Player)sender);
					getServer().broadcastMessage(ChatColor.DARK_AQUA + ((Player)sender).getName() + ChatColor.RED + " se ha unido al equipo " + ChatColor.GOLD + "CAZA FANTASMAS");
				}
			}
			return true;
		}
		return false;
	}


	public void kitCazafantasma(Player p){
		ItemStack l  = new ItemStack(Material.REDSTONE_TORCH_ON) ;
		ItemMeta im = l.getItemMeta();
		im.setDisplayName(ChatColor.AQUA + "linterna");
		im.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
		im.addEnchant(Enchantment.KNOCKBACK, 1, true);
		l.setItemMeta(im);
		p.setItemInHand(l);
		

	}

	public void kitFantasma(Player p){
		
		//kit fantasma
		//quitamos las cosas del jugador
		p.getInventory().clear();
		
		//a�adimos cosas del kit fantasma
		ItemStack pechera = new ItemStack (Material.CHAINMAIL_CHESTPLATE);
		p.getInventory().setChestplate(pechera);
		
		ItemStack patucos = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta)patucos.getItemMeta();
		lam.setColor(Color.fromRGB(255, 255, 255));
		patucos.setItemMeta(lam);
		p.getInventory().setBoots(patucos);
	
		
		ItemStack huesito = new ItemStack(Material.BONE);
		ItemMeta huesitoMeta = huesito.getItemMeta();
		huesitoMeta.addEnchant(Enchantment.DAMAGE_ALL, 3,true);
        p.setItemInHand(huesito);
	}
	
	@EventHandler
	public void da�os (EntityDamageByEntityEvent e){
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if(damager instanceof Player && damaged instanceof Player){
			Player pDamager = (Player)damager;
			Player pDamaged = (Player)damaged;

			if(cazafantasmas.contains(pDamager) && cazafantasmas.contains(pDamaged)){
				e.setCancelled(true);
				return;
			}

			if(pDamager.getItemInHand()!= null && pDamager.getItemInHand().getItemMeta() != null && pDamager.getItemInHand().getItemMeta().getDisplayName() != null && 
					pDamager.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "linterna")){

				//AQUI SABEMOS QUE EL JUGADOR QUE HA HECHO DA�O A OTRO TIENE LA LINTERNA

				pDamaged.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,20,1));



			}
		}

	}

	@EventHandler
	public void luz (PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(p.getItemInHand()!= null && p.getItemInHand().getItemMeta() != null && p.getItemInHand().getItemMeta().getDisplayName() != null && 
					p.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "linterna")){
				long tiempoActual = System.currentTimeMillis();
				long tiempoTranscurrido = tiempoActual - tiempoUltimoGolpe;
				if(tiempoTranscurrido > 4000){

					tiempoUltimoGolpe = System.currentTimeMillis();
					p.removePotionEffect(PotionEffectType.BLINDNESS);
					long tiempoA = System.currentTimeMillis();
					long tiempoT = tiempoActual - tiempoUltimoGolpe;
					if(tiempoT > 4000){

						tiempoCooldown = System.currentTimeMillis();
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,20,10000));

					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlaceEvent (BlockPlaceEvent e){
		
		if(e.getPlayer().getGameMode() == GameMode.SURVIVAL){
			e.setCancelled(true);
		}
		
		
		
	}
     

	
	





	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}



}
