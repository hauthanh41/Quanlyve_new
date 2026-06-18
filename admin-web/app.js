/* ── CONFIG ── */
const API = 'http://localhost:3000/api';
let token    = localStorage.getItem('sk_token') || '';
let adminId  = parseInt(localStorage.getItem('sk_admin_id') || '0');
let adminName = localStorage.getItem('sk_admin_name') || 'Quản trị viên';
let currentPage = 'dashboard';
let currentConvId = null;
let msgPollTimer = null;
let notifPollTimer = null;

/* ── HELPERS ── */
async function api(method, path, body) {
  const res = await fetch(API + path, {
    method,
    headers: { 'Content-Type':'application/json', ...(token?{Authorization:'Bearer '+token}:{}) },
    body: body ? JSON.stringify(body) : undefined
  });
  const data = await res.json().catch(()=>({}));
  if (!res.ok) throw new Error(data.message || 'Lỗi '+res.status);
  return data;
}
const fmtVND = n => Number(n||0).toLocaleString('vi-VN')+'đ';
const fmtNum = n => Number(n||0).toLocaleString('vi-VN');
const fmtDate = s => s ? s.slice(0,10) : '—';
const fmtTime = s => s ? s.slice(0,16).replace('T',' ') : '—';
const shortTime = s => { if(!s) return ''; const d=new Date(s); return d.toLocaleTimeString('vi-VN',{hour:'2-digit',minute:'2-digit'}); };

function badge(status) {
  const m = {
    AVAILABLE:['green','HOẠT ĐỘNG'],  CONFIRMED:['green','Đã TT'],
    CANCELLED:['red','Đã hủy'],       PENDING:['orange','Chờ TT'],
    DEPARTED: ['blue','Đã bay'],      DELAYED:['orange','Trễ chuyến'],
    FULL:     ['red','Hết chỗ'],
    CUSTOMER: ['blue','KH'],          ADMIN:['purple','Admin'],
    active:   ['green','Hoạt động'],  inactive:['gray','Ngừng'],
    ECONOMY:  ['gray','Phổ thông'],   BUSINESS:['blue','Thương gia'],   FIRST:['purple','Hạng nhất'],
    BOOKED:   ['blue','Đã đặt'],      CHECKED_IN:['green','Check-in'],
    PAID:     ['green','Đã TT'],      UNPAID:['orange','Chưa TT'],      REFUNDED:['gray','Hoàn tiền'],
    CASH:     ['gray','Tiền mặt'],    BANKING:['blue','Chuyển khoản'],
    MOMO:     ['purple','MoMo'],      VNPAY:['blue','VNPay']
  };
  const [cls,lbl] = m[status]||['gray',status||'—'];
  return `<span class="badge badge-${cls}">${lbl}</span>`;
}

function airlineColor(code) {
  const colors = ['#1e40af','#0d9488','#7c3aed','#b45309','#be185d','#065f46'];
  let h = 0; for(const c of (code||'')) h=(h*31+c.charCodeAt(0))%colors.length;
  return colors[h];
}

function closePanels() {
  document.getElementById('notif-panel').style.display='none';
}

/* ── LOGIN ── */
document.getElementById('login-btn').onclick = async () => {
  const email = document.getElementById('l-email').value.trim();
  const pass  = document.getElementById('l-pass').value;
  const errEl = document.getElementById('login-err');
  try {
    const res = await api('POST','/auth/login',{email,password:pass});
    if (!['ADMIN','STAFF'].includes(res.user?.role)) throw new Error('Tài khoản không có quyền truy cập hệ thống admin');
    token     = res.token;
    adminId   = res.user.user_id || res.user.id;
    adminName = res.user.full_name || 'Quản trị viên';
    localStorage.setItem('sk_token', token);
    localStorage.setItem('sk_admin_id', adminId);
    localStorage.setItem('sk_admin_name', adminName);
    errEl.style.display='none';
    initApp();
  } catch(e) {
    errEl.textContent = e.message;
    errEl.style.display='block';
  }
};
document.getElementById('l-pass').addEventListener('keydown', e => e.key==='Enter' && document.getElementById('login-btn').click());

function doLogout() {
  token=''; adminId=0;
  localStorage.removeItem('sk_token'); localStorage.removeItem('sk_admin_id'); localStorage.removeItem('sk_admin_name');
  clearInterval(msgPollTimer); clearInterval(notifPollTimer);
  document.getElementById('app').style.display='none';
  document.getElementById('login-page').style.display='flex';
}

/* ── INIT ── */
function initApp() {
  document.getElementById('login-page').style.display='none';
  document.getElementById('app').style.display='flex';
  document.getElementById('sb-uname').textContent = adminName;
  document.getElementById('sb-avatar-text').textContent = adminName[0]?.toUpperCase()||'A';
  // Nav
  document.querySelectorAll('.nav-link').forEach(el => {
    el.onclick = e => { e.preventDefault(); navTo(el.dataset.page); };
  });
  document.getElementById('global-search').addEventListener('input', e => globalSearch(e.target.value));
  startNotifPoll();
  navTo('dashboard');
}

function navTo(page) {
  currentPage = page;
  document.querySelectorAll('.nav-link').forEach(n => n.classList.toggle('active', n.dataset.page===page));
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const el = document.getElementById('page-'+page);
  if (el) el.classList.add('active');
  clearInterval(msgPollTimer);
  closePanels();
  const fns = { dashboard:renderDashboard, flights:renderFlights, bookings:renderBookings,
                customers:renderCustomers, messages:renderMessages, revenue:renderRevenue,
                alerts:renderAlerts, settings:renderSettings };
  fns[page]?.();
}

function globalSearch(q) {
  if (!q) return;
  navTo('flights');
  setTimeout(() => {
    const input = document.getElementById('flight-search');
    if (input) { input.value=q; filterFlights(); }
  }, 300);
}

/* ── NOTIFICATIONS ── */
function toggleNotifPanel() {
  const p = document.getElementById('notif-panel');
  const vis = p.style.display !== 'none';
  p.style.display = vis ? 'none' : 'flex';
  if (!vis) loadNotifs();
}

async function loadNotifs() {
  try {
    const notifs = await api('GET','/notifications');
    const list = document.getElementById('notif-list');
    const unread = notifs.filter(n=>!n.is_read).length;
    const badge = document.getElementById('notif-badge');
    badge.textContent = unread;
    badge.style.display = unread > 0 ? 'flex' : 'none';
    list.innerHTML = notifs.length
      ? notifs.map(n=>`<div class="notif-item ${n.is_read?'':'unread'}" onclick="readNotif(${n.id},this)">
          <div class="notif-title">${n.title}</div>
          <div class="notif-body">${n.body}</div>
          <div class="notif-time">${fmtTime(n.created_at)}</div>
        </div>`).join('')
      : '<div style="padding:20px;text-align:center;color:#94a3b8">Không có thông báo</div>';
  } catch(_) {}
}

async function readNotif(id, el) {
  await api('PATCH','/notifications/'+id+'/read').catch(()=>{});
  el.classList.remove('unread');
}

async function markAllRead() {
  await api('PATCH','/notifications/read-all').catch(()=>{});
  loadNotifs();
}

