# 🤖 CI/CD - Quality Management Addon

Pipeline de integração e entrega contínua com **versionamento automático** para o addon.

---

## ⚡ Início Rápido (3 Passos)

### 1️⃣ Criar Tag Inicial
```powershell
git tag -a v1.0.0 -m "Release inicial"
git push origin v1.0.0
```

### 2️⃣ Adicionar CI/CD
```powershell
git add .github/
git commit -m "feat: adicionar CI/CD com versionamento automático"
git push origin feat-dev-lcds
```

### 3️⃣ Mergear para Main
```powershell
# Quando pronto, merge para main aciona o CI/CD completo
git checkout main
git merge feat-dev-lcds
git push origin main
```

✅ **Pronto!** O CI/CD vai criar a release automaticamente.

---

## 🎯 Como Funciona

### Resumo Simples

1. **Git Tags** = Versão atual (ex: `v1.0.0`)
2. **Mensagem de Commit** = Tipo de incremento
   - `feat:` → versão 1.0.0 → **1.1.0**
   - `fix:` → versão 1.0.0 → **1.0.1**
   - `BREAKING CHANGE:` → versão 1.0.0 → **2.0.0**
3. **CI/CD** atualiza `build.gradle` automaticamente

### Fluxo Visual

```
VOCÊ faz:                  CI/CD executa:              Resultado:
─────────                  ──────────────              ──────────

git commit -m "feat: X"    → Detecta última tag       → Tag v1.1.0 ✅
git push origin main         Analisa commit            
                             Calcula nova versão       → Gradle lê tag
                             Cria tag v1.1.0             version = "1.1.0" ✅
                             Executa gerarAddon        
                             Cria release              → GitHub Release ✅
```

### Como o CI/CD Detecta a Versão

```bash
# 1. Busca última tag Git
git describe --tags --abbrev=0  # Ex: v1.2.5

# 2. Analisa commits novos
git log v1.2.5..HEAD
# Exemplo de commits:
# - "fix: corrigir bug"
# - "feat: nova funcionalidade"  ← Este define!
# - "chore: atualizar docs"

# 3. Determina incremento
"feat:" encontrado → MINOR
Nova versão: 1.2.5 → 1.3.0

# 4. Cria nova tag Git
git tag -a v1.3.0 -m "Release v1.3.0"

# 5. Gradle lê a versão da tag automaticamente
# O build.gradle tem uma função que busca:
version = getVersionFromGit()  // Retorna "1.3.0"

# 6. Executa gerarAddon com a versão correta
./gradlew gerarAddon  // Usa versão 1.3.0 automaticamente

# 7. Cria release e artefatos
```

### 🎯 Como o build.gradle Pega a Versão Automaticamente

O `build.gradle` tem uma função que busca a versão da última tag Git:

```gradle
def getVersionFromGit() {
    try {
        def tag = 'git describe --tags --abbrev=0'.execute().text.trim()
        return tag.startsWith('v') ? tag.substring(1) : tag  // v1.0.0 → 1.0.0
    } catch (Exception e) {
        return "1.0.0-SNAPSHOT"  // Padrão se não houver tag
    }
}

version = getVersionFromGit()  // Sempre usa a última tag!
```

**✅ Vantagens:**
- ✅ Git Tags = Única fonte da verdade
- ✅ Funciona localmente e no CI/CD
- ✅ Addon Sankhya usa a versão correta automaticamente
- ✅ Não precisa editar `build.gradle` manualmente
- ✅ Sem commits automáticos desnecessários

---

## 📋 Convenções de Commit

| Prefixo | Incremento | Exemplo |
|---------|-----------|---------|
| `feat:` | MINOR | 1.2.3 → **1.3.0** |
| `fix:` | PATCH | 1.2.3 → **1.2.4** |
| `BREAKING CHANGE:` | MAJOR | 1.2.3 → **2.0.0** |
| Outros | PATCH | 1.2.3 → **1.2.4** |

### Exemplos

```bash
# Incrementa MINOR (1.0.0 → 1.1.0)
git commit -m "feat: adicionar relatório de auditoria"

# Incrementa PATCH (1.0.0 → 1.0.1)
git commit -m "fix: corrigir validação de data"

# Incrementa MAJOR (1.0.0 → 2.0.0)
git commit -m "BREAKING CHANGE: remover API legada"
```

---

## 🔄 Workflows

### 1. CI/CD Principal (ci-cd.yml)
**Quando:** Push para `main`, `master`, `feat-dev-lcds`, `develop`

**Faz:**
- ✅ Analisa commits e calcula nova versão
- ✅ Cria tag Git (ex: v1.3.0)
- ✅ Gradle lê versão da tag automaticamente
- ✅ Executa `gerarAddon` → gera `qualitymanagement.exts`
- ✅ Cria GitHub Release com arquivo `.exts` (apenas main/master)
- ✅ Upload artefatos (90 dias)

### 2. Build Dev (build-dev.yml)
**Quando:** Push para branches `feat-*`, `feature/*`, `dev-*`

**Faz:**
- ✅ Build e `gerarAddon` → gera `.exts`
- ✅ Upload artefatos (30 dias)
- ❌ NÃO cria release

