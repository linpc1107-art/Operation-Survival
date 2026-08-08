let token = '';
let player = null;
let currentWeapon = null;
let monsters = [];
let selectedMonster = null;
let stageMetaList = [];

function resolveApiUrl(path) {
  if (!path) {
    return '/api';
  }

  if (path.startsWith('/')) {
    return path;
  }

  return '/api/' + path.replace(/^\/+/, '');
}

function setMessage(message, type = 'info') {
  const el = document.getElementById('appMessage');
  el.textContent = message;
  el.className = 'message';
  if (type === 'success') {
    el.classList.add('success');
  }
  if (type === 'warn') {
    el.classList.add('warn');
  }
  if (type === 'error') {
    el.classList.add('error');
  }
}

function updateAuthUI() {
  const loggedIn = !!token;
  document.getElementById('tokenBox').textContent = '狀態：' + (loggedIn ? '已登入' : '尚未登入');
  document.querySelectorAll('.requires-login').forEach(button => {
    button.disabled = !loggedIn;
  });

  if (!loggedIn) {
    document.getElementById('progressInfo').textContent = '尚未登入，無法讀取進度';
  }
}

function saveToken() {
  localStorage.setItem('survivalToken', token || '');
}

function restoreToken() {
  token = localStorage.getItem('survivalToken') || '';
  updateAuthUI();
}

function updateGuideText(message) {
  document.getElementById('gameGuide').textContent = message;
}

function lockStageOptions(maxStage) {
  const select = document.getElementById('stageSelect');
  Array.from(select.options).forEach(option => {
    const stage = Number(option.value);
    option.disabled = stage > maxStage;
    option.textContent = stage > maxStage ? `第 ${stage} 關（未解鎖）` : `第 ${stage} 關`;
  });

  if (Number(select.value) > maxStage) {
    select.value = String(maxStage);
  }
}

function setBattleMessage(message) {
  const el = document.getElementById('battleAnimation');
  el.textContent = message;
  el.classList.remove('animating');
  void el.offsetWidth;
  el.classList.add('animating');
}

function getAuthInputs() {
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  return { username, password };
}

function validateAuthInputs(username, password) {
  if (!username) {
    return '請輸入帳號';
  }
  if (username.length < 4 || username.length > 20) {
    return '帳號長度必須在 4 到 20 個字元之間';
  }
  if (!password) {
    return '請輸入密碼';
  }
  if (password.length < 6) {
    return '密碼長度至少需要 6 個字元';
  }
  return null;
}

async function apiRequest(url, options = {}) {
  const resolvedUrl = resolveApiUrl(url);
  const res = await fetch(resolvedUrl, options);
  let data = null;

  try {
    data = await res.json();
  } catch (e) {
    data = null;
  }

  if (!res.ok) {
    const message = (data && (data.message || data.error)) || `請求失敗（${res.status}）`;
    if (res.status === 401 || res.status === 403) {
      token = '';
      saveToken();
      updateAuthUI();
    }
    throw new Error(message);
  }

  return data;
}

function resetCombatView() {
  document.getElementById('battleAnimation').textContent = '等待戰鬥開始...';
  document.getElementById('battleLog').textContent = '尚未進行戰鬥';
}

function difficultyProfile(mode) {
  switch ((mode || 'NORMAL').toUpperCase()) {
    case 'CASUAL':
      return { label: 'CASUAL', art: '[+==]', text: '輕鬆模式：玩家輸出較高，怪物壓力較低。' };
    case 'HARDCORE':
      return { label: 'HARDCORE', art: '[##!]', text: '硬派模式：怪物更耐打、更危險，但獎勵更高。' };
    default:
      return { label: 'NORMAL', art: '[===]', text: '標準模式：平衡的傷害、獎勵與壓力。' };
  }
}

function weaponProfile(name) {
  switch ((name || 'Pistol').toUpperCase()) {
    case 'BLADE':
      return { type: 'BLADE', art: '///>', text: '近戰刀鋒：出手俐落，適合技能爆發。' };
    case 'SPEAR':
      return { type: 'SPEAR', art: '--->', text: '長槍型態：線性推進，擅長穩定輸出。' };
    default:
      return { type: 'PISTOL', art: '=== >', text: '手槍型態：穩定、泛用，適合初次推圖。' };
  }
}

