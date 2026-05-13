# API 仕様書 — WeatherCalendarApp

このアプリで使用している外部APIのまとめ。Apidog登録の参照用。

- Weather: `docs/openapi-weather.yaml`
- Calendar: `docs/openapi-calendar.yaml`

---

## 1. Weather API（OpenWeather One Call API 3.0）

### 共通設定
| 項目 | 値 |
|---|---|
| Base URL | `https://api.openweathermap.org` |
| 認証 | クエリパラメータ `appid` |
| Retrofitインターフェース | `weather/remote/apiinterface/WeatherApiService.kt` |

### 1-1. `GET /data/3.0/onecall` — 天気取得

**Query Parameters**

| 名前 | 型 | 必須 | デフォルト | 説明 |
|---|---|---|---|---|
| `lat` | number (double) | ✅ | — | 緯度 |
| `lon` | number (double) | ✅ | — | 経度 |
| `appid` | string | ✅ | — | OpenWeather APIキー |
| `exclude` | string | ✅ | `minutely,alerts` | 除外パート |
| `units` | string | ✅ | `metric` | 単位系 |
| `lang` | string | ✅ | `ja` | 言語 |

**Response 200 — `WeatherRemote`**

| Schema | フィールド |
|---|---|
| `WeatherRemote` | `lat: number`, `lon: number`, `timezone: string`, `current: CurrentRemote`, `hourly: HourlyRemote[]`, `daily: DailyRemote[]` |
| `CurrentRemote` | `dt: integer(int64)`, `temp: number`, `feels_like: number`, `humidity: integer`, `wind_speed: number`, `weather: WeatherDescriptionRemote[]` |
| `HourlyRemote` | `dt: integer(int64)`, `temp: number`, `pop: number`, `weather: WeatherDescriptionRemote[]` |
| `DailyRemote` | `dt: integer(int64)`, `temp: TempRemote`, `pop: number`, `weather: WeatherDescriptionRemote[]` |
| `WeatherDescriptionRemote` | `id: integer`, `icon: string` |
| `TempRemote` | `min: number`, `max: number` |

---

## 2. Calendar API（Google Calendar API v3）

### 共通設定
| 項目 | 値 |
|---|---|
| Base URL | `https://www.googleapis.com` |
| 認証 | Header `Authorization: Bearer <accessToken>` |
| Retrofitインターフェース | `calendar/remote/apiinterface/CalendarApiService.kt` |

### 2-1. `GET /calendar/v3/calendars/{calendarId}/events` — 予定一覧取得

**Path** : `calendarId: string`（例 `primary`）
**Header** : `Authorization: Bearer <token>`
**Query**

| 名前 | 型 | 必須 |
|---|---|---|
| `timeMin` | string (RFC3339) | ❌ |
| `timeMax` | string (RFC3339) | ❌ |
| `singleEvents` | boolean | ✅ |
| `orderBy` | string | ❌ |
| `maxResults` | integer | ❌ |
| `pageToken` | string | ❌ |

**Response 200** : `CalendarResRemote`

### 2-2. `POST /calendar/v3/calendars/{calendarId}/events` — 予定作成

**Path / Header** : 2-1 と同一
**Body** : `CreateEventReqRemote`
**Response 200** : `EventRemote`

### 2-3. `PUT /calendar/v3/calendars/{calendarId}/events/{eventId}` — 予定更新

**Path** : `calendarId`, `eventId`
**Header / Body / Response** : 2-2 と同一

### 2-4. `DELETE /calendar/v3/calendars/{calendarId}/events/{eventId}` — 予定削除

**Path / Header** : 2-3 と同一
**Body** : なし
**Response 204** : No Content

### Calendar Schemas

| Schema | フィールド |
|---|---|
| `CalendarResRemote` | `items: EventRemote[]?`, `nextPageToken: string?` |
| `EventRemote` | `id: string?`, `summary: string?`, `start: EventDateTime?`, `end: EventDateTime?`, `colorId: string?` |
| `EventDateTime` | `dateTime: string?`, `date: string?` |
| `CreateEventReqRemote` | `summary: string?`, `start: EventDateTimeRemote`, `end: EventDateTimeRemote`, `colorId: string?` |
| `EventDateTimeRemote` | `dateTime: string?`, `date: string?`, `timeZone: string?` |

---

## Apidog インポート手順

1. Apidog プロジェクト「WeatherCalendarApp」を開く
2. 左サイドバー「設定 → データインポート」 → 「OpenAPI Spec」を選択
3. `docs/openapi-weather.yaml` をアップロード → インポート
4. 同様に `docs/openapi-calendar.yaml` をインポート
5. インポート完了後、エンドポイントが `Weather` / `Calendar` タグ別に登録される
