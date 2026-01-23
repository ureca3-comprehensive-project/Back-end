// ====== 설정 ======
const API_BASE = ""; // 같은 서버면 빈 문자열. 분리 배포면 "http://localhost:8080" 같은 값으로 바꿔주세요.

// 엔드포인트는 "당신의 백엔드 경로"에 맞게 여기만 조정하면 됩니다.
const EP = {
  dashboard: "/api/admin/dashboard",
  recentFailures: "/api/admin/messages/recent-failures",

  billingList: "/api/admin/billing-statements",
  billingDetail: (id) => `/api/admin/billing-statements/${id}`,

  msgList: "/api/admin/messages",
  msgDetail: (id) => `/api/admin/messages/${id}`,

  tplList: "/messages/template", // 예: 기존 컨트롤러가 /messages/template 일 가능성 높음
  tplCreate: "/messages/template",
  tplUpdate: (id) => `/messages/template/${id}`,
  tplDelete: (id) => `/messages/template/${id}`,
  tplPreview: "/messages/template/preview", // 없다면 백엔드에 맞춰 변경

  batchRuns: "/api/admin/batch/runs",
  batchTrigger: "/api/admin/batch/run",

  failList: "/api/admin/monitor/failures",
  failSummary: "/api/admin/monitor/failures/summary",
  failTrend: "/api/admin/monitor/failures/trend",
};

// ====== 유틸 ======
const $ = (sel, root=document) => root.querySelector(sel);
const $$ = (sel, root=document) => Array.from(root.querySelectorAll(sel));

function fmt(n){
  if (n === null || n === undefined) return "-";
  const num = Number(n);
  if (Number.isNaN(num)) return String(n);
  return num.toLocaleString("ko-KR");
}
function fmtDt(s){
  if (!s) return "-";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return String(s);
  return d.toLocaleString("ko-KR");
}
function badge(status){
  const s = String(status || "").toUpperCase();
  if (["SUCCESS","OK","SENT"].includes(s)) return `<span class="badge good">SUCCESS</span>`;
  if (["FAIL","FAILED","ERROR"].includes(s)) return `<span class="badge bad">FAIL</span>`;
  if (["RETRY","RETRYING","PENDING"].includes(s)) return `<span class="badge warn">${s}</span>`;
  return `<span class="badge brand">${s || "-"}</span>`;
}