function startNotifPoll() {
  loadNotifBadge();
  notifPollTimer = setInterval(loadNotifBadge, 15000);
}

async function loadNotifBadge() {
  try {
    const data = await api('GET','/notifications/unread-count');
    const count = data.count || 0;
    const badge = document.getElementById('notif-badge');
    badge.textContent = count;
    badge.style.display = count > 0 ? 'flex' : 'none';
  } catch(_) {}
}

/* ── DASHBOARD ── */
async function renderDashboard() {
  const el = document.getElementById('page-dashboard');
  el.innerHTML = `
  <div class="page-header">
    <div>
      <div class="page-title">Tổng quan vận hành</div>
      <div class="page-sub">Giám sát thời gian thực đội bay và hiệu suất thương mại.</div>
    </div>
    <button class="btn btn-teal" onclick="navTo('flights')">+ Nhập chuyến bay mới</button>
  </div>
  <div class="stats-row" id="dash-stats">
    <div class="stat-card"><div class="stat-label">TỔNG CHUYẾN BAY <span class="stat-icon">✈</span></div><div class="stat-value" id="ds-flights">—</div><div class="stat-trend trend-up" id="ds-ft"></div></div>
    <div class="stat-card"><div class="stat-label">ĐẶT VÉ HÔM NAY <span class="stat-icon">🎫</span></div><div class="stat-value" id="ds-book">—</div><div class="stat-trend trend-up" id="ds-bt"></div></div>
    <div class="stat-card"><div class="stat-label">DOANH THU <span class="stat-icon">💰</span></div><div class="stat-value" id="ds-rev">—</div></div>
    <div class="stat-card"><div class="stat-label">TÀU BAY HOẠT ĐỘNG <span class="stat-icon">✈</span></div><div class="stat-value" id="ds-plane">—</div></div>
  </div>
  <div class="dash-grid">
    <div class="card">
      <div class="card-header"><div class="card-title">Lịch trình bay</div><a href="#" onclick="navTo('flights')" style="font-size:12px;color:#3b82f6">Xem lịch trình đầy đủ →</a></div>
      <div class="schedule-head"><span>SỐ HIỆU</span><span>TUYẾN</span><span>KHỞI HÀNH</span><span>TRẠNG THÁI</span></div>
      <div id="dash-schedule">Đang tải...</div>
    </div>
    <div class="card">
      <div class="card-title" style="margin-bottom:14px">Hoạt động gần đây</div>
      <div id="dash-activity">Đang tải...</div>
    </div>
  </div>
  <div class="radar-banner">
    <div>
      <div style="font-size:11px;letter-spacing:1px;color:#60a5fa;margin-bottom:6px">🛰 ĐỒNG BỘ DỮ LIỆU KHÔNG LƯU TRỰC TIẾP</div>
      <div style="font-size:13px;color:#cbd5e1">Trực quan hóa các hành lang bay khu vực và hệ thống thời tiết. Hệ thống đang hoạt động bình thường.</div>
    </div>
    <button class="btn btn-outline" style="flex-shrink:0">Mở rộng bản đồ radar đầy đủ</button>
  </div>
  <div style="text-align:center;font-size:12px;color:#94a3b8;margin-top:20px">
    © 2024 SkyOps Systems. Bảo lưu mọi quyền. &nbsp;|&nbsp; Trung tâm tuân thủ &nbsp;|&nbsp; Tài liệu API &nbsp;|&nbsp; Kiểm tra bảo mật
  </div>`;
  try {
    const [flights, bookings] = await Promise.all([api('GET','/flights'), api('GET','/bookings')]);
    const totalRev = bookings.reduce((s,b)=>s+ +b.total_amount,0);
    document.getElementById('ds-flights').textContent = fmtNum(flights.length);
    document.getElementById('ds-book').textContent    = fmtNum(bookings.length);
    document.getElementById('ds-rev').textContent     = fmtVND(totalRev);
    document.getElementById('ds-plane').textContent   = flights.filter(f=>f.status==='AVAILABLE').length + ' / ' + flights.length;
    // Schedule
    document.getElementById('dash-schedule').innerHTML = flights.slice(0,5).map(f=>`
      <div class="schedule-row">
        <div><b style="color:#3b82f6">${f.flight_code}</b></div>
        <div>${f.departure_airport} <span style="color:#94a3b8">→</span> ${f.arrival_airport}</div>
        <div>${shortTime(f.departure_time)||'—'} UTC</div>
        <div>${badge(f.status)}</div>
      </div>`).join('');
    // Activity
    document.getElementById('dash-activity').innerHTML = bookings.slice(0,5).map((b,i)=>`
      <div class="act-item">
        <div class="act-ic ${['ic-blue','ic-green','ic-orange'][i%3]}">📋</div>
        <div><div style="font-weight:600">${b.full_name||'Khách'} đặt vé</div>
          <div>${fmtDate(b.booking_date)} — ${fmtVND(b.total_amount)}</div></div>
      </div>`).join('');
  } catch(e) { console.error(e); }
}

/* ── FLIGHTS ── */
let allFlights = [];
let flightPage = 1;
const FLIGHT_PER_PAGE = 10;

async function renderFlights() {
  const el = document.getElementById('page-flights');
  el.innerHTML = `
  <div class="page-header">
    <div>
      <div class="page-title">Quản lý chuyến bay</div>
      <div class="page-sub">Theo dõi và quản lý hoạt động bay thời gian thực trên toàn mạng lưới.</div>
    </div>
    <button class="btn btn-teal" onclick="openFlightModal()">+ Thêm chuyến bay mới</button>
  </div>
  <div class="stats-row" id="flight-stats"></div>
  <div class="card">
    <div class="filters-row">
      <input id="flight-search" type="text" placeholder="🔍  Tìm mã, tuyến bay..." oninput="filterFlights()" style="flex:1;max-width:320px"/>
      <select id="flight-status-filter" onchange="filterFlights()">
        <option value="">Tất cả trạng thái</option>
        <option>AVAILABLE</option><option>DEPARTED</option><option>CANCELLED</option><option>DELAYED</option>
      </select>
      <input type="date" id="flight-date-filter" onchange="filterFlights()"/>
    </div>
    <div class="tbl-wrap">
      <table>
        <thead><tr>
          <th>MÃ CHUYẾN BAY</th><th>HÃNG</th><th>TUYẾN BAY</th>
          <th>GIỜ ĐI</th><th>GIỜ ĐẾN</th><th>TÀU BAY</th><th>GIÁ VÉ</th><th>TRẠNG THÁI</th><th>THAO TÁC</th>
        </tr></thead>
        <tbody id="flight-tbody"></tbody>
      </table>
    </div>
    <div class="pagination" id="flight-pag"></div>
  </div>`;
  try {
    const [flights, planes] = await Promise.all([api('GET','/flights'), api('GET','/airplanes')]);
    allFlights = flights;
    window._planes = planes;
    // Stats
    document.getElementById('flight-stats').innerHTML = `
      <div class="stat-card"><div class="stat-label">CHUYẾN BAY ĐANG HOẠT ĐỘNG</div><div class="stat-value">${flights.filter(f=>f.status==='AVAILABLE').length}</div></div>
      <div class="stat-card"><div class="stat-label">TỶ LỆ ĐÚNG GIỜ</div><div class="stat-value" style="color:#16a34a">94.2%</div></div>
      <div class="stat-card"><div class="stat-label">SỐ CA CHẬM CHUYẾN</div><div class="stat-value" style="color:#dc2626">${flights.filter(f=>f.status==='DELAYED').length}</div></div>
      <div class="stat-card"><div class="stat-label">TỔNG LƯỢT BAY</div><div class="stat-value">${fmtNum(flights.length)}</div></div>`;
    flightPage = 1;
    filterFlights();
  } catch(e) { console.error(e); }
}

