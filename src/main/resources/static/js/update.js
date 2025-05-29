// update.js

// Handle copy button and start polling
function setupCopy() {
  const btn = document.querySelector('.copy-btn');
  const code = document.getElementById('address-code');
  if (!btn || !code) return;
  const original = btn.textContent;
  btn.addEventListener('click', () => {
    navigator.clipboard.writeText(code.innerText)
      .then(() => {
        btn.textContent = 'Copied';
        setTimeout(() => btn.textContent = original, 2000);
      });
  });
}

// Renders one <li> for a given tx object, using SVG icons
function renderItem(tx) {
  const li = document.createElement('li');
  li.className = !tx.confirmed
    ? 'pending'
    : (tx.incoming ? 'received' : 'sent');

  const iconName = tx.incoming ? 'receive' : 'send';
  const altText  = tx.incoming ? 'Received' : 'Sent';

  li.innerHTML = `
    <div class="tx-left">
      <img class="tx-icon" src="/icons/${iconName}.svg" alt="${altText} Icon"/>
      <div class="tx-info">
        <span class="tx-label">${altText}</span>
        <a href="https://mempool.space/testnet/tx/${tx.txid}"
           target="_blank"
           class="tx-id-link">${tx.txid}</a>
      </div>
    </div>
    <div class="tx-right">
      <span class="tx-amount">${
        tx.incoming ? tx.change + ' TEST' : tx.sent + ' TEST'
      }</span>
      ${!tx.incoming ? `<span class="tx-fee">Fee: ${tx.fee} sats</span>` : ''}
      <span class="tx-badge">${
        tx.confirmed ? 'Standard' : 'Pending'
      }</span>
    </div>
  `;
  return li;
}

// Fetch & re-render every 5 seconds
async function update() {
  try {
    const [txRes, balRes] = await Promise.all([
      fetch('/api/transactions'),
      fetch('/api/balance')
    ]);
    const txs     = await txRes.json();
    const { balance } = await balRes.json();

    // Update balance
    document.getElementById('balance').textContent = balance;

    // Rebuild list (with SVG icons & mempool links)
    const ul = document.querySelector('.tx-list');
    ul.innerHTML = '';
    txs.forEach(tx => ul.appendChild(renderItem(tx)));
  } catch (e) {
    console.error('Failed to refresh data', e);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  setupCopy();
  update();
  setInterval(update, 5000);
});