async function fetchJson(path, opt={}){
  const url = API_BASE + path;
  const res = await fetch(url, {
    headers: {"Content-Type":"application/json", ...(opt.headers||{})},
    ...opt
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json();
}

// 백엔드 응답이 ApiResponse 형태({success,data,error})일 때 data만 추출
function unwrap(resp){
  if (resp && typeof resp === "object" && "success" in resp && "data" in resp) return resp.data;
  return resp;
}

// ====== 레이아웃 공통 ======
function setActiveNav(){
  const page = document.body.dataset.page;
  $$(`.nav a`).forEach(a=>{
    const p = a.dataset.page;
    a.classList.toggle("active", p === page);
  });
}

function setMetaPill(){
  const el = $("#metaPill");
  if (!el) return;
  const now = new Date();
  el.innerHTML = `<strong>NOW</strong><span>${now.toLocaleString("ko-KR")}</span>`;
}

// ====== 모달 ======
function openModal(title, html){
  $("#modalTitle").textContent = title || "상세";
  $("#modalBody").innerHTML = html || "";
  $("#modal").classList.add("open");
}
function closeModal(){ $("#modal").classList.remove("open"); }
window.__closeModal = closeModal;

// ====== 샘플 데이터(백엔드 미연동이어도 화면 확인 가능) ======
const mock = {
  dashboard: {
    todayBillingCount: 128,
    msgSuccess: 1092,
    msgFail: 37,
    channelRatio: { EMAIL: 62, SMS: 28, PUSH: 10 },
    batch: { running: 1, lastRunAt: new Date().toISOString(), lastStatus: "SUCCESS" }
  },
  recentFailures: [
    { messageId: 9012, channel:"EMAIL", provider:"SES", status:"FAIL", errorCode:"TPL_MISSING_VAR", createdAt:new Date().toISOString() },
    { messageId: 9015, channel:"SMS", provider:"NCP-SENS", status:"FAIL", errorCode:"PROVIDER_429", createdAt:new Date().toISOString() },
  ],
  billingList: [
    { id: 1001, userId: 21, period:"2026-01", status:"ISSUED", amount: 34800, createdAt:new Date().toISOString() },
    { id: 1002, userId: 22, period:"2026-01", status:"PENDING", amount: 57200, createdAt:new Date().toISOString() },
  ],
  msgList: [
    { id: 5001, status:"SUCCESS", channel:"EMAIL", provider:"SES", to:"user1@test.com", templateId: 12, createdAt:new Date().toISOString() },
    { id: 5002, status:"RETRY", channel:"SMS", provider:"NCP-SENS", to:"010-****-1234", templateId: 7, createdAt:new Date().toISOString() },
    { id: 5003, status:"FAIL", channel:"PUSH", provider:"FCM", to:"deviceToken...", templateId: 3, createdAt:new Date().toISOString() },
  ],
  templates: [
    { id: 12, name:"청구서 안내", channel:"EMAIL", version: 3, updatedAt:new Date().toISOString() },
    { id: 7, name:"납부 요청", channel:"SMS", version: 1, updatedAt:new Date().toISOString() },
  ],
  batchRuns: [
    { id:"run-20260122-01", job:"BillingBatch", status:"SUCCESS", startedAt:new Date(Date.now()-1000*60*20).toISOString(), endedAt:new Date().toISOString() },
    { id:"run-20260121-01", job:"RetryBatch", status:"SUCCESS", startedAt:new Date(Date.now()-1000*60*60*3).toISOString(), endedAt:new Date(Date.now()-1000*60*60*3+1000*60*7).toISOString() },
  ],
  monitorSummary: [
    { errorCode:"TPL_MISSING_VAR", count: 12 },
    { errorCode:"PROVIDER_429", count: 9 },
    { errorCode:"TIMEOUT", count: 6 },
  ],
  monitorTrend: [
    { date:"2026-01-18", fail: 3 },
    { date:"2026-01-19", fail: 8 },
    { date:"2026-01-20", fail: 5 },
    { date:"2026-01-21", fail: 11 },
    { date:"2026-01-22", fail: 7 },
  ],
  failures: [
    { messageId: 7001, channel:"EMAIL", errorCode:"TIMEOUT", provider:"SES", createdAt:new Date().toISOString() },
    { messageId: 7002, channel:"SMS", errorCode:"PROVIDER_429", provider:"NCP-SENS", createdAt:new Date().toISOString() },
  ]
};

// ====== 페이지별 로더 ======
async function loadDashboard(){
  let sum, fails;
  try{
    sum = unwrap(await fetchJson(EP.dashboard));
    fails = unwrap(await fetchJson(EP.recentFailures));
  }catch(e){
    sum = mock.dashboard;
    fails = mock.recentFailures;
  }

  $("#kpiBilling").textContent = fmt(sum.todayBillingCount);
  $("#kpiSuccess").textContent = fmt(sum.msgSuccess);
  $("#kpiFail").textContent = fmt(sum.msgFail);

  const br = sum.channelRatio || {EMAIL:0,SMS:0,PUSH:0};
  const max = Math.max(br.EMAIL||0, br.SMS||0, br.PUSH||0, 1);

  $("#barEmail").style.height = `${Math.round((br.EMAIL||0)/max*100)}%`;
  $("#barSms").style.height   = `${Math.round((br.SMS||0)/max*100)}%`;
  $("#barPush").style.height  = `${Math.round((br.PUSH||0)/max*100)}%`;

  $("#barEmailVal").textContent = `${br.EMAIL||0}%`;
  $("#barSmsVal").textContent   = `${br.SMS||0}%`;
  $("#barPushVal").textContent  = `${br.PUSH||0}%`;

  const b = sum.batch || {};
  $("#batchBadge").innerHTML = badge(b.lastStatus || "UNKNOWN");
  $("#batchMeta").textContent = `Running: ${fmt(b.running||0)} · Last: ${fmtDt(b.lastRunAt)}`;

  const rows = (fails||[]).map(f=>`
    <tr>
      <td>${fmt(f.messageId)}</td>
      <td>${f.channel||"-"}</td>
      <td>${f.provider||"-"}</td>
      <td>${badge(f.status)}</td>
      <td>${f.errorCode||"-"}</td>
      <td class="small">${fmtDt(f.createdAt)}</td>
    </tr>
  `).join("");
  $("#recentFailTbody").innerHTML = rows || `<tr><td colspan="6" class="small">최근 실패가 없습니다.</td></tr>`;
}

async function loadBilling(){
  async function query(){
    const params = new URLSearchParams();
    const from = $("#from").value; const to = $("#to").value;
    const userId = $("#userId").value; const status = $("#status").value;
    if (from) params.set("from", from);
    if (to) params.set("to", to);
    if (userId) params.set("userId", userId);
    if (status) params.set("status", status);

    let list;
    try{ list = unwrap(await fetchJson(`${EP.billingList}?${params.toString()}`)); }
    catch(e){ list = mock.billingList; }

    $("#billingTbody").innerHTML = (list||[]).map(x=>`
      <tr>
        <td>${fmt(x.id)}</td>
        <td>${fmt(x.userId)}</td>
        <td>${x.period||"-"}</td>
        <td>${x.status||"-"}</td>
        <td>${fmt(x.amount)}</td>
        <td class="small">${fmtDt(x.createdAt)}</td>
        <td><button class="btn" data-detail="${x.id}">상세</button></td>
      </tr>
    `).join("") || `<tr><td colspan="7" class="small">결과가 없습니다.</td></tr>`;

    $$("button[data-detail]").forEach(btn=>{
      btn.onclick = async ()=>{
        const id = btn.dataset.detail;
        let detail;
        try{ detail = unwrap(await fetchJson(EP.billingDetail(id))); }
        catch(e){ detail = { ...mock.billingList.find(v=>String(v.id)===String(id)), items:[{name:"기본요금",amount:22000},{name:"부가서비스",amount:12800}]}; }

        openModal(`요금 명세서 #${id}`, `
          <div class="grid one">
            <div class="card">
              <h3>요약</h3>
              <div class="small">User: <b>${fmt(detail.userId)}</b> · Period: <b>${detail.period||"-"}</b> · Status: <b>${detail.status||"-"}</b></div>
              <div class="kpi-num" style="margin-top:8px">${fmt(detail.amount)}원</div>
            </div>
            <div class="card">
              <h3>항목</h3>
              <table class="table">
                <thead><tr><th>항목</th><th>금액</th></tr></thead>
                <tbody>
                  ${(detail.items||[]).map(it=>`<tr><td>${it.name}</td><td>${fmt(it.amount)}원</td></tr>`).join("") || `<tr><td colspan="2" class="small">항목 없음</td></tr>`}
                </tbody>
              </table>
              <hr class="sep">
              <div class="small">연관 메시지 발송 이력은 “메시지 발송 이력” 화면에서 billId로 조회하도록 연결하면 좋아요.</div>
            </div>
          </div>
        `);
      };
    });
  }

  $("#searchBtn").onclick = query;
  $("#resetBtn").onclick = ()=>{ $("#from").value=""; $("#to").value=""; $("#userId").value=""; $("#status").value=""; query(); };
  query();
}

async function loadMessages(){
  async function query(){
    const params = new URLSearchParams();
    const st = $("#mStatus").value;
    const ch = $("#mChannel").value;
    const q = $("#mQ").value;
    if (st) params.set("status", st);
    if (ch) params.set("channel", ch);
    if (q) params.set("q", q);

    let list;
    try{ list = unwrap(await fetchJson(`${EP.msgList}?${params.toString()}`)); }
    catch(e){ list = mock.msgList; }

    $("#msgTbody").innerHTML = (list||[]).map(x=>`
      <tr data-mid="${x.id}">
        <td>${fmt(x.id)}</td>
        <td>${badge(x.status)}</td>
        <td>${x.channel||"-"}</td>
        <td>${x.provider||"-"}</td>
        <td>${x.to||"-"}</td>
        <td>${fmt(x.templateId)}</td>
        <td class="small">${fmtDt(x.createdAt)}</td>
      </tr>
    `).join("") || `<tr><td colspan="7" class="small">결과가 없습니다.</td></tr>`;

    $$("tr[data-mid]").forEach(tr=>{
      tr.onclick = async ()=>{
        const id = tr.dataset.mid;
        let detail;
        try{ detail = unwrap(await fetchJson(EP.msgDetail(id))); }
        catch(e){
          detail = {
            id, status: (list.find(v=>String(v.id)===String(id))||{}).status,
            channel: (list.find(v=>String(v.id)===String(id))||{}).channel,
            provider: (list.find(v=>String(v.id)===String(id))||{}).provider,
            to: (list.find(v=>String(v.id)===String(id))||{}).to,
            payload: { userName:"민석", amount:34800 },
            attempts: [
              { attemptNo:1, status:"FAIL", httpStatus:429, providerMessageId:"x-1", createdAt:new Date(Date.now()-1000*60*4).toISOString() },
              { attemptNo:2, status:"RETRY", httpStatus:0, providerMessageId:"-", createdAt:new Date().toISOString() },
            ]
          };
        }

        openModal(`메시지 상세 #${id}`, `
          <div class="grid two">
            <div class="card">
              <h3>요약</h3>
              <div class="small">Status: ${badge(detail.status)} · Channel: <b>${detail.channel}</b> · Provider: <b>${detail.provider}</b></div>
              <hr class="sep">
              <div class="small"><b>To</b>: ${detail.to||"-"}</div>
              <div class="small"><b>Payload</b>:</div>
              <pre style="margin:8px 0 0; padding:12px; border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03); overflow:auto;">${escapeHtml(JSON.stringify(detail.payload||{}, null, 2))}</pre>
            </div>
            <div class="card">
              <h3>시도(Attempts)</h3>
              <table class="table">
                <thead><tr><th>No</th><th>Status</th><th>HTTP</th><th>ProviderMsgId</th><th>At</th></tr></thead>
                <tbody>
                  ${(detail.attempts||[]).map(a=>`
                    <tr>
                      <td>${fmt(a.attemptNo)}</td>
                      <td>${badge(a.status)}</td>
                      <td>${fmt(a.httpStatus)}</td>
                      <td class="small">${a.providerMessageId||"-"}</td>
                      <td class="small">${fmtDt(a.createdAt)}</td>
                    </tr>
                  `).join("") || `<tr><td colspan="5" class="small">시도 이력 없음</td></tr>`}
                </tbody>
              </table>
              <hr class="sep">
              <div class="small">이 화면이 “재시도/비동기 처리 설계” 설명의 핵심이므로, 백엔드에서 attempts/queueStatus/nextRetryAt 같은 필드를 내려주면 완성도가 확 올라갑니다.</div>
            </div>
          </div>
        `);
      };
    });
  }

  $("#mSearchBtn").onclick = query;
  $("#mResetBtn").onclick = ()=>{ $("#mStatus").value=""; $("#mChannel").value=""; $("#mQ").value=""; query(); };
  query();
}

function escapeHtml(s){
  return String(s)
    .replaceAll("&","&amp;")
    .replaceAll("<","&lt;")
    .replaceAll(">","&gt;")
    .replaceAll('"',"&quot;")
    .replaceAll("'","&#039;");
}

async function loadTemplates(){
  let list;
  try{ list = unwrap(await fetchJson(EP.tplList)); }
  catch(e){ list = mock.templates; }

  function render(){
    $("#tplTbody").innerHTML = (list||[]).map(t=>`
      <tr>
        <td>${fmt(t.id)}</td>
        <td><b>${t.name||"-"}</b><div class="small">v${fmt(t.version||1)}</div></td>
        <td>${t.channel||"-"}</td>
        <td class="small">${fmtDt(t.updatedAt)}</td>
        <td style="white-space:nowrap;">
          <button class="btn" data-edit="${t.id}">수정</button>
          <button class="btn" data-del="${t.id}">삭제</button>
        </td>
      </tr>
    `).join("") || `<tr><td colspan="5" class="small">템플릿이 없습니다.</td></tr>`;

    $$("button[data-edit]").forEach(b=>{
      b.onclick = ()=>{
        const id = b.dataset.edit;
        const t = list.find(x=>String(x.id)===String(id));
        openTplModal("템플릿 수정", t);
      };
    });
    $$("button[data-del]").forEach(b=>{
      b.onclick = async ()=>{
        const id = b.dataset.del;
        if (!confirm(`템플릿 #${id} 삭제할까요?`)) return;

        try{
          await fetchJson(EP.tplDelete(id), {method:"DELETE"});
          list = list.filter(x=>String(x.id)!==String(id));
          render();
        }catch(e){
          // 데모 모드
          list = list.filter(x=>String(x.id)!==String(id));
          render();
        }
      };
    });
  }

  async function saveTpl(payload, id=null){
    if (id){
      try{
        await fetchJson(EP.tplUpdate(id), {method:"PATCH", body: JSON.stringify(payload)});
      }catch(e){}
      list = list.map(x=>String(x.id)===String(id)
        ? {...x, ...payload, updatedAt: new Date().toISOString(), version:(x.version||1)+1}
        : x
      );
    }else{
      try{
        const created = unwrap(await fetchJson(EP.tplCreate, {method:"POST", body: JSON.stringify(payload)}));
        list = [created, ...list];
      }catch(e){
        const newId = Math.max(0, ...list.map(x=>Number(x.id)||0)) + 1;
        list = [{id:newId, version:1, updatedAt:new Date().toISOString(), ...payload}, ...list];
      }
    }
    closeModal();
    render();
  }

  async function previewTpl(){
    const id = $("#pvTplId").value;
    const vars = $("#pvVars").value;
    let parsed = {};
    try{ parsed = JSON.parse(vars||"{}"); }catch(e){ alert("변수 JSON이 올바르지 않습니다."); return; }

    try{
      const res = unwrap(await fetchJson(EP.tplPreview, {method:"POST", body: JSON.stringify({ templateId: Number(id), variables: parsed })}));
      openModal("미리보기 결과", `
        <div class="card">
          <h3>Subject</h3>
          <pre style="margin:0; padding:12px; border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03); overflow:auto;">${escapeHtml(res.subject||"-")}</pre>
          <hr class="sep">
          <h3>Body</h3>
          <pre style="margin:0; padding:12px; border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03); overflow:auto;">${escapeHtml(res.body||"-")}</pre>
        </div>
      `);
    }catch(e){
      openModal("미리보기(데모)", `
        <div class="card">
          <h3>Subject</h3>
          <pre style="margin:0; padding:12px; border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03); overflow:auto;">[데모] 템플릿 #${id} 제목</pre>
          <hr class="sep">
          <h3>Body</h3>
          <pre style="margin:0; padding:12px; border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03); overflow:auto;">[데모] variables:\n${escapeHtml(JSON.stringify(parsed,null,2))}</pre>
        </div>
      `);
    }
  }

  function openTplModal(title, t){
    const isEdit = !!t;
    openModal(title, `
      <div class="grid one">
        <div class="card">
          <h3>기본 정보</h3>
          <div class="form">
            <div class="field">
              <label>이름</label>
              <input id="tplName" class="input" value="${escapeHtml(t?.name||"")}" placeholder="예) 청구서 안내">
            </div>
            <div class="field">
              <label>채널</label>
              <select id="tplChannel">
                ${["EMAIL","SMS","PUSH"].map(ch=>`<option ${t?.channel===ch?"selected":""}>${ch}</option>`).join("")}
              </select>
            </div>
          </div>
          <hr class="sep">
          <div class="small">subject/body 템플릿은 백엔드 DTO에 맞춰 추가 입력칸을 늘리면 됩니다.</div>
        </div>
      </div>
    `);

    $("#modalSave").onclick = async ()=>{
      const payload = { name: $("#tplName").value.trim(), channel: $("#tplChannel").value };
      if (!payload.name){ alert("이름은 필수입니다."); return; }
      await saveTpl(payload, t?.id);
    };
  }

  $("#tplNewBtn").onclick = ()=> openTplModal("템플릿 생성", null);
  $("#tplPreviewBtn").onclick = previewTpl;

  // 모달 저장 버튼 기본값(페이지마다 덮어씌움)
  $("#modalSave").onclick = ()=> closeModal();

  render();
}

async function loadBatch(){
  async function query(){
    let runs;
    try{ runs = unwrap(await fetchJson(EP.batchRuns)); }
    catch(e){ runs = mock.batchRuns; }

    $("#batchTbody").innerHTML = (runs||[]).map(r=>`
      <tr>
        <td>${r.id||"-"}</td>
        <td>${r.job||"-"}</td>
        <td>${badge(r.status)}</td>
        <td class="small">${fmtDt(r.startedAt)}</td>
        <td class="small">${fmtDt(r.endedAt)}</td>
      </tr>
    `).join("") || `<tr><td colspan="5" class="small">실행 이력이 없습니다.</td></tr>`;
  }

  $("#batchRunBtn").onclick = async ()=>{
    try{
      await fetchJson(EP.batchTrigger, {method:"POST", body: JSON.stringify({ job: $("#batchJob").value })});
      alert("배치 실행 요청 완료");
    }catch(e){
      alert("배치 실행(데모): 백엔드 엔드포인트 연결 전입니다.");
    }
    query();
  };

  query();
}

async function loadMonitoring(){
  let sum, trend, fails;
  try{
    sum = unwrap(await fetchJson(EP.failSummary));
    trend = unwrap(await fetchJson(EP.failTrend));
    fails = unwrap(await fetchJson(EP.failList));
  }catch(e){
    sum = mock.monitorSummary;
    trend = mock.monitorTrend;
    fails = mock.failures;
  }

  $("#sumTbody").innerHTML = (sum||[]).map(x=>`
    <tr><td><b>${x.errorCode}</b></td><td>${fmt(x.count)}</td></tr>
  `).join("") || `<tr><td colspan="2" class="small">데이터 없음</td></tr>`;

  // 간단 SVG 라인차트
  const w = 520, h = 160, pad = 18;
  const arr = (trend||[]).map(x=>({x: x.date, y: Number(x.fail)||0}));
  const maxY = Math.max(...arr.map(v=>v.y), 1);
  const pts = arr.map((v,i)=>{
    const px = pad + (arr.length<=1 ? 0 : (i*(w-2*pad)/(arr.length-1)));
    const py = h-pad - (v.y/maxY)*(h-2*pad);
    return [px, py];
  });

  const d = pts.map((p,i)=>`${i===0?"M":"L"} ${p[0].toFixed(1)} ${p[1].toFixed(1)}`).join(" ");
  $("#trendBox").innerHTML = `
    <svg viewBox="0 0 ${w} ${h}" width="100%" height="160" style="border:1px solid var(--line); border-radius:14px; background:rgba(17,24,39,.03)">
      <path d="${d}" fill="none" stroke="rgba(236,0,140,.95)" stroke-width="3" />
      ${pts.map(p=>`<circle cx="${p[0]}" cy="${p[1]}" r="4" fill="rgba(197,1,61,.95)" />`).join("")}
      <text x="${pad}" y="${pad}" font-size="12" fill="rgba(17,24,39,.55)">최근 실패 추이</text>
      <text x="${w-pad}" y="${pad}" text-anchor="end" font-size="12" fill="rgba(17,24,39,.55)">max ${maxY}</text>
    </svg>
    <div class="small" style="margin-top:8px;">(백엔드에서 날짜별 실패 집계를 내려주면 이 그래프가 실제 모니터링으로 동작합니다)</div>
  `;

  $("#failTbody").innerHTML = (fails||[]).map(f=>`
    <tr>
      <td>${fmt(f.messageId)}</td>
      <td>${f.channel||"-"}</td>
      <td><b>${f.errorCode||"-"}</b></td>
      <td>${f.provider||"-"}</td>
      <td class="small">${fmtDt(f.createdAt)}</td>
    </tr>
  `).join("") || `<tr><td colspan="5" class="small">실패 데이터가 없습니다.</td></tr>`;
}

// ====== 부트스트랩 ======
document.addEventListener("DOMContentLoaded", ()=>{
  setActiveNav();
  setMetaPill();

  // 모달 닫기
  $("#modalClose").onclick = closeModal;
  $("#modalCancel").onclick = closeModal;

  const page = document.body.dataset.page;
  if (page === "dashboard") loadDashboard();
  if (page === "billing") loadBilling();
  if (page === "messages") loadMessages();
  if (page === "templates") loadTemplates();
  if (page === "batch") loadBatch();
  if (page === "monitoring") loadMonitoring();
});