function filterFlights() {
  const q = (document.getElementById('flight-search')?.value||'').toLowerCase();
  const status = document.getElementById('flight-status-filter')?.value||'';
  const date   = document.getElementById('flight-date-filter')?.value||'';
  let data = allFlights.filter(f => {
    const matchQ = !q || f.flight_code?.toLowerCase().includes(q) ||
      f.departure_airport?.toLowerCase().includes(q) || f.arrival_airport?.toLowerCase().includes(q);
    const matchS = !status || f.status===status;
    const matchD = !date || (f.departure_time||'').startsWith(date);
    return matchQ && matchS && matchD;
  });
  renderFlightTable(data);
}

function renderFlightTable(data) {
  const total = data.length;
  const pages = Math.ceil(total/FLIGHT_PER_PAGE);
  const slice = data.slice((flightPage-1)*FLIGHT_PER_PAGE, flightPage*FLIGHT_PER_PAGE);
  const tbody = document.getElementById('flight-tbody');
  if (!tbody) return;
  tbody.innerHTML = slice.map(f => {
    const code = f.flight_code||'';
    const prefix = code.split('-')[0]||code.slice(0,2);
    const col = airlineColor(prefix);
    return `<tr>
      <td><span class="flight-code" onclick="openFlightDetail(${f.flight_id})">${code}</span></td>
      <td><span class="airline-logo" style="background:${col}">${prefix.slice(0,2)}</span>${f.airplane_name||'—'}</td>
      <td><b>${f.departure_airport}</b><span class="route-arrow">→</span><b>${f.arrival_airport}</b></td>
      <td>${shortTime(f.departure_time)||'—'}</td>
      <td>${shortTime(f.arrival_time)||'—'}</td>
      <td>${f.airplane_name||'—'}</td>
      <td>${fmtVND(f.price)}</td>
      <td>${badge(f.status)}</td>
      <td style="display:flex;gap:6px">
        <button class="btn btn-icon btn-outline" title="Sửa" onclick="openFlightModal(${JSON.stringify(f).replace(/"/g,'&quot;')})">✏</button>
        <button class="btn btn-sm btn-outline" onclick="openFlightDetail(${f.flight_id})">Chi tiết</button>
        <button class="btn btn-icon btn-danger" title="Xóa" onclick="deleteFlight(${f.flight_id})">🗑</button>
      </td>
    </tr>`;
  }).join('') || '<tr><td colspan="9" style="text-align:center;color:#94a3b8;padding:24px">Không có chuyến bay nào</td></tr>';

  // Pagination
  const pag = document.getElementById('flight-pag');
  if (!pag) return;
  const from = total===0?0:(flightPage-1)*FLIGHT_PER_PAGE+1;
  const to = Math.min(flightPage*FLIGHT_PER_PAGE, total);
  let html = `<div class="pag-info">Hiện thị ${from}-${to} trong số ${fmtNum(total)} chuyến bay hoạt động</div>`;
  html += `<button class="pag-btn" onclick="flightPage=Math.max(1,flightPage-1);filterFlights()">‹</button>`;
  for (let i=1;i<=Math.min(pages,5);i++) html+=`<button class="pag-btn ${i===flightPage?'active':''}" onclick="flightPage=${i};filterFlights()">${i}</button>`;
  if(pages>5) html+=`<span style="padding:0 6px">…</span><button class="pag-btn ${pages===flightPage?'active':''}" onclick="flightPage=${pages};filterFlights()">${pages}</button>`;
  html += `<button class="pag-btn" onclick="flightPage=Math.min(${pages},flightPage+1);filterFlights()">›</button>`;
  pag.innerHTML = html;
}

function openFlightDetail(id) { alert('Chi tiết chuyến bay #' + id + ' (tính năng mở rộng)'); }

async function deleteFlight(id) {
  if (!confirm('Xóa chuyến bay này?')) return;
  try { await api('DELETE','/flights/'+id); renderFlights(); } catch(e) { alert(e.message); }
}

function openFlightModal(f) {
  f = f || {};
  const planes = (window._planes||[]).map(p=>`<option value="${p.airplane_id}" ${f.airplane_id==p.airplane_id?'selected':''}>${p.airplane_name}</option>`).join('');
  const html = `<div class="modal-bg" id="fm">
    <div class="modal">
      <div class="modal-title">${f.flight_id?'Sửa chuyến bay':'Thêm chuyến bay mới'}</div>
      <div class="form-grid">
        <div class="form-group"><label>Mã chuyến bay</label><input id="f-code" value="${f.flight_code||''}"/></div>
        <div class="form-group"><label>Giá vé (VND)</label><input id="f-price" type="number" value="${f.price||''}"/></div>
        <div class="form-group"><label>Sân bay đi (ID)</label><input id="f-dep" type="number" value="${f.departure_airport_id||''}"/></div>
        <div class="form-group"><label>Sân bay đến (ID)</label><input id="f-arr" type="number" value="${f.arrival_airport_id||''}"/></div>
        <div class="form-group"><label>Giờ khởi hành</label><input id="f-dt" type="datetime-local" value="${(f.departure_time||'').slice(0,16)}"/></div>
        <div class="form-group"><label>Giờ đến</label><input id="f-at" type="datetime-local" value="${(f.arrival_time||'').slice(0,16)}"/></div>
        <div class="form-group"><label>Máy bay</label><select id="f-plane"><option value="">-- Chọn --</option>${planes}</select></div>
        <div class="form-group"><label>Trạng thái</label>
          <select id="f-stat">
            ${['AVAILABLE','DEPARTED','CANCELLED','DELAYED'].map(s=>`<option ${f.status===s?'selected':''}>${s}</option>`).join('')}
          </select></div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" onclick="document.getElementById('fm').remove()">Hủy</button>
        <button class="btn btn-primary" onclick="saveFlight(${f.flight_id||'null'})">Lưu</button>
      </div>
    </div></div>`;
  document.body.insertAdjacentHTML('beforeend', html);
}

async function saveFlight(id) {
  const body = {
    flight_code: document.getElementById('f-code').value,
    price: document.getElementById('f-price').value,
    departure_airport_id: +document.getElementById('f-dep').value,
    arrival_airport_id: +document.getElementById('f-arr').value,
    departure_time: document.getElementById('f-dt').value,
    arrival_time: document.getElementById('f-at').value,
    airplane_id: +document.getElementById('f-plane').value,
    status: document.getElementById('f-stat').value
  };
  try {
    if (id) await api('PUT','/flights/'+id, body);
    else await api('POST','/flights', body);
    document.getElementById('fm').remove();
    renderFlights();
  } catch(e) { alert(e.message); }
}

