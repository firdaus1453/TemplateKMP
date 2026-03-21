#!/usr/bin/env bash
#
# validate-module.sh — Validates that a KMP/CMP feature module follows
# the correct Clean Architecture structure.
#
# Usage:
#   scripts/validate-module.sh <module-path>
#
# Example:
#   scripts/validate-module.sh feature/chat
#
# Exit codes:
#   0 — All checks passed
#   1 — Validation errors found

set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <module-path>"
    echo "Example: $0 feature/chat"
    exit 1
fi

MODULE_PATH="$1"
ERRORS=0

info()  { echo "  ✅ $1"; }
warn()  { echo "  ⚠️  $1"; }
error() { echo "  ❌ $1"; ERRORS=$((ERRORS + 1)); }

echo ""
echo "🔍 Validating module: $MODULE_PATH"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 1. Check domain module exists (required)
echo ""
echo "📦 Checking module structure..."
if [[ -d "$MODULE_PATH/domain" ]]; then
    info "domain/ module exists"
else
    error "domain/ module is MISSING (required)"
fi

# 2. Check optional modules
for layer in data presentation database; do
    if [[ -d "$MODULE_PATH/$layer" ]]; then
        info "$layer/ module exists"
    fi
done

# 3. Check build.gradle.kts in each submodule
echo ""
echo "🔧 Checking build files..."
for dir in "$MODULE_PATH"/*/; do
    layer=$(basename "$dir")
    if [[ -f "$dir/build.gradle.kts" ]]; then
        info "$layer/build.gradle.kts exists"

        # Check convention plugin usage
        if grep -q "convention\." "$dir/build.gradle.kts" 2>/dev/null; then
            info "$layer uses convention plugin"
        else
            warn "$layer does NOT use a convention plugin — consider using one"
        fi
    else
        error "$layer/ is missing build.gradle.kts"
    fi
done

# 4. Check domain module purity (no framework imports)
echo ""
echo "🧹 Checking domain purity..."
DOMAIN_SRC="$MODULE_PATH/domain/src/commonMain/kotlin"
if [[ -d "$DOMAIN_SRC" ]]; then
    # Check for forbidden imports in domain
    FORBIDDEN_IMPORTS=$(grep -r "import io\.ktor\|import androidx\.\|import org\.koin\|import android\." "$DOMAIN_SRC" 2>/dev/null || true)
    if [[ -z "$FORBIDDEN_IMPORTS" ]]; then
        info "domain/ has no framework imports (pure Kotlin)"
    else
        error "domain/ contains framework imports — must be pure Kotlin:"
        echo "$FORBIDDEN_IMPORTS" | head -5 | sed 's/^/        /'
    fi
else
    warn "domain/src/commonMain/kotlin/ not found — skipping purity check"
fi

# 5. Check data module depends on domain
echo ""
echo "🔗 Checking dependency rules..."
DATA_BUILD="$MODULE_PATH/data/build.gradle.kts"
if [[ -f "$DATA_BUILD" ]]; then
    if grep -q "domain" "$DATA_BUILD" 2>/dev/null; then
        info "data depends on domain"
    else
        error "data does NOT depend on domain — add implementation(projects.feature.<name>.domain)"
    fi
fi

# 6. Check presentation module depends on domain
PRES_BUILD="$MODULE_PATH/presentation/build.gradle.kts"
if [[ -f "$PRES_BUILD" ]]; then
    if grep -q "domain" "$PRES_BUILD" 2>/dev/null; then
        info "presentation depends on domain"
    else
        error "presentation does NOT depend on domain"
    fi

    # Check that presentation does NOT depend on data directly
    if grep -q "\.data)" "$PRES_BUILD" 2>/dev/null; then
        error "presentation depends on data DIRECTLY — violates Clean Architecture"
    else
        info "presentation does NOT depend on data directly"
    fi
fi

# 7. Check settings.gradle.kts includes module
echo ""
echo "📋 Checking settings.gradle.kts registration..."
SETTINGS_FILE="settings.gradle.kts"
if [[ -f "$SETTINGS_FILE" ]]; then
    MODULE_GRADLE_PATH=$(echo "$MODULE_PATH" | sed 's|/|:|g')
    for dir in "$MODULE_PATH"/*/; do
        layer=$(basename "$dir")
        FULL_PATH=":$MODULE_GRADLE_PATH:$layer"
        if grep -q "$FULL_PATH" "$SETTINGS_FILE" 2>/dev/null; then
            info "$FULL_PATH registered in settings.gradle.kts"
        else
            error "$FULL_PATH NOT found in settings.gradle.kts — add: include(\"$FULL_PATH\")"
        fi
    done
fi

# 8. Check DI module exists
echo ""
echo "💉 Checking Koin DI..."
DI_FILES=$(find "$MODULE_PATH" -path "*/di/*Module*" -name "*.kt" 2>/dev/null || true)
if [[ -n "$DI_FILES" ]]; then
    info "Koin DI module(s) found:"
    echo "$DI_FILES" | sed 's/^/        /'
else
    warn "No Koin DI module found — create *Module.kt in a di/ package"
fi

# 9. Check test files exist
echo ""
echo "🧪 Checking tests..."
TEST_FILES=$(find "$MODULE_PATH" -path "*/commonTest/*" -name "*.kt" 2>/dev/null || true)
if [[ -n "$TEST_FILES" ]]; then
    TEST_COUNT=$(echo "$TEST_FILES" | wc -l | tr -d ' ')
    info "$TEST_COUNT test file(s) found"
else
    warn "No test files found in commonTest/ — add unit tests"
fi

# Summary
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [[ $ERRORS -eq 0 ]]; then
    echo "✅ All checks passed!"
    exit 0
else
    echo "❌ $ERRORS error(s) found. Fix them before proceeding."
    exit 1
fi
