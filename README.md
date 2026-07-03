# WeatherCalendarApp

[日本語](#日本語) · [한국어](#한국어) · [English](#english)

---

## 日本語

天気予報と Google カレンダーを 1 画面で確認できる Android アプリ。ホーム画面ウィジェット 3 種類を提供し、現在地またはお気に入り地点の天気と直近の予定をひと目で把握できます。

### 主な機能

- **天気予報**: OpenWeatherMap One Call API 3.0 による現在地・登録地点の天気（時間ごと/日ごと）
- **Google カレンダー連携**: 予定の取得・追加・編集（Google アカウント連携）
- **ホームウィジェット**: 3 サイズ提供
  - Mini (2x1): 天気コンパクト表示
  - Small (2x2): 天気のみ
  - Medium (4x2): 天気＋カレンダー
- **位置情報**: GPS による現在地取得 + Google Places によるエイリアス地点登録

### 技術スタック

| 領域 | 採用技術 |
|---|---|
| 言語 | Kotlin (JVM target 11) |
| UI | Jetpack Compose / Material 3 / Glance (Widget) |
| アーキテクチャ | 6 層クリーンアーキテクチャ + MVVM |
| DI | Hilt + KSP |
| 非同期 | Kotlin Coroutines |
| 通信 | Retrofit + Gson / kotlinx.serialization |
| 永続化 | Room / DataStore Preferences |
| Navigation | Navigation Compose |
| 画像 | Coil |
| 位置情報 | Google Play Services Location / Places API |
| 認証 | Google Play Services Auth |
| バックグラウンド | WorkManager |

### アーキテクチャ（6 層クリーンアーキテクチャ）

各 feature (`weather` / `calendar` / `setting`) は以下の 6 層構造で実装されています:

```
feature/
├── domain/         # ビジネスロジック（model / repository interface / usecase）
├── data/           # データソース interface
├── remote/         # API 通信（Retrofit interface / Hilt module / DataSource 実装）
├── local/          # ローカル永続化（Room DAO / DataStore / Entity）
├── presentation/   # UI 状態管理（ViewModel / UiState）
└── ui/             # 画面実装（Jetpack Compose）
```

### 動作環境

| 項目 | 値 |
|---|---|
| minSdk | 33 (Android 13) |
| targetSdk / compileSdk | 35 (Android 15) |
| Java | 11 |
| Build Variants | debug / release |
| 現在バージョン | 1.0.21 (versionCode 22) |

### セットアップ

#### 1. リポジトリ取得

```bash
git clone https://github.com/<your-account>/WeatherCalendarApp.git
cd WeatherCalendarApp
```

#### 2. `local.properties` 作成

ルートディレクトリの `local.properties.example` をコピーして `local.properties` を作成し、各値を入力してください。

```properties
ONE_CALL_API_KEY=<OpenWeatherMap One Call API 3.0 の API キー>
PLACES_API_KEY=<Google Places API キー>
```

##### API キー取得方法

- **OpenWeatherMap One Call API 3.0**: <https://openweathermap.org/api/one-call-3> でアカウント作成 → API key 発行（One Call API 3.0 のサブスクリプション登録が必要、無料枠あり）
- **Google Places API**: <https://developers.google.com/maps/documentation/places/web-service/get-api-key> の手順で Google Cloud プロジェクトを作成し API キー発行

#### 3. Google カレンダー連携の設定

Google カレンダー連携を利用する場合、自分の Google Cloud プロジェクトに **Android OAuth クライアント**を登録する必要があります。OAuth Client ID はコードに埋め込まず、`packageName + SHA-1` のペアで識別されるため、各自で以下を設定してください。

1. [Google Cloud Console](https://console.cloud.google.com/) で新規プロジェクトを作成（既存プロジェクト流用可）
2. 「APIs & Services」→「Library」で **Google Calendar API** を有効化
3. 「APIs & Services」→「OAuth consent screen」で User Type を **External** に設定し、テストユーザーに自分の Google アカウントを追加
4. 「APIs & Services」→「Credentials」→「Create credentials」→「OAuth client ID」を選択
   - Application type: **Android**
   - Package name: `com.anri.weathercalendarapp`
   - SHA-1 certificate fingerprint: 下記コマンドで取得した値

##### SHA-1 取得方法

Android Studio が自動生成する debug キーストア（`~/.android/debug.keystore`）の SHA-1 を取得します:

```bash
# macOS / Linux
keytool -list -v -keystore ~/.android/debug.keystore \
  -storepass android -keypass android -alias androiddebugkey | grep SHA1

# Windows (PowerShell)
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -storepass android -keypass android -alias androiddebugkey | Select-String SHA1
```

#### 4. ビルド

```bash
# debug ビルド
./gradlew :app:assembleDebug
```

### Android 権限

- `INTERNET` / `ACCESS_NETWORK_STATE`: API 通信
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: 現在地取得
- `ACCESS_BACKGROUND_LOCATION`: バックグラウンド位置取得（ウィジェット更新時）

### ライセンス

[Apache License 2.0](LICENSE)

---

## 한국어

날씨 예보와 Google 캘린더를 한 화면에서 확인할 수 있는 Android 앱입니다. 3 가지 홈 화면 위젯을 제공하여 현재 위치 또는 즐겨찾기 지점의 날씨와 가까운 일정을 한눈에 파악할 수 있습니다.

### 주요 기능

- **날씨 예보**: OpenWeatherMap One Call API 3.0 기반 현재 위치 및 등록 지점 날씨 (시간별 / 일별)
- **Google 캘린더 연동**: 일정 조회 · 추가 · 편집 (Google 계정 연동)
- **홈 위젯**: 3 가지 사이즈 제공
  - Mini (2x1): 날씨 컴팩트 표시
  - Small (2x2): 날씨만 표시
  - Medium (4x2): 날씨 + 캘린더
- **위치 정보**: GPS 기반 현재 위치 + Google Places 기반 별칭 지점 등록

### 기술 스택

| 영역 | 채택 기술 |
|---|---|
| 언어 | Kotlin (JVM target 11) |
| UI | Jetpack Compose / Material 3 / Glance (Widget) |
| 아키텍처 | 6 계층 클린 아키텍처 + MVVM |
| DI | Hilt + KSP |
| 비동기 | Kotlin Coroutines |
| 네트워크 | Retrofit + Gson / kotlinx.serialization |
| 영속성 | Room / DataStore Preferences |
| Navigation | Navigation Compose |
| 이미지 | Coil |
| 위치 | Google Play Services Location / Places API |
| 인증 | Google Play Services Auth |
| 백그라운드 | WorkManager |

### 아키텍처 (6 계층 클린 아키텍처)

각 feature (`weather` / `calendar` / `setting`) 는 다음 6 계층 구조로 구현되어 있습니다:

```
feature/
├── domain/         # 비즈니스 로직 (model / repository interface / usecase)
├── data/           # 데이터 소스 interface
├── remote/         # API 통신 (Retrofit interface / Hilt module / DataSource 구현)
├── local/          # 로컬 영속화 (Room DAO / DataStore / Entity)
├── presentation/   # UI 상태 관리 (ViewModel / UiState)
└── ui/             # 화면 구현 (Jetpack Compose)
```

### 동작 환경

| 항목 | 값 |
|---|---|
| minSdk | 33 (Android 13) |
| targetSdk / compileSdk | 35 (Android 15) |
| Java | 11 |
| Build Variants | debug / release |
| 현재 버전 | 1.0.21 (versionCode 22) |

### 셋업

#### 1. 저장소 클론

```bash
git clone https://github.com/<your-account>/WeatherCalendarApp.git
cd WeatherCalendarApp
```

#### 2. `local.properties` 작성

루트 디렉토리의 `local.properties.example` 을 복사하여 `local.properties` 를 만들고 각 값을 입력하세요.

```properties
ONE_CALL_API_KEY=<OpenWeatherMap One Call API 3.0 키>
PLACES_API_KEY=<Google Places API 키>
```

##### API 키 발급 방법

- **OpenWeatherMap One Call API 3.0**: <https://openweathermap.org/api/one-call-3> 에서 계정 생성 후 API key 발급 (One Call API 3.0 구독 등록 필요, 무료 한도 있음)
- **Google Places API**: <https://developers.google.com/maps/documentation/places/web-service/get-api-key> 의 절차로 Google Cloud 프로젝트 생성 및 API 키 발급

#### 3. Google 캘린더 연동 설정

Google 캘린더 연동을 사용하려면 본인의 Google Cloud 프로젝트에 **Android OAuth 클라이언트**를 등록해야 합니다. OAuth Client ID 는 코드에 포함되지 않고 `packageName + SHA-1` 쌍으로 식별되므로, 각자 다음 절차로 설정하세요.

1. [Google Cloud Console](https://console.cloud.google.com/) 에서 새 프로젝트 생성 (기존 프로젝트 재사용 가능)
2. 「APIs & Services」→「Library」에서 **Google Calendar API** 활성화
3. 「APIs & Services」→「OAuth consent screen」에서 User Type 을 **External** 로 설정하고, 테스트 사용자에 본인의 Google 계정 추가
4. 「APIs & Services」→「Credentials」→「Create credentials」→「OAuth client ID」 선택
   - Application type: **Android**
   - Package name: `com.anri.weathercalendarapp`
   - SHA-1 certificate fingerprint: 아래 명령어로 얻은 값

##### SHA-1 확인 방법

Android Studio 가 자동 생성하는 debug 키스토어 (`~/.android/debug.keystore`) 의 SHA-1 을 추출합니다:

```bash
# macOS / Linux
keytool -list -v -keystore ~/.android/debug.keystore \
  -storepass android -keypass android -alias androiddebugkey | grep SHA1

# Windows (PowerShell)
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -storepass android -keypass android -alias androiddebugkey | Select-String SHA1
```

#### 4. 빌드

```bash
# debug 빌드
./gradlew :app:assembleDebug
```

### Android 권한

- `INTERNET` / `ACCESS_NETWORK_STATE`: API 통신
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: 현재 위치 취득
- `ACCESS_BACKGROUND_LOCATION`: 백그라운드 위치 취득 (위젯 업데이트 시)

### 라이선스

[Apache License 2.0](LICENSE)

---

## English

An Android app that shows weather forecasts and Google Calendar in a single screen. It ships three home-screen widgets so you can glance at the weather for your current location (or favorite places) alongside upcoming events.

### Features

- **Weather forecast**: hourly/daily forecasts for current location and registered places via OpenWeatherMap One Call API 3.0
- **Google Calendar integration**: read, add, and edit events via the Google account integration
- **Home widgets**: three sizes
  - Mini (2x1): compact weather
  - Small (2x2): weather only
  - Medium (4x2): weather + calendar
- **Location**: GPS-based current location + alias places via Google Places

### Tech stack

| Area | Technology |
|---|---|
| Language | Kotlin (JVM target 11) |
| UI | Jetpack Compose / Material 3 / Glance (Widget) |
| Architecture | 6-layer clean architecture + MVVM |
| DI | Hilt + KSP |
| Concurrency | Kotlin Coroutines |
| Networking | Retrofit + Gson / kotlinx.serialization |
| Persistence | Room / DataStore Preferences |
| Navigation | Navigation Compose |
| Images | Coil |
| Location | Google Play Services Location / Places API |
| Auth | Google Play Services Auth |
| Background | WorkManager |

### Architecture (6-layer clean architecture)

Each feature (`weather` / `calendar` / `setting`) is structured into the following six layers:

```
feature/
├── domain/         # business logic (model / repository interface / usecase)
├── data/           # data source interfaces
├── remote/         # API access (Retrofit interfaces / Hilt module / DataSource impls)
├── local/          # local persistence (Room DAO / DataStore / entities)
├── presentation/   # UI state (ViewModel / UiState)
└── ui/             # screens (Jetpack Compose)
```

### Requirements

| Item | Value |
|---|---|
| minSdk | 33 (Android 13) |
| targetSdk / compileSdk | 35 (Android 15) |
| Java | 11 |
| Build variants | debug / release |
| Current version | 1.0.21 (versionCode 22) |

### Setup

#### 1. Clone

```bash
git clone https://github.com/<your-account>/WeatherCalendarApp.git
cd WeatherCalendarApp
```

#### 2. Create `local.properties`

Copy `local.properties.example` at the project root to `local.properties` and fill in the values.

```properties
ONE_CALL_API_KEY=<OpenWeatherMap One Call API 3.0 key>
PLACES_API_KEY=<Google Places API key>
```

##### How to obtain API keys

- **OpenWeatherMap One Call API 3.0**: create an account at <https://openweathermap.org/api/one-call-3> and issue an API key (requires subscribing to One Call API 3.0; free tier available)
- **Google Places API**: follow the steps at <https://developers.google.com/maps/documentation/places/web-service/get-api-key> to create a Google Cloud project and issue the API key

#### 3. Set up Google Calendar integration

To use Google Calendar integration, you must register your own **Android OAuth client** on a Google Cloud project. The OAuth Client ID is not embedded in the code; it is identified by the `packageName + SHA-1` pair, so each developer needs to configure their own.

1. Create a new project in [Google Cloud Console](https://console.cloud.google.com/) (or reuse an existing one)
2. Enable **Google Calendar API** under "APIs & Services" → "Library"
3. Under "APIs & Services" → "OAuth consent screen", set User Type to **External** and add your Google account as a test user
4. Under "APIs & Services" → "Credentials" → "Create credentials" → "OAuth client ID":
   - Application type: **Android**
   - Package name: `com.anri.weathercalendarapp`
   - SHA-1 certificate fingerprint: the value obtained by the command below

##### How to obtain SHA-1

Extract the SHA-1 from the debug keystore auto-generated by Android Studio (`~/.android/debug.keystore`):

```bash
# macOS / Linux
keytool -list -v -keystore ~/.android/debug.keystore \
  -storepass android -keypass android -alias androiddebugkey | grep SHA1

# Windows (PowerShell)
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -storepass android -keypass android -alias androiddebugkey | Select-String SHA1
```

#### 4. Build

```bash
# debug build
./gradlew :app:assembleDebug
```

### Android permissions

- `INTERNET` / `ACCESS_NETWORK_STATE`: API access
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: current location
- `ACCESS_BACKGROUND_LOCATION`: background location (for widget updates)

### License

[Apache License 2.0](LICENSE)