function monsterProfile(monster) {
  if (!monster) {
    return { art: '[ ? ]', text: '尚未鎖定目標' };
  }

  const name = (monster.name || '').toUpperCase();
  if (name.includes('DRAGON')) {
    return { art: '/\\^/\\', text: '高空壓制型 Boss' };
  }
  if (name.includes('DEMON')) {
    return { art: '<##>', text: '高爆發終局首領' };
  }
  if (name.includes('GOLEM')) {
    return { art: '[[]]', text: '高護甲重型單位' };
  }
  if (name.includes('WOLF')) {
    return { art: '/^^\\', text: '高速撲擊型敵人' };
  }
  if (name.includes('WITCH')) {
    return { art: '{*_*}', text: '持續騷擾型法術怪' };
  }
  if (name.includes('SKELETON')) {
    return { art: '|x_x|', text: '耐磨消耗型敵人' };
  }
  if (name.includes('TROLL')) {
    return { art: '(##)', text: '高血量壓迫型敵人' };
  }
  if (name.includes('ORC')) {
    return { art: '[:|:]', text: '中距離重擊型敵人' };
  }
  if (name.includes('GOBLIN')) {
    return { art: '<..>', text: '靈活偷襲型敵人' };
  }
  return { art: '(oo)', text: '基礎近戰型怪物' };
}

function renderDifficultyPanel(mode) {
  const profile = difficultyProfile(mode);
  document.getElementById('difficultyBadge').textContent = profile.label;
  document.getElementById('difficultyArt').textContent = profile.art;
  document.getElementById('difficultyMeta').textContent = profile.text;
  document.getElementById('difficultySelect').value = profile.label;
}

function renderWeaponPanel(data) {
  currentWeapon = data;
  const profile = weaponProfile(data?.name || 'Pistol');
  document.getElementById('weaponTypeBadge').textContent = profile.type;
  document.getElementById('weaponArt').textContent = profile.art;
  document.getElementById('weaponTypeSelect').value = profile.type;
  document.getElementById('weaponMeta').textContent = `${data.name}｜Lv ${data.level}｜攻擊 ${data.attack}。${profile.text}`;
}

function updatePlayerSummary(data) {
  player = data;
  document.getElementById('playerSummary').textContent = `玩家 ${data.nickname}，正在挑戰第 ${data.currentStage} 關`;
  document.getElementById('playerBadge').textContent = data.nickname || '冒險者';
  document.getElementById('gold').textContent = data.gold;
  document.getElementById('level').textContent = data.level;
  document.getElementById('hp').textContent = `${data.hp}/${data.maxHp}`;
  document.getElementById('exp').textContent = data.experience;
  document.getElementById('potion').textContent = data.potionCount ?? 0;
  document.getElementById('wins').textContent = data.totalWins ?? 0;
  document.getElementById('saveVersion').textContent = data.saveVersion ?? 1;
  document.getElementById('skillLevel').textContent = data.skillLevel ?? 1;
  document.getElementById('skillCooldown').textContent = data.skillCooldown ?? 0;
  document.getElementById('hpBar').style.width = `${Math.max(8, (data.hp / data.maxHp) * 100)}%`;
  const expRatio = Math.min(100, (data.experience % 100) / 100 * 100);
  document.getElementById('expBar').style.width = `${expRatio}%`;
  lockStageOptions(data.currentStage || 1);
  renderDifficultyPanel(data.difficulty || 'NORMAL');

  if ((data.currentStage || 1) >= 5) {
    updateGuideText('最終關卡已解鎖！擊敗第 5 關怪物完成通關。');
  } else {
    updateGuideText('玩法提示：先在當前關卡刷怪成長，再前往下一關。');
  }

  const skillButton = document.getElementById('skillAttackBtn');
  if ((data.skillCooldown ?? 0) > 0) {
    skillButton.textContent = `技能突擊（冷卻 ${data.skillCooldown}）`;
  } else {
    skillButton.textContent = '技能突擊';
  }
}

