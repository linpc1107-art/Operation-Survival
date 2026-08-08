# Operation Survival API 文件

## 基本資訊

- Base URL: http://127.0.0.1:8080
- Content-Type: application/json
- 驗證方式: Bearer Token（JWT）

## 驗證流程

1. 呼叫 POST /api/auth/register 建立帳號
2. 呼叫 POST /api/auth/login 取得 token
3. 對受保護端點帶入 Authorization: Bearer <token>

## 權限規則

公開端點：

- GET /
- GET /hello
- POST /api/auth/register
- POST /api/auth/login
- GET /api/monster/stage/{stage}
- GET /api/stage/list
- GET /api/stage/{id}
- GET /api/shop/items

需要登入的端點：

- POST /api/auth/logout
- GET /api/player/me
- POST /api/player/heal
- POST /api/player/advance-stage
- POST /api/player/buy-potion
- POST /api/player/use-potion
- POST /api/player/upgrade-skill
- POST /api/player/end-game
- GET /api/weapon/me
- POST /api/weapon/upgrade
- POST /api/battle/attack
- GET /api/progress
- POST /api/progress/save
- POST /api/shop/buy

## 通用錯誤格式

### 驗證失敗 400

```json
{
  "timestamp": "2026-07-23T11:45:10.147Z",
  "status": 400,
  "message": "帳號長度必須在 4 到 20 個字元之間",
  "errors": {
    "username": "帳號長度必須在 4 到 20 個字元之間"
  }
}
```

### 業務錯誤 400

```json
{
  "timestamp": "2026-07-23T11:45:10.147Z",
  "status": 400,
  "message": "金幣不足，購買此道具需要 180 金幣"
}
```

## 認證 API

### POST /api/auth/register

建立新帳號。

Request:

```json
{
  "username": "demoUser",
  "password": "123456"
}
```

規則：

- username: 必填，4 到 20 字元
- password: 必填，至少 6 字元

Response 200:

```json
{
  "message": "註冊成功",
  "user": {
    "id": 1,
    "username": "demoUser",
    "password": null
  }
}
```

### POST /api/auth/login

登入並取得 JWT。

Request:

```json
{
  "username": "demoUser",
  "password": "123456"
}
```

Response 200:

```json
{
  "message": "登入成功",
  "token": "<jwt-token>"
}
```

### POST /api/auth/logout

伺服器端不維護 session，此端點主要回傳登出提示，前端應自行移除 token。

Headers:

```text
Authorization: Bearer <jwt-token>
```

Response 200:

```json
{
  "message": "登出成功",
  "username": "demoUser"
}
```

## 系統與公開查詢 API

### GET /

Response 200:

```text
Operation Survival Backend 啟動成功
```

### GET /hello

Response 200:

```text
Hello Spring Boot
```

### GET /api/stage/list

取得全部關卡資訊。

Response 200:

```json
[
  {
    "id": 1,
    "name": "廢墟外圍",
    "description": "新手訓練區",
    "recommendedLevel": 1,
    "bossStage": false
  }
]
```

### GET /api/stage/{id}

取得單一關卡資訊，id 範圍為 1 到 5。

Response 200:

```json
{
  "id": 5,
  "name": "深淵核心",
  "description": "最終首領戰",
  "recommendedLevel": 8,
  "bossStage": true
}
```

### GET /api/monster/stage/{stage}

取得指定關卡的怪物列表。

Response 200:

```json
[
  {
    "id": 1,
    "name": "Slime",
    "hp": 30,
    "attack": 5,
    "rewardGold": 10,
    "stage": 1
  }
]
```

### GET /api/shop/items

取得商店道具清單。

Response 200:

```json
[
  {
    "code": "POTION",
    "name": "治療藥水",
    "price": 60,
    "description": "+1 藥水，可在戰鬥後回復生命"
  },
  {
    "code": "FIELD_MEDKIT",
    "name": "戰地醫療包",
    "price": 100,
    "description": "立即恢復至滿血"
  }
]
```

## 玩家 API

以下端點都需要 Bearer Token。

### GET /api/player/me

取得目前登入玩家資料。

Response 200:

```json
{
  "id": 1,
  "username": "demoUser",
  "nickname": "demoUser",
  "gold": 120,
  "currentStage": 2,
  "hp": 96,
  "maxHp": 110,
  "level": 2,
  "experience": 45,
  "potionCount": 2,
  "totalWins": 1,
  "skillLevel": 1,
  "skillCooldown": 0,
  "saveVersion": 1,
  "lastSavedAt": "2026-07-23T19:20:11"
}
```

### POST /api/player/heal

花費金幣立即回復生命。

Response 200:

```json
{
  "message": "HP 已回復",
  "hp": 100,
  "maxHp": 100,
  "gold": 20,
  "potionCount": 2
}
```

### POST /api/player/advance-stage

前往下一關，會檢查等級與金幣。

Response 200:

