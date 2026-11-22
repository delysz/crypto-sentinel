/* ══════════════════════════════════════════════════════════════
   Crypto Sentinel — Portfolio dashboard (client-side)
   Consume /api/portfolio/** y /api/alerts vía fetch().
   Requiere Chart.js (ya cargado en index.html).
   ══════════════════════════════════════════════════════════════ */
(function () {
    'use strict';

    const root = document.getElementById('portfolioSection');
    if (!root) return; // sección no renderizada (usuario no autenticado)

    /* ── Formatters ─────────────────────────────────────────── */
    const LOCALE = 'es-ES';
    const CURRENCY = root.dataset.currency || 'USD';
    const fmtMoney = new Intl.NumberFormat(LOCALE, { style: 'currency', currency: CURRENCY, minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const fmtPrice = new Intl.NumberFormat(LOCALE, { style: 'currency', currency: CURRENCY, minimumFractionDigits: 2, maximumFractionDigits: 6 });
    const fmtQty = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 8 });
    const fmtPct = new Intl.NumberFormat(LOCALE, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const fmtDate = new Intl.DateTimeFormat(LOCALE, { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    const fmtDay = new Intl.DateTimeFormat(LOCALE, { day: '2-digit', month: 'short' });

    const isNum = v => typeof v === 'number' && isFinite(v);
    const money = v => isNum(v) ? fmtMoney.format(v) : '—';
    const price = v => isNum(v) ? fmtPrice.format(v) : '—';
    const qty = v => isNum(v) ? fmtQty.format(v) : '—';
    const pct = v => isNum(v) ? (v > 0 ? '+' : '') + fmtPct.format(v) + ' %' : '—';
    const signedMoney = v => isNum(v) ? (v > 0 ? '+' : '') + fmtMoney.format(v) : '—';
    const plClass = v => !isNum(v) || v === 0 ? 'pf-neutral' : (v > 0 ? 'pf-pos' : 'pf-neg');

    function fmtDateTime(v) {
        if (!v) return '—';
        const d = new Date(v);
        return isNaN(d) ? String(v) : fmtDate.format(d);
    }

    function esc(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    /* Accepts "1,5" or "1.5" */
    function parseNum(str) {
        if (str == null) return NaN;
        const s = String(str).trim().replace(/\s/g, '').replace(',', '.');
        if (s === '') return NaN;
        return Number(s);
    }

    /* ── HTTP helper ────────────────────────────────────────── */
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    class ApiError extends Error {
        constructor(status, message) { super(message); this.status = status; }
    }

    async function api(url, opts = {}) {
        const method = (opts.method || 'GET').toUpperCase();
        const headers = { 'Accept': 'application/json' };
        if (opts.body !== undefined) headers['Content-Type'] = 'application/json';
        if (method !== 'GET' && csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        let res;
        try {
            res = await fetch(url, {
                method,
                headers,
                credentials: 'same-origin',
                body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined
            });
        } catch (e) {
            throw new ApiError(0, 'No se pudo conectar con el servidor');
        }

        // Sesión caducada → Spring redirige al formulario de login (HTML)
        if (res.redirected && /\/login/.test(res.url)) {
            throw new ApiError(401, 'Sesión caducada. Vuelve a iniciar sesión.');
        }

        const ct = res.headers.get('content-type') || '';
        let data = null;
        if (res.status !== 204) {
            const text = await res.text();
            if (text && ct.includes('json')) {
                try { data = JSON.parse(text); } catch (_) { data = null; }
            } else if (text && !res.ok) {
                data = null;
            } else if (text && res.ok && !ct.includes('json')) {
                // 200 con HTML (p.ej. página de login) → no es la API esperada
                throw new ApiError(res.status, 'Respuesta inesperada del servidor');
            }
        }

        if (!res.ok) {
            // El cuerpo de error por defecto de Spring Boot ({timestamp, status, error, path}) no es
            // un mensaje útil para el usuario → usar texto genérico en ese caso.
            const springDefault = data && data.timestamp !== undefined && data.path !== undefined;
            const msg = (!springDefault && data && (data.error || data.message)) || defaultError(res.status);
            throw new ApiError(res.status, msg);
        }
        return { status: res.status, data };
    }

    function defaultError(status) {
        switch (status) {
            case 400: return 'Datos no válidos';
            case 401: return 'No autenticado';
            case 403: return 'Acceso denegado';
            case 404: return 'Recurso no encontrado';
            default: return status >= 500 ? 'Error interno del servidor (' + status + ')' : 'Error ' + status;
        }
    }

    /* ── Toast / flash ──────────────────────────────────────── */
    const flashBox = document.getElementById('pfFlash');
    let flashTimer;
    function flash(msg, type = 'ok') {
        if (!flashBox) return;
        flashBox.textContent = msg;
        flashBox.className = 'pf-flash show ' + (type === 'error' ? 'pf-flash-err' : 'pf-flash-ok');
        clearTimeout(flashTimer);
        flashTimer = setTimeout(() => { flashBox.className = 'pf-flash'; }, 4000);
    }

    /* ── Chart defaults (estética HUD) ──────────────────────── */
    const COLORS = { crypto: '#00ffe7', etf: '#7b2fff', stock: '#ffd200', total: '#00ffe7' };
    const TYPE_LABEL = { CRYPTO: 'Cripto', ETF: 'ETF', STOCK: 'Acción' };
    const gridColor = 'rgba(0,255,231,.07)';
    const tickColor = 'rgba(200,216,240,.45)';
    const tickFont = { family: "'JetBrains Mono', monospace", size: 10 };

    let distChart = null;
    let perfChart = null;

    function showEmpty(canvasId, emptyId, empty) {
        const c = document.getElementById(canvasId);
        const e = document.getElementById(emptyId);
        if (c) c.parentElement.style.display = empty ? 'none' : '';
        if (e) e.style.display = empty ? '' : 'none';
    }

    /* ══ 1. SUMMARY / KPIs ═══════════════════════════════════ */
    async function loadSummary() {
        const setText = (id, txt, cls) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.textContent = txt;
            if (cls !== undefined) el.className = 'pf-kpi-value ' + cls;
        };
        try {
            const { data: s } = await api('/api/portfolio/summary');
            if (!s) throw new ApiError(0, 'Sin datos');
            setText('kpiTotal', money(s.totalValue));
            setText('kpiInvested', money(s.totalInvested));
            setText('kpiPL', signedMoney(s.profitLoss), plClass(s.profitLoss));
            const plPct = document.getElementById('kpiPLPct');
            if (plPct) {
                plPct.textContent = pct(s.profitLossPercentage);
                plPct.className = 'pf-kpi-sub ' + plClass(s.profitLossPercentage);
            }
            renderDistribution(s);
        } catch (e) {
            ['kpiTotal', 'kpiInvested', 'kpiPL'].forEach(id => setText(id, '—', ''));
            const plPct = document.getElementById('kpiPLPct');
            if (plPct) { plPct.textContent = 'No disponible'; plPct.className = 'pf-kpi-sub'; }
            renderDistribution(null);
            console.warn('[portfolio] summary:', e.message);
        }
    }

    /* ══ 2a. DISTRIBUTION DOUGHNUT ═══════════════════════════ */
    function renderDistribution(s) {
        const vals = s ? [s.cryptoValue, s.etfValue, s.stockValue].map(v => isNum(v) && v > 0 ? v : 0) : [0, 0, 0];
        const total = vals.reduce((a, b) => a + b, 0);
        const empty = total <= 0;
        showEmpty('distChart', 'distEmpty', empty);
        if (empty) { if (distChart) { distChart.destroy(); distChart = null; } return; }

        const labels = ['Cripto', 'ETF', 'Acciones'];
        const colors = [COLORS.crypto, COLORS.etf, COLORS.stock];
        if (distChart) {
            distChart.data.datasets[0].data = vals;
            distChart.update();
            return;
        }
        distChart = new Chart(document.getElementById('distChart'), {
            type: 'doughnut',
            data: {
                labels,
                datasets: [{
                    data: vals,
                    backgroundColor: colors.map(c => c + '55'),
                    borderColor: colors,
                    borderWidth: 1.5,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '62%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: tickColor, font: tickFont, boxWidth: 10, padding: 14 }
                    },
                    tooltip: {
                        callbacks: {
                            label: ctx => {
                                const t = ctx.dataset.data.reduce((a, b) => a + b, 0);
                                const p = t > 0 ? ctx.parsed / t * 100 : 0;
                                return ` ${ctx.label}: ${money(ctx.parsed)} (${fmtPct.format(p)} %)`;
                            }
                        }
                    }
                }
            }
        });
    }

    /* ══ 2b. PERFORMANCE HISTORY LINE ════════════════════════ */
    async function loadPerformance(days) {
        let rows = [];
        let errMsg = null;
        try {
            const { data } = await api('/api/portfolio/performance/history?days=' + encodeURIComponent(days));
            rows = Array.isArray(data) ? data.filter(r => r && r.date) : [];
        } catch (e) {
            errMsg = e.message;
            console.warn('[portfolio] performance:', e.message);
        }
        rows.sort((a, b) => String(a.date).localeCompare(String(b.date)));

        const emptyEl = document.getElementById('perfEmpty');
        if (emptyEl) {
            emptyEl.innerHTML = errMsg
                ? '◌ Historial no disponible<br><span style="opacity:.5">' + esc(errMsg) + '</span>'
                : '◌ Sin historial de rendimiento todavía<br><span style="opacity:.5">se generará con los snapshots diarios</span>';
        }
        const empty = rows.length === 0;
        showEmpty('perfChart', 'perfEmpty', empty);
        if (perfChart) { perfChart.destroy(); perfChart = null; }
        if (empty) return;

        const labels = rows.map(r => {
            const d = new Date(r.date + 'T00:00:00');
            return isNaN(d) ? r.date : fmtDay.format(d);
        });
        const series = (key, label, color, opts = {}) => ({
            label,
            data: rows.map(r => isNum(r[key]) ? r[key] : null),
            borderColor: color,
            backgroundColor: color + '22',
            borderWidth: opts.width || 1.2,
            pointRadius: rows.length === 1 ? 3 : 0,
            pointHoverRadius: 4,
            tension: 0.35,
            fill: !!opts.fill,
            borderDash: opts.dash || [],
            spanGaps: true
        });

        const canvas = document.getElementById('perfChart');
        const ctx = canvas.getContext('2d');
        const grad = ctx.createLinearGradient(0, 0, 0, canvas.parentElement.clientHeight || 260);
        grad.addColorStop(0, 'rgba(0,255,231,.22)');
        grad.addColorStop(1, 'rgba(0,255,231,0)');
        const totalDs = series('totalValue', 'Total', COLORS.total, { width: 2, fill: true });
        totalDs.backgroundColor = grad;

        perfChart = new Chart(canvas, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    totalDs,
                    series('cryptoValue', 'Cripto', '#00b3a3', { dash: [4, 3] }),
                    series('etfValue', 'ETF', COLORS.etf, { dash: [4, 3] }),
                    series('stockValue', 'Acciones', COLORS.stock, { dash: [4, 3] })
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'bottom', labels: { color: tickColor, font: tickFont, boxWidth: 10, padding: 12 } },
                    tooltip: {
                        callbacks: {
                            label: c => ` ${c.dataset.label}: ${money(c.parsed.y)}`,
                            afterBody: items => {
                                const r = rows[items[0].dataIndex];
                                if (!r || !isNum(r.dailyChange)) return '';
                                return `Variación diaria: ${signedMoney(r.dailyChange)} (${pct(r.dailyChangePercentage)})`;
                            }
                        }
                    }
                },
                scales: {
                    x: { grid: { color: gridColor }, ticks: { color: tickColor, font: tickFont, maxRotation: 0, autoSkipPadding: 12 } },
                    y: {
                        grid: { color: gridColor },
                        ticks: {
                            color: tickColor, font: tickFont,
                            callback: v => new Intl.NumberFormat(LOCALE, { style: 'currency', currency: CURRENCY, notation: 'compact', maximumFractionDigits: 1 }).format(v)
                        }
                    }
                }
            }
        });
    }

    /* ══ 1b. RISK ════════════════════════════════════════════ */
    const RISK_LABEL = { low: 'BAJO', medium: 'MEDIO', high: 'ALTO' };

    function renderRisk(r, note) {
        const badge = document.getElementById('kpiRiskBadge');
        const score = document.getElementById('kpiRiskScore');
        const rec = document.getElementById('kpiRiskRec');
        if (!badge) return;
        if (!r) {
            badge.textContent = '—';
            badge.className = 'pf-risk-badge pf-risk-none';
            score.textContent = '';
            rec.textContent = note || 'Sin evaluación. Pulsa «Recalcular».';
            rec.title = '';
            return;
        }
        const level = String(r.riskLevel || '').toLowerCase();
        badge.textContent = RISK_LABEL[level] || (r.riskLevel || '—').toUpperCase();
        badge.className = 'pf-risk-badge pf-risk-' + (RISK_LABEL[level] ? level : 'none');
        score.textContent = isNum(r.riskScore) ? fmtPct.format(r.riskScore).replace(/,00$/, '') + ' / 100' : '';
        const when = r.assessmentDate ? ' · ' + fmtDateTime(r.assessmentDate) : '';
        rec.textContent = (r.recommendation || '') + when;
        rec.title = r.recommendation || '';
    }

    async function loadRisk() {
        try {
            const { data } = await api('/api/portfolio/risk');
            renderRisk(data);
        } catch (e) {
            renderRisk(null, e.status === 404 ? null : 'Riesgo no disponible: ' + e.message);
        }
    }

    async function recalcRisk(btn) {
        btn.disabled = true;
        const old = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
        try {
            const { status, data } = await api('/api/portfolio/risk', { method: 'POST' });
            if (status === 204 || !data) {
                renderRisk(null, 'Cartera vacía: no hay nada que evaluar.');
                flash('La cartera está vacía; no se ha generado evaluación de riesgo.', 'error');
            } else {
                renderRisk(data);
                flash('Riesgo recalculado.');
            }
        } catch (e) {
            flash('No se pudo recalcular el riesgo: ' + e.message, 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = old;
        }
    }

    /* ══ 3. HOLDINGS TABLE ═══════════════════════════════════ */
    let holdings = [];
    let sortKey = 'marketValue';
    let sortDir = -1; // desc
    const editing = new Set();

    async function loadHoldings() {
        const tbody = document.getElementById('holdingsBody');
        try {
            const { data } = await api('/api/portfolio/holdings');
            holdings = Array.isArray(data) ? data : [];
            renderHoldings();
        } catch (e) {
            holdings = [];
            tbody.innerHTML = `<tr><td colspan="9"><div class="empty-state">◌ No se pudieron cargar las posiciones<br><span style="opacity:.5">${esc(e.message)}</span></div></td></tr>`;
            document.getElementById('holdingsCount').textContent = '— posiciones';
        }
        populateAlertAssets();
    }

    function sortValue(h, key) {
        const v = h[key];
        if (key === 'symbol' || key === 'name' || key === 'type') return String(v || '').toLowerCase();
        return isNum(v) ? v : -Infinity;
    }

    function renderHoldings() {
        const tbody = document.getElementById('holdingsBody');
        document.getElementById('holdingsCount').textContent = holdings.length + (holdings.length === 1 ? ' posición' : ' posiciones');

        document.querySelectorAll('#holdingsTable th[data-sort]').forEach(th => {
            const ind = th.querySelector('.pf-sort');
            if (ind) ind.textContent = th.dataset.sort === sortKey ? (sortDir < 0 ? '▼' : '▲') : '';
            th.setAttribute('aria-sort', th.dataset.sort === sortKey ? (sortDir < 0 ? 'descending' : 'ascending') : 'none');
        });

        if (holdings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9"><div class="empty-state">◌ Cartera vacía —<br><span style="opacity:.4">aún no hay posiciones registradas</span></div></td></tr>';
            return;
        }

        const sorted = holdings.slice().sort((a, b) => {
            const va = sortValue(a, sortKey), vb = sortValue(b, sortKey);
            if (va < vb) return -1 * sortDir;
            if (va > vb) return 1 * sortDir;
            return 0;
        });

        tbody.innerHTML = sorted.map(h => {
            const sym = esc(h.symbol);
            const type = String(h.type || '').toUpperCase();
            const isEdit = editing.has(h.symbol);
            const qtyCell = isEdit
                ? `<input type="text" inputmode="decimal" class="form-control form-control-sm hud-input pf-edit-input" data-field="quantity" value="${esc(h.quantity ?? '')}" aria-label="Cantidad de ${sym}">`
                : qty(h.quantity);
            const avgCell = isEdit
                ? `<input type="text" inputmode="decimal" class="form-control form-control-sm hud-input pf-edit-input" data-field="averageBuyPrice" value="${esc(h.averageBuyPrice ?? '')}" placeholder="(vacío)" aria-label="Precio medio de ${sym}">`
                : price(h.averageBuyPrice);
            const actions = isEdit
                ? `<button type="button" class="btn-hud pf-btn-xs" data-action="save" title="Guardar"><i class="bi bi-check-lg"></i></button>
                   <button type="button" class="btn-hud btn-hud-danger pf-btn-xs" data-action="cancel" title="Cancelar"><i class="bi bi-x-lg"></i></button>`
                : `<button type="button" class="btn-hud pf-btn-xs" data-action="edit" title="Editar"><i class="bi bi-pencil"></i></button>`;
            return `<tr data-symbol="${sym}" class="${isEdit ? 'pf-row-edit' : ''}">
                <td class="ps-3">
                    <div class="asset-name">${esc(h.name || h.symbol)}</div>
                    <div class="asset-symbol">${esc(String(h.symbol || '').toUpperCase())}</div>
                </td>
                <td class="text-center"><span class="pf-type pf-type-${esc(type.toLowerCase())}">${esc(TYPE_LABEL[type] || type || '—')}</span></td>
                <td class="text-end">${qtyCell}</td>
                <td class="text-end">${avgCell}</td>
                <td class="text-end price-current">${price(h.currentPrice)}</td>
                <td class="text-end">${money(h.marketValue)}</td>
                <td class="text-end price-prev">${money(h.invested)}</td>
                <td class="text-end ${plClass(h.profitLoss)}">
                    <div>${signedMoney(h.profitLoss)}</div>
                    <div class="pf-small">${pct(h.profitLossPercentage)}</div>
                </td>
                <td class="text-center pe-3 text-nowrap">
                    ${actions}
                    <div class="pf-row-error" role="alert"></div>
                </td>
            </tr>`;
        }).join('');
    }

    async function saveHolding(tr) {
        const symbol = tr.dataset.symbol;
        const errBox = tr.querySelector('.pf-row-error');
        const qIn = tr.querySelector('[data-field="quantity"]');
        const pIn = tr.querySelector('[data-field="averageBuyPrice"]');
        errBox.textContent = '';

        const q = parseNum(qIn.value);
        if (!isNum(q) || q < 0) { errBox.textContent = 'Cantidad no válida'; qIn.focus(); return; }
        let p = null;
        if (pIn.value.trim() !== '') {
            p = parseNum(pIn.value);
            if (!isNum(p) || p < 0) { errBox.textContent = 'Precio medio no válido'; pIn.focus(); return; }
        }

        tr.querySelectorAll('button, input').forEach(el => el.disabled = true);
        try {
            const { data } = await api('/api/portfolio/holdings/' + encodeURIComponent(symbol), {
                method: 'PUT',
                body: { quantity: q, averageBuyPrice: p }
            });
            editing.delete(symbol);
            if (data && data.symbol) {
                const i = holdings.findIndex(h => h.symbol === symbol);
                if (i >= 0) holdings[i] = data;
            }
            renderHoldings();
            flash('Posición ' + String(symbol).toUpperCase() + ' actualizada.');
            // Refrescar agregados que dependen de las posiciones
            refreshAggregates();
        } catch (e) {
            tr.querySelectorAll('button, input').forEach(el => el.disabled = false);
            errBox.textContent = e.status === 404 ? 'La posición ya no existe' : e.message;
        }
    }

    function bindHoldings() {
        const tbody = document.getElementById('holdingsBody');
        tbody.addEventListener('click', e => {
            const btn = e.target.closest('button[data-action]');
            if (!btn) return;
            const tr = btn.closest('tr');
            const symbol = tr.dataset.symbol;
            switch (btn.dataset.action) {
                case 'edit':
                    editing.add(symbol);
                    renderHoldings();
                    tbody.querySelector(`tr[data-symbol="${CSS.escape(symbol)}"] [data-field="quantity"]`)?.focus();
                    break;
                case 'cancel':
                    editing.delete(symbol);
                    renderHoldings();
                    break;
                case 'save':
                    saveHolding(tr);
                    break;
            }
        });
        tbody.addEventListener('keydown', e => {
            if (!e.target.classList.contains('pf-edit-input')) return;
            const tr = e.target.closest('tr');
            if (e.key === 'Enter') { e.preventDefault(); saveHolding(tr); }
            if (e.key === 'Escape') { editing.delete(tr.dataset.symbol); renderHoldings(); }
        });

        document.querySelectorAll('#holdingsTable th[data-sort]').forEach(th => {
            const handler = () => {
                const key = th.dataset.sort;
                if (sortKey === key) sortDir = -sortDir;
                else { sortKey = key; sortDir = (key === 'name' || key === 'type') ? 1 : -1; }
                renderHoldings();
            };
            th.addEventListener('click', handler);
            th.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); handler(); } });
        });
    }

    /* ══ 4. PRICE ALERTS ═════════════════════════════════════ */
    function populateAlertAssets() {
        const sel = document.getElementById('alertAsset');
        if (!sel) return;
        const prev = sel.value;
        const opts = holdings
            .slice()
            .sort((a, b) => String(a.name || a.symbol).localeCompare(String(b.name || b.symbol), LOCALE))
            .map(h => `<option value="${esc(h.symbol)}" data-name="${esc(h.name || '')}">${esc(h.name || h.symbol)} (${esc(String(h.symbol).toUpperCase())})</option>`)
            .join('');
        sel.innerHTML = '<option value="">' + (holdings.length ? 'Selecciona un activo…' : 'Sin posiciones en cartera') + '</option>' + opts;
        sel.disabled = holdings.length === 0;
        if (prev && holdings.some(h => h.symbol === prev)) sel.value = prev;
        updateAlertHint();
    }

    function updateAlertHint() {
        const sel = document.getElementById('alertAsset');
        const hint = document.getElementById('alertHint');
        if (!sel || !hint) return;
        const h = holdings.find(x => x.symbol === sel.value);
        hint.textContent = h && isNum(h.currentPrice) ? 'Precio actual: ' + price(h.currentPrice) : '';
    }

    async function loadAlerts() {
        const list = document.getElementById('priceAlertsBody');
        const count = document.getElementById('priceAlertsCount');
        try {
            const { data } = await api('/api/alerts');
            const alerts = (Array.isArray(data) ? data : []).filter(a => a && a.active !== false);
            count.textContent = alerts.length + (alerts.length === 1 ? ' activa' : ' activas');
            if (alerts.length === 0) {
                list.innerHTML = '<tr><td colspan="5"><div class="empty-state" style="padding:2rem 1rem">◌ Sin alertas de precio activas</div></td></tr>';
                return;
            }
            list.innerHTML = alerts.map(a => {
                const above = String(a.alertType).toLowerCase() === 'above';
                return `<tr data-id="${esc(a.id)}">
                    <td class="ps-3">
                        <div class="alert-crypto">${esc(a.assetName || a.assetSymbol)}</div>
                        <div class="asset-symbol">${esc(String(a.assetSymbol || '').toUpperCase())}</div>
                    </td>
                    <td class="text-center">
                        <span class="${above ? 'badge-up' : 'badge-down pf-no-pulse'}">${above ? '▲ SUPERA' : '▼ CAE BAJO'}</span>
                    </td>
                    <td class="text-end alert-price">${price(a.targetPrice)}</td>
                    <td class="text-end alert-time d-none d-md-table-cell">${esc(fmtDateTime(a.createdDate))}</td>
                    <td class="text-center pe-3">
                        <button type="button" class="btn-del" data-action="delete-alert" title="Eliminar alerta" aria-label="Eliminar alerta">✕</button>
                    </td>
                </tr>`;
            }).join('');
        } catch (e) {
            count.textContent = '—';
            list.innerHTML = `<tr><td colspan="5"><div class="empty-state" style="padding:2rem 1rem">◌ No se pudieron cargar las alertas<br><span style="opacity:.5">${esc(e.message)}</span></div></td></tr>`;
        }
    }

    function bindAlerts() {
        const form = document.getElementById('alertForm');
        const errBox = document.getElementById('alertError');
        const sel = document.getElementById('alertAsset');
        sel.addEventListener('change', updateAlertHint);

        form.addEventListener('submit', async e => {
            e.preventDefault();
            errBox.textContent = '';
            const symbol = sel.value;
            const opt = sel.selectedOptions[0];
            const type = form.querySelector('[name="alertType"]').value;
            const target = parseNum(form.querySelector('[name="targetPrice"]').value);
            if (!symbol) { errBox.textContent = 'Selecciona un activo.'; return; }
            if (!isNum(target) || target <= 0) { errBox.textContent = 'Introduce un precio objetivo mayor que 0.'; return; }

            const btn = form.querySelector('button[type="submit"]');
            btn.disabled = true;
            try {
                await api('/api/alerts', {
                    method: 'POST',
                    body: {
                        assetSymbol: symbol,
                        assetName: (opt && opt.dataset.name) || null,
                        targetPrice: target,
                        alertType: type
                    }
                });
                form.querySelector('[name="targetPrice"]').value = '';
                flash('Alerta creada.');
                loadAlerts();
            } catch (err) {
                errBox.textContent = err.message;
            } finally {
                btn.disabled = false;
            }
        });

        document.getElementById('priceAlertsBody').addEventListener('click', async e => {
            const btn = e.target.closest('button[data-action="delete-alert"]');
            if (!btn) return;
            const tr = btn.closest('tr');
            if (!confirm('¿Eliminar esta alerta de precio?')) return;
            btn.disabled = true;
            try {
                await api('/api/alerts/' + encodeURIComponent(tr.dataset.id), { method: 'DELETE' });
                flash('Alerta eliminada.');
            } catch (err) {
                if (err.status !== 404) {
                    btn.disabled = false;
                    flash('No se pudo eliminar la alerta: ' + err.message, 'error');
                    return;
                }
            }
            loadAlerts();
        });
    }

    /* ── Orquestación ───────────────────────────────────────── */
    function currentDays() {
        return document.getElementById('perfDays')?.value || '30';
    }

    function refreshAggregates() {
        loadSummary();
        loadPerformance(currentDays());
    }

    function init() {
        bindHoldings();
        bindAlerts();
        document.getElementById('btnRecalcRisk')?.addEventListener('click', e => recalcRisk(e.currentTarget));
        document.getElementById('perfDays')?.addEventListener('change', e => loadPerformance(e.target.value));
        document.getElementById('btnRefreshPortfolio')?.addEventListener('click', () => {
            loadSummary(); loadHoldings(); loadPerformance(currentDays()); loadRisk(); loadAlerts();
        });

        // Cada carga es independiente: un fallo no bloquea las demás
        Promise.allSettled([loadSummary(), loadHoldings(), loadPerformance(currentDays()), loadRisk(), loadAlerts()]);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();
})();
