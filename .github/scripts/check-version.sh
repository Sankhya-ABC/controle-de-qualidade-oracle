#!/bin/bash
# Script para verificar qual será a próxima versão baseada nos commits

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Analisando versionamento...${NC}\n"

# Buscar última tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
echo -e "${YELLOW}📌 Última tag: ${LAST_TAG}${NC}"

# Remover 'v' se existir
VERSION=${LAST_TAG#v}

# Extrair major, minor, patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"

echo -e "${YELLOW}📊 Versão atual: ${MAJOR}.${MINOR}.${PATCH}${NC}\n"

# Analisar commits desde a última tag
echo -e "${BLUE}📝 Commits desde a última tag:${NC}"
if git rev-parse "${LAST_TAG}" >/dev/null 2>&1; then
    COMMITS=$(git log ${LAST_TAG}..HEAD --pretty=format:"%h - %s")
else
    COMMITS=$(git log --pretty=format:"%h - %s")
fi

if [ -z "$COMMITS" ]; then
    echo -e "${RED}❌ Nenhum commit novo desde a última tag${NC}"
    exit 0
fi

echo "$COMMITS"
echo ""

# Determinar tipo de versão
BUMP_TYPE="PATCH"
NEW_MAJOR=$MAJOR
NEW_MINOR=$MINOR
NEW_PATCH=$PATCH

if echo "$COMMITS" | grep -qiE "^[a-f0-9]+ - (BREAKING CHANGE|major):"; then
    BUMP_TYPE="MAJOR"
    NEW_MAJOR=$((MAJOR + 1))
    NEW_MINOR=0
    NEW_PATCH=0
    echo -e "${RED}💥 Breaking change detectado!${NC}"
elif echo "$COMMITS" | grep -qiE "^[a-f0-9]+ - (feat|feature|minor):"; then
    BUMP_TYPE="MINOR"
    NEW_MINOR=$((MINOR + 1))
    NEW_PATCH=0
    echo -e "${GREEN}✨ Nova feature detectada!${NC}"
else
    BUMP_TYPE="PATCH"
    NEW_PATCH=$((PATCH + 1))
    echo -e "${YELLOW}🔧 Mudanças de patch detectadas${NC}"
fi

NEW_VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🎯 Tipo de bump: ${BUMP_TYPE}${NC}"
echo -e "${GREEN}📦 Próxima versão: v${NEW_VERSION}${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Mostrar comandos para criar release
echo -e "${BLUE}📋 Para criar a release:${NC}"
echo -e "   git push origin main"
echo ""
echo -e "${BLUE}📋 Para criar tag manualmente:${NC}"
echo -e "   git tag -a v${NEW_VERSION} -m \"Release v${NEW_VERSION}\""
echo -e "   git push origin v${NEW_VERSION}"

