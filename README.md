# FindPower 💸

**FindPower** é uma aplicação Android nativa para gestão financeira inteligente. O projeto utiliza **Inteligência Artificial (Google Gemini)** para mentoria, **Firebase** para sincronização em nuvem e notificações, e **Room** para funcionamento offline.

---

## 🚀 Funcionalidades Principais

- **Gestão de Gastos:** Registo de despesas/receitas com categorias e persistência híbrida (Local + Nuvem).
- **Mentoria com IA:** Chat de consultoria financeira que analisa os teus dados reais usando o **Google Gemini**.
- **Notificações Push:** Alertas automáticos via **FCM v1** ao registar novas movimentações.
- **Modo Offline:** Consulta e registo de dados mesmo sem internet (sincronização automática posterior).
- **Relatórios:** Gráficos interativos para análise de saúde financeira.

---

## 🛠 Como Rodar o Projeto

### 1. Pré-requisitos
- **Android Studio Jellyfish** (ou superior).
- **JDK 17** configurado no Android Studio.
- Um dispositivo físico ou emulador com **Android 13 (API 33)** ou superior (para testar permissões de Push).

### 2. Configuração do Firebase
- Crie um projeto na [Consola do Firebase](https://console.firebase.google.com/).
- Ative o **Authentication** (método Email/Password) e o **Realtime Database**.
- Descarregue o ficheiro `google-services.json` e coloque-o na pasta `app/` do projeto.

### 3. Variáveis de Ambiente (Importante)
O projeto utiliza o ficheiro `local.properties` para gerir chaves sensíveis de forma segura. Adicione as seguintes linhas no seu `local.properties` (na raiz do projeto):

```properties
# Chave para a Mentoria com IA
gemini.api.key=SUA_API_KEY_AQUI

# ID do seu projeto Firebase (encontrado nas definições do projeto)
firebase.project.id=seu-projeto-id

# Token OAuth2 para envio de Push (ver Nota Importante abaixo)
fcm.access.token=ya29.vossa_chave_oauth2_aqui
```

### 4. Execução
1. Faça o **Sync Project with Gradle Files**.
2. Compile e rode a app (`Run 'app'`).
3. No primeiro acesso, aceite a **permissão de notificações** para que o Push funcione.

---

## ⚠️ Detalhes Importantes (Checklist Técnica)

- **Expiração do Token FCM:** O `fcm.access.token` é um token OAuth2 que **expira a cada 60 minutos**. Se o push parar de funcionar com erro 401, deve gerar um novo token via Google Cloud CLI (`gcloud auth print-access-token`) e atualizar o `local.properties`.
- **Offline-First:** O projeto utiliza o padrão **Repository**, onde os dados são salvos primeiro no **Room**. O sucesso da gravação local é independente da internet; a sincronização com o Firebase ocorre em segundo plano.
- **Injeção de Dependências:** Utilizamos **Dagger Hilt**. Caso encontre erros de compilação relacionados a "Unresolved reference", certifique-se de que o plugin do Hilt está ativo e faça um *Rebuild Project*.
- **Permissões Android 13+:** A aplicação está preparada para o novo modelo de permissões. O token de push só é registado no servidor após o utilizador clicar em "Permitir" na caixa de diálogo de notificações.

---

## 📦 Stack Técnica
- **Linguagem:** Kotlin
- **Arquitetura:** MVVM + Repository Pattern
- **DI:** Dagger Hilt
- **DB:** Room (Local) & Firebase RTDB (Remote)
- **Networking:** Retrofit 2 & OkHttp
- **Push:** Firebase Cloud Messaging (v1 HTTP)
- **IA:** Google Gemini AI SDK

---
## 👥 Equipa
- **Maria**
- **Laura**

*Projeto desenvolvido para fins académicos e de portefólio.*