async function register() {
  const { username, password } = getAuthInputs();
  const validationMessage = validateAuthInputs(username, password);
  if (validationMessage) {
    setMessage(validationMessage, 'warn');
    return;
  }

  try {
    const data = await apiRequest('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    setMessage(data.message || '註冊成功', 'success');
  } catch (error) {
    setMessage(error.message || '註冊失敗', 'error');
  }
}

async function login() {
  const { username, password } = getAuthInputs();
  if (!username || !password) {
    setMessage('請輸入帳號與密碼', 'warn');
    return;
  }

  try {
    const data = await apiRequest('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    token = data.token || '';
    saveToken();
    updateAuthUI();
    setMessage('登入成功，已載入你的角色資料', 'success');
    if (token) {
      await loadPlayer();
      await loadWeapon();
      await loadProgress();
      if (player) {
        document.getElementById('stageSelect').value = String(player.currentStage || 1);
      }
      await loadMonsters();
    }
  } catch (error) {
    token = '';
    saveToken();
    updateAuthUI();
    setMessage(error.message || '登入失敗', 'error');
  }
}

async function logout() {
  if (!token) {
    setMessage('目前尚未登入', 'warn');
    return;
  }
  try {
    await apiRequest('/api/auth/logout', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
  } catch (error) {
    // Stateless JWT logout: even if API fails, still clear local token.
  }

  token = '';
  player = null;
  saveToken();
  updateAuthUI();
  lockStageOptions(1);
  document.getElementById('stageSelect').value = '1';
  document.getElementById('playerSummary').textContent = '尚未載入角色資料';
  document.getElementById('playerBadge').textContent = '冒險者';
  renderWeaponPanel({ name: 'Pistol', level: 1, attack: 10 });
  renderDifficultyPanel('NORMAL');
  resetCombatView();
  await loadMonsters();
  setMessage('已登出', 'success');
}

async function loadPlayer() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  const data = await apiRequest('/api/player/me', {
    headers: { Authorization: 'Bearer ' + token }
  });
  updatePlayerSummary(data);
}

async function loadWeapon() {
  if (!token) {
    return;
  }
  const data = await apiRequest('/api/weapon/me', {
    headers: { Authorization: 'Bearer ' + token }
  });
  renderWeaponPanel(data);
}

async function upgradeWeapon() {
  if (!token) {
    return;
  }
  const data = await apiRequest('/api/weapon/upgrade', {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + token }
  });
  setBattleMessage(`武器升級完成：Lv ${data.level}`);
  await loadPlayer();
  await loadWeapon();
  setMessage(`武器已升級到 Lv ${data.level}`, 'success');
}

async function loadProgress() {
  if (!token) {
    return;
  }
  const data = await apiRequest('/api/progress', {
    headers: { Authorization: 'Bearer ' + token }
  });
  document.getElementById('progressInfo').textContent = JSON.stringify(data.progress || data, null, 2);
}

async function saveProgress() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/progress/save', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    document.getElementById('progressInfo').textContent = JSON.stringify(data.progress || data, null, 2);
    setMessage(data.message || '進度已儲存', 'success');
  } catch (error) {
    setMessage(error.message || '儲存進度失敗', 'error');
  }
}

async function loadShopItems() {
  try {
    const data = await apiRequest('/api/shop/items');
    document.getElementById('shopInfo').textContent = JSON.stringify(data, null, 2);
  } catch (error) {
    document.getElementById('shopInfo').textContent = error.message || '商店讀取失敗';
  }
}

async function buyShopItem(itemCode) {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/shop/buy?itemCode=' + encodeURIComponent(itemCode), {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    document.getElementById('shopInfo').textContent = JSON.stringify(data, null, 2);
    await loadPlayer();
    if (itemCode === 'SHARPEN_KIT') {
      await loadWeapon();
    }
    setMessage(data.message || '購買成功', 'success');
  } catch (error) {
    setMessage(error.message || '購買失敗', 'error');
  }
}

async function upgradeSkill() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/player/upgrade-skill', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    setBattleMessage(`技能升級成功：Lv ${data.skillLevel}`);
    document.getElementById('battleLog').textContent = JSON.stringify(data, null, 2);
    await loadPlayer();
    setMessage(`技能升級成功，目前 Lv ${data.skillLevel}`, 'success');
  } catch (error) {
    setMessage(error.message || '技能升級失敗', 'error');
  }
}

async function healPlayer() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/player/heal', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    setBattleMessage(`醫療包已使用：${data.message}`);
    document.getElementById('battleLog').textContent = JSON.stringify(data, null, 2);
    await loadPlayer();
    setMessage('角色已醫療並恢復 HP', 'success');
  } catch (error) {
    setMessage(error.message || '回復失敗', 'error');
  }
}

async function quickHeal() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }

  if ((player?.hp ?? 0) >= (player?.maxHp ?? 0)) {
    setMessage('目前 HP 已滿', 'warn');
    return;
  }

  if ((player?.potionCount ?? 0) > 0) {
    await usePotion();
    return;
  }

  await healPlayer();
}