/* ── BOOKINGS ── */
let allBookings = [];
let bookPage = 1;
const BOOK_PER_PAGE = 10;
let selectedBooking = null;

async function renderBookings() {
  const el = document.getElementById('page-bookings');
  el.innerHTML = `
  <div class="page-header">
    <div>
      <div class="page-title">Quản lý đặt chỗ</div>
      <div class="page-sub">Tổng quan thời gian thực về tất cả các yêu cầu đặt chỗ hiện tại và sắp tới.</div>
    </div>
    <div class="page-actions">
      <button class="btn btn-outline" onclick="exportCSV()">⬇ XUẤT FILE CSV</button>
    </div>
  </div>
  <div class="booking-layout">
    <div>
      <div class="card" style="padding:0">
        <div style="padding:14px 16px;border-bottom:1px solid #f1f5f9;display:flex;gap:10px;flex-wrap:wrap">
          <input id="book-search" type="text" placeholder="🔍 Tìm tên, mã đặt chỗ..." oninput="filterBookings()"
            style="flex:1;min-width:180px;padding:8px 12px;border:1px solid #e2e8f0;border-radius:8px;font-size:13px;outline:none"/>
          <select id="book-status-f" onchange="filterBookings()" style="padding:8px 12px;border:1px solid #e2e8f0;border-radius:8px">
            <option value="">Tất cả trạng thái</option>
            <option>PENDING</option><option>CONFIRMED</option><option>CANCELLED</option>
          </select>
        </div>
        <div class="tbl-wrap">
          <table>
            <thead><tr>
              <th><input type="checkbox" id="chk-all" onchange="toggleAllChk(this)"/></th>
              <th>MÃ ĐẶT CHỖ</th><th>TÊN HÀNH KHÁCH</th><th>SỐ HIỆU CHUYẾN</th>
              <th>GHẾ</th><th>THANH TOÁN</th><th>NGÀY</th>
            </tr></thead>
            <tbody id="book-tbody"></tbody>
          </table>
        </div>
        <div class="pagination" id="book-pag"></div>
      </div>
    </div>
    <div id="detail-panel">
      <div class="detail-panel"><div style="text-align:center;color:#94a3b8;padding:40px 20px">Chọn một đặt chỗ để xem chi tiết</div></div>
    </div>
  </div>`;
  try {
    allBookings = await api('GET','/bookings');
    bookPage = 1;
    filterBookings();
  } catch(e) { console.error(e); }
}

function filterBookings() {
  const q = (document.getElementById('book-search')?.value||'').toLowerCase();
  const st = document.getElementById('book-status-f')?.value||'';
  const data = allBookings.filter(b =>
    (!q || (b.full_name||'').toLowerCase().includes(q) || String(b.booking_id).includes(q)) &&
    (!st || b.booking_status===st)
  );
  renderBookingTable(data);
}

function renderBookingTable(data) {
  const total = data.length;
  const pages = Math.max(1, Math.ceil(total/BOOK_PER_PAGE));
  if (bookPage > pages) bookPage = pages;
  const slice = data.slice((bookPage-1)*BOOK_PER_PAGE, bookPage*BOOK_PER_PAGE);
  const tbody = document.getElementById('book-tbody');
  if (!tbody) return;

  tbody.innerHTML = slice.map(b => {
    const id = b.booking_id;
    const st = b.booking_status || 'PENDING';
    const tickets = b.tickets || [];
    const seat = tickets[0]?.seat_number || '—';
    const flight = tickets[0]?.flight_code || '—';
    return `<tr style="cursor:pointer" onclick="selectBooking(${id})">
      <td><input type="checkbox" class="row-chk" value="${id}"/></td>
      <td><span style="color:#3b82f6;font-weight:700">#${id}</span></td>
      <td><div style="font-weight:600">${b.full_name||'—'}</div><div style="font-size:11px;color:#64748b">${classLabel(tickets[0]?.class_type)}</div></td>
      <td><span style="color:#3b82f6">${flight}</span></td>
      <td>${seat}</td>
      <td>${badge(st)}</td>
      <td>${fmtDate(b.booking_date)}</td>
    </tr>`;
  }).join('') || '<tr><td colspan="7" style="text-align:center;color:#94a3b8;padding:24px">Không có đặt chỗ nào</td></tr>';

  const pag = document.getElementById('book-pag');
  if (!pag) return;
  const from=total===0?0:(bookPage-1)*BOOK_PER_PAGE+1, to=Math.min(bookPage*BOOK_PER_PAGE,total);
  pag.innerHTML = `<div class="pag-info">Hiện thị ${from}-${to} trong số ${fmtNum(total)} đặt chỗ</div>
    <button class="pag-btn" onclick="bookPage=Math.max(1,bookPage-1);filterBookings()">‹</button>
    ${[...Array(Math.min(pages,5)).keys()].map(i=>`<button class="pag-btn ${i+1===bookPage?'active':''}" onclick="bookPage=${i+1};filterBookings()">${i+1}</button>`).join('')}
    <button class="pag-btn" onclick="bookPage=Math.min(${pages},bookPage+1);filterBookings()">›</button>`;
}

function classLabel(c) { return c==='BUSINESS'?'Hạng Thương gia':c==='FIRST'?'Hạng Nhất':'Hạng Phổ thông'; }

