package Currency;

public class Xarin extends Coin {
		
	public Xarin(Rarity rarity,int level) {
		super(rarity, level);
	}

	@Override
	public String generalInfo() {
		return "✨ Xarin Coins (Rare)\n"
			    + "- Xarin coins are rare and special.\n"
			    + "- You can earn 1 Xarin coin per hour by being online with your camera on in voice channels.\n"
			    + "- Coins are calculated and awarded when you leave the channel, based on how long you stayed with your cam on.\n"
			    + "- This encourages active participation and engagement with the community.\n"
			    + "- Keep your cam on while hanging out to earn these rare coins!";
	}


}
