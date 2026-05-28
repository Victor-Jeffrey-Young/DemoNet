<script setup>
const props = defineProps({ modelValue: Object })
const emit = defineEmits(['update:modelValue'])

function set(k, v) {
  const obj = JSON.parse(JSON.stringify(props.modelValue || {}))
  obj[k] = v
  emit('update:modelValue', obj)
}
</script>

<template>
  <h4 class="text-sm font-medium text-amber-400 mt-4 mb-3 border-t border-gray-700 pt-4 flex items-center gap-2">
    <span>📖</span> 书籍信息
  </h4>
  <div class="grid grid-cols-2 gap-4">
    <el-form-item label="作者" size="default">
      <el-input :model-value="modelValue?.author || ''" @input="set('author', $event)" placeholder="如: 刘慈欣" />
    </el-form-item>
    <el-form-item label="年份">
      <el-input :model-value="modelValue?.year || ''" @input="set('year', $event)" placeholder="如: 2008" />
    </el-form-item>
    <el-form-item label="页数">
      <el-input :model-value="modelValue?.pages || ''" @input="set('pages', $event)" placeholder="如: 312" />
    </el-form-item>
    <el-form-item label="分类">
      <el-input :model-value="modelValue?.category || ''" @input="set('category', $event)" placeholder="如: 科幻, 文学" />
    </el-form-item>
    <el-form-item label="试读链接" class="col-span-2">
      <el-input :model-value="modelValue?.reader_url || ''" @input="set('reader_url', $event)" placeholder="PDF 链接或在线试读地址" />
    </el-form-item>
    <el-form-item label="Bilibili 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.bilibili || ''" @input="set('videos.bilibili', $event)" placeholder="BV 号或 B站链接" />
    </el-form-item>
    <el-form-item label="YouTube 视频" class="col-span-2">
      <el-input :model-value="modelValue?.videos?.youtube || ''" @input="set('videos.youtube', $event)" placeholder="YouTube 链接或 ID" />
    </el-form-item>
  </div>
</template>