async function selectBooking(id) {
  try {
    const b = await api('GET','/bookings/'+id);
    selectedBooking = b;
    const st = b.booking_status || 'PENDING';
    const ticket = (b.tickets||[])[0]||{};
    document.getElementById('detail-panel').innerHTML = `
      <div class="detail-panel">
        <div class="dp-header">
          <div class="dp-title">Chi tiết đặt chỗ</div>
          <div class="dp-badge">${b.booking_id ? 'B-'+b.booking_id : '—'}</div>
        </div>
        <div class="passenger-row">
          <div class="p-avatar">${(b.full_name||'?')[0]?.toUpperCase()}</div>
          <div>
            <div class="p-name">${b.full_name||'—'}</div>
            <div class="p-sub">Hạng thành viên: <b style="color:#f59e0b">Vàng</b></div>
            <div style="display:flex;gap:6px;margin-top:6px">${badge(ticket.class_type||'ECONOMY')}</div>
          </div>
        </div>
        <div style="font-size:12px;font-weight:700;color:#94a3b8;margin-bottom:8px;letter-spacing:.5px">LIÊN HỆ HÀNH KHÁCH</div>
        <div class="contact-row">📧 ${b.email||'—'}</div>
        <div class="contact-row">📞 ${b.phone||'—'}</div>
        <div class="flight-card">
          <div class="fc-route">
            <span>${(ticket.flight_code||'—').split('-')[0]||'—'}</span>
            <span class="fc-arrow">→✈</span>
            <span>${ticket.flight_code||'—'}</span>
          </div>
          <div class="fc-grid">
            <div class="fc-item"><label>CHUYẾN BAY</label><span>${ticket.flight_code||'—'}</span></div>
            <div class="fc-item"><label>GHẾ</label><span>${ticket.seat_number||'—'} (${ticket.class_type==='BUSINESS'?'Lối đi':'Cửa sổ'})</span></div>
            <div class="fc-item"><label>KHỞI HÀNH</label><span>${fmtDate(b.booking_date||b.created_at)}</span></div>
            <div class="fc-item"><label>GIÁ VÉ</label><span>${fmtVND(ticket.ticket_price||b.total_amount)}</span></div>
          </div>
        </div>
        <div class="dp-actions">
          <div style="display:flex;gap:8px">
            <button class="btn btn-outline" style="flex:1" onclick="alert('Tính năng đổi lịch đang phát triển')">Đổi lịch</button>
            <button class="btn btn-danger" style="flex:1" onclick="cancelBook(${id})">${st==='CANCELLED'?'Đã hủy':'Hủy chuyến'}</button>
          </div>
          ${st==='PENDING'?`<button class="btn btn-teal" style="width:100%" onclick="confirmBook(${id})">✅ Xác nhận đặt chỗ</button>`:''}
          <button class="btn btn-outline" style="width:100%" onclick="alert('In thẻ lên máy bay: #${id}')">🖨 In thẻ lên máy bay</button>
        </div>
        <div style="margin-top:16px">
          <div style="font-size:12px;font-weight:700;color:#94a3b8;margin-bottom:10px;letter-spacing:.5px">HOẠT ĐỘNG GẦN ĐÂY</div>
          <div class="act-item"><div class="act-ic ic-green">✓</div><div>Đặt vé được tạo<div style="color:#94a3b8">${fmtTime(b.booking_date)}</div></div></div>
          ${st==='CONFIRMED'?`<div class="act-item"><div class="act-ic ic-blue">📧</div><div>Email xác nhận đã gửi<div style="color:#94a3b8">Tự động</div></div></div>`:''}
        </div>
      </div>`;
  } catch(e) { console.error(e); }
}

async function confirmBook(id) {
  try { await api('PATCH','/bookings/'+id+'/confirm'); alert('Xác nhận thành công!'); renderBookings(); } catch(e) { alert(e.message); }
}
async function cancelBook(id) {
  if (!confirm('Hủy đặt chỗ này?')) return;
  try { await api('PATCH','/bookings/'+id+'/cancel'); renderBookings(); } catch(e) { alert(e.message); }
}

function toggleAllChk(master) {
  document.querySelectorAll('.row-chk').forEach(c => c.checked = master.checked);
}

function exportCSV() {
  const rows = allBookings.map(b => [b.booking_id||b.id, b.full_name, b.email, b.booking_status||b.status, b.total_amount].join(','));
  const csv = 'ID,Tên,Email,Trạng thái,Tổng tiền\n' + rows.join('\n');
  const a = document.createElement('a');
  a.href = 'data:text/csv;charset=utf-8,\uFEFF' + encodeURIComponent(csv);
  a.download = 'bookings.csv'; a.click();
}

/* ── CUSTOMERS ── */
let allCustomers = [];
let custPage = 1;
const CUST_PER_PAGE = 10;

async function renderCustomers() {
  const el = document.getElementById('page-customers');
  el.innerHTML = `
  <div class="page-header">
    <div>
      <div class="page-title">Quản lý khách hàng</div>
      <div class="page-sub">Quản lý và giám sát hồ sơ hành khách và thống kê lòng trung thành.</div>
    </div>
    <button class="btn btn-teal" onclick="openAddCustomerModal()">👤+ Thêm khách hàng mới</button>
  </div>
  <div class="stats-row" id="cust-stats"></div>
  <div class="card">
    <div class="card-header">
      <div class="tier-tabs">
        <button class="tier-tab active" onclick="filterCustTier('all',this)">Tất cả khách hàng</button>
        <button class="tier-tab" onclick="filterCustTier('CONFIRMED',this)">Đã xác nhận</button>
        <button class="tier-tab" onclick="filterCustTier('PENDING',this)">Chờ</button>
      </div>
      <div style="display:flex;gap:8px">
        <input id="cust-search" type="text" placeholder="🔍 Tìm khách hàng..." oninput="filterCustomers()"
          style="padding:8px 12px;border:1px solid #e2e8f0;border-radius:8px;font-size:13px;outline:none;width:220px"/>
        <button class="btn btn-outline btn-sm" onclick="exportCustCSV()">⬇</button>
      </div>
    </div>
    <div class="tbl-wrap">
      <table>
        <thead><tr>
          <th>TÊN KHÁCH HÀNG ↓</th><th>ĐỊA CHỈ EMAIL</th><th>HẠNG TV</th><th>SĐT</th><th>VAI TRÒ</th><th>TRẠNG THÁI</th><th></th>
        </tr></thead>
        <tbody id="cust-tbody"></tbody>
      </table>
    </div>
    <div class="pagination" id="cust-pag"></div>
  </div>`;
  try {
    const [users, bookings] = await Promise.all([api('GET','/users'), api('GET','/bookings')]);
    allCustomers = users;
    const bookCounts = {};
    bookings.forEach(b => { bookCounts[b.user_id] = (bookCounts[b.user_id]||0)+1; });
    window._bookCounts = bookCounts;
    // Stats
    const custs = users.filter(u=>u.role==='CUSTOMER');
    document.getElementById('cust-stats').innerHTML = `
      <div class="stat-card"><div class="stat-label">TỔNG KHÁCH HÀNG</div><div class="stat-value">${fmtNum(custs.length)}</div><div class="stat-trend trend-up">↗ +4.2%</div></div>
      <div class="stat-card"><div class="stat-label">LƯỢT ĐẶT CHỖ</div><div class="stat-value">${fmtNum(bookings.length)}</div><div class="stat-trend trend-up">↗ +12%</div></div>
      <div class="stat-card"><div class="stat-label">TỶ LỆ THÂN THIẾT</div><div class="stat-value">68%</div><div class="stat-trend trend-down">↘ -1.5%</div></div>
      <div class="stat-card"><div class="stat-label">GIÁ VÉ TRUNG BÌNH</div><div class="stat-value">${bookings.length?fmtVND(bookings.reduce((s,b)=>s+ +b.total_amount,0)/bookings.length):'—'}</div></div>`;
    filterCustomers();
  } catch(e) { console.error(e); }
}

let custTierFilter = 'all';
function filterCustTier(tier, btn) {
  custTierFilter = tier;
  document.querySelectorAll('.tier-tab').forEach(t=>t.classList.remove('active'));
  btn.classList.add('active');
  filterCustomers();
}

function filterCustomers() {
  const q = (document.getElementById('cust-search')?.value||'').toLowerCase();
  const data = allCustomers.filter(u =>
    (!q || u.full_name?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q))
  );
  renderCustTable(data);
}

function tierLabel(bookCount) {
  if (bookCount>=20) return ['🥇','Vàng','#f59e0b'];
  if (bookCount>=10) return ['🥈','Bạc','#94a3b8'];
  return ['🥉','Đồng','#cd7c4d'];
}

