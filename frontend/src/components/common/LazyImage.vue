<script setup>
import { ref, computed, onErrorCaptured } from 'vue'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  // 加载失败时的后备图片类型
  fallbackType: {
    type: String,
    default: 'default' // default | upload | poster | cover
  },
  // CSS 类名
  class: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['error', 'load'])

const hasError = ref(false)
const isLoading = ref(true)

// 后备图片映射（使用占位图服务）
const fallbackImages = {
  default: 'https://placehold.co/600x400/1a1a2e/808080?text=No+Image',
  upload: 'https://placehold.co/600x400/1a1a2e/808080?text=File+Not+Found',
  poster: 'https://placehold.co/300x450/1a1a2e/808080?text=No+Poster',
  cover: 'https://placehold.co/1920x1080/1a1a2e/808080?text=No+Cover'
}

function handleError(event) {
  hasError.value = true
  isLoading.value = false
  emit('error', event)

  // 符合 Observability Skill：结构化日志
  if (import.meta.env.DEV) {
    console.debug('[LazyImage] Load failed:', {
      event: 'image_load_failed',
      src: props.src,
      fallback_type: props.fallbackType
    })
  }
}

function handleLoad(event) {
  isLoading.value = false
  hasError.value = false
  emit('load', event)
}

function getFallbackSrc() {
  return fallbackImages[props.fallbackType] || fallbackImages.default
}

// 根据 src 自动判断 fallback 类型
function detectFallbackType(src) {
  if (!src) return 'default'
  if (src.startsWith('/uploads/')) return 'upload'
  if (src.includes('poster')) return 'poster'
  if (src.includes('cover')) return 'cover'
  return 'default'
}

// 自动检测 fallback 类型
const autoFallbackType = computed(() => {
  return detectFallbackType(props.src)
})
</script>

<template>
  <div class="lazy-image-wrapper relative" :class="props.class">
    <!-- 骨架屏加载状态（符合 Frontend UI Engineering Skill） -->
    <div
      v-if="isLoading"
      class="absolute inset-0 bg-gray-900 animate-pulse rounded"
      role="status"
      aria-label="图片加载中"
    />

    <!-- 主图片 -->
    <img
      v-show="!hasError"
      :src="src"
      :alt="alt"
      :class="[
        'transition-opacity duration-300',
        { 'opacity-0': isLoading, 'opacity-100': !isLoading }
      ]"
      @load="handleLoad"
      @error="handleError"
      loading="lazy"
    />

    <!-- 加载失败时的后备图片（符合可访问性标准） -->
    <img
      v-if="hasError"
      :src="getFallbackSrc()"
      :alt="alt || '图片加载失败'"
      class="absolute inset-0 w-full h-full object-cover opacity-60"
      role="img"
      aria-label="图片加载失败，显示占位图"
    />

    <!-- 可选的错误提示（仅屏幕阅读器可见） -->
    <span
      v-if="hasError"
      class="sr-only"
      role="alert"
    >
      图片加载失败
    </span>
  </div>
</template>

<style scoped>
/* 屏幕阅读器专用样式（符合 WCAG 可访问性标准） */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
