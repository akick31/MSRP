#!/bin/bash
set -e

IMAGES_DIR="./images"
SERVER_URL="https://api.msrpgame.com/api/v1/msrp/admin/images"
ADMIN_KEY="${MSRP_ADMIN_KEY:?MSRP_ADMIN_KEY env var is required}"

for file in "$IMAGES_DIR"/*.jpg; do
    item_id=$(basename "$file" .jpg)
    echo "Uploading $item_id..."
    curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$SERVER_URL/$item_id" \
        -H "Content-Type: application/octet-stream" \
        -H "X-Admin-Key: $ADMIN_KEY" \
        --data-binary "@$file"
    echo ""
done

echo "Done."