function renderCustTable(data) {
  const total=data.length, pages=Math.max(1,Math.ceil(total/CUST_PER_PAGE));
  if(custPage>pages) custPage=pages;
  const slice=data.slice((custPage-1)*CUST_PER_PAGE, custPage*CUST_PER_PAGE);
  const tbody=document.getElementById('cust-tbody');
  if(!tbody)return;
  tbody.innerHTML=slice.map(u=>{
    const cnt=(window._bookCounts||{})[u.user_id]||0;
    const [ico,tier,col]=tierLabel(cnt);
    const av=u.full_name?.split(' ').slice(-1)[0]?.[0]?.toUpperCase()||'?';
    const avCol=['#3b82f6','#0d9488','#7c3aed','#f59e0b'][u.user_id%4];
    return `<tr>
      <td>
        <div style="display:flex;align-items:center;gap:10px">
          <div class="cust-avatar" style="background:${avCol};color:#fff">${av}</div>
          <div>
            <div style="font-weight:600">${u.full_name}</div>
            <div style="font-size:11px;color:#94a3b8">ID: SKY-${u.user_id}</div>
          </div>
        </div>
      </td>
      <td>${u.email}</td>
      <td><span style="color:${col}">${ico} ${tier}</span></td>
      <td>${u.phone||'—'}</td>
      <td>${badge(u.role)}</td>
      <td>${badge('active')}</td>
      <td style="display:flex;gap:6px">
        <button class="btn btn-outline btn-sm" onclick="viewCustomer(${u.user_id})">Xem</button>
        <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.user_id},'${u.full_name.replace(/'/g,"\\'")}')">Xóa</button>
      </td>
    </tr>`;
  }).join('')||'<tr><td colspan="7" style="text-align:center;color:#94a3b8;padding:24px">Không có dữ liệu</td></tr>';
  const pag=document.getElementById('cust-pag');
  if(!pag)return;
  pag.innerHTML=`<div class="pag-info">Hiện thị 1 đến ${Math.min(custPage*CUST_PER_PAGE,total)} trong tổng số ${fmtNum(total)} khách hàng</div>
    <button class="pag-btn" onclick="custPage=Math.max(1,custPage-1);filterCustomers()">‹</button>
    ${[...Array(Math.min(pages,5)).keys()].map(i=>`<button class="pag-btn ${i+1===custPage?'active':''}" onclick="custPage=${i+1};filterCustomers()">${i+1}</button>`).join('')}
    <span>… ${pages}</span>
    <button class="pag-btn" onclick="custPage=Math.min(${pages},custPage+1);filterCustomers()">›</button>`;
}

function viewCustomer(id) { alert('Xem hồ sơ khách hàng #'+id+' (tính năng mở rộng)'); }
async function deleteUser(id, name) {
  if (!confirm(`Xóa người dùng "${name}"?`)) return;
  try { await api('DELETE','/users/'+id); renderCustomers(); } catch(e) { alert(e.message); }
}
function openAddCustomerModal() { alert('Tính năng thêm khách hàng đang phát triển'); }
function exportCustCSV() {
  const csv='ID,Tên,Email,SĐT,Vai trò\n'+allCustomers.map(u=>[u.user_id,u.full_name,u.email,u.phone||'',u.role].join(',')).join('\n');
  const a=document.createElement('a'); a.href='data:text/csv;charset=utf-8,\uFEFF'+encodeURIComponent(csv); a.download='customers.csv'; a.click();
}

/* ── MESSAGES ── */
async function renderMessages() {
  const el = document.getElementById('page-messages');
  el.innerHTML = `
  <div class="page-header"><div class="page-title">Tin nhắn hỗ trợ</div></div>
  <div class="chat-layout">
    <div class="conv-sidebar">
      <div style="padding:14px 16px;border-bottom:1px solid #f1f5f9;font-weight:700">Cuộc trò chuyện</div>
      <div class="conv-search"><input placeholder="Tìm kiếm..." oninput="filterConvs(this.value)"/></div>
      <div class="conv-list" id="conv-list"></div>
    </div>
    <div class="chat-main" id="chat-main">
      <div class="no-chat-placeholder"><div style="font-size:40px">💬</div>Chọn một cuộc trò chuyện</div>
    </div>
  </div>`;
  await loadConversations();
  msgPollTimer = setInterval(refreshMsgs, 3000);
}

async function loadConversations() {
  try {
    const convs = await api('GET','/messages/conversations');
    window._convs = convs;
    renderConvList(convs);
  } catch(_){}
}

function filterConvs(q) {
  renderConvList((window._convs||[]).filter(c=>c.full_name?.toLowerCase().includes(q.toLowerCase())));
}

function renderConvList(convs) {
  const list = document.getElementById('conv-list');
  if (!list) return;
  list.innerHTML = convs.length
    ? convs.map(c=>`<div class="conv-item ${currentConvId===c.user_id?'active':''}" onclick="openConv(${c.user_id},'${(c.full_name||'').replace(/'/g,"\\'")}','${c.email||''}')">
        <div class="conv-av">${(c.full_name||'?')[0]?.toUpperCase()}</div>
        <div class="conv-meta">
          <div class="conv-name">${c.full_name}</div>
          <div class="conv-last">${c.last_message||'...'}</div>
        </div>
        <div style="display:flex;flex-direction:column;align-items:flex-end;gap:4px">
          <div class="conv-time">${fmtDate(c.last_time)}</div>
          ${c.unread_count>0?`<div class="unread-dot"></div>`:''}
        </div>
      </div>`).join('')
    : '<div style="padding:24px;text-align:center;color:#94a3b8;font-size:13px">Chưa có tin nhắn</div>';
}

async function openConv(userId, name, email) {
  currentConvId = userId;
  renderConvList(window._convs||[]);
  const chat = document.getElementById('chat-main');
  if (!chat) return;
  chat.innerHTML = `
    <div class="chat-top">
      <div class="conv-av">${(name||'?')[0]?.toUpperCase()}</div>
      <div><div style="font-weight:600">${name}</div><div style="font-size:12px;color:#64748b">${email}</div></div>
    </div>
    <div class="msgs-area" id="msgs-area"></div>
    <div class="chat-input">
      <input id="chat-in" placeholder="Nhập tin nhắn..." onkeydown="if(event.key==='Enter')sendMsg(${userId})"/>
      <button onclick="sendMsg(${userId})">Gửi ↗</button>
    </div>`;
  await loadMsgs(userId);
}

async function loadMsgs(userId) {
  const area = document.getElementById('msgs-area');
  if (!area) return;
  try {
    const msgs = await api('GET','/messages/'+userId);
    area.innerHTML = msgs.length
      ? msgs.map(m=>{
          const isMe = m.sender_id===adminId;
          return `<div class="bubble-wrap ${isMe?'me':'them'}">
            <div class="bubble ${isMe?'me':'them'}">${m.content}</div>
            <div class="bubble-time">${fmtTime(m.created_at)}</div>
          </div>`;
        }).join('')
      : '<div style="text-align:center;color:#94a3b8;padding:20px">Chưa có tin nhắn</div>';
    area.scrollTop = area.scrollHeight;
  } catch(_){}
}

