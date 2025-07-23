package Currency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public abstract class Coin {
	protected String coinId;  // add this field
	boolean dirty = false;
	Rarity rarity;
	int level;
	Vault vault = null;
	public Coin(Rarity rarity, int level) {
		
		if (Arrays.stream(Rarity.values()).noneMatch(r -> r.equals(rarity))) {
		    throw new IllegalArgumentException("Rarity "+rarity+" is Impossible.");
		}
		if (level < 0 || level > 10) {
			throw new IllegalArgumentException("Level "+level+" exceeds the expectation.");
		}
		this.rarity = rarity;
		this.level = level;
	}
	
	public String getCoinId() {
        return coinId;
    }
	
	 public void setCoinId(String coinId) {
	        this.coinId = coinId;
	    }
	
	public String getCoinStat() {
		String type;
		if (this instanceof Xarin) {
			type = "Xarin";
		}
		else if (this instanceof Zyra) {
			type = "Zyra";
		}
		else {
			throw new IllegalArgumentException("This coin is neither Xarin nor Zyra!");
		}
		
		return "Type: "+type+"\n"
			  +"Rarity: "+rarity+"\n"
			  +"Level: "+level;
	}
	
	
	public abstract String generalInfo();
	
	public Rarity getRarity() {
		return rarity;
	}
	
	public int getLevel() {
		return level;
	}
	
	
	/*
	 * 
	 * @return null if this coin doesn't belong to any Vault
	 */
	public Vault getVault() {
		return vault;
	}
	


	public boolean isDirty() {
	    return dirty;
	}

	public void markClean() {
	    this.dirty = false;
	}

	/*
	 * @pre | getLevel() <=10
	 */
	public  void levelUp() {
		if (level <= 10) {
			level++;
			this.dirty = true;
		}
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	/*
	 * @pre | getRarity() != Rarity.LEGENDARY
	 */
	public void gradeUp() {
		int ordinal = rarity.ordinal();
		if (ordinal < Rarity.values().length-1) {
			rarity = Rarity.values()[ordinal+1] ;
			this.dirty = true;
			
		}
	}
}
