package DAO;

import java.io.File;
import java.sql.*;
import java.util.*;

import Currency.Coin;
import Currency.GuildInfo;
import Currency.Rarity;
import Currency.Vault;
import Currency.Xarin;
import Currency.Zyra;

public class VaultDAO {
    private final String dbPath;
    private final String guildId;
    
    public VaultDAO(String guildId) {
    	this.guildId = guildId;
        this.dbPath = "data/server_" + guildId + ".db";  // separate DB per server
        
        createTableIfNotExists();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void createTableIfNotExists() {
        String coinSql = """
            CREATE TABLE IF NOT EXISTS coins (
                coin_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                coin_type TEXT NOT NULL,
                level INTEGER NOT NULL,
                rarity TEXT NOT NULL
            );
        """;

        String crystaraSql = """
            CREATE TABLE IF NOT EXISTS crystara (
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                amount INTEGER NOT NULL,
                PRIMARY KEY (user_id, guild_id)
            );
        """;
        

        String guildInfoSql = """
            CREATE TABLE IF NOT EXISTS guild_info (
                shop_channel_id TEXT NOT NULL,
                intro_channel_id TEXT NOT NULL,
                role1 TEXT,
                role2 TEXT,
                role3 TEXT,
                role4 TEXT,
                role5 TEXT,
                role6 TEXT,
                role7 TEXT,
                role8 TEXT,
                role9 TEXT,
                role10 TEXT
            );
        """; 

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(coinSql);
            stmt.execute(crystaraSql);
            stmt.execute(guildInfoSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public void setGuildInfo(String shopChannelId, String introChannelId, List<String> roleIds) {
        String sql = """
            INSERT INTO guild_info (
                guild_id, shop_channel_id, intro_channel_id,
                role1, role2, role3, role4, role5,
                role6, role7, role8, role9, role10
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(guild_id) DO UPDATE SET
                shop_channel_id = excluded.shop_channel_id,
                intro_channel_id = excluded.intro_channel_id,
                role1 = excluded.role1, role2 = excluded.role2, role3 = excluded.role3,
                role4 = excluded.role4, role5 = excluded.role5, role6 = excluded.role6,
                role7 = excluded.role7, role8 = excluded.role8, role9 = excluded.role9,
                role10 = excluded.role10;
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guildId);
            pstmt.setString(2, shopChannelId);
            pstmt.setString(3, introChannelId);

            // Fill in up to 10 role IDs (null if missing)
            for (int i = 0; i < 10; i++) {
                pstmt.setString(4 + i, i < roleIds.size() ? roleIds.get(i) : null);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Optional<GuildInfo> getGuildInfo() {
        String sql = "SELECT * FROM guild_info";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String shopChannelId = rs.getString("shop_channel_id");
                String introChannelId = rs.getString("intro_channel_id");

                List<String> roles = new ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    String roleId = rs.getString("role" + i);
                    if (roleId != null) roles.add(roleId);
                }

                return Optional.of(new GuildInfo(guildId, shopChannelId, introChannelId, roles));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    
        
    public int getCrystara(String userId) {
        String sql = "SELECT amount FROM crystara WHERE user_id = ? AND guild_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public void setCrystara(String userId, int amount) {
        String sql = """
            INSERT INTO crystara (user_id, guild_id, amount)
            VALUES (?, ?, ?)
            ON CONFLICT(user_id, guild_id) DO UPDATE SET amount = excluded.amount
        """;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addCrystara(String userId, int delta) {
        int current = getCrystara(userId);
        setCrystara(userId, current + delta);
    }
    
    public void removeCrystara(String userId, int delta) {
    	int current = getCrystara(userId);
    	setCrystara(userId,current - delta);
    }

    public void saveCoin(String userId, String guildId, Coin coin) {
        String sql = """
            INSERT INTO coins (coin_id, user_id, guild_id, coin_type, level, rarity)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String newId = UUID.randomUUID().toString();
            pstmt.setString(1, newId);
            pstmt.setString(2, userId);
            pstmt.setString(3, guildId);
            pstmt.setString(4, coin.getClass().getSimpleName());
            pstmt.setInt(5, coin.getLevel());
            pstmt.setString(6, coin.getRarity().name());
            pstmt.executeUpdate();
            
            coin.setCoinId(newId); // <-- Set the ID on the Coin instance
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Coin> loadCoins(String userId, String guildId) {
        List<Coin> coins = new ArrayList<>();
        String sql = "SELECT coin_id, coin_type, level, rarity FROM coins WHERE user_id = ? AND guild_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String coinId = rs.getString("coin_id");
                String type = rs.getString("coin_type");
                int level = rs.getInt("level");
                String rarityStr = rs.getString("rarity");
                Rarity rarity = Rarity.valueOf(rarityStr);

                Coin coin = null;
                if ("Zyra".equals(type)) {
                    coin = new Zyra(rarity, level);
                } else if ("Xarin".equals(type)) {
                    coin = new Xarin(rarity, level);
                }

                if (coin != null) {
                    coin.setCoinId(coinId);  // assign DB ID
                    coins.add(coin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coins;
    }
    
    public void removeCoin(String coinId) {
        String sql = "DELETE FROM coins WHERE coin_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, coinId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Vault loadVaultForUser(String userId) {
        Vault vault = new Vault(guildId, userId);  // Vault constructor loads coins from DB already
        // Alternatively, if Vault doesn't load automatically, you can load coins here:
        // List<Coin> coins = loadCoins(userId, guildId);
        // vault.setVault(coins); // you’d need a setter or constructor to set vault manually
        return vault;
    }

    public void saveVault(Vault vault) {
        for (Coin coin : vault.getVault()) {
            saveOrUpdateCoin(vault.getUserId(), vault.getGuildId(), coin);
        }
    }
    
    public void saveOrUpdateCoin(String userId, String guildId, Coin coin) {
        // If coin has an ID, try updating, else insert new
        if (coin.getCoinId() == null) {
            // Insert new coin
            String insertSql = """
                INSERT INTO coins (coin_id, user_id, guild_id, coin_type, level, rarity)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                String newId = UUID.randomUUID().toString();
                coin.setCoinId(newId);
                pstmt.setString(1, newId);
                pstmt.setString(2, userId);
                pstmt.setString(3, guildId);
                pstmt.setString(4, coin.getClass().getSimpleName());
                pstmt.setInt(5, coin.getLevel());
                pstmt.setString(6, coin.getRarity().name());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update existing coin
            String updateSql = """
                UPDATE coins
                SET level = ?, rarity = ?
                WHERE coin_id = ?
                """;
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, coin.getLevel());
                pstmt.setString(2, coin.getRarity().name());
                pstmt.setString(3, coin.getCoinId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    // Coin ID not found, insert instead
                    saveOrUpdateCoin(userId, guildId, coin);  // recursive call, but now coinId is set, so insert
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public void deleteAllCoinsForUser(String userId) {
        String sql = "DELETE FROM coins WHERE user_id = ? AND guild_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
    public void deleteDatabaseFile() {
        File dbFile = new File(dbPath);
        System.out.println("Attempting to delete: " + dbFile.getAbsolutePath());

        if (dbFile.exists()) {
            if (dbFile.delete()) {
                System.out.println("Deleted DB file for guild: " + guildId);
            } else {
                System.err.println("Failed to delete DB file (file may still be in use)");
            }
        } else {
            System.err.println("DB file does not exist at: " + dbPath);
        }
    }

    


}