async function changeDifficulty() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }

  const mode = document.getElementById('difficultySelect').value;
  try {
    const data = await apiRequest('/api/player/difficulty?mode=' + encodeURIComponent(mode), {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    renderDifficultyPanel(data.difficulty || mode);
    await loadPlayer();
    setMessage(`難易度已切換為 ${data.difficulty}`, 'success');
  } catch (error) {
    setMessage(error.message || '切換難易度失敗', 'error');
  }
}

async function changeWeaponType() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }

  const type = document.getElementById('weaponTypeSelect').value;
  try {
    const data = await apiRequest('/api/weapon/change?type=' + encodeURIComponent(type), {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    renderWeaponPanel(data);
    setMessage(`已切換為 ${data.name}`, 'success');
  } catch (error) {
    setMessage(error.message || '切換武器失敗', 'error');
  }
}

async function buyPotion() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/player/buy-potion', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    setBattleMessage(`補給完成：${data.message}`);
    document.getElementById('battleLog').textContent = JSON.stringify(data, null, 2);
    await loadPlayer();
    setMessage('藥水購買成功', 'success');
  } catch (error) {
    setMessage(error.message || '購買藥水失敗', 'error');
  }
}

async function usePotion() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/player/use-potion', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    setBattleMessage(`狀態回復：${data.message}`);
    document.getElementById('battleLog').textContent = JSON.stringify(data, null, 2);
    await loadPlayer();
    setMessage('藥水使用成功', 'success');
  } catch (error) {
    setMessage(error.message || '使用藥水失敗', 'error');
  }
}

async function advanceStage() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  try {
    const data = await apiRequest('/api/player/advance-stage', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });
    setBattleMessage(`已進入 ${data.currentStage} 關`);
    document.getElementById('battleLog').textContent = JSON.stringify(data, null, 2);
    document.getElementById('stageSelect').value = data.currentStage;
    await loadPlayer();
    await loadMonsters();
    setMessage(`已前往第 ${data.currentStage} 關`, 'success');
  } catch (error) {
    setMessage(error.message || '前往下一關失敗', 'error');
  }
}

async function endGame() {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }

  const confirmed = confirm('確定要結束本局遊戲並重置角色進度嗎？');
  if (!confirmed) {
    return;
  }

  try {
    const data = await apiRequest('/api/player/end-game', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    });

    document.getElementById('stageSelect').value = '1';
    resetCombatView();
    await loadPlayer();
    await loadWeapon();
    await loadMonsters();
    setBattleMessage('本局已結束，已重置為新手狀態');
    setMessage(data.message || '遊戲已重置', 'success');
  } catch (error) {
    setMessage(error.message || '結束遊戲失敗', 'error');
  }
}

async function loadMonsters() {
  const stage = document.getElementById('stageSelect').value;
  if (player && Number(stage) > (player.currentStage || 1)) {
    setMessage('此關卡尚未解鎖', 'warn');
    document.getElementById('stageSelect').value = String(player.currentStage || 1);
    return;
  }
  const data = await apiRequest('/api/monster/stage/' + stage);
  await loadStageMeta(stage);
  monsters = Array.isArray(data) ? data : [];
  const select = document.getElementById('monsterSelect');
  select.innerHTML = '';
  monsters.forEach((monster) => {
    const option = document.createElement('option');
    option.value = monster.id;
    option.textContent = `${monster.name}（HP ${monster.hp}）`;
    select.appendChild(option);
  });
  if (monsters.length) {
    selectedMonster = monsters[0];
    select.value = selectedMonster.id;
    renderMonsterDetail(selectedMonster);
  } else {
    selectedMonster = null;
    renderMonsterDetail(null);
  }
}

async function loadStageMeta(stage) {
  if (!stageMetaList.length) {
    try {
      stageMetaList = await apiRequest('/api/stage/list');
    } catch (error) {
      stageMetaList = [];
    }
  }

  try {
    const detail = await apiRequest('/api/stage/' + stage);
    const stageInfoText = `關卡情報：${detail.name}｜建議等級 ${detail.recommendedLevel}｜${detail.description}${detail.bossStage ? '｜Boss 關卡' : ''}`;
    document.getElementById('stageInfo').textContent = stageInfoText;
  } catch (error) {
    const fallback = stageMetaList.find(s => Number(s.id) === Number(stage));
    if (fallback) {
      document.getElementById('stageInfo').textContent = `關卡情報：${fallback.name}｜建議等級 ${fallback.recommendedLevel}｜${fallback.description}`;
    } else {
      document.getElementById('stageInfo').textContent = '關卡資訊載入失敗';
    }
  }
}

