<script setup>
import { computed, ref, watch } from 'vue'
import TypeIcon from '../../TypeIcon.vue'
import EpubReader from './EpubReader.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  info: {
    type: Object,
    default: () => ({})
  }
})

// Video logic
function isEmbed(url) {
  return url && (url.includes("youtube.com/embed") || url.includes("player.bilibili.com"))
}

const videos = computed(() => props.info.videos || {})
const videoSources = computed(() => Object.entries(videos.value).filter(([, v]) => v).map(([k, v]) => ({ key: k, url: v })))
const activeVideoUrl = ref("")

watch(() => props.item, () => {
  if (videos.value.steam) activeVideoUrl.value = videos.value.steam
  else if (videos.value.bilibili) activeVideoUrl.value = videos.value.bilibili
  else if (videos.value.youtube) activeVideoUrl.value = videos.value.youtube
  else if (props.item?.mediaUrl && isEmbed(props.item.mediaUrl)) activeVideoUrl.value = props.item.mediaUrl
  else activeVideoUrl.value = ""
}, { immediate: true })

const isSteamVideo = computed(() => activeVideoUrl.value && activeVideoUrl.value.includes("steamstatic.com"))

// Reader logic
const isBook = computed(() => props.item?.type === "book")
const readerUrl = computed(() => {
  if (!isBook.value) return ""
  const url = props.info.reader_url || props.item?.mediaUrl || ""
  if (!url) return ""
  if (url.endsWith(".pdf")) return url + "#view=FitH&toolbar=0"
  if (url.includes("books.google")) return url + "&printsec=frontcover&output=embed"
  return url
})
const isEpub = computed(() => readerUrl.value?.endsWith(".epub"))
const isPdf = computed(() => readerUrl.value?.endsWith(".pdf"))

// Music logic
const isMusic = computed(() => props.item?.type === "music")
const musicEmbedUrl = computed(() => {
  if (!isMusic.value || !props.item?.externalLink) return ""
  const link = props.item.externalLink
  if (link.includes("music.apple.com")) return link.replace("music.apple.com", "embed.music.apple.com")
  return ""
})
const musicPreviewActive = computed(() => isMusic.value && !!props.info.preview_url && !activeVideoUrl.value && !musicEmbedUrl.value)

// Hero Aspect Ratio
const heroAspectClass = computed(() => {
  if (activeVideoUrl.value || readerUrl.value) return "aspect-video"
  if (musicEmbedUrl.value) return "h-[450px]"
  if (musicPreviewActive.value) return ""
  return "aspect-video"
})

// Video switch exposed to parent if needed, or we can render tabs inside here? 
// The original code rendered the video switch buttons in the header of Detail.vue.
// To keep things clean, let's expose `videoSources`, `activeVideoUrl` so the parent can render the buttons.
defineExpose({
  videoSources,
  activeVideoUrl,
  readerUrl,
  isMusic
})
</script>

<template>
  <div class="rounded-t-2xl overflow-hidden relative" :class="[heroAspectClass, !activeVideoUrl && !readerUrl && !musicEmbedUrl && !musicPreviewActive ? 'bg-linear-to-br from-gray-800 to-gray-950 flex items-center justify-center text-6xl' : '']">
    
    <template v-if="isEpub">
      <EpubReader :item="item" :reader-url="readerUrl" />
    </template>
    
    <template v-else-if="isPdf">
      <embed :src="readerUrl" type="application/pdf" class="w-full h-full" />
    </template>
    
    <iframe v-else-if="readerUrl" :src="readerUrl" class="w-full h-full" frameborder="0" />
    
    <iframe v-else-if="activeVideoUrl && !isSteamVideo" :src="activeVideoUrl" class="w-full h-full" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen />
    
    <video v-else-if="activeVideoUrl && isSteamVideo" :src="activeVideoUrl" class="w-full h-full object-cover" controls autoplay />
    
    <iframe v-else-if="musicEmbedUrl" :src="musicEmbedUrl" class="w-full h-full" frameborder="0" allow="autoplay *; encrypted-media *; fullscreen *" />
    
    <div v-else-if="musicPreviewActive" class="w-full py-12 bg-linear-to-br from-fuchsia-950 via-gray-900 to-gray-950 flex items-center justify-center relative overflow-hidden">
      <img v-if="item.coverUrl" :src="item.coverUrl" class="absolute inset-0 w-full h-full object-cover opacity-30 blur-2xl scale-110" alt="" />
      <div class="relative z-10 flex flex-col items-center gap-5">
        <div class="w-36 h-36 rounded-xl overflow-hidden shadow-2xl shadow-fuchsia-900/30 ring-1 ring-white/10">
          <img v-if="item.coverUrl" :src="item.coverUrl" class="w-full h-full object-cover" alt="" />
          <div v-else class="w-full h-full bg-fuchsia-900/50 flex items-center justify-center text-5xl">
            <TypeIcon :type="item.type" size="16" />
          </div>
        </div>
        <div class="text-center">
          <p class="text-lg font-semibold text-white/90">{{ item.title }}</p>
          <p class="text-sm text-fuchsia-300/80">{{ info.artist || "" }}</p>
        </div>
        <audio controls :src="info.preview_url" class="w-64 h-8" style="accent-color: #d946ef" />
      </div>
    </div>
    
    <span v-else><TypeIcon :type="item.type" size="28" /></span>
  </div>
</template>
