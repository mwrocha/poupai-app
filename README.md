# Poupaí

Aplicativo Android nativo de gestão financeira pessoal e acompanhamento de investimentos, escrito em **Kotlin + Jetpack Compose**. Cobre transações, metas, alocação de carteira, dividendos, imposto de renda, rebalanceamento, gamificação e relatórios consolidados.

O backend é separado (Spring Boot, deploy no Render) e mora em outro repositório. Este projeto é exclusivamente o cliente Android.

---

## Sumário

- [Visão geral](#visão-geral)
- [Stack técnica](#stack-técnica)
- [Arquitetura](#arquitetura)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Setup local](#setup-local)
- [Configuração de ambiente](#configuração-de-ambiente)
- [Design system](#design-system)
- [Convenções de código](#convenções-de-código)
- [Como o app conversa com o backend](#como-o-app-conversa-com-o-backend)
- [Roadmap](#roadmap)

---

## Visão geral

O Poupaí concentra três frentes em um único app:

- **Finanças pessoais** — transações de receita/despesa, categorização por tags, indicadores mensais, gráficos de evolução, projeção de gastos e maior despesa do período.
- **Investimentos** — carteira por tipo de ativo (renda variável, renda fixa, cripto), livro contábil de aportes/resgates/atualizações, alocação-alvo vs real, dividendos recebidos, comparativo vs CDI e cálculo de imposto de renda sobre operações tributáveis.
- **Hábito** — metas com prazo e aporte sugerido mensal, streak diário e sistema de pontos/badges para incentivar uso recorrente.

Todas as telas suportam **light/dark theme** unificados em torno de uma paleta roxa e oferecem ocultar valores (toggle olho) e pull-to-refresh.

---

## Stack técnica

| Camada | Tecnologia | Por quê |
|---|---|---|
| Linguagem | **Kotlin 1.9.24** | Padrão Android atual; null-safety, coroutines, extension functions. |
| UI | **Jetpack Compose** (BOM 2024.06) | Declarativo, integra naturalmente com state, reduz boilerplate XML/Adapter; melhor produtividade para um app feature-rich. |
| Design | **Material 3** + tokens custom | Material 3 dá componentes prontos (FAB, Card, ModalNavigationDrawer, FilterChip). Em cima dele criamos uma camada de tokens semânticos (`PoupaiTokens`) que isola light/dark e a identidade roxa do app. |
| Navegação | **Navigation Compose** | Type-safe routes, integração nativa com Compose, deep-linking simples. Para um app de ~25 telas o overhead é mínimo. |
| Injeção de dependência | **Hilt 2.51** | Builds anotação + KSP, integração first-class com ViewModel via `hiltViewModel()`. Alternativa Koin foi descartada por exigir mais boilerplate de configuração e perder verificação em compile-time. |
| Processamento de anotações | **KSP** | Substitui kapt — 2x mais rápido em builds incrementais, suportado por Hilt e Room. |
| Async | **Kotlinx Coroutines 1.8** + **Flow / StateFlow** | Cancelamento estrutural, integração com lifecycle (`viewModelScope`, `repeatOnLifecycle`). Flow para streams reativos (preferências, observação de Room). |
| Camada HTTP | **Retrofit 2.11** + **OkHttp 4.12** + **Gson** | Retrofit cobre 95% dos casos REST sem cerimônia; OkHttp dá interceptors (auth, logging). Gson escolhido por simplicidade — para evolução futura vale considerar Moshi/kotlinx-serialization (sem reflexão, melhor para R8). |
| Persistência local | **Room 2.6** | Type-safe SQL, integra com Flow, schema versionado. Usado para cache offline-friendly de dados que não precisam estar sempre frescos. |
| Preferências | **DataStore Preferences 1.1** | Substitui SharedPreferences — coroutines-based, transacional, sem ANR risk. Usado para token JWT, flags (`USE_PRODUCTION`, `hideValues`, tema), dados leves do user. |
| Imagens | **Coil 2.6** | Coroutines-native, footprint pequeno, integra com Compose via `AsyncImage`. Picasso/Glide foram preteridos por usar `AsyncTask`/callbacks. |
| Background work | **WorkManager 2.9** + Hilt Worker | Notificações agendadas (lembrete diário 20h) que sobrevivem reboot e respeitam Doze mode. |

### Por que **não** usar X

- **MVI**: o app usa MVVM com `StateFlow<UiState>` por feature, que cobre 100% dos casos sem o overhead conceitual de Intent/Reducer. MVI puro seria over-engineering para o tamanho.
- **Compose Navigation Type-Safe (Compose Multiplatform)**: ainda em beta na época da escolha; rotas string-based + `Route` sealed class cobrem com baixo custo.
- **Modularização Gradle multi-módulo**: o app inteiro vive num único módulo `:app`. Splitting (`:core`, `:feature-investments`, etc.) só se justifica acima de ~50 telas ou em equipes maiores; manter monomódulo acelera builds e simplifica refactors.
- **Kotlin Multiplatform**: não há cliente iOS no roadmap próximo. Quando entrar, vale reavaliar isolando a camada `domain`.

---

## Arquitetura

Clean Architecture em **3 camadas** dentro de um único módulo Gradle.

```
┌───────────────────────────────────────────────────────────────┐
│  features/<nome>/ui          → Composables (Screen, cards)    │
│  features/<nome>/viewmodel   → ViewModel + lógica de UI       │
│  features/<nome>/state       → UiState data class             │
├───────────────────────────────────────────────────────────────┤
│  domain/model                → Entidades puras (Investment,   │
│  domain/repository           ↑   Goal, Transaction, etc.)     │
│  domain/usecase              ↑                                │
├───────────────────────────────────────────────────────────────┤
│  data/remote (Retrofit)      → DTOs, API services             │
│  data/local  (Room)          → DAOs, Entities                 │
│  data/mapper                 → DTO/Entity ↔ Domain            │
│  data/repository             → Impl que orquestra remote+local│
└───────────────────────────────────────────────────────────────┘
```

### Princípios

- **`domain` é puro Kotlin** — sem dependência de Android, Retrofit ou Room. Modelos são `data class` simples.
- **`data` depende de `domain`** — Repository implementations expostas via interface do `domain`.
- **`features` depende de `domain` e do design system, nunca de `data` direto** — ViewModels recebem repositories via Hilt usando a interface.
- **UiState imutável** — cada feature tem um único `XxxUiState` data class. Mudanças via `_uiState.update { it.copy(...) }`. Isso elimina inconsistências de estado parcial.

### Por que Clean (e não MVVM "flat")

Camada de domain explícita facilita:
- Testes unitários de regra de negócio sem mockar Android
- Trocar Room por outra fonte (ou DataSource em memória) sem mexer em UI
- Migrar para Multiplatform no futuro com baixo atrito

O custo é um mapeamento extra (DTO → Domain → UI), que vale a pena pra um app com regras de IR, cálculo de rentabilidade, comparativo CDI etc.

---

## Estrutura de pastas

```
app/src/main/kotlin/io/poupai/app/
├── MainActivity.kt
├── PoupaiApp.kt                      Application (Hilt entry point)
│
├── core/
│   ├── analytics/                    Tracking (placeholder/futuro)
│   ├── database/                     PoupaiDatabase + Converters (Room)
│   ├── designsystem/components/      Componentes compartilhados:
│   │     PoupaiDrawer, PoupaiDrawerScaffold, PoupaiHeader,
│   │     PullToRefresh, EyeToggleIcon, StaleChip, GradientButton
│   ├── di/                           Módulos Hilt (Network/Database/Repository)
│   ├── navigation/                   PoupaiNavHost + Route sealed class
│   ├── network/                      SessionManager, AuthInterceptor, Resource<T>
│   ├── notification/                 NotificationScheduler (WorkManager)
│   ├── theme/                        Theme.kt + PoupaiTokens.kt (light/dark)
│   └── util/                         PreferencesManager, DateFormatter,
│                                     CpfVisualTransformation, etc.
│
├── data/
│   ├── local/                        DAOs + Entities (Room)
│   ├── mapper/                       Conversões entre camadas
│   ├── remote/
│   │   ├── api/                      Interfaces Retrofit
│   │   └── dto/                      Request/Response DTOs
│   └── repository/                   Implementations
│
├── domain/
│   ├── model/                        Investment, Goal, Transaction,
│   │                                 Dividend, RebalanceItem, etc.
│   ├── repository/                   Interfaces (contratos)
│   └── usecase/                      AddTransactionUseCase,
│                                     GetTransactionsUseCase, etc.
│
└── features/
    ├── allocation/        Alocação de carteira
    ├── auth/              Login + Welcome
    ├── dashboard/         Home com hero + atalhos + metas + transações
    ├── dividends/         Lista, distribuição por tipo, top pagadores
    ├── finances/          Receitas/despesas, gráficos, categorias
    ├── gamification/      Streak, pontos, badges
    ├── goals/             Metas com aporte sugerido e prazo
    ├── incometax/         IR: classificação FII + cálculo por mês/ano
    ├── investmentbook/    Livro contábil (aportes/resgates/atualizações)
    ├── investmentdetail/  Detalhe de ativo individual
    ├── investments/       Lista + benchmark CDI + donut alocação
    ├── onboarding/        Slides iniciais
    ├── profile/           Edição de dados pessoais e foto
    ├── register/          Cadastro (credenciais + perfil)
    ├── settings/          Tema, notificações, sobre
    ├── splash/            Splash + WelcomeAfterLogin
    ├── tags/              Categorias de despesa
    └── transactions/      CRUD de receitas/despesas
```

Cada feature segue **sempre** a mesma estrutura interna: `ui/` (Composables) + `viewmodel/` + `state/`. Isso reduz fricção cognitiva ao navegar entre features novas.

---

## Setup local

### Pré-requisitos

- **Android Studio** Iguana (2023.2) ou superior
- **JDK 17** (configurado no `compileOptions`/`kotlinOptions`)
- **Android SDK 35** (compileSdk) com **mínimo SDK 26** (Android 8.0)

### Primeiro build

```bash
git clone <repo-url>
cd poupai
# Android Studio: Open → selecione a pasta → aguarde sync do Gradle
./gradlew :app:assembleDebug
```

Ou compile só (sem APK):

```bash
./gradlew :app:compileDebugKotlin
```

### Rodando no emulador

Recomendado **API 33+** (Tiramisu) para validar permissões modernas (`READ_MEDIA_IMAGES`, `POST_NOTIFICATIONS`).

---

## Configuração de ambiente

Crie um arquivo `local.properties` na raiz (já está no `.gitignore`):

```properties
sdk.dir=/caminho/para/Android/Sdk

# Backend: true aponta pro Render (produção), false pro localhost
USE_PRODUCTION=true

# Token opcional para integração BRAPI (cotações B3)
BRAPI_TOKEN=
```

### Como o switch de backend funciona

O `app/build.gradle.kts` lê `USE_PRODUCTION` do `local.properties` e injeta como `BuildConfig.USE_PRODUCTION`. O `NetworkModule.kt` resolve a `BASE_URL`:

| `USE_PRODUCTION` | Onde roda | URL |
|---|---|---|
| `true` (default) | qualquer | `https://poupai-backend.onrender.com/` |
| `false` | emulador | `http://10.0.2.2:8080/` |
| `false` | device físico | URL do device (configurada no NetworkModule) |

Builds de **release** sempre apontam para produção, independentemente do `local.properties`.

> Depois de alterar `USE_PRODUCTION`, rode **Build → Rebuild Project** no Android Studio — o `BuildConfig` precisa ser regerado.

---

## Design system

### Paleta

O app inteiro gira em torno de **variações de roxo** (`Purple40`, `Purple60`, `PurpleDark`, `PurpleLight`, intermediários como `#7C5295`). Cores semânticas `GreenPositive`/`RedNegative` ficam reservadas exclusivamente para chips pequenos de gain/loss — onde a convenção universal (verde sobe / vermelho desce) é mais clara que a marca.

### Tokens semânticos

Em vez de espalhar `Color(0xFF...)` pelas telas, todo lugar consome `PoupaiTheme.tokens.*`:

```kotlin
PoupaiTheme.tokens.bg              // fundo da tela
PoupaiTheme.tokens.surface         // cards
PoupaiTheme.tokens.surfaceAlt      // categorias, chips claros
PoupaiTheme.tokens.surfaceSunken   // barras de progresso, grids
PoupaiTheme.tokens.textPrimary     // texto forte
PoupaiTheme.tokens.textSecondary   // texto neutro
PoupaiTheme.tokens.textMuted       // legendas
PoupaiTheme.tokens.divider         // linhas finas
PoupaiTheme.tokens.accentBright    // chip selecionado (Purple40 light / Purple60 dark)
```

`PoupaiTokens` troca os valores entre light e dark via `CompositionLocal`, então **uma única refatoração nessas constantes** propaga para todo o app.

### Padrões visuais reutilizados

- **Hero card**: gradiente de 3 stops `PurpleDark → Purple40 → #6B4396`, com saldo principal + stats inline separados por divisor sutil. Usado em Finances, Investments, Goals, Tags, Dashboard.
- **Section title**: ícone Material 16dp em `Purple40` + label `titleSmall SemiBold textSecondary`.
- **Avatar de categoria**: `RoundedCornerShape(10-12dp)` com cor de accent em alpha `0.12-0.14`.
- **Chip de % gain/loss**: pequeno, com `GreenPositive`/`RedNegative` — única exceção semântica à paleta roxa.
- **Empty state**: ícone Material em chip circular `PurpleLight.copy(alpha = 0.55f)`, nunca emojis grandes.

### Navegação

- **9 telas top-level** (Dashboard, Investments, Finances, Transactions, Tags, Goals, Gamification, Profile, Settings) compartilham o drawer lateral via `PoupaiDrawerScaffold`.
- **Telas profundas** (detalhe de investimento, livro contábil, formulários, classificação IR, etc.) usam `popBackStack()` com botão back tradicional.
- Trocar de feature pelo drawer faz `launchSingleTop + popUpTo(Dashboard)` — Dashboard fica preservado como home, sem pilha gigante.

---

## Convenções de código

### State management

Cada feature tem **um** `UiState` data class:

```kotlin
data class TransactionsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val hideValues: Boolean = false,
    val errorMessage: String? = null,
    // ...
)
```

ViewModel atualiza via `update { copy(...) }` para preservar imutabilidade:

```kotlin
_uiState.update { it.copy(isLoading = true) }
```

### Resource<T> wrapper

Chamadas de rede retornam `Resource<T>` (`Loading`/`Success`/`Error`) — define o tratamento padronizado em qualquer ViewModel.

### Refresh sem flash

Sempre que há dados em tela, o `loadXxx()` evita setar `isLoading = true` durante refresh:

```kotlin
is Resource.Loading -> _uiState.update { current ->
    val hasData = current.items.isNotEmpty()
    if (hasData) current.copy(errorMessage = null)
    else current.copy(isLoading = true, errorMessage = null)
}
```

### Formato de data

API trabalha em ISO `yyyy-MM-dd`. UI trabalha em pt-BR `dd-MM-yyyy`. Conversões centralizadas em `DateFormatter.kt` com `isoToDisplay()`, `displayToIso()`, `applyMask()`.

---

## Como o app conversa com o backend

- **Auth JWT**: token guardado no DataStore, injetado via `AuthInterceptor` em todas as chamadas
- **Sessão expirada**: 401 dispara `SessionManager.onSessionExpired()` (SharedFlow) — MainActivity escuta e redireciona pro Welcome
- **Erros HTTP**: `Resource.Error(message)` propaga até o ViewModel, que expõe em `uiState.errorMessage`

Endpoints principais (todos no mesmo Spring Boot):

- `/auth/login`, `/auth/register`
- `/transactions`, `/finances`
- `/investments`, `/investments/{id}/entries`, `/investments/rebalance`, `/investments/benchmark`
- `/dividends`, `/incometax`
- `/goals`, `/tags`
- `/gamification/status`

---

## Roadmap

Funcionalidades já entregues:

- CRUD de transações, tags, metas, investimentos, dividendos
- Livro contábil com aporte/resgate/atualização de cotação manual
- Cálculo de IR (isenção de R$ 20k para ações, 20% para FIIs, R$ 35k para cripto)
- Comparativo de rentabilidade vs CDI (taxa BCB série 4391, base 252)
- Rebalanceamento sugerido com base em alocação-alvo
- Dashboard com gamificação (streak + pontos)
- Dark theme completo
- Drawer lateral em todas as telas top-level

Próximos passos sob consideração:

- Integração com API de cotação (BRAPI / Yahoo) para atualizar `currentValue` de ações automaticamente em vez de exigir entrada manual
- Importação de extratos OFX/CSV
- Notificações inteligentes (gastos acima da média, metas próximas do prazo)
- Compose Multiplatform para cliente iOS (avaliar quando houver demanda)
- Modularização Gradle se o app crescer significativamente

---

## Licença

Projeto pessoal. Sem licença pública até o momento.