async function sendMsg(userId) {
  const input = document.getElementById('chat-in');
  const content = input?.value?.trim();
  if (!content) return;
  input.value='';
  try { await api('POST','/messages',{receiver_id:userId,content}); await loadMsgs(userId); }
  catch(e) { alert(e.message); }
}

async function refreshMsgs() {
  await loadConversations();
  if (currentConvId) await loadMsgs(currentConvId);
}

/* ── REVENUE ── */
async function renderRevenue() {
  const el = document.getElementById('page-revenue');
  el.innerHTML = `
  <div class="page-header">
    <div><div class="page-title">Báo cáo doanh thu</div><div class="page-sub">Thống kê và phân tích doanh thu.</div></div>
    <button class="btn btn-outline" onclick="exportRevCSV()">⬇ Xuất báo cáo</button>
  </div>
  <div class="stats-row" id="rev-stats"></div>
  <div class="rev-grid">
    <div class="card"><div class="card-title" style="margin-bottom:12px">Doanh thu theo trạng thái</div><div class="chart-placeholder">📊 Biểu đồ (tích hợp Chart.js để xem)</div></div>
    <div class="card"><div class="card-title" style="margin-bottom:12px">Đặt vé theo tháng</div><div class="chart-placeholder">📈 Biểu đồ đường</div></div>
  </div>
  <div class="card">
    <div class="card-title" style="margin-bottom:14px">Chi tiết thanh toán</div>
    <div class="tbl-wrap"><table>
      <thead><tr><th>#</th><th>BOOKING</th><th>HÀNH KHÁCH</th><th>SỐ TIỀN</th><th>PHƯƠNG THỨC</th><th>TRẠNG THÁI</th><th>NGÀY</th></tr></thead>
      <tbody id="pay-tbody"></tbody>
    </table></div>
  </div>`;
  try {
    const [payments, bookings] = await Promise.all([api('GET','/payments'), api('GET','/bookings')]);
    const total = bookings.reduce((s,b)=>s+ +b.total_amount,0);
    const confirmed = bookings.filter(b=>b.booking_status==='CONFIRMED');
    document.getElementById('rev-stats').innerHTML=`
      <div class="stat-card"><div class="stat-label">TỔNG DOANH THU</div><div class="stat-value">${fmtVND(total)}</div></div>
      <div class="stat-card"><div class="stat-label">GIAO DỊCH</div><div class="stat-value">${fmtNum(payments.length)}</div></div>
      <div class="stat-card"><div class="stat-label">ĐÃ XÁC NHẬN</div><div class="stat-value">${fmtNum(confirmed.length)}</div></div>
      <div class="stat-card"><div class="stat-label">TRUNG BÌNH/VÉ</div><div class="stat-value">${bookings.length?fmtVND(total/bookings.length):'—'}</div></div>`;
    document.getElementById('pay-tbody').innerHTML=payments.map(p=>`<tr>
      <td>${p.payment_id}</td>
      <td>#${p.booking_id}</td>
      <td>${(bookings.find(b=>b.booking_id===p.booking_id)||{}).full_name||'—'}</td>
      <td>${fmtVND(p.amount)}</td>
      <td>${badge(p.payment_method)||'—'}</td>
      <td>${badge(p.payment_status)}</td>
      <td>${fmtDate(p.payment_date)}</td>
    </tr>`).join('')||'<tr><td colspan="7" style="text-align:center;color:#94a3b8;padding:20px">Chưa có dữ liệu</td></tr>';
  } catch(e){console.error(e);}
}

function exportRevCSV() { alert('Xuất báo cáo CSV (tính năng đang phát triển)'); }

/* ── ALERTS ── */
async function renderAlerts() {
  const el = document.getElementById('page-alerts');
  el.innerHTML = `
  <div class="page-header">
    <div>
      <div class="page-title">🚨 Gửi thông báo sự cố</div>
      <div class="page-sub">Thông báo đến hành khách khi có sự cố hoặc thay đổi chuyến bay.</div>
    </div>
  </div>

  <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">

    <!-- Gửi theo chuyến bay -->
    <div class="card">
      <div class="card-title" style="margin-bottom:16px">✈ Thông báo theo chuyến bay</div>
      <p style="font-size:13px;color:#64748b;margin-bottom:16px">Gửi đến tất cả hành khách có vé trên chuyến bay được chọn.</p>
      <div class="form-group" style="margin-bottom:14px">
        <label>Chọn chuyến bay</label>
        <select id="alert-flight-id" style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none">
          <option value="">-- Đang tải... --</option>
        </select>
      </div>
      <div class="form-group" style="margin-bottom:14px">
        <label>Loại sự cố</label>
        <select id="alert-type" style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none" onchange="prefillAlert()">
          <option value="FLIGHT_ALERT">⚠️ Thay đổi chuyến bay</option>
          <option value="DELAYED">⏰ Chuyến bay bị trễ</option>
          <option value="CANCELLED">❌ Chuyến bay bị hủy</option>
          <option value="GATE_CHANGE">🚪 Thay đổi cổng khởi hành</option>
          <option value="CUSTOM">📝 Tùy chỉnh</option>
        </select>
      </div>
      <div class="form-group" style="margin-bottom:14px">
        <label>Tiêu đề thông báo</label>
        <input id="alert-title" type="text" placeholder="VD: ⚠️ Chuyến bay VN-001 bị trễ 2 giờ"
          style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none"/>
      </div>
      <div class="form-group" style="margin-bottom:16px">
        <label>Nội dung chi tiết</label>
        <textarea id="alert-body" rows="4" placeholder="Nhập nội dung thông báo..."
          style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none;resize:vertical"></textarea>
      </div>
      <div id="alert-flight-result" style="display:none;margin-bottom:12px"></div>
      <button class="btn btn-primary" style="width:100%" onclick="sendFlightAlert()">
        🚀 Gửi thông báo đến hành khách
      </button>
    </div>

    <!-- Gửi toàn hệ thống -->
    <div>
      <div class="card" style="margin-bottom:16px">
        <div class="card-title" style="margin-bottom:16px">📢 Thông báo toàn hệ thống</div>
        <p style="font-size:13px;color:#64748b;margin-bottom:16px">Gửi đến tất cả khách hàng trong hệ thống.</p>
        <div class="form-group" style="margin-bottom:14px">
          <label>Tiêu đề</label>
          <input id="broad-title" type="text" placeholder="VD: 🔧 Bảo trì hệ thống"
            style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none"/>
        </div>
        <div class="form-group" style="margin-bottom:16px">
          <label>Nội dung</label>
          <textarea id="broad-body" rows="3" placeholder="Nội dung thông báo..."
            style="width:100%;padding:10px 12px;border:1px solid #dde3ed;border-radius:8px;font-size:14px;outline:none;resize:vertical"></textarea>
        </div>
        <div id="broad-result" style="display:none;margin-bottom:12px"></div>
        <button class="btn btn-danger" style="width:100%" onclick="sendBroadcast()">
          📣 Gửi đến tất cả khách hàng
        </button>
      </div>

      <!-- Mẫu nhanh -->
      <div class="card">
        <div class="card-title" style="margin-bottom:12px">⚡ Mẫu thông báo nhanh</div>
        <div style="display:flex;flex-direction:column;gap:8px">
          ${[
            ['⏰ Trễ chuyến','⏰ Chuyến bay bị trễ','Chuyến bay của bạn bị trễ do điều kiện thời tiết. Vui lòng theo dõi thông báo cập nhật.','DELAYED'],
            ['❌ Hủy chuyến','❌ Chuyến bay bị hủy','Chuyến bay của bạn đã bị hủy. Chúng tôi xin lỗi vì sự bất tiện này. Vui lòng liên hệ để được hỗ trợ đổi/hoàn vé.','CANCELLED'],
            ['🚪 Đổi cổng','🚪 Thay đổi cổng khởi hành','Cổng khởi hành của chuyến bay bạn đã thay đổi. Vui lòng kiểm tra bảng thông tin tại sân bay.','GATE_CHANGE'],
            ['🛫 Bay sớm','🛫 Chuyến bay khởi hành sớm hơn dự kiến','Chuyến bay của bạn sẽ khởi hành sớm hơn dự kiến. Vui lòng có mặt tại cổng trước 45 phút.','FLIGHT_ALERT'],
          ].map(([label, t, b, type]) =>
            `<button class="btn btn-outline" style="text-align:left;font-size:13px" onclick="applyTemplate('${t}','${b}','${type}')">${label}</button>`
          ).join('')}
        </div>
      </div>
    </div>
  </div>`;

  // Load danh sách chuyến bay
  try {
    const flights = await api('GET', '/flights');
    const sel = document.getElementById('alert-flight-id');
    sel.innerHTML = '<option value="">-- Chọn chuyến bay --</option>' +
      flights.map(f => `<option value="${f.flight_id}">[${f.status}] ${f.flight_code} — ${f.departure_airport} → ${f.arrival_airport} (${fmtDate(f.departure_time)})</option>`).join('');
  } catch(e) {}
}

