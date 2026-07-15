#!/bin/bash
# Document integrity check script
# Usage: bash scripts/check-docs.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DOC_DIR="$PROJECT_ROOT/document"

echo "======================================"
echo "DemoNet Documentation Integrity Check"
echo "======================================"
echo ""

# Check document counts
echo "📊 Document Inventory:"
echo "  Core Documents (01-11): $(find "$DOC_DIR" -maxdepth 1 -name '[0-1][0-9]-*.md' | grep -E '(0[1-9]|1[01])-' | wc -l | xargs)"
echo "  Reports (19-24): $(find "$DOC_DIR" -maxdepth 1 -name '[12][0-9]-*.md' | grep -E '(19|2[0-4])-' | wc -l | xargs)"
echo "  ADRs: $(find "$DOC_DIR/decisions" -name 'ADR-*.md' 2>/dev/null | wc -l | xargs)"
echo "  Archive: $(find "$DOC_DIR/archive" -name '*.md' 2>/dev/null | wc -l | xargs)"
echo ""

# Check for broken links (only in non-archive documents)
echo "🔗 Checking for broken links..."
BROKEN_LINKS=0

while IFS= read -r line; do
    FILE=$(echo "$line" | cut -d: -f1)
    
    # Skip archive directory
    if [[ "$FILE" == *"/archive/"* ]]; then
        continue
    fi
    
    LINK=$(echo "$line" | cut -d']' -f2 | cut -d'(' -f2 | cut -d')' -f1)
    
    # Skip external links and anchors
    if [[ "$LINK" == http* ]] || [[ "$LINK" == "#"* ]] || [[ "$LINK" == *"#"* && ! "$LINK" =~ \.md ]]; then
        continue
    fi
    
    # Skip regex patterns (e.g., .*\.md)
    if [[ "$LINK" == *"*"* ]]; then
        continue
    fi
    
    # Remove anchor if present
    LINK_FILE=$(echo "$LINK" | cut -d'#' -f1)
    
    # Skip empty links
    if [ -z "$LINK_FILE" ]; then
        continue
    fi
    
    # Resolve relative path
    if [[ "$LINK_FILE" == ../* ]]; then
        # Path goes up from document directory
        BASE_DIR="$(dirname "$FILE")"
        FULL_PATH="$(cd "$BASE_DIR" && cd "$(dirname "$LINK_FILE")" && pwd)/$(basename "$LINK_FILE")"
    elif [[ "$LINK_FILE" == ./* ]]; then
        FULL_PATH="$(dirname "$FILE")/$(echo "$LINK_FILE" | sed 's|^\./||')"
    else
        FULL_PATH="$(dirname "$FILE")/$LINK_FILE"
    fi
    
    if [ ! -f "$FULL_PATH" ]; then
        echo "  ❌ Broken link in $(basename "$FILE"): $LINK"
        BROKEN_LINKS=$((BROKEN_LINKS + 1))
    fi
done < <(grep -r '\[.*\](.*\.md[#)]' "$DOC_DIR" --include="*.md" 2>/dev/null | grep -v "/archive/")

if [ $BROKEN_LINKS -eq 0 ]; then
    echo "  ✅ All links valid"
else
    echo "  ⚠️  Found $BROKEN_LINKS broken link(s)"
fi
echo ""

# Check for missing core documents
echo "📄 Checking core document sequence..."
MISSING_CORE=0
for i in $(seq -f "%02g" 1 11); do
    if ! ls "$DOC_DIR/${i}-"*.md 1> /dev/null 2>&1; then
        echo "  ❌ Missing: ${i}-*.md"
        MISSING_CORE=$((MISSING_CORE + 1))
    fi
done

if [ $MISSING_CORE -eq 0 ]; then
    echo "  ✅ Core documents 01-11 complete"
else
    echo "  ⚠️  Missing $MISSING_CORE core document(s)"
fi
echo ""

# Check for missing ADRs
echo "🏛️  Checking ADR sequence..."
MISSING_ADR=0
for i in $(seq -f "%03g" 1 13); do
    if ! ls "$DOC_DIR/decisions/ADR-${i}-"*.md 1> /dev/null 2>&1; then
        echo "  ❌ Missing: ADR-${i}-*.md"
        MISSING_ADR=$((MISSING_ADR + 1))
    fi
done

if [ $MISSING_ADR -eq 0 ]; then
    echo "  ✅ ADR-001 through ADR-013 complete"
else
    echo "  ⚠️  Missing $MISSING_ADR ADR(s)"
fi
echo ""

# Summary
echo "======================================"
if [ $BROKEN_LINKS -eq 0 ] && [ $MISSING_CORE -eq 0 ] && [ $MISSING_ADR -eq 0 ]; then
    echo "✅ Documentation integrity check PASSED"
    exit 0
else
    echo "⚠️  Documentation integrity check FAILED"
    echo "   Broken links: $BROKEN_LINKS"
    echo "   Missing core docs: $MISSING_CORE"
    echo "   Missing ADRs: $MISSING_ADR"
    exit 1
fi
