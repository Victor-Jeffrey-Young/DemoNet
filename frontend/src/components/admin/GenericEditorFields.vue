<script setup>
import { computed } from 'vue'

const props = defineProps({ modelValue: Object, type: String })
const emit = defineEmits(['update:modelValue'])

const jsonStr = computed({
  get: () => JSON.stringify(props.modelValue || {}, null, 2),
  set: (val) => {
    try { emit('update:modelValue', JSON.parse(val)) } catch {}
  }
})
</script>

<template>
  <h4 class="text-sm font-medium text-blue-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>📝</span> 扩展信息 (JSON)
  </h4>
  <p class="text-xs text-gray-300 mb-2">当前品类「{{ type }}」暂无专属编辑器，请在下方手动编辑 info_json。</p>
  <el-form-item>
    <el-input
      v-model="jsonStr"
      type="textarea"
      :rows="6"
      placeholder='{"key": "value"}'
      class="font-mono text-xs"
    />
  </el-form-item>
</template>
