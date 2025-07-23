package Currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.VaultDAO;


public class Vault {
    private List<Coin> vault = new ArrayList<>();
    private VaultDAO dao;
    private String guildId;
    private String userId;

    public Vault(String guildId, String userId) {
        this.guildId = guildId;
        this.userId = userId;
        this.dao = new VaultDAO(guildId);
        loadVault();
    }
    
    
    public String getUserId() {
    	return userId;
    }
    
    public String getGuildId() {
    	return guildId;
    }

    private void loadVault() {
        vault = dao.loadCoins(userId, guildId);
        for (Coin coin : vault) {
            coin.vault = this;
        }
    }

    public void addCoin(Coin coin) {
        if (!vault.contains(coin)) {
            vault.add(coin);
            coin.vault = this;
            dao.saveCoin(userId, guildId, coin);
        }
    }
    
    public void removeCoin(String coinId) {
        // First, find the coin in the vault
        Coin toRemove = null;
        for (Coin coin : vault) {
            if (coin.getCoinId().equals(coinId)) {
                toRemove = coin;
                break;
            }
        }

        // If found, remove from vault and database
        if (toRemove != null) {
            vault.remove(toRemove);
            dao.removeCoin(coinId);
        }
    }
    
    public void removeAllCoinsFrom(String userId) {
    	vault.clear();
    	dao.deleteAllCoinsForUser(userId);
    }


    public List<Coin> getVault() {
        return Collections.unmodifiableList(vault);
    }

    public List<Zyra> getZyras() {
        List<Zyra> zyras = new ArrayList<>();
        for (Coin c : vault) {
            if (c instanceof Zyra) zyras.add((Zyra) c);
        }
        return Collections.unmodifiableList(zyras);
    }

    public List<Xarin> getXarins() {
        List<Xarin> xarins = new ArrayList<>();
        for (Coin c : vault) {
            if (c instanceof Xarin) xarins.add((Xarin) c);
        }
        return Collections.unmodifiableList(xarins);
    }
    
    public void saveDirtyCoins() {
        for (Coin coin : vault) {
            if (coin.isDirty()) {
                dao.saveOrUpdateCoin(userId, guildId, coin);
                coin.markClean();
            }
        }
    }

    
    
}
