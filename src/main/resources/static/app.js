const buttonsEl  = document.getElementById('error-buttons');
const loadingEl  = document.getElementById('loading');
const errorMsgEl = document.getElementById('error-msg');
const panelsEl   = document.getElementById('panels');

async function loadButtons() {
  const manifest = await fetch('/errors/manifest.json').then(r => r.json());
  manifest.forEach(entry => {
    const btn = document.createElement('button');
    btn.textContent = entry.label;
    btn.dataset.name = entry.name;
    btn.addEventListener('click', () => handleClick(entry.name));
    buttonsEl.appendChild(btn);
  });
}

async function handleClick(name) {
  loadingEl.classList.remove('hidden');
  errorMsgEl.classList.add('hidden');
  panelsEl.classList.add('hidden');

  const [sourceCode, errorOutput] = await Promise.all([
    fetch(`/errors/${name}.java`).then(r => r.text()),
    fetch(`/errors/${name}.error.txt`).then(r => r.text()),
  ]);

  document.getElementById('source-code').textContent  = sourceCode;
  document.getElementById('error-output').textContent = errorOutput;

  let res;
  try {
    res = await fetch('/api/explain-error', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceCode, errorOutput }),
    });
  } catch (err) {
    showError(`Network error: ${err.message}`);
    return;
  }

  loadingEl.classList.add('hidden');

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    showError(`HTTP ${res.status}${body.error ? ` — ${body.error}` : ''}`);
    return;
  }

  const data = await res.json();
  document.getElementById('llm-error-type').textContent  = data.errorType;
  document.getElementById('llm-line-number').textContent = data.lineNumber === 0 ? 'unknown' : data.lineNumber;
  document.getElementById('llm-explanation').textContent = data.plainExplanation;
  document.getElementById('llm-suggestion').textContent  = data.suggestion;
  panelsEl.classList.remove('hidden');
}

function showError(msg) {
  loadingEl.classList.add('hidden');
  errorMsgEl.textContent = msg;
  errorMsgEl.classList.remove('hidden');
  panelsEl.classList.remove('hidden');
}

loadButtons();