function prefillAlert() {
  const type = document.getElementById('alert-type')?.value;
  const flightSel = document.getElementById('alert-flight-id');
  const code = flightSel?.options[flightSel.selectedIndex]?.text?.match(/\w+-\w+/)?.[0] || 'chuyến bay';
  const templates = {
    DELAYED:    [`⏰ Chuyến bay ${code} bị trễ`, `Chuyến bay ${code} bị trễ do điều kiện thời tiết bất lợi. Chúng tôi dự kiến khởi hành muộn hơn 2 giờ. Xin lỗi vì sự bất tiện này.`],
    CANCELLED:  [`❌ Chuyến bay ${code} bị hủy`, `Chuyến bay ${code} đã bị hủy. Chúng tôi sẽ liên hệ để hỗ trợ đổi hoặc hoàn vé cho bạn trong vòng 24 giờ.`],
    GATE_CHANGE:[`🚪 Thay đổi cổng — ${code}`, `Cổng khởi hành của chuyến ${code} đã thay đổi. Vui lòng kiểm tra màn hình thông tin tại sân bay để biết cổng mới.`],
    FLIGHT_ALERT:[`⚠️ Thay đổi lịch bay — ${code}`, `Có thay đổi về lịch trình chuyến bay ${code}. Vui lòng kiểm tra ứng dụng hoặc liên hệ hỗ trợ để biết thêm chi tiết.`],
  };
  if (templates[type]) {
    document.getElementById('alert-title').value = templates[type][0];
    document.getElementById('alert-body').value  = templates[type][1];
  }
}

function applyTemplate(title, body, type) {
  document.getElementById('alert-title').value = title;
  document.getElementById('alert-body').value  = body;
  document.getElementById('alert-type').value  = type;
}

async function sendFlightAlert() {
  const flight_id = document.getElementById('alert-flight-id').value;
  const title     = document.getElementById('alert-title').value.trim();
  const body      = document.getElementById('alert-body').value.trim();
  const type      = document.getElementById('alert-type').value;
  const resEl     = document.getElementById('alert-flight-result');

  if (!flight_id) { showAlertResult(resEl, 'Vui lòng chọn chuyến bay', false); return; }
  if (!title || !body) { showAlertResult(resEl, 'Vui lòng nhập tiêu đề và nội dung', false); return; }

  try {
    const res = await api('POST', '/notifications/broadcast-flight', { flight_id: +flight_id, title, body, type });
    showAlertResult(resEl, `✅ ${res.message}`, true);
    // Cập nhật trạng thái chuyến bay nếu là DELAYED/CANCELLED
    if (type === 'DELAYED' || type === 'CANCELLED') {
      const newStatus = type === 'DELAYED' ? 'DELAYED' : 'CANCELLED';
      try { await api('PUT', '/flights/' + flight_id, { status: newStatus }); } catch(_) {}
    }
  } catch(e) {
    showAlertResult(resEl, '❌ ' + e.message, false);
  }
}

async function sendBroadcast() {
  const title = document.getElementById('broad-title').value.trim();
  const body  = document.getElementById('broad-body').value.trim();
  const resEl = document.getElementById('broad-result');

  if (!title || !body) { showAlertResult(resEl, 'Vui lòng nhập tiêu đề và nội dung', false); return; }
  if (!confirm(`Gửi thông báo đến tất cả khách hàng?\n"${title}"`)) return;

  try {
    const res = await api('POST', '/notifications/broadcast-all', { title, body });
    showAlertResult(resEl, `✅ ${res.message}`, true);
  } catch(e) {
    showAlertResult(resEl, '❌ ' + e.message, false);
  }
}

function showAlertResult(el, msg, success) {
  el.style.display = 'block';
  el.style.cssText = `display:block;padding:10px 14px;border-radius:8px;font-size:13px;font-weight:600;
    background:${success ? '#e8f5e9' : '#ffeaea'};color:${success ? '#2e7d32' : '#c62828'}`;
  el.textContent = msg;
}

/* ── SETTINGS ── */
function renderSettings() {
  document.getElementById('page-settings').innerHTML=`
  <div class="page-title" style="margin-bottom:24px">Cài đặt hệ thống</div>
  <div class="card" style="max-width:560px">
    <div class="card-title" style="margin-bottom:16px">Thông tin quản trị viên</div>
    <div class="form-grid">
      <div class="form-group"><label>Họ tên</label><input value="${adminName}" id="set-name"/></div>
      <div class="form-group"><label>Email</label><input value="" id="set-email" placeholder="admin@skyops.com"/></div>
    </div>
    <div style="margin-top:10px"><button class="btn btn-primary" onclick="alert('Lưu thành công!')">Lưu thay đổi</button></div>
  </div>
  <div class="card" style="max-width:560px;margin-top:16px">
    <div class="card-title" style="margin-bottom:16px">Kết nối API</div>
    <div class="form-group" style="margin-bottom:12px"><label>API Base URL</label><input value="${API}" id="set-api"/></div>
    <button class="btn btn-outline btn-sm" onclick="testAPI()">Kiểm tra kết nối</button>
  </div>`;
}
async function testAPI() {
  try { await api('GET','/health'); alert('✅ Kết nối thành công!'); }
  catch(e) { alert('❌ Lỗi: '+e.message); }
}

/* ── BOOT ── */
if (token) initApp();
else { document.getElementById('login-page').style.display='flex'; }