## 📦 Artefato Gerado

O `gerarAddon` gera o arquivo:
```
build/libs/qualitymanagement.exts
```

Este é o **addon compilado** pronto para importar no Sankhya.

---

## 🛠️ Verificar Versão Localmente

Antes de commitar, veja qual será a próxima versão:

```powershell
# Windows
.\.github\scripts\check-version.ps1

# Linux/Mac
bash .github/scripts/check-version.sh
```

Saída exemplo:
```
🔍 Analisando versionamento...
📌 Última tag: v1.2.5
📝 Commits desde a última tag:
abc123 - feat: adicionar novo módulo

✨ Nova feature detectada!
🎯 Tipo de bump: MINOR
📦 Próxima versão: v1.3.0
```

---

## 📦 Download de Artefatos

### Via GitHub Releases (Produção)
1. Acesse: https://github.com/lukzy23/gestao-de-qualidade/releases
2. Selecione a versão desejada
3. Baixe o arquivo `qualitymanagement.exts`
4. Importe no Sankhya (Importação de Pacotes)

### Via GitHub Actions (Desenvolvimento)
1. Acesse: https://github.com/lukzy23/gestao-de-qualidade/actions
2. Selecione o workflow executado
3. Role até "Artifacts"
4. Baixe o arquivo `.exts`

### Via CLI
```bash
# Listar runs
gh run list

# Download de artefatos
gh run download

# Download de uma release específica
gh release download v1.0.0
```

---

## 🐛 Problemas Comuns

### ❌ Build falha no CI mas funciona local
**Causa:** Caminhos absolutos do Windows no `build.gradle`

**Solução:** Já configurado para funcionar no Linux (Ubuntu). Se necessário:
```gradle
// No build.gradle, evite:
serverFolder = 'C:\\wildfly_producao'  // ❌

// Use variável de ambiente:
serverFolder = System.getenv('WILDFLY_HOME') ?: ''  // ✅
```

### ❌ Permissão negada para criar releases
**Solução:**
1. GitHub → Settings → Actions → General
2. Workflow permissions → "Read and write permissions" ✅

### ❌ Versão não incrementa
**Verifique:**
- ✅ Está fazendo push para `main` ou `master`?
- ✅ Tag inicial existe? (`git tag -l`)
- ✅ Usou convenção correta? (`feat:`, `fix:`, etc)

---

## 📝 Exemplos Práticos

### Desenvolver Nova Feature
```bash
# 1. Criar branch
git checkout -b feat-gestao-mudancas

# 2. Desenvolver e commitar
git add .
git commit -m "feat: adicionar módulo de gestão de mudanças"

# 3. Push (aciona workflow de dev)
git push origin feat-gestao-mudancas

# 4. Merge para main (aciona CI/CD completo)
git checkout main
git merge feat-gestao-mudancas
git push origin main

# Resultado: Versão incrementada automaticamente! 🎉
```

### Corrigir Bug
```bash
git checkout -b fix-validacao
git add .
git commit -m "fix: corrigir validação de formulário"
git push origin main

# Resultado: PATCH incrementado automaticamente
```

---

## 🎓 Boas Práticas

1. ✅ **Use convenções de commit** - Garante versionamento correto
2. ✅ **Teste em branch dev** - Antes de mergear para main
3. ✅ **Use `[skip ci]`** - Para commits que não precisam de build
   ```bash
   git commit -m "docs: atualizar README [skip ci]"
   ```
4. ✅ **Verifique versão antes de push** - Use script `check-version`

---

## 📂 Estrutura de Arquivos

```
.github/
├── workflows/
│   ├── ci-cd.yml           # Workflow principal
│   ├── build-dev.yml       # Workflow de desenvolvimento
│   └── gradle.properties   # Config Gradle para CI
├── scripts/
│   ├── check-version.ps1   # Verificar versão (Windows)
│   └── check-version.sh    # Verificar versão (Linux/Mac)
└── README.md               # Esta documentação

build.gradle                # Versão atualizada automaticamente
```

---

## ❓ Dúvidas Frequentes

**P: Preciso editar o `version` no `build.gradle` manualmente?**  
R: ❌ NÃO! O Gradle busca automaticamente da última tag Git.

**P: Como começo se não tenho nenhuma tag?**  
R: Crie a primeira tag: `git tag -a v1.0.0 -m "Release inicial"`

**P: Posso pular o CI/CD em um commit?**  
R: ✅ Sim! Use `[skip ci]` na mensagem: `git commit -m "docs: update [skip ci]"`

**P: Onde ficam os artefatos?**  
R: GitHub Releases (produção) e Actions Artifacts (desenvolvimento)

**P: A versão funciona localmente também?**  
R: ✅ Sim! Se você tem tags locais, o Gradle usa elas. Execute `./gradlew properties | grep version` para ver.

**P: Onde fica o arquivo .exts gerado?**  
R: `build/libs/qualitymanagement.exts` - Pronto para importar no Sankhya!

**P: Posso gerar o addon localmente?**  
R: ✅ Sim! Execute `./gradlew gerarAddon` e o arquivo `.exts` será gerado.

---

**🚀 Pronto para começar?** Execute os [3 passos](#-início-rápido-3-passos) acima!
