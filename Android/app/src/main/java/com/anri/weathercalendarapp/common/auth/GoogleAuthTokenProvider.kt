package com.anri.weathercalendarapp.common.auth

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * requestAccessToken の戻り値。
 * - Success: 新しい/有効なトークンを取得済み
 * - NeedsConsent: ユーザー同意が必要（連携解除/スコープ変更/初回）→ 再連携UI
 * - TransientFailure: ネットワーク/サービスエラー等の一時的失敗 → UI は変えない
 */
sealed class TokenResult {
    data class Success(val token: String) : TokenResult()
    data object NeedsConsent : TokenResult()
    data object TransientFailure : TokenResult()
}

interface GoogleAuthTokenProvider {
    suspend fun authorize(context: Context): AuthorizationResult

    /** キャッシュされたトークンを取得（再認証を試みる） */
    suspend fun ensureAccessToken(context: Context): String?

    /**
     * アクセストークンを取得する。連携状態を区別して返す。
     * - forceRefresh=false: キャッシュが有効ならそのまま返す
     * - forceRefresh=true: キャッシュを破棄して authorize() を強制呼び出し（401 リトライ時に使用）
     */
    suspend fun requestAccessToken(context: Context, forceRefresh: Boolean = false): TokenResult

    /** キャッシュトークンをクリア（ログアウト時） */
    fun clearCachedToken()

    /** Google側の認可を取り消す（ログアウト時） */
    suspend fun revokeAuthorization()

    /** キャッシュトークンを取得（nullの場合あり） */
    fun getCachedToken(): String?

    /** アクセストークンを使ってGoogleアカウントのメールアドレスを取得 */
    suspend fun fetchAccountEmail(accessToken: String): String?

    /** 外部から取得したトークン（consent flow 経由など）をキャッシュに保存 */
    fun setCachedToken(token: String)
}

@Singleton
class GoogleAuthTokenProviderImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : GoogleAuthTokenProvider {

    companion object {
        private const val TAG = "GoogleAuthTokenProvider"
        private const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"
        private const val TOKEN_LIFETIME_MS = 3600 * 1000L  // 1時間
        private const val REFRESH_MARGIN_MS = 5 * 60 * 1000L  // 5分前に再取得
    }

    @Volatile
    private var cachedAccessToken: String? = null
    @Volatile
    private var tokenExpiresAt: Long = 0L

    override suspend fun authorize(context: Context): AuthorizationResult {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(CALENDAR_SCOPE)))
            .build()

        val authorizationClient: AuthorizationClient = Identity.getAuthorizationClient(context)
        val result = authorizationClient.authorize(authorizationRequest).await()

        // トークンが直接取得できた場合はキャッシュ
        result.accessToken?.let {
            cachedAccessToken = it
            tokenExpiresAt = System.currentTimeMillis() + TOKEN_LIFETIME_MS
        }

        return result
    }

    override suspend fun ensureAccessToken(context: Context): String? {
        // キャッシュが有効期限内（5分のマージンあり）ならそのまま返す
        val now = System.currentTimeMillis()
        if (cachedAccessToken != null && now < tokenExpiresAt - REFRESH_MARGIN_MS) {
            return cachedAccessToken
        }

        return try {
            val result = authorize(context)
            if (!result.hasResolution()) {
                result.accessToken?.also {
                    cachedAccessToken = it
                    tokenExpiresAt = System.currentTimeMillis() + TOKEN_LIFETIME_MS
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun requestAccessToken(context: Context, forceRefresh: Boolean): TokenResult {
        if (forceRefresh) {
            cachedAccessToken = null
            tokenExpiresAt = 0L
        } else {
            val now = System.currentTimeMillis()
            cachedAccessToken?.let {
                if (now < tokenExpiresAt - REFRESH_MARGIN_MS) {
                    return TokenResult.Success(it)
                }
            }
        }

        return try {
            val result = authorize(context)
            when {
                !result.hasResolution() && result.accessToken != null -> {
                    // authorize() 側でキャッシュ済み
                    TokenResult.Success(result.accessToken!!)
                }
                result.hasResolution() -> TokenResult.NeedsConsent
                else -> TokenResult.TransientFailure
            }
        } catch (e: Exception) {
            TokenResult.TransientFailure
        }
    }

    override fun clearCachedToken() {
        cachedAccessToken = null
        tokenExpiresAt = 0L
    }

    override fun setCachedToken(token: String) {
        cachedAccessToken = token
        tokenExpiresAt = System.currentTimeMillis() + TOKEN_LIFETIME_MS
    }

    override suspend fun revokeAuthorization() {
        val token = cachedAccessToken ?: ensureAccessToken(appContext)

        // 1. デバイス側: GoogleAuthUtilのローカルトークンキャッシュを無効化
        if (token != null) {
            withContext(Dispatchers.IO) {
                try {
                    GoogleAuthUtil.clearToken(appContext, token)
                } catch (e: Exception) {
                    Log.e(TAG, "revokeAuthorization clearToken: ${e.message}", e)
                }
            }
        }

        // 2. サーバー側: アクセストークンを取り消す
        if (token != null) {
            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val body = okhttp3.FormBody.Builder()
                        .add("token", token)
                        .build()
                    val request = Request.Builder()
                        .url("https://oauth2.googleapis.com/revoke")
                        .post(body)
                        .build()
                    client.newCall(request).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "revokeAuthorization revoke: ${e.message}", e)
                }
            }
        }

        // 3. デバイス側: Googleサインイン状態をクリア
        try {
            Identity.getSignInClient(appContext).signOut().await()
        } catch (e: Exception) {
            Log.e(TAG, "revokeAuthorization signOut: ${e.message}", e)
        }
    }

    override fun getCachedToken(): String? {
        if (cachedAccessToken != null && System.currentTimeMillis() >= tokenExpiresAt) {
            cachedAccessToken = null
            tokenExpiresAt = 0L
        }
        return cachedAccessToken
    }

    override suspend fun fetchAccountEmail(accessToken: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                // Calendar APIのプライマリカレンダー情報からオーナーメールを取得
                // （emailスコープ不要、calendarスコープのみで取得可能）
                val request = Request.Builder()
                    .url("https://www.googleapis.com/calendar/v3/calendars/primary")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                val response = client.newCall(request).execute()
                response.use {
                    if (it.isSuccessful) {
                        val json = JSONObject(it.body?.string() ?: "")
                        json.optString("id").takeIf { id -> id.isNotEmpty() && id.contains("@") }
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
