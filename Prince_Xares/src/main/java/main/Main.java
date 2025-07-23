package main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import Currency.Coin;
import Currency.Rarity;
import Currency.Vault;
import Currency.Xarin;
import Currency.Zyra;
import DAO.VaultDAO;
import Shop.Pouch;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Main extends ListenerAdapter
{
	// Possible VaultDao's from all servers 
	private final Map<String, VaultDAO> vaultDAOs = new ConcurrentHashMap<>();
	 // Temporary cache to hold coins per user before they decide
	private List<Coin> coins = new ArrayList<Coin>();
	
	/*
	 * Returns the VaultDAO of a server that is currently used for the events
	 */
    private VaultDAO getVaultDAO(String guildId) {
        return vaultDAOs.computeIfAbsent(guildId, id -> new VaultDAO(id));
    }
    
    
    public static void main(String[] args)
    {
    	
    	Properties config = new Properties();
    	try (FileInputStream in = new FileInputStream("resource/.config.properties")) {
            config.load(in);
        } catch (IOException e) {
            System.err.println("Could not load config.properties");
            e.printStackTrace();
            return;
        }

        String token = config.getProperty("bot.token");

        if (token == null || token.isBlank()) {
            System.err.println("Bot token not found in config.properties");
            return;
        }
        JDA jda = JDABuilder.createDefault(token,
        										GatewayIntent.GUILD_MEMBERS,
        										GatewayIntent.GUILD_MESSAGES,
        										GatewayIntent.GUILD_EXPRESSIONS,
        										GatewayIntent.SCHEDULED_EVENTS
        										)
        		.setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new Main())
                .build();
        
        

        // You might need to reload your Discord client if you don't see the commands
        CommandListUpdateAction commands = jda.updateCommands();

        // Simple reply commands
        commands.addCommands(
            Commands.slash("say", "Makes the bot say what you tell it to")
                .setContexts(InteractionContextType.ALL) // Allow the command to be used anywhere (Bot DMs, Guild, Friend DMs, Group DMs)
                .setIntegrationTypes(IntegrationType.ALL) // Allow the command to be installed anywhere (Guilds, Users)
                .addOption(STRING, "content", "What the bot should say", true) // you can add required options like this too
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        // Commands without any inputs
        commands.addCommands(
            Commands.slash("leave", "Make the bot leave the server")
                // The default integration types are GUILD_INSTALL.
                // Can't use this in DMs, and in guilds the bot isn't in.
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)) // only admins should be able to use this command.
        );
        
        commands.addCommands(
        		Commands.slash("stats", "roles or activity")
        		.addOptions(
        		        new OptionData(OptionType.STRING, "type", "The type of stat to show", true)
        		            .addChoice("Roles", "roles")
        		            .addChoice("Membership", "membership"),
        		        new OptionData(OptionType.USER, "member", "The member to check", true)
        		    )
        		);
        
        commands.addCommands(
        	    Commands.slash("vault", "Manage a user's coin vault")
        	        .addOptions(
        	            new OptionData(OptionType.STRING, "action", "What you want to do", true)
        	                .addChoice("View", "view")
        	                //.addChoice("Upgrade Coin","upgrade") Is removed to hold the fairness 
        	                .addChoice("Level Up", "levelup")
        	                .addChoice("Admin View","adminview")
        	                .addChoice("Remove", "remove")
        	                .addChoice("clear", "clear"),
        	            new OptionData(OptionType.INTEGER,"level","level of the coin( [1,10] )",false),
        	            new OptionData(OptionType.USER, "user", "Target user for the action", false),
        	            new OptionData(OptionType.STRING, "id", "Coin Id",false)
        	        )
        	        .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
        	);
        commands.addCommands(
        		Commands.slash("add","Add Coins Manually")
        		.addOptions(
        				new OptionData(OptionType.STRING, "coin", "Coin type", false)
    	                .addChoice("Xarin", "xarin")
    	                .addChoice("Zyra", "zyra"),
    	                new OptionData(OptionType.STRING,"rarity","Rarity type of the coin ",false)
    	            		.addChoice("Common", "common")
    	            		.addChoice("Rare", "rare")
    	            		.addChoice("Epic","epic")
    	            		.addChoice("Legendary", "legendary"),
    	            	new OptionData(OptionType.INTEGER,"level","level of the coin( [1,10] )",false),
    	            	new OptionData(OptionType.STRING,"user","Add to this user",false)
        				)
        		.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        		);
        commands.addCommands(Commands.slash("crystara","Get Crystara"));
        
        
        

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }

    /*
     * Overrided methode from JDA library
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    	// If no user is given targetUser == event.getMember() 
    	Member targetUser =  event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
    	String targetId = targetUser.getId() != null ? targetUser.getId():null;
    	String guildId = event.getGuild().getId();                
    	
    	boolean isWiseGod = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("Vault viewer"));
    	boolean isAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
    	
    	String coinType = event.getOption("coin") != null ? event.getOption("coin").getAsString() : null;

    	               
        VaultDAO vaultDAO = getVaultDAO(guildId);
   
        
        Vault targetVault = vaultDAO.loadVaultForUser(targetId);
        
        if (event.getGuild() == null)
            return;
        switch (event.getName()) {
            case "say":
                say(event, event.getOption("content").getAsString());
                break;
            case "leave":
                leave(event);
                break;
            case "stats":
                // Just call stats normally; it now defers internally
                stats(event);
                break;
            case "crystara":
            	String guildId1 = event.getGuild().getId();
            	VaultDAO vaultDao1 = getVaultDAO(guildId1);
            	Member targetedUser = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
            	int crystaras = vaultDao1.getCrystara(targetedUser.getId());
            	event.reply("You have: "+crystaras + " 💎𝓒!").setEphemeral(true).queue();
            	return;
            
            case "add":
            	if( !isWiseGod && !isAdmin) {
            		event.reply("Only Admin can add coins manually!").setEphemeral(true).queue();
            		return;
            	}
            	if (event.getOption("rarity") == null) {
            		event.reply("Please choose the correct rarity!").setEphemeral(true).queue();
            		return;
            	}
            	if(event.getOption("level") == null) {
            		event.reply("Please choose a valid level for the Coin!").setEphemeral(true).queue();
            		return;
            	}
            	
            	if (event.getOption("level").getAsInt()<1 || event.getOption("level").getAsInt()>10) {
            		event.reply("Level Range for a coin is 1-10.").setEphemeral(true).queue();
            		return;
            	}
            	if (targetVault.getVault().size() >= 10) {
            		event.reply("You can't add more coins!").setEphemeral(true).queue();;
            		return;
            	}
            	if (isWiseGod || isAdmin) {
            		Coin coin = null;
            		Rarity rarity;
            		
            		String rarityInput = event.getOption("rarity").getAsString();
        			rarity = Rarity.valueOf(rarityInput.toUpperCase());
        			int level = event.getOption("level").getAsInt();
        			
            		if (coinType.equalsIgnoreCase("Xarin")) {
            			coin = new Xarin(rarity,level);
            		} else if (coinType.equalsIgnoreCase("Zyra")) {
            			coin = new Zyra(rarity,level);
            		}
            		
            		if (coin != null) {
            			targetVault.addCoin(coin);
            			event.reply("A " +coinType.toString()+" Coin"+" | "+coin.getCoinId().toString()+" | "+"Level: "+level+" | "+"Rarity: "+rarity.toString()+ " is added to the "+targetUser.getEffectiveName()+"'s Vault.").setEphemeral(true).queue();
            			break;
            		}
            	}            	
            	break;
            case "vault":
            	String action = event.getOption("action").getAsString();                              
                if (targetVault == null) {
                    event.reply("You have no vault data yet,Please contact the Staff memebers!").setEphemeral(true).queue();
                    return;
                }
                
                // Format vault data to string, e.g. list coins and levels
                switch (action) {
                case "adminview":
                	if ((!isWiseGod || !isAdmin) && !targetUser.equals(event.getMember())) {
                		event.reply("Authentication failed! Only Admin and the Owner of the Vault can see this Vault!").setEphemeral(true).queue();
                		break;
         
                	}
                	else if (targetVault == null || targetVault.getVault().isEmpty()) {
                        event.reply("💰 " + targetUser.getEffectiveName() + "'s vault is empty.").setEphemeral(true).queue();
                        break;
                    } else {
                    	event.reply("💰 " + targetUser.getEffectiveName() + "'s Vault:\n" + formatVaultAdmin(targetVault)).setEphemeral(true).queue();
                    	break;
                    }
                             	
//                case "upgrade":
//                	
//                	Coin nCoin = null;
//                	String uCoinId = event.getOption("id").getAsString();
//                	for (Coin coin : targetVault.getVault()) {
//                	    if (coin.getCoinId().equals(uCoinId)) {
//                	        nCoin = coin;
//                	        break;
//                	    }
//                	}
//                	nCoin.gradeUp();
//                	targetVault.saveDirtyCoins();
//                	event.reply("Coin is upgraded!").setEphemeral(true).queue();
//                	break;
                	
                case "levelup":
                	Coin lCoin = null;
                	int targetCrystara = vaultDAO.getCrystara(targetId);
                	if (event.getOption("id").getAsString() == null) {
                		event.reply("You need to identify the Coin with the /id to level Up!").setEphemeral(true).queue();
                		break;
                	}
                	String lCoinId = event.getOption("id").getAsString();
                	for (Coin coin : targetVault.getVault()) {
                	    if (coin.getCoinId().equals(lCoinId)) {
                	        lCoin = coin;
                	        break;
                	    }
                	}
                	if (lCoin == null) {
                		event.reply("No coin with this id exists in your Vault!").setEphemeral(true).queue();
                		return;
                	}
                	int levell = lCoin.getLevel();
                	Rarity rarityl = lCoin.getRarity();
                	if (levell <0 || levell >10) {
                		event.reply("The Coin level can not be negative or higher than 10!Please report this matter to the supporter!").setEphemeral(true).queue();
                		return;
                	}
                	if (Arrays.stream(Rarity.values()).noneMatch(rarity -> rarity.equals(rarityl)) ) {
                		event.reply("This Rarity doesn't exist yet!Please report this matter to the supporter!").setEphemeral(true).queue();
                		return;
                	}
                	int cost = 0;
                	if (levell < 10 && !rarityl.equals(Rarity.LEGENDARY)) { //Level Upgrade Cost(LUC) 
                		cost = (int)(Math.pow(rarityl.ordinal() * 10 + levell, 2)) ;
                		
                	}
                	
                	else if (levell < 10 && rarityl.equals(Rarity.LEGENDARY) ) { // Level Upgrade Cost (LUC) for Legendary coins
                		cost = 1200 + (int)(Math.pow(rarityl.ordinal() * 10 + levell, 2));
                	}
                	else if (levell == 10 && rarityl.ordinal() < 3) { // Tier Upgrade Cost(TUC) = LUC + Extra Cost(Ec)
                		cost = (int)(Math.pow(rarityl.ordinal() * 10 + levell, 2)) + (rarityl.ordinal() + 1) * 100;
                		if (targetCrystara < cost) {
                			event.reply("You need " + cost +" 💎𝓒 but you have "+targetCrystara).setEphemeral(true).queue();
                			return;
                		}
                		vaultDAO.removeCrystara(targetId, cost);
                		lCoin.setLevel(1);
                		lCoin.gradeUp();
                		targetVault.saveDirtyCoins();
                		event.reply("Congrats! Your " + lCoin.getClass().getSimpleName()+" is upgraded to " + lCoin.getRarity().toString() +" by spending " +cost +" 💎𝓒!" ).setEphemeral(true).queue();
                		return;
                	}
                	if (targetCrystara < cost) {
            			event.reply("You need " + cost +" 💎𝓒 but you have "+targetCrystara).setEphemeral(true).queue();
            			return;
            		}
                	vaultDAO.removeCrystara(targetId, cost);
            		lCoin.levelUp();
                	targetVault.saveDirtyCoins();
                	event.reply("Your "+ lCoin.getClass().getSimpleName() + " is advanced to Lv." + lCoin.getLevel() + " using "+cost + " 💠𝓒!" ).setEphemeral(true).queue();
                	break;
                	
                	
                case "view":
                    if (targetVault == null || targetVault.getVault().isEmpty()) {
                        event.reply("💰 " + targetUser.getEffectiveName() + "'s vault is empty.").setEphemeral(true).queue();
                        break;
                    } else {
                        event.deferReply().queue(hook -> {
                            // Pass the avatar URL of the target user to showVault
                            String avatarUrl = targetUser.getEffectiveAvatarUrl();
                            showVault(hook, targetVault, targetUser.getEffectiveName(), avatarUrl);
                        });
                        return;
                    }
                    

                    
                
                case "remove":
                	String coinId = event.getOption("id").getAsString();

                	if( (!isWiseGod && !isAdmin) || !event.getMember().equals(targetUser) ) {
                		event.reply("Only Admin and the Owner of the vault can remove a coin from this Vault!").setEphemeral(true).queue();
                		return;
                	}
                	
                	if (isWiseGod || isAdmin || event.getMember().equals(targetUser)) {

                	    Coin rCoin = null;
                	    for (Coin coin : targetVault.getVault()) {
                	        if (coin.getCoinId().equals(coinId)) {
                	            rCoin = coin;
                	            break;
                	        }
                	    }

                	    if (rCoin == null) {
                	        event.reply("❌ No coin with that ID was found in the vault.").setEphemeral(true).queue();
                	        return;
                	    }
                	    event.reply(
                	    	   "| Type: " + rCoin.getClass().getSimpleName() +
           	                 "\n| Id: " + rCoin.getCoinId() +
           	                 "\n| Rarity: " + rCoin.getRarity().toString() +
           	                 "\n| Level: " + rCoin.getLevel() +
           	                 "\n ❌ is deleted from " + targetUser.getEffectiveName() + "'s Vault.")
           	         																				.setEphemeral(true).queue();
                	    
                	    targetVault.removeCoin(coinId);

                	    
                	}
                	break;
                	
                	
                	
                case "clear":
                	if((!isWiseGod && !isAdmin) || !event.getMember().equals(targetUser)) {
                		event.reply("Only Admin or the Owner of the Vault can clear this Vault!").setEphemeral(true).queue();
                		break;
                	}
                	if (isWiseGod || isAdmin || event.getMember().equals(targetUser)) {
                		targetVault.removeAllCoinsFrom(targetId);
                		event.reply("💰 All coins have been cleared from " + targetUser.getAsMention() + "'s vault.").queue();
                	    break;
                	}
                	break;
                }
                break;
            
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
                break;
        }
    }
    
    /*
     * Overrided methode from JDA library
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        String userId = event.getUser().getId();
        String guildId = event.getGuild().getId();
        VaultDAO vaultDAO = getVaultDAO(guildId);
        Vault targetVault = vaultDAO.loadVaultForUser(userId);
        int crystara = vaultDAO.getCrystara(userId);
        
        
        
        if (buttonId.startsWith("role")) {
        	 Role role1 = event.getGuild().getRoleById("1389706613803188314");
        	 Role role2 = event.getGuild().getRoleById("1389706881018232923");
        	 Role role3 = event.getGuild().getRoleById("1390808600863047801");
        	 Role role4 = event.getGuild().getRoleById("1396835013185765386");
        	 Role role5 = event.getGuild().getRoleById("1396834818473459763");
        	 Role role6 = event.getGuild().getRoleById("1396834669886050478");
        	 Role role7 = event.getGuild().getRoleById("1396835120962605117");
        	 Role role8 = event.getGuild().getRoleById("1396835220304564264");
        	 Role role9 = event.getGuild().getRoleById("1396835368203976774");
        	 Role role10 = event.getGuild().getRoleById("1396834896579919882");
        	int xarinCount = (int) targetVault.getVault().stream()
    			    .filter(coin -> coin instanceof Xarin)          // Filter by Zyra class
    			    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY)) // Filter by legendary rarity (case-insensitive)
    			    .filter(coin -> coin.getLevel() == 10)
    			    .count();
    		int zyraCount = (int) targetVault.getVault().stream()
    			    .filter(coin -> coin instanceof Zyra)          // Filter by Zyra class
    			    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY)) // Filter by legendary rarity (case-insensitive)
    			    .filter(coin -> coin.getLevel() == 10)
    			    .count();
        	
        	if(buttonId.equals("role1")) {
        		if (xarinCount <5 || zyraCount <5) {
        			event.reply("You don't have sufficient amount of Coins for this role").setEphemeral(true).queue();
        			return;
        		}
        		else if(xarinCount>=5 && zyraCount >=5) {
        			if(role1 != null) {
        				vaultDAO.deleteAllCoinsForUser(userId);
        				 event.getGuild().addRoleToMember(event.getMember(), role1).queue(
        				            success -> event.reply("Congrats! You're now a "+role1.getName()).setEphemeral(true).queue(),
        				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
        				        );
        				 return;
            		
        			}else {
        				event.reply("Role has not been found!").queue();
        				return;
        			}
        			
        		}
        		
        		
        		
        	}
        	else if (buttonId.equals("role2") && role2 != null) {
        		if (zyraCount >=1) {
        			String zyraId = targetVault.getVault().stream()
        				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
        				    .filter(coin -> coin instanceof Zyra)
        				    .filter(coin -> coin.getLevel() == 10)
        				    .map(coin -> coin.getCoinId())
        				    .findFirst()
        				    .orElse(null);  // returns null if no match found
        			vaultDAO.removeCoin(zyraId);
        			event.getGuild().addRoleToMember(event.getMember(), role2).queue(
				            success -> event.reply("Congrats! You're now a "+role2.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;

        		} else {
        			event.reply("Role didn't found!").queue();
        			return;
        		}
        		
        	}
        	
        	else if (buttonId.equals("role3") && role3 != null) {
        		if (xarinCount >=1) {
        			String xarinId = targetVault.getVault().stream()
        				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
        				    .filter(coin -> coin instanceof Xarin)
        				    .filter(coin -> coin.getLevel() == 10)
        				    .map(coin -> coin.getCoinId())
        				    .findFirst()
        				    .orElse(null);  // returns null if no match found
        			vaultDAO.removeCoin(xarinId);
        			event.getGuild().addRoleToMember(event.getMember(), role3).queue(
				            success -> event.reply("Congrats! You're now a "+role3.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;

        		} else {
        			event.reply("Role didn't found!").queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role4") && role4 != null) {
        		String xarinId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Xarin)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);  // returns null if no match found
        		
        		String zyraId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Zyra)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);
        		if(zyraId != null && xarinId != null  && crystara >=5000) {
        			vaultDAO.removeCoin(xarinId);
        			vaultDAO.removeCoin(zyraId);
        			vaultDAO.removeCrystara(userId, 5000);
        			event.getGuild().addRoleToMember(event.getMember(), role4).queue(
				            success -> event.reply("Congrats! You're now a "+role4.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        			
        		} else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role5") && role5 != null) {
        		String xarinId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Xarin)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);  // returns null if no match found
        		
        		String zyraId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Zyra)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);
        		if(zyraId != null && xarinId != null) {
        			vaultDAO.removeCoin(xarinId);
        			vaultDAO.removeCoin(zyraId);
        			event.getGuild().addRoleToMember(event.getMember(), role5).queue(
				            success -> event.reply("Congrats! You're now a "+role5.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        			
        		} else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role6") && role6 != null) {
        		String xarinId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Xarin)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);  // returns null if no match found
        		
        		String zyraId = targetVault.getVault().stream()
    				    .filter(coin -> coin.getRarity().equals(Rarity.LEGENDARY))
    				    .filter(coin -> coin instanceof Zyra)
    				    .filter(coin -> coin.getLevel() == 1)
    				    .map(coin -> coin.getCoinId())
    				    .findFirst()
    				    .orElse(null);
        		if(zyraId != null || xarinId != null) {
        			String toDelete = xarinId != null ? xarinId : zyraId;
        			vaultDAO.removeCoin(toDelete);
        			event.getGuild().addRoleToMember(event.getMember(), role6).queue(
				            success -> event.reply("Congrats! You're now a "+role6.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        			
        		} else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        		
        	}
        	
        	else if (buttonId.equals("role7") && role7 != null) {
        		if(crystara >= 4000) {
        			vaultDAO.removeCrystara(userId, 4000);
        			event.getGuild().addRoleToMember(event.getMember(), role7).queue(
				            success -> event.reply("Congrats! You're now a "+role7.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        		}else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role8") && role8 != null) {
        		if(crystara >= 4000) {
        			vaultDAO.removeCrystara(userId, 4000);
        			event.getGuild().addRoleToMember(event.getMember(), role8).queue(
				            success -> event.reply("Congrats! You're now a "+role8.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        		}else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role9") && role9 != null) {
        		if(crystara >= 2000) {
        			vaultDAO.removeCrystara(userId, 2000);
        			event.getGuild().addRoleToMember(event.getMember(), role9).queue(
				            success -> event.reply("Congrats! You're now a "+role9.getName()).setEphemeral(true).queue(),
				            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
				        );
        			return;
        		}else {
        			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
        			return;
        		}
        	}
        	
        	else if (buttonId.equals("role10") && role10 != null) {
        		event.getGuild().addRoleToMember(event.getMember(), role10).queue(
			            success -> event.reply("Congrats! You're now a "+role10.getName()).setEphemeral(true).queue(),
			            error -> event.reply("Failed to add role: " + error.getMessage()).queue()
			        );
    			return;
        	}else {
    			event.reply("You don't have enough matertials for this role!").setEphemeral(true).queue();
    			return;
    		}
        	
        }
        
        // Handle buying pouches (generate coins but don't save yet)
        if (buttonId.startsWith("buy_")) {
            if (buttonId.equals("buy_random")) {
            	if (crystara <60) {
            		event.reply("You need **60** 💎𝓒 but you have **"+crystara +"** 💎𝓒!").setEphemeral(true).queue();
            		return;
            	}
            	vaultDAO.removeCrystara(userId, 60);
                Pouch pouch = new Pouch(5);
                coins = new ArrayList<>(pouch.getCoins());
            } else {
            	if (crystara < 100) {
            		event.reply("You need **100** 💎𝓒 but you have **"+crystara +"** 💎𝓒!").setEphemeral(true).queue();
            		return;
            	}
            	vaultDAO.removeCrystara(userId, 100);
                boolean isXarin = buttonId.equals("buy_xarin");
                Pouch pouch = new Pouch(5, isXarin);
                coins = new ArrayList<>(pouch.getCoins());
            }
           
            // After Generating the coins give the option to choose each coin!
            List<ActionRow> actionRows = new ArrayList<ActionRow>();
            int coinIndex = 0;
            	 List<Button> buttons = new ArrayList<Button>();
            	 for(Coin coin : coins) {
            		 String id ="Coin_" + userId + "_" + coinIndex;
        			 String label = coin.getClass().getSimpleName().toString() + " lv."+coin.getLevel() +" Rarity: "+coin.getRarity();
        			 buttons.add(Button.secondary(id, label));
        			 coinIndex++;
            	 }
            	 if(buttons.size() >0) {
            		 actionRows.add(ActionRow.of(buttons));
            	}
            
            //System.out.println(actionRows);
            // Buttons: Accept or Decline all coins
            Button accept = Button.success("accept_coins:" + userId, "Add All to Vault");
            Button decline = Button.danger("decline_coins:" + userId, "Skip All");

            event.reply("Select the coins of your choice:\n")
            	 .setComponents(actionRows)
                 .addActionRow(accept, decline)
                 .setEphemeral(true)
                 .queue();
            return;
        }
 
        if (buttonId.startsWith("Coin_")) {
        	
        	String[] parts = buttonId.split("_");
        	if (parts.length != 3) return; //Wrong Id
        	
        	if (coins == null || coins.isEmpty()) { //No coins 
                event.reply("No coins to add or already processed.").setEphemeral(true).queue();
                return;
            }
        	
        	String authorId = parts[1];
            if (!authorId.equals(userId)) return; // only allow same user
            
            if (targetVault.getVault().size() >= 10) {
            	event.reply("You have reached the maximum amount of Coins in your Vault!").setEphemeral(true).queue();
            	return;
            }
            
        	int index = Integer.parseInt(parts[2]);
        	Coin coin = coins.get(index);
        	vaultDAO.saveCoin(userId, guildId, coin);
        	coins.remove(index);
        	List<Button> updatedButtons = new ArrayList<>();
        	for (int i = 0; i < coins.size(); i++) {
        	    Coin c = coins.get(i);
        	    String id = "Coin_" + userId + "_" + i;
        	    String label = c.getClass().getSimpleName() + " lv." + c.getLevel() + " Rarity: " + c.getRarity();
        	    updatedButtons.add(Button.secondary(id, label));
        	}
        	event.editMessage("Select remaining coins:")
        	     .setActionRow(updatedButtons)
        	     .queue();
        }

        // Handle accept or decline buttons
        if (buttonId.startsWith("accept_coins:") || buttonId.startsWith("decline_coins:")) {
            String[] parts = buttonId.split(":");
            if (parts.length != 2) return;  // safety check
            String authorId = parts[1];
            if (!authorId.equals(userId)) return; // only allow same user

            if (buttonId.startsWith("accept_coins:")) {
            	
            	
            	if (targetVault.getVault().size() >= 10) {
                	event.reply("You have reached the maximum amount of Coins in your Vault!").setEphemeral(true).queue();
                	return;
                }
            	
            	else if ( targetVault.getVault().size()+5 > 10) {
            		event.reply("You can't add more than "+(10 -targetVault.getVault().size()) +" Coins to your Vault!" ).setEphemeral(true).queue();
            		return;
            	}
            	
            	else if (coins == null || coins.isEmpty()) {
                    event.reply("No coins to add or already processed.").setEphemeral(true).queue();
                    return;
                }
                // Save all coins to vault
                for (Coin coin : coins) {
                    vaultDAO.saveCoin(userId, guildId, coin);
                }
                event.editMessage("✅ All coins have been added to your vault!").setComponents().queue();
                return;
            } else {
            	//empty the cach
                coins = new ArrayList<Coin>();
                event.editMessage("❌ You declined all coins from the pouch.").setComponents().queue();
            }
            return;
        }

        // Handle other buttons here if needed...
    }
  
    /*
     * Override methode from JDA library
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();
        
        
        VaultDAO vaultDAO = new VaultDAO(guildId);
        Vault existingVault = vaultDAO.loadVaultForUser(userId);
        
        

        // Welcome the user in the intro channel (replace "intro-channel-id" with your channel ID)
        TextChannel introChannel = event.getGuild().getTextChannelById("1393530608277524551");
        if (introChannel != null && existingVault == null) {
            Vault vault = new Vault(guildId, userId);
            Coin starterCoin = new Zyra(Rarity.RARE, 1);
            vault.addCoin(starterCoin);            
            vaultDAO.saveVault(vault);
            introChannel.sendMessage("Welcome " + event.getUser().getAsMention() + " to the server! Here's a starter coin for you!").queue();
        }
    }
    
    /*
     * Override methode from JDA library
     */
    @Override
    public void onReady(ReadyEvent event) {
    	// Put all the guildIds in the vaultDaos just in case 
    	for (Guild guild : event.getJDA().getGuilds()) {
            String guildId = guild.getId();
            VaultDAO vaultDAO = new VaultDAO(guildId);
            vaultDAOs.put(guildId, vaultDAO);
        }
    	
    	
    	String roleMessageId = "1396839361823248557";
    	String messageId = "1394758917405278309";
        String channelId = "1394745367240769726";
        TextChannel shopChannel = event.getJDA().getTextChannelById(channelId);

        if (shopChannel != null) {
            Button xarinButton = Button.secondary("buy_xarin", "Buy Xarin Pouch");
            Button zyraButton = Button.secondary("buy_zyra", "Buy Zyra Pouch");
            Button randomButton = Button.secondary("buy_random", "Buy Random Pouch");
            Button role1 = Button.secondary("role1", "Vault Viewer");
            Button role2 = Button.secondary("role2", "Cool One");
            Button role3 = Button.secondary("role3", "Wise child");
            Button role4 = Button.secondary("role4", "Elder God");
            Button role5 = Button.secondary("role5", "God");
            Button role6 = Button.secondary("role6", "Immortal");
            Button role7 = Button.secondary("role7", "Elf");
            Button role8 = Button.secondary("role8", "Demon");
            Button role9 = Button.secondary("role9", "Dwarf");
            Button role10 = Button.secondary("role10", "Human");
            String pouchShopMessage = """
            		🔮 **Welcome, seeker, to the Arcane Pouch Emporium!**

            		Within these enchanted satchels lie treasures unknown. Select a pouch below, and may fate favor your draw:

            		🟡 **Xarin Pouch** — 100 💎𝓒  
            		> Forged in the golden forges of the Flamevault. Rich with power and rarity.

            		🟣 **Zyra Pouch** — 100 💎𝓒  
            		> Whispered into being by shadow and starlight. Mysterious and potent.

            		🎲 **Random Pouch** — 60 💎𝓒  
            		> A gamble of the gods. Will fortune bless you... or test you?

            		🛍️ **Choose wisely, and may the coins guide your destiny.**
            		""";

            
            String roleShopMessage = """
            		🏰 **Welcome, traveler, to the Hall of Titles**

            		Here you may claim powerful roles — but each title comes with a price. Prove your worth, and the realm shall recognize you.

            		---

            		🔍 **Vault Viewer** – _5x **Legendary Level 10 Xarin**, 5x **Legendary Level 10 Zyra**_  
            		> Guardians of the vaults, trusted with secrets few may hold.

            		😎 **Cool One** – _1x **Legendary Level 10 Zyra Coin**_  
            		> Only the chillest may wield this title.

            		🧠 **Wise Child** – _1x **Legendary Level 10 Xarin Coin**_  
            		> Youthful and brilliant. A rare combination.

            		👴 **Elder God** – _1x **Legendary Level 1 Xarin**, 1x **Legendary Level 1 Zyra**, 5000 💎𝓒 _  
            		> The oldest of powers... only few remember your name.

            		👑 **God** – _1x **Legendary Level 1 Xarin**, 1x **Legendary Level 1 Zyra**_  
            		> You walk among mortals, but you are far beyond.

            		♾️ **Immortal** – _1x **Legendary Level 1 Coin** (any type)_  
            		> Death has no claim on you.

            		🌿 **Elf** – _**4000** 💎𝓒_   
            		> One with nature, fast and graceful.

            		🔥 **Demon** – _**4000** 💎𝓒_ 
            		> Fire, chaos, and shadows obey your will.

            		⚒️ **Dwarf** – _**2000** 💎𝓒_ 
            		> Master of craftsmanship and strongholds.

            		🧍‍♂️ **Human** – _Free to claim_  
            		> Versatile, balanced, and ever-evolving.

            		---

            		🎯 **How to claim a role**  
            		Click the button below your chosen role. The keepers of the vault will inspect your worth.

            		✨ May your legend grow ever brighter.
            		""";

            shopChannel.retrieveMessageById(messageId).queue(message -> {
                message.editMessage(pouchShopMessage)
                       .setActionRow(xarinButton, zyraButton,randomButton)
                       .queue();  // <-- this is the only queue() needed
            });
            shopChannel.retrieveMessageById(roleMessageId).queue(message -> {
                message.editMessage(roleShopMessage) // Update the content
                       .setComponents(
                           ActionRow.of(role1, role2, role3, role4, role5),
                           ActionRow.of(role6, role7, role8, role9, role10)
                       )
                       .queue(); // Very important!
            });



            
    	    }
        
        
      
        
       //Calculate the crystaras for all memebers at the first day of the month
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(23);
        
        if (today.equals(firstDayOfMonth)) {
        	String funChannelId = "1396207168998477994";
            TextChannel funChannel = event.getJDA().getTextChannelById(funChannelId);
            String thumbsUp = "👍";
            String thumbsDown = "👎";
            if (funChannel != null) {
            	funChannel.getHistory().retrievePast(100).queue(messages -> {           		
                    for (Message message : messages) {
                    	String authorId = message.getAuthor().getId();
                        String guildId = funChannel.getGuild().getId();
                        VaultDAO vaultDAO = vaultDAOs.get(guildId);
                        List<MessageReaction> reactions = message.getReactions();
                        for (MessageReaction reaction : reactions) {
                        	String emoji = reaction.getEmoji().getFormatted();
                        	if (emoji.equals(thumbsDown) || emoji.equals(thumbsUp)) {
                                int count = emoji.equals(thumbsUp) ?  reaction.getCount()*100000: reaction.getCount();
                                vaultDAO.addCrystara(authorId, count);
                        	}
                            

                        }
                    }
                });
            }
        	
            
        }
        
        
        
      //making sure that each server has a db
        JDA jda = event.getJDA();
        for (Guild guild : jda.getGuilds()) {  
            VaultDAO dao = new VaultDAO(guild.getId());
        }

        
    }
   
    /*
     * Override methode from JDA library
     */
    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        String guildId = event.getGuild().getId();
        VaultDAO dao = new VaultDAO(guildId);
        dao.deleteDatabaseFile();  // 💣 deletes the actual SQLite file
    }
    
    /*
     * Override methode from JDA library
     */
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Member member = event.getMember();

        // User joins a voice channel
        if (event.getChannelJoined() != null) {
            AudioChannelUnion joined = event.getChannelJoined();
            System.out.println(member.getEffectiveName() + " joined voice channel: " + joined.getName() + " at " + Instant.now());
        }

        // User leaves a voice channel
        if (event.getChannelLeft() != null) {
            AudioChannelUnion left = event.getChannelLeft();
            System.out.println(member.getEffectiveName() + " left voice channel: " + left.getName() + " at " + Instant.now());
        }
    }
    
    /*
     * Just a test function 
     */
    public void say(SlashCommandInteractionEvent event, String content)
    {
        event.reply(content).queue(); // This requires no permissions!
    }

    /*
     * To make the bot leave the server 
     */
    public void leave(SlashCommandInteractionEvent event)
    {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS))
            event.reply("You do not have permissions to kick me.").setEphemeral(true).queue();
        else
            event.reply("Leaving the server... :wave:") // Yep we received it
                 .flatMap(v -> event.getGuild().leave()) // Leave server after acknowledging the command
                 .queue();
    }
    
    /*
     * To show the stats of the user(roles / memebership Periode)
     */
    public void stats(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats") || event == null) {
            return;
        }
        
        // Defer the reply to acknowledge the interaction and give yourself more time
        event.deferReply(true).queue(hook -> {
            // run the logic asynchronously inside the deferReply callback
            String type = event.getOption("type").getAsString();
            Member member = event.getOption("member").getAsMember();
            
            if (member == null) {
                hook.sendMessage("❌ Could not find that member.").queue();
                return;
            }
            
            if (type.equalsIgnoreCase("roles")) {
                List<Role> roles = member.getRoles();
                if (roles.isEmpty()) {
                    hook.sendMessage("This member has no roles.").queue();
                    return;
                }
        
                String roleList = roles.stream()
                    .map(Role::getAsMention)
                    .collect(Collectors.joining(", "));
        
                hook.sendMessage("📊 **Roles for " + member.getEffectiveName() + "**:\n" + roleList).queue();
            } else if (type.equalsIgnoreCase("membership")) {
                OffsetDateTime joinTime = member.getTimeJoined();
                Duration timeInServer = Duration.between(joinTime, OffsetDateTime.now());
                String formatted = String.format("📅 %s has been in the server for %d days.",
                    member.getEffectiveName(), timeInServer.toDays());
                hook.sendMessage(formatted).queue();
            } else {
                hook.sendMessage("❌ Unknown stat type: " + type).queue();
            }
        });
    }
    
    /*
     * Help Function for resizing a pic
     */
    private static BufferedImage resizeImageToBufferedImage(File originalImageFile, int targetWidth, int targetHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalImageFile);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /*
     * Help function to create a cached base Image for the Vault
     * It won't store the image anywhere and after sending it 
     * No file will be made or stored!
     */
    private static ByteArrayInputStream createVaultImageWithTextAboveInMemory(List<Coin> coins, String username, BufferedImage userProfilePic) throws IOException {
        int coinImgWidth = 128;
        int coinImgHeight = 128;
        int paddingBetweenCoins = 30;
        int columns = 5;
        int totalSlots = 10;

        Font titleFont = new Font("Serif", Font.BOLD, 28);
        Font coinFont = new Font("Arial", Font.PLAIN, 16);

        BufferedImage[] coinImages = new BufferedImage[totalSlots];
        String[] coinInfos = new String[totalSlots];

        for (int i = 0; i < totalSlots; i++) {
            if (i < coins.size()) {
                Coin coin = coins.get(i);
                String coinName = coin.getClass().getSimpleName();
                int level = coin.getLevel();
                String rarity = coin.getRarity().name();

                coinInfos[i] = coinName + "\nLevel: " + level + "\nRarity: " + rarity;

                String filename = coinName.toLowerCase() + "_" + rarity.toUpperCase() + ".png";
                File originalImageFile = new File("resource/" + filename);
                if (!originalImageFile.exists()) {
                    throw new IOException("Missing image file: " + filename);
                }

                coinImages[i] = resizeImageToBufferedImage(originalImageFile, coinImgWidth, coinImgHeight);
            } else {
                coinImages[i] = null;
                coinInfos[i] = null;
            }
        }

        // Calculate layout metrics
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dTemp = tempImg.createGraphics();
        g2dTemp.setFont(coinFont);
        FontMetrics fmCoin = g2dTemp.getFontMetrics();
        int coinTextHeight = 3 * fmCoin.getHeight(); // max 3 lines
        g2dTemp.setFont(titleFont);
        FontMetrics fmTitle = g2dTemp.getFontMetrics();
        int titleHeight = fmTitle.getHeight();
        g2dTemp.dispose();

        int rowCount = 2;
        int totalWidth = columns * coinImgWidth + (columns - 1) * paddingBetweenCoins;
        int totalHeight = titleHeight + rowCount * (coinImgHeight + coinTextHeight + 20) + 40;

        // Extra width to fit profile pic next to username (profile pic ~ titleHeight x titleHeight)
        int profilePicSize = titleHeight;
        totalWidth += profilePicSize + 10; // 10px padding between pic and name

        BufferedImage combined = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combined.createGraphics();

        // Background
        GradientPaint gp = new GradientPaint(0, 0, new Color(35, 35, 45), totalWidth, totalHeight, new Color(60, 60, 75));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, totalWidth, totalHeight);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw vault title and profile picture
        g2d.setFont(titleFont);
        g2d.setColor(Color.WHITE);
        String vaultTitle = username + "'s Vault";

        int titleX = profilePicSize + 10; // start text after profile pic + padding
        int titleY = titleHeight;

        // Draw profile picture as circle with smooth edges
        if (userProfilePic != null) {
            BufferedImage profilePicResized = new BufferedImage(profilePicSize, profilePicSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gPic = profilePicResized.createGraphics();
            gPic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gPic.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, profilePicSize, profilePicSize)); // circle clip
            gPic.drawImage(userProfilePic, 0, 0, profilePicSize, profilePicSize, null);
            gPic.dispose();

            g2d.drawImage(profilePicResized, 0, titleY - profilePicSize, null);
        }

        g2d.drawString(vaultTitle, titleX, titleY);

        // Coin drawing start
        g2d.setFont(coinFont);
        FontMetrics coinFm = g2d.getFontMetrics();

        int startY = titleHeight + 20;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columns; col++) {
                int i = row * columns + col;

                int x = col * (coinImgWidth + paddingBetweenCoins);
                int y = startY + row * (coinImgHeight + coinTextHeight + 20);

                if (coinImages[i] != null) {
                    g2d.drawImage(coinImages[i], x, y, null);

                    // Draw text info below image
                    String[] lines = coinInfos[i].split("\n");
                    for (int j = 0; j < lines.length; j++) {
                        String line = lines[j];
                        int textWidth = coinFm.stringWidth(line);
                        int textX = x + (coinImgWidth - textWidth) / 2;
                        int textY = y + coinImgHeight + (j + 1) * coinFm.getHeight();
                        g2d.drawString(line, textX, textY);
                    }

                } else {
                    // Draw dotted placeholder
                    Stroke oldStroke = g2d.getStroke();
                    g2d.setColor(Color.GRAY);
                    float[] dash = {6f, 6f};
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, 0f));
                    g2d.drawOval(x, y, coinImgWidth, coinImgHeight);
                    g2d.setStroke(oldStroke);

                    // Draw "No Coin" text centered
                    String noCoinText = "No Coin";
                    int textWidth = coinFm.stringWidth(noCoinText);
                    int textX = x + (coinImgWidth - textWidth) / 2;
                    int textY = y + coinImgHeight / 2 + coinFm.getAscent() / 2;
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawString(noCoinText, textX, textY);
                }
            }
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combined, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /*
     * Help function to get access to people's pfp
     */
    @SuppressWarnings("deprecation")
	private static BufferedImage loadImageFromUrl(String url) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            return ImageIO.read(in);
        }
    }


    

    /*
     * Normal side of A vault (generates a picture based of the user's Vault)
     * However the generated file won't be saved anyWhere
     */
    private void showVault(InteractionHook channel, Vault vault, String username, String avatarUrl) {
        List<Coin> coins = vault.getVault();

        if (coins.isEmpty()) {
            channel.sendMessage("Your vault is empty.").setEphemeral(true).queue();
            return;
        }

        try {
            BufferedImage profilePic = loadImageFromUrl(avatarUrl);

            ByteArrayInputStream imageStream = createVaultImageWithTextAboveInMemory(coins, username, profilePic);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setImage("attachment://vault.png");

            channel.sendMessageEmbeds(embed.build())
                   .addFiles(FileUpload.fromData(imageStream, "vault.png"))
                   .queue();

        } catch (IOException e) {
            channel.sendMessage("❌ Failed to generate vault image.").setEphemeral(true).queue();
            e.printStackTrace();
        }
    }


  
    /*
     * To get the Coin id
     * Only with permission!
     */
    private String formatVaultAdmin(Vault vault) {
        List<Coin> coins = vault.getVault();
        if (coins.isEmpty()) {
            return "Your vault is empty.";
        }

        StringBuilder sb = new StringBuilder("");
        for (Coin coin : coins) {
            sb.append("- ")
              .append(coin.getClass().getSimpleName())   // Coin type
              .append(" | Id: ").append(coin.getCoinId())
              .append(" | Level: ").append(coin.getLevel())
              .append(" | Rarity: ").append(coin.getRarity().name())
              .append("\n");
        }
        return sb.toString();
    }
    
    
}