```json
{
  "message": "已進入下一關",
  "currentStage": 3,
  "hp": 110,
  "maxHp": 110,
  "gold": 150,
  "potionCount": 2
}
```

### POST /api/player/buy-potion

直接購買藥水。

Response 200:

```json
{
  "message": "購買藥水成功",
  "gold": 60,
  "potionCount": 3
}
```

### POST /api/player/use-potion

消耗一瓶藥水回復生命。

Response 200:

```json
{
  "message": "使用藥水成功",
  "hp": 100,
  "maxHp": 100,
  "potionCount": 1
}
```

### POST /api/player/upgrade-skill

升級技能，費用公式為 150 + 目前技能等級 × 120。

Response 200:

```json
{
  "message": "技能升級成功",
  "skillLevel": 2,
  "gold": 230,
  "upgradeCost": 270
}
```

### POST /api/player/end-game

重置本局進度與角色成長。

Response 200:

```json
{
  "message": "本局遊戲已結束，進度已重置",
  "currentStage": 1,
  "level": 1,
  "gold": 0
}
```

## 武器 API

### GET /api/weapon/me

Response 200:

```json
{
  "id": 1,
  "name": "生存者短刀",
  "level": 1,
  "attack": 10
}
```

### POST /api/weapon/upgrade

升級武器。回傳中的 upgradeCost 是本次升級消耗值。

Response 200:

```json
{
  "message": "武器升級成功",
  "level": 2,
  "attack": 16,
  "gold": 180,
  "playerLevel": 2,
  "playerMaxHp": 110,
  "upgradeCost": 280
}
```

## 戰鬥 API

### POST /api/battle/attack

進行一次戰鬥。若要使用技能，將 useSkill 設為 true。

Request:

```json
{
  "monsterId": 1,
  "useSkill": false
}
```

Response 200:

```json
{
  "playerHp": 84,
  "monsterHp": 0,
  "damage": 12,
  "monsterDamage": 0,
  "monsterDead": true,
  "rewardGold": 10,
  "message": "勝利！你擊敗了 Slime，並獲得獎勵",
  "monsterName": "Slime",
  "monsterStage": 1,
  "roundCount": 3,
  "playerAlive": true,
  "playerLevel": 1,
  "playerExperience": 30,
  "playerMaxHp": 100,
  "playerCurrentStage": 2,
  "playerPotionCount": 2,
  "stageAdvanced": true,
  "gameCompleted": false,
  "skillUsed": false,
  "skillCooldown": 0,
  "bossBattle": false,
  "bossSecondPhaseTriggered": false,
  "battleLog": [
    "戰鬥開始！Slime 出現於第 1 階段",
    "第 1 回合：你造成 12 點傷害，怪物剩餘 18 HP"
  ]
}
```

說明：

- useSkill 為 true 時，若技能未冷卻，傷害會提高並將 skillCooldown 設為 2
- 若玩家 HP 歸零，message 會回傳陣亡訊息
- 最終關卡首領可能觸發 bossSecondPhaseTriggered

## 進度 API

### GET /api/progress

讀取目前存檔快照。

Response 200:

```json
{
  "message": "目前進度讀取成功",
  "progress": {
    "nickname": "demoUser",
    "level": 2,
    "currentStage": 2,
    "gold": 120,
    "hp": 96,
    "maxHp": 110,
    "experience": 45,
    "potionCount": 2,
    "totalWins": 1,
    "skillLevel": 1,
    "skillCooldown": 0,
    "saveVersion": 1,
    "lastSavedAt": "2026-07-23T19:20:11"
  }
}
```

### POST /api/progress/save

儲存目前進度。

Response 200:

```json
{
  "message": "進度已儲存",
  "progress": {
    "nickname": "demoUser",
    "level": 2,
    "currentStage": 2,
    "gold": 120,
    "hp": 96,
    "maxHp": 110,
    "experience": 45,
    "potionCount": 2,
    "totalWins": 1,
    "skillLevel": 1,
    "skillCooldown": 0,
    "saveVersion": 1,
    "lastSavedAt": "2026-07-23T19:20:11"
  }
}
```

## 商店 API

### POST /api/shop/buy?itemCode=POTION

以 query parameter 指定道具代碼。

可用代碼：

- POTION
- FIELD_MEDKIT
- SHARPEN_KIT
- COOLANT

Response 200:

```json
{
  "message": "購買成功：獲得 1 瓶治療藥水",
  "result": {
    "itemCode": "POTION",
    "cost": 60,
    "message": "購買成功：獲得 1 瓶治療藥水",
    "gold": 60,
    "hp": 100,
    "maxHp": 100,
    "potionCount": 3,
    "skillCooldown": 0
  }
}
```

## 測試建議順序

1. POST /api/auth/register
2. POST /api/auth/login
3. GET /api/player/me
4. GET /api/monster/stage/1
5. POST /api/battle/attack
6. GET /api/weapon/me
7. POST /api/progress/save
8. POST /api/shop/buy?itemCode=POTION