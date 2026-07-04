<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import TypeIcon from '../../TypeIcon.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  readerUrl: {
    type: String,
    required: true
  }
})

const bookReaderRef = ref(null)
const epubReady = ref(false)
let epubRendition = null

function destroyEpub() {
  if (epubRendition) {
    epubRendition.destroy()
    epubRendition = null
  }
  epubReady.value = false
}

async function startEpub() {
  if (epubRendition) {
    destroyEpub()
    return
  }
  if (!props.readerUrl || !bookReaderRef.value) return
  epubReady.value = true
  await nextTick()
  try {
    if (!window.ePub) {
      await new Promise((resolve, reject) => {
        const s = document.createElement("script")
        s.src = "https://unpkg.com/epubjs@0.3/dist/epub.min.js"
        s.onload = resolve
        s.onerror = reject
        document.head.appendChild(s)
      })
    }
    if (!window.ePub) {
      epubReady.value = false
      return
    }
    const book = window.ePub(props.readerUrl)
    epubRendition = book.renderTo(bookReaderRef.value, {
      width: "100%",
      height: "100%",
      spread: "none",
      flow: "paginated",
    })
    epubRendition.display()
  } catch (e) {
    console.warn("EPUB load failed:", e.message)
    destroyEpub()
  }
}

onUnmounted(() => {
  destroyEpub()
})
</script>

<template>
  <div class="w-full h-full relative">
    <div ref="bookReaderRef" class="w-full h-full bg-white absolute inset-0" v-show="epubReady" />
    <div v-if="!epubReady" class="w-full h-full bg-linear-to-br from-amber-100 to-amber-50 flex flex-col items-center justify-center gap-3 absolute inset-0">
      <TypeIcon :type="item.type" size="24" />
      <span class="text-amber-800 text-sm font-medium">{{ item.title }}</span>
      <button @click="startEpub" class="px-5 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-sm font-semibold transition-colors shadow-lg relative z-10">
        开始阅读
      </button>
    </div>
  </div>
</template>
