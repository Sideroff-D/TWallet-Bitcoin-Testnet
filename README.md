# TWalletBTC - Bitcoin Testnet Wallet (Spring Boot)

TWalletBTC is a Bitcoin Testnet wallet web app built with Java, Spring Boot, Thymeleaf, and BitcoinJ.

---

## 🔗 Real Blockchain Integration (Bitcoin Testnet)

TWalletBTC is not a simulation — it connects directly to the **Bitcoin Testnet blockchain** using public APIs from [mempool.space](https://mempool.space/testnet).

- All wallet addresses are real testnet SegWit addresses (`tb1...`)
- The app pulls live data from the mempool and testnet blockchain
- You can send and receive actual **testnet BTC (tBTC)**
- Compatible with wallets like **Blockstream Green (in testnet mode)**

This is ideal for demonstrating real blockchain behavior using safe, non-production coins.

## Demo Video

[![Watch the video](https://img.youtube.com/vi/4O8KRD9FNmU/maxresdefault.jpg)](https://www.youtube.com/watch?v=4O8KRD9FNmU)

---

## Features

- Generate Bitcoin testnet SegWit address (tb1...)
- View balance 
- View incoming/outgoing transactions
- Auto-refresh for balance and TX list every 10 seconds
- Pending transactions shown in orange
- QR code for receiving
- Simple send-to-address functionality



---

## Requirements

- Java 17+
- Maven 3.6+
- Internet connection (to access testnet data via mempool.space API)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Sideroff-D/TWallet-Bitcoin-Testnet.git
cd TWallet-Bitcoin-Testnet
```

### 2. Configure Wallet

No configuration is strictly required.  
If no address or private key is provided, the app will automatically generate a **new wallet** each time it starts.

#### Optional: Use system environment variables

You can set these before running the app to use a specific wallet:

```bash
export LIQUID_ADDRESS=tb1q...your_testnet_address
export LIQUID_PRIVATE_KEY=your_base64_encoded_private_key
```

### 3. Run the app

```bash
./mvnw spring-boot:run
# or if Maven is installed globally:
mvn spring-boot:run
```

App will be available at:

```
http://localhost:8080
```

---

## Routes and Pages

### 🏠 Main Flow:

- `GET /` → Landing page with "Create New Wallet" or "Restore Wallet"
- `GET /setup` → Generates and displays a new mnemonic
- `POST /setup` → Accepts and stores generated seed
- `GET /restore` → Shows restore form
- `POST /restore` → Accepts input mnemonic and restores wallet
- `GET /home` → Main dashboard after setup/restore

### 📡 AJAX API Endpoints:

- `GET /api/balance` → Returns current balance as JSON
- `GET /api/transactions` → Returns latest TXs as Thymeleaf fragment or JSON

---

## UI Features and Behavior

- Responsive dashboard layout (`.dashboard-grid`)
- Panels:
  - Wallet Info: balance, address, copy button
  - Send Panel: to-address + amount input
  - Transactions List: incoming (green), outgoing (red), pending (orange)
- Sticky header with "TWallet Testnet" title and nav links
- Card styling using CSS variables
- SVG icons from `/static/icons/*.svg`
- Clipboard support for address copy
- Faucet links to get testnet coins

---

## Frontend Auto-Refresh

### update.js:

- Polls `/api/balance` and `/api/transactions` every 5 seconds
- Updates DOM with new balance and TXs (no full reload)
- Pending TXs are auto-updated when confirmed

---

## Technologies

- Spring Boot
- Thymeleaf
- BitcoinJ
- ZXing (QR code)
- [mempool.space](https://mempool.space/testnet) API

---

