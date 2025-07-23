package Shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Currency.Coin;
import Currency.Rarity;
import Currency.Xarin;
import Currency.Zyra;

import java.util.Map;
import java.util.Random;

public class Pouch {
    private final List<Coin> coins = new ArrayList<>();
    
    
    /*
     * Random generator of an specific type of coin (Xarin / Zyra)
     */
    public Pouch(int amount,boolean isXarin) {
        Random random = new Random();

        // Define chances out of 100 for rarities
        Map<Rarity, Integer> rarityChances = Map.of(
            Rarity.COMMON, 60,
            Rarity.RARE, 25,
            Rarity.EPIC, 15
        );

        // Define chances out of 100 for levels
        Map<Integer, Integer> levelChances = Map.of(
            1, 20,
            2, 18,
            3, 15,
            4, 12,
            5, 10,
            6, 8,
            7, 6,
            8, 5,
            9, 4,
            10, 2
        );

        for (int i = 0; i < amount; i++) {
            Rarity rarity = pickWeighted(rarityChances, random);
            int level = pickWeighted(levelChances, random);
            Coin coin = isXarin ? new Xarin(rarity, level) : new Zyra(rarity, level);
            coins.add(coin);
        }
    }

    
    /*
     * Randome Coin Generator of 2 types(Xarin / Zyra) and 4 rarities(common,rare,epic,legendary) and 10 levels([1,10])
     */
    public Pouch(int amount) {
    	Random random = new Random();
    	// Define chances out of 100 for rarities
        Map<Rarity, Integer> rarityChances = Map.of(
            Rarity.COMMON, 60,
            Rarity.RARE, 25,
            Rarity.EPIC, 15
        );

        // Define chances out of 100 for levels
        Map<Integer, Integer> levelChances = Map.of(
            1, 20,
            2, 18,
            3, 15,
            4, 12,
            5, 10,
            6, 8,
            7, 6,
            8, 5,
            9, 4,
            10, 2
        );
        
        for (int i = 0; i < amount; i++) {
        	boolean isXarin = random.nextBoolean();
            Rarity rarity = pickWeighted(rarityChances, random);
            int level = pickWeighted(levelChances, random);
            Coin coin = isXarin ? new Xarin(rarity, level) : new Zyra(rarity, level);
            coins.add(coin);
        }
    }
    
    
    /*
     * Helper Function for generating random stuffs based of their Weights
     */
    private <T> T pickWeighted(Map<T, Integer> chances, Random random) {
        int total = chances.values().stream().mapToInt(Integer::intValue).sum();
        int r = random.nextInt(total);
        int cumulative = 0;
        for (Map.Entry<T, Integer> entry : chances.entrySet()) {
            cumulative += entry.getValue();
            if (r < cumulative) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Should never happen if chances map is not empty");
    }
    
    
    /*
     * Retruns an unmodifiable List of coins 
     * In case of need to change assign it to another Variabel!
     */
    public List<Coin> getCoins() {
        return Collections.unmodifiableList(coins);
    }
}