function renderMonsterDetailFromSelection() {
  const selectedId = document.getElementById('monsterSelect').value;
  selectedMonster = monsters.find(monster => monster.id == selectedId) || null;
  renderMonsterDetail(selectedMonster);
}

function renderMonsterDetail(monster) {
  const nameEl = document.getElementById('monsterName');
  const stageEl = document.getElementById('monsterStage');
  const metaEl = document.getElementById('monsterMeta');
  const barEl = document.getElementById('monsterBar');
  const artEl = document.getElementById('monsterArt');
  if (!monster) {
    nameEl.textContent = '等待選擇怪物';
    stageEl.textContent = '無怪物';
    metaEl.innerHTML = '';
    artEl.textContent = '[ ? ]';
    barEl.style.width = '0%';
    return;
  }
  const profile = monsterProfile(monster);
  nameEl.textContent = monster.name;
  stageEl.textContent = `第 ${monster.stage} 關`;
  artEl.textContent = profile.art;
  metaEl.innerHTML = `<span class="chip">攻擊 ${monster.attack}</span><span class="chip">獎勵 ${monster.rewardGold} 金幣</span><span class="chip">${profile.text}</span>`;
  barEl.style.width = '100%';
}

async function performAttack(useSkill) {
  if (!token) {
    setMessage('請先登入', 'warn');
    return;
  }
  const monsterId = document.getElementById('monsterSelect').value;
  setBattleMessage(useSkill ? '技能蓄力中，準備突擊...' : '戰鬥開始，回合即將展開...');
  try {
    const data = await apiRequest('/api/battle/attack', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      body: JSON.stringify({ monsterId: Number(monsterId), useSkill })
    });
    const logLines = Array.isArray(data.battleLog) ? data.battleLog : [JSON.stringify(data)];
    document.getElementById('battleLog').textContent = logLines.join('\n');
    if (data.monsterDead) {
      setBattleMessage(`${data.monsterName} 被擊敗！`);
      setMessage(`勝利！已擊敗 ${data.monsterName}`, 'success');
    } else {
      setBattleMessage(`${data.monsterName} 仍在戰鬥中`);
      setMessage('戰鬥結束，怪物仍存活，請再挑戰', 'warn');
    }

    if (data.skillUsed) {
      updateGuideText(`技能已發動，冷卻剩餘 ${data.skillCooldown ?? 0} 場戰鬥。`);
    }

    if (data.bossBattle && data.bossSecondPhaseTriggered) {
      setMessage('警告：Boss 進入二階段狂暴，請準備補給與技能循環', 'warn');
    }

    if (data.stageAdvanced) {
      if (player) {
        player.currentStage = data.playerCurrentStage;
      }
      updateGuideText(`已解鎖第 ${data.playerCurrentStage} 關，建議先整理裝備再前進。`);
      document.getElementById('stageSelect').value = String(data.playerCurrentStage);
    }

    if (data.gameCompleted) {
      updateGuideText('你已完成主線通關！可繼續農裝備或重玩新角色。');
      setBattleMessage('通關完成！Operation Survival Clear');
      setMessage('主線通關完成，恭喜！', 'success');
    }

    if (selectedMonster) {
      selectedMonster = { ...selectedMonster, hp: data.monsterHp || 0 };
      renderMonsterDetail(selectedMonster);
    }
    await loadPlayer();
    if (data.stageAdvanced) {
      await loadMonsters();
    }
  } catch (error) {
    setBattleMessage('戰鬥請求失敗');
    document.getElementById('battleLog').textContent = error.message || '戰鬥失敗';
    setMessage(error.message || '戰鬥失敗', 'error');
  }
}

async function attackMonster() {
  await performAttack(false);
}

async function skillAttack() {
  await performAttack(true);
}

window.onload = async function () {
  setMessage('已載入前端，正在準備遊戲資料', 'success');
  restoreToken();
  renderWeaponPanel({ name: 'Pistol', level: 1, attack: 10 });
  renderDifficultyPanel('NORMAL');
  document.getElementById('stageSelect').value = '1';
  if (token) {
    try {
      await loadPlayer();
      await loadWeapon();
      await loadProgress();
      if (player) {
        document.getElementById('stageSelect').value = String(player.currentStage || 1);
      }
    } catch (error) {
      token = '';
      saveToken();
      updateAuthUI();
    }
  }
  await loadMonsters();
  updateAuthUI();
};
