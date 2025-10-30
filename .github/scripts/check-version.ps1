# Script PowerShell para verificar qual será a próxima versão baseada nos commits

Write-Host "🔍 Analisando versionamento...`n" -ForegroundColor Blue

# Buscar última tag
$LAST_TAG = git describe --tags --abbrev=0 2>$null
if (-not $LAST_TAG) {
    $LAST_TAG = "v0.0.0"
}

Write-Host "📌 Última tag: $LAST_TAG" -ForegroundColor Yellow

# Remover 'v' se existir
$VERSION = $LAST_TAG -replace '^v', ''

# Extrair major, minor, patch
$parts = $VERSION -split '\.'
$MAJOR = [int]$parts[0]
$MINOR = [int]$parts[1]
$PATCH = [int]$parts[2]

Write-Host "📊 Versão atual: $MAJOR.$MINOR.$PATCH`n" -ForegroundColor Yellow

# Analisar commits desde a última tag
Write-Host "📝 Commits desde a última tag:" -ForegroundColor Blue

try {
    git rev-parse $LAST_TAG 2>&1 | Out-Null
    $COMMITS = git log "$LAST_TAG..HEAD" --pretty=format:"%h - %s"
} catch {
    $COMMITS = git log --pretty=format:"%h - %s"
}

if (-not $COMMITS) {
    Write-Host "❌ Nenhum commit novo desde a última tag" -ForegroundColor Red
    exit 0
}

$COMMITS | ForEach-Object { Write-Host $_ }
Write-Host ""

# Determinar tipo de versão
$BUMP_TYPE = "PATCH"
$NEW_MAJOR = $MAJOR
$NEW_MINOR = $MINOR
$NEW_PATCH = $PATCH

if ($COMMITS -match "^[a-f0-9]+ - (BREAKING CHANGE|major):") {
    $BUMP_TYPE = "MAJOR"
    $NEW_MAJOR = $MAJOR + 1
    $NEW_MINOR = 0
    $NEW_PATCH = 0
    Write-Host "💥 Breaking change detectado!" -ForegroundColor Red
} elseif ($COMMITS -match "^[a-f0-9]+ - (feat|feature|minor):") {
    $BUMP_TYPE = "MINOR"
    $NEW_MINOR = $MINOR + 1
    $NEW_PATCH = 0
    Write-Host "✨ Nova feature detectada!" -ForegroundColor Green
} else {
    $BUMP_TYPE = "PATCH"
    $NEW_PATCH = $PATCH + 1
    Write-Host "🔧 Mudanças de patch detectadas" -ForegroundColor Yellow
}

$NEW_VERSION = "$NEW_MAJOR.$NEW_MINOR.$NEW_PATCH"

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host "🎯 Tipo de bump: $BUMP_TYPE" -ForegroundColor Green
Write-Host "📦 Próxima versão: v$NEW_VERSION" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host ""

# Mostrar comandos para criar release
Write-Host "📋 Para criar a release:" -ForegroundColor Blue
Write-Host "   git push origin main"
Write-Host ""
Write-Host "📋 Para criar tag manualmente:" -ForegroundColor Blue
Write-Host "   git tag -a v$NEW_VERSION -m `"Release v$NEW_VERSION`""
Write-Host "   git push origin v$NEW_VERSION"

