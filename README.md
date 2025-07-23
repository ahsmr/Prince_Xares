# Prince_Xares 🤖

**Prince_Xares** is a multi-feature Discord bot built using [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA). It introduces an interactive collectible coin system called **Vault**, with powerful administrative tools, statistics tracking, and custom image generation — all backed by SQLite.

## ✨ Features

- 🧠 Slash Commands with rich options
- 💰 Vault System: collectible coins with rarity, levels, and graphical display
- 🎨 Dynamic vault image generation with profile avatars
- 📊 Role and Membership Statistics
- 🛡 Server-specific permission checks
- 🗃 SQLite DB per-server (automatically cleaned on leave)

## 🛠 Commands Overview

| Command      | Description |
|-------------|-------------|
| `/say`       | Bot repeats your message (admin only) |
| `/leave`     | Forces bot to leave the server |
| `/stats`     | Shows member roles or membership duration |
| `/vault`     | Manage/view your coin vault |
| `/add`       | Admin-only coin injection |
| `/crystara`  | Get Crystara (WIP/Secret) |


| Subcommand   | Description                                                                                  |
|--------------|----------------------------------------------------------------------------------------------|
| `view`       | View your vault as an image showing up to 10 coins with their levels and rarities.          |
| `levelup`    | Level up a specific coin in your vault if you have enough Crystara.                         |
| `adminview`  | (Admin only + Owner of the Vault) View detailed vault info of any user, including internal coin IDs.             |
| `remove`     | Remove a coin from your vault by specifying its Coin ID.                                    |
| `clear`      | Clear your entire vault (can be restricted to admins or the vault owner).                   |
### Crystara_usage for upgrading each Coin:
![crystara_usage](https://github.com/user-attachments/assets/1d4b5b43-13da-44ec-bc89-94b1d1bdb7d7)


    
### Shop Feature
- The bot includes a **shop system** where admins can select a channel for the shop.
- The shop automatically creates **two messages**:
  1. **Pouch Shop** — Users can buy pouches using **Crystara** currency.
    - Visual Example : <img width="1102" height="514" alt="image" src="https://github.com/user-attachments/assets/0ae4ea72-7779-4eae-97ab-49003996f1f9" />

  2. **Role Shop** — Users can purchase roles using either **Crystara** or **coins**.
      - Visual Example :
        <img width="1099" height="816" alt="image" src="https://github.com/user-attachments/assets/962a1179-322e-440e-8e28-8a00c640a849" />
        <img width="1101" height="609" alt="image" src="https://github.com/user-attachments/assets/67f31e1e-04d0-439c-81ff-a6c79761c332" />



        
- This setup allows users to interact with the shop messages to make purchases directly.

### Notes:
- Some subcommands require additional parameters such as:
  - `user` — Target another user’s vault (admin permissions required).
  - `id` — Specify the coin ID for actions like level up or removal.
  - `view` generates a dynamic image showing coins with stats and the user’s profile picture.


## 🖼 Vault System

Vaults are represented as in-memory image files generated dynamically using `BufferedImage`. No file writes or persistent image caching are used — everything is built and sent in real-time with profile pictures and layout logic.(All the images are AI generated)

Example output (visualized):
<img width="1007" height="617" alt="image" src="https://github.com/user-attachments/assets/1aae8253-0e65-4913-98c8-12c715f86207" />



## To install the bot in your server:
Click on [Install](https://discord.com/oauth2/authorize?client_id=1389683347420221714)

### 📄 `LICENSE` (MIT with attribution clause)

```txt
MIT License

Copyright (c) 2025 ahsmr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED...

Attribution:
This project includes original work by **ahsmr**. If used, reused, or modified (publicly or privately), you must:

- Credit the original author: `ahsmr`
- Provide a link to the original repository: https://github.com/ahsmr/Prince_Xares

This project also makes use of the [JDA library](https://github.com/DV8FromTheWorld/JDA), licensed under the Apache License 2.0. Please ensure you comply with its terms as well.
