# Política de Segurança

O mobile-app-ecociente é o aplicativo Android do EcoCiente (login com Firebase Authentication e Facebook, dados no Firestore, notificações push) e inclui um pequeno backend Node.js que lê PostgreSQL e usa o Firebase Admin. Ele lida com e-mail, cadastro do usuário e perfil (morador, síndico, cooperativa).

## Versões suportadas

| Versão | Suporte |
| --- | --- |
| `main` (mais recente) | Sim |
| Demais branches | Não |

## Reportando uma vulnerabilidade

**Não abra issue, Pull Request ou discussão pública** para relatar vulnerabilidades.

Use o relato privado do GitHub: <https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/security/advisories/new> (Security → Advisories → Report a vulnerability).

> Mantenedores: ative *Private vulnerability reporting* em Settings → Code security.

Inclua no relato:

- o impacto da falha;
- os passos para reproduzir, usando **dados fictícios**;
- o commit testado e a configuração;
- se possível, uma ideia de correção.

Depois do relato: confirmaremos o recebimento, corrigiremos de forma privada e publicaremos um advisory com crédito a quem reportou, se desejado. Pedimos um prazo razoável para a correção antes de qualquer divulgação pública.

## Escopo

**Dentro do escopo**

- Burla de autenticação ou de perfil no app ou no backend (`backend/api`)
- Acesso a documentos de outro usuário no Firestore ou falhas em `firestore.rules`
- Vazamento de dados pessoais, tokens (JWT), chaves ou `FIREBASE_SERVICE_ACCOUNT_KEY_BASE64` em logs, respostas ou repositório
- Injeção SQL no backend
- Segredos commitados e falhas nos workflows (`ci-android.yml`, `pr-bot.yml`) que exponham segredos
- Armazenamento inseguro de dados no dispositivo

**Fora do escopo**

- Bugs sem impacto de segurança
- Falhas em serviços de terceiros (Firebase, Facebook, Vercel, GitHub)
- Valores fictícios usados no CI (`FACEBOOK_*`) e telas de demonstração marcadas como tal (área do síndico em `debug`)
- Negação de serviço no próprio deploy

## Como o sistema protege os dados

| Camada | Onde | O que faz |
| --- | --- | --- |
| Autenticação | Firebase Authentication, `AutenticacaoExternaRepository` | Login no app; as APIs externas usam JWT renovável |
| Autorização no Firestore | `firestore.rules` | Cada usuário lê e escreve só o próprio documento em `usuarios/{uid}`; `codigosRecuperacao` não é acessível pelo app |
| Segredos | `.gitignore`, `.env.example` | `.env` não é versionado; o exemplo traz só campos vazios |
| Backend | `backend/lib/firebaseAdmin.js`, `backend/lib/postgres.js` | Credenciais do Admin SDK e do banco vêm de variáveis de ambiente |
| CI | `.github/workflows/ci-android.yml` | Usa valores fictícios para o Facebook, sem login real |

Observação: o app ainda não valida o papel de síndico no servidor; a área do síndico é uma demonstração (veja o README).

## Checklist de deploy

- `FIREBASE_SERVICE_ACCOUNT_KEY_BASE64`, `DATABASE_URL`/`DB_PASSWORD` e as chaves do Facebook apenas em variáveis de ambiente (Vercel ou Secrets), nunca no código.
- Publicar `firestore.rules` junto com mudanças de perfil e acesso.
- Usuário de banco do backend com o mínimo de permissões (somente leitura, se possível).
- Revisar o `app/google-services.json` (configuração pública do cliente Firebase) e restringir as chaves de API no console do Firebase/Google Cloud.
- Assinar o app de produção com uma chave guardada fora do repositório.
