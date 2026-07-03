#!/bin/bash
for file in frontend/src/components/admin/AdminItemForm.vue frontend/src/components/admin/*EditorFields.vue; do
  sed -i 's/size="small"//g' "$file"
  sed -i 's/el-button--small//g' "$file"
done
