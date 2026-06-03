package io.poupai.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark

// Biometria fraca + credencial de tela (PIN/padrão/senha). WEAK (em vez de STRONG)
// porque é só um bloqueio de app — sem CryptoObject — e a combinação STRONG+DEVICE_CREDENTIAL
// não é suportada em APIs < 30. WEAK+DEVICE_CREDENTIAL funciona desde a API 26.
private val LOCK_AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

object AppLock {

    /** O aparelho consegue autenticar (tem biometria OU credencial de tela configurada)? */
    fun canAuthenticate(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(LOCK_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /** Mostra o prompt de biometria/credencial. [onSuccess] libera o app. */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {},
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear o Poupaí")
            .setSubtitle("Confirme sua identidade para continuar")
            .setAllowedAuthenticators(LOCK_AUTHENTICATORS)
            .build()
        prompt.authenticate(info)
    }
}

/**
 * Envolve o conteúdo do app com um portão de bloqueio. Quando [enabled] é true e o
 * aparelho consegue autenticar, exige biometria/credencial:
 *  - na abertura a frio (processo novo);
 *  - sempre que o app volta do segundo plano (ON_STOP -> re-bloqueia, prompt no ON_RESUME).
 *
 * Usa `remember` (não `rememberSaveable`) de propósito: qualquer recriação de processo
 * volta a trancar. Se o aparelho perdeu a credencial, o portão abre (fail-open) para não
 * trancar o usuário pra fora permanentemente.
 */
@Composable
fun AppLockGate(
    activity: FragmentActivity,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (!enabled || !AppLock.canAuthenticate(activity)) {
        content()
        return
    }

    var unlocked by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Volta para segundo plano -> re-bloqueia.
                Lifecycle.Event.ON_STOP -> unlocked = false
                // Em primeiro plano e bloqueado -> dispara o prompt.
                // (addObserver dispara ON_RESUME na carga a frio, cobrindo a abertura.)
                Lifecycle.Event.ON_RESUME ->
                    if (!unlocked) AppLock.authenticate(activity, onSuccess = { unlocked = true })
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize()) {
        content()
        if (!unlocked) {
            AppLockScreen(
                onUnlock = { AppLock.authenticate(activity, onSuccess = { unlocked = true }) },
            )
        }
    }
}

@Composable
private fun AppLockScreen(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))))
            // Swallow de toques para nada vazar para o conteúdo atrás.
            .pointerInput(Unit) { detectTapGestures {} },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Poupaí bloqueado",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Confirme sua identidade para continuar",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Fingerprint, null, tint = Purple40, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Desbloquear", color = Purple40, fontWeight = FontWeight.Bold)
            }
        }
    }
}
