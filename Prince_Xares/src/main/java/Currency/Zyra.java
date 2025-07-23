package Currency;

public class Zyra extends Coin{
	
	
	
	
	public Zyra(Rarity rarity,int level) {
		super(rarity,level);
	}

	@Override
	public String generalInfo() {
		return "💎 Zyra Coins (Common)\n"
			    + "- Zyra coins are more common and easy to earn.\n"
			    + "- You automatically receive 1 Zyra coin every 12 hours simply by being a member and online in the server at least once during that period.\n"
			    + "- Just pop in and stay active to collect your daily Zyra coins!";
	}
	
}
