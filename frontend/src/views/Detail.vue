<script setup>
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getItemBySlug, getHotItems } from "../api/item";
import { useAuthStore } from "../stores/auth";
import { saveUserItem, getUserItemStatus } from "../api/auth";
import AppCard from "../components/AppCard.vue";
import ReviewSection from "../components/ReviewSection.vue";
import AdminItemForm from "../components/admin/AdminItemForm.vue";
import * as echarts from "echarts/dist/echarts.esm.js";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const item = ref(null);
const related = ref([]);
const loading = ref(true);
const myStatus = ref(null);
const savingStatus = ref(false);
const dropdownOpen = ref(false);
let dropdownTimer = null;

function showDropdown() {
    clearTimeout(dropdownTimer);
    dropdownOpen.value = true;
}
function hideDropdown() {
    dropdownTimer = setTimeout(() => {
        dropdownOpen.value = false;
    }, 150);
}

const typeMetaMap = {
    game: {
        label: "游戏",
        emoji: "🎮",
        color: "text-green-400 bg-green-900/50",
    },
    movie: { label: "电影", emoji: "🎬", color: "text-red-400 bg-red-900/50" },
    anime: {
        label: "动漫",
        emoji: "🎭",
        color: "text-purple-400 bg-purple-900/50",
    },
    boardgame: {
        label: "桌游",
        emoji: "🎲",
        color: "text-yellow-400 bg-yellow-900/50",
    },
    model: {
        label: "模型",
        emoji: "🧩",
        color: "text-blue-400 bg-blue-900/50",
    },
    book: {
        label: "书籍",
        emoji: "📖",
        color: "text-amber-400 bg-amber-900/50",
    },
    music: {
        label: "音乐",
        emoji: "🎵",
        color: "text-pink-400 bg-pink-900/50",
    },
    digital: {
        label: "数码",
        emoji: "📱",
        color: "text-cyan-400 bg-cyan-900/50",
    },
    coffee: {
        label: "咖啡",
        emoji: "☕",
        color: "text-orange-400 bg-orange-900/50",
    },
    offline: {
        label: "线下",
        emoji: "🏛️",
        color: "text-indigo-400 bg-indigo-900/50",
    },
};

const statusOptions = [
    { key: "want_to_play", label: "想玩" },
    { key: "played", label: "已体验" },
    { key: "loved", label: "最爱" },
    { key: "dropped", label: "弃了" },
];

onMounted(() => fetchItem());
watch(
    () => route.params.slug,
    () => fetchItem(),
);

async function fetchItem() {
    loading.value = true;
    related.value = [];
    myStatus.value = null;
    try {
        item.value = await getItemBySlug(route.params.slug);
        if (!item.value) {
            router.replace("/");
            return;
        }
        const d = await getHotItems({ type: item.value.type, limit: 4 });
        related.value = (d || []).filter((r) => r.id !== item.value.id);
        if (auth.isLoggedIn) {
            try {
                const ui = await getUserItemStatus(item.value.id);
                if (ui) myStatus.value = ui.status;
            } catch {}
        }
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
}

const typeMeta = computed(
    () =>
        typeMetaMap[item.value?.type] || {
            label: item.value?.type,
            emoji: "",
            color: "text-gray-400 bg-gray-800",
        },
);
const info = computed(() => {
    try {
        return JSON.parse(item.value?.infoJson || "{}");
    } catch {
        return {};
    }
});
const hasDemo = computed(() => !!info.value.demo_url);
const currentStatusLabel = computed(
    () => statusOptions.find((s) => s.key === myStatus.value)?.label || "收藏",
);

function goBack() {
    router.push(item.value?.type ? `/list/${item.value.type}` : "/");
}
function editItem() { showEditDialog.value = true; loadEditTags() }

async function loadEditTags() {
  try {
    const { getAdminTags } = await import('../api/admin')
    editTags.value = await getAdminTags()
  } catch {}
}
function isEmbed(url) {
    return (
        url &&
        (url.includes("youtube.com/embed") ||
            url.includes("player.bilibili.com"))
    );
}

const isBook = computed(() => item.value?.type === "book");
const isMusic = computed(() => item.value?.type === "music");
const isPdf = computed(() => readerUrl.value?.endsWith(".pdf"));
const musicEmbedUrl = computed(() => {
    if (!isMusic.value || !item.value?.externalLink) return "";
    const link = item.value.externalLink;
    if (link.includes("music.apple.com"))
        return link.replace("music.apple.com", "embed.music.apple.com");
    return "";
});
const musicPreviewActive = computed(
    () =>
        isMusic.value &&
        !!info.value.preview_url &&
        !activeVideoUrl.value &&
        !musicEmbedUrl.value,
);
const heroAspectClass = computed(() => {
    if (activeVideoUrl.value || readerUrl.value) return "aspect-video";
    if (musicEmbedUrl.value) return "h-[450px]";
    if (musicPreviewActive.value) return "";
    return "aspect-video";
});
const pdfViewerUrl = computed(() => {
    if (!isPdf.value) return "";
    try {
        return (
            "https://docs.google.com/viewer?url=" +
            encodeURIComponent(window.location.origin + readerUrl.value) +
            "&embedded=true"
        );
    } catch {
        return readerUrl.value;
    }
});
const readerUrl = computed(() => {
    if (!isBook.value) return "";
    const i = info.value;
    const url = i.reader_url || item.value?.mediaUrl || "";
    if (!url) return "";
    if (url.endsWith(".pdf")) return url + "#view=FitH&toolbar=0";
    if (url.includes("books.google"))
        return url + "&printsec=frontcover&output=embed";
    return url;
});
const isEpub = computed(() => readerUrl.value?.endsWith(".epub"));
const bookReaderRef = ref(null);
const epubReady = ref(false);
let epubRendition = null;

function destroyEpub() {
    if (epubRendition) {
        epubRendition.destroy();
        epubRendition = null;
    }
    epubReady.value = false;
}

onUnmounted(() => {
    destroyEpub();
    if (radarChartRef.value) {
        const c = echarts.getInstanceByDom(radarChartRef.value);
        if (c) c.dispose();
    }
});

async function startEpub() {
    if (epubRendition) {
        destroyEpub();
        return;
    }
    if (!readerUrl.value || !bookReaderRef.value) return;
    epubReady.value = true;
    await nextTick();
    try {
        if (!window.ePub) {
            await new Promise((resolve, reject) => {
                const s = document.createElement("script");
                s.src = "https://unpkg.com/epubjs@0.3/dist/epub.min.js";
                s.onload = resolve;
                s.onerror = reject;
                document.head.appendChild(s);
            });
        }
        if (!window.ePub) {
            epubReady.value = false;
            return;
        }
        const book = window.ePub(readerUrl.value);
        epubRendition = book.renderTo(bookReaderRef.value, {
            width: "100%",
            height: "100%",
            spread: "none",
            flow: "paginated",
        });
        epubRendition.display();
    } catch (e) {
        console.warn("EPUB load failed:", e.message);
        destroyEpub();
    }
}

const videos = computed(() => info.value.videos || {});
const videoSources = computed(() =>
    Object.entries(videos.value).map(([k, v]) => ({ key: k, url: v })),
);
const activeVideoUrl = ref("");

watch(
    () => item.value,
    () => {
        if (videos.value.bilibili) activeVideoUrl.value = videos.value.bilibili;
        else if (videos.value.youtube)
            activeVideoUrl.value = videos.value.youtube;
        else if (item.value?.mediaUrl && isEmbed(item.value.mediaUrl))
            activeVideoUrl.value = item.value.mediaUrl;
        else activeVideoUrl.value = "";
    },
);

function switchVideo(url) {
    activeVideoUrl.value = url;
}
async function setStatus(status) {
    if (!auth.isLoggedIn) {
        router.push({ name: "Login", query: { redirect: route.fullPath } });
        return;
    }
    savingStatus.value = true;
    try {
        await saveUserItem(item.value.id, status);
        myStatus.value = status;
    } catch (e) {
        console.error(e);
    } finally {
        savingStatus.value = false;
    }
}

const infoFields = computed(() => {
    if (!item.value) return [];
    const t = item.value.type;
    const i = info.value;
    const map = {
        game: [
            ["开发商", i.developer],
            ["类型", i.genre],
            ["平台", i.platform],
        ],
        movie: [
            ["导演", i.director],
            ["年份", i.year],
            ["时长", i.duration],
        ],
        anime: [
            ["制作社", i.studio],
            ["集数", i.episodes],
            ["年份", i.year],
        ],
        boardgame: [
            ["人数", i.players],
            ["时长", i.playtime],
            ["重度", i.weight],
        ],
        model: [
            ["等级", i.grade],
            ["比例", i.scale],
            ["材质", i.material],
            ["系列", i.series],
        ],
        book: [
            ["作者", i.author],
            ["页数", i.pages],
            ["年份", i.year],
            ["分类", i.category],
        ],
        music: [
            ["艺人", i.artist],
            ["年份", i.year],
            ["风格", i.genre],
            ["曲目", i.tracks],
        ],
        digital: [
            ["品牌", i.brand],
            ["类别", i.category],
            ["年份", i.year],
            ["特性", i.features],
        ],
        coffee: [
            ["产地", i.origin],
            ["烘焙度", i.roast],
            ["处理法", i.process],
            ["品种", i.variety],
        ],
        offline: [
            ["类型", i.event_type],
            ["地点", i.venue],
            ["日期", i.date],
            ["时间", i.time],
        ],
    };
    return (map[t] || []).filter((f) => f[1] != null && f[1] !== "");
});

const isCoffee = computed(() => item.value?.type === "coffee");
const isOffline = computed(() => item.value?.type === "offline");
const isBoardgame = computed(() => item.value?.type === "boardgame");
const showBoardgameRules = ref(false);
const rulePage = ref(0);
const ruleFlipAnim = ref('next');
const ruleThumbs = ref(null);
const showEditDialog = ref(false);
const editTags = ref([]);
const lightboxPage = ref(null);

// Coffee radar chart
const radarChartRef = ref(null);
const scaDimensions = ['aroma','flavor','aftertaste','acidity','body','balance','uniformity','clean_cup','sweetness'];
const scaLabels = ['香气','风味','余韵','酸质','醇厚度','平衡感','一致性','干净度','甜度'];
const hasScaScores = computed(() => {
  if (!isCoffee.value) return false;
  return typeof info.value.total_cup_points === 'number' && info.value.total_cup_points > 0;
});

// Brew guide calculator
const brewDose = ref(15);
const brewRatio = ref('1:15');
const brewTemp = ref('92°C');
const brewRatioNum = computed(() => {
  const parts = brewRatio.value.split(':');
  return parts.length === 2 ? parseFloat(parts[1]) : 15;
});
const brewTime = computed(() => {
  const dose = brewDose.value;
  if (dose <= 15) return '2:00-2:30';
  if (dose <= 20) return '2:30-3:00';
  return '3:00-3:30';
});

function renderRadarChart() {
  if (!radarChartRef.value || !hasScaScores.value) return;
  try {
    const el = radarChartRef.value;
    const existing = echarts.getInstanceByDom(el);
    if (existing) existing.dispose();
    const chart = echarts.init(el);
    const values = scaDimensions.map(d => Number(info.value[d]) || 0);
    chart.setOption({
      backgroundColor: '#1a120a',
      title: { text: 'SCA 评分', left: 'center', textStyle: { color: '#fbbf24', fontSize: 14 } },
      radar: {
        indicator: scaDimensions.map((d, i) => ({ name: scaLabels[i], max: 10 })),
        center: ['50%', '55%'],
        radius: '60%',
        axisName: { fontSize: 9, color: '#fbbf24' },
        splitArea: { areaStyle: { color: ['rgba(251,191,36,0.04)', 'rgba(251,191,36,0.1)'] } },
        axisLine: { lineStyle: { color: 'rgba(251,191,36,0.3)' } },
        splitLine: { lineStyle: { color: 'rgba(251,191,36,0.15)' } }
      },
      series: [{
        type: 'radar',
        data: [{ value: values, name: 'SCA', areaStyle: { color: 'rgba(251,191,36,0.35)' } }],
        lineStyle: { color: '#fbbf24', width: 2.5 },
        itemStyle: { color: '#fbbf24', borderColor: '#fff', borderWidth: 1 },
        symbol: 'circle',
        symbolSize: 5,
        label: { show: true, formatter: '{c}', color: '#fbbf24', fontSize: 10, distance: 8 }
      }]
    });
    setTimeout(() => chart.resize(), 100);
  } catch(e) {
    console.error('radar:', e);
  }
}

// watch for radar chart readiness
watch([hasScaScores, () => route.params.slug], ([show]) => {
  if (show) {
    // retry until ref is bound
    let attempts = 0;
    const tryRender = () => {
      if (radarChartRef.value) {
        renderRadarChart();
      } else if (attempts++ < 20) {
        setTimeout(tryRender, 100);
      }
    };
    tryRender();
  }
}, { immediate: true });

function openLightbox(idx) { lightboxPage.value = idx; }
function closeLightbox() { lightboxPage.value = null; }
function lightboxPrev() { if (lightboxPage.value > 0) lightboxPage.value--; }
function lightboxNext() { if (lightboxPage.value < ruleImages.value.length - 1) lightboxPage.value++; }

function onLightboxKey(e) {
  if (e.key === 'ArrowLeft') { e.preventDefault(); lightboxPrev() }
  else if (e.key === 'ArrowRight') { e.preventDefault(); lightboxNext() }
  else if (e.key === 'Escape') { e.preventDefault(); closeLightbox() }
}

watch(lightboxPage, (val) => {
  if (val != null) window.addEventListener('keydown', onLightboxKey)
  else window.removeEventListener('keydown', onLightboxKey)
})
const lightboxSrc = computed(() => lightboxPage.value != null ? ruleImages.value[lightboxPage.value] : null);
const ruleImages = computed(() => {
  if (!isBoardgame.value) return [];
  const raw = info.value.rule_images || '';
  if (!raw) return [];
  return raw.split(',').map(u => u.trim()).filter(Boolean);
});
const ruleHasContent = computed(() => info.value.rule_text || ruleImages.value.length > 0);

function ruleNext() { if (rulePage.value < ruleImages.value.length - 1) { ruleFlipAnim.value = 'next'; rulePage.value++; scrollThumb(); } }
function rulePrev() { if (rulePage.value > 0) { ruleFlipAnim.value = 'prev'; rulePage.value--; scrollThumb(); } }
function scrollThumb() {
  if (!ruleThumbs.value) return;
  const el = ruleThumbs.value.children[rulePage.value];
  if (el) el.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
}
const coffeeFlavors = computed(() => {
    if (!isCoffee.value) return [];
    const raw = info.value.flavor_notes || info.value.flavor || "";
    if (!raw) return [];
    return raw
        .split(",")
        .map((f) => f.trim())
        .filter(Boolean);
});
</script>

<template>
    <div class="min-h-screen bg-gray-950 text-white">
        <main class="max-w-5xl mx-auto px-6 py-10">
            <div v-if="loading" class="text-center text-gray-500 py-20">
                加载中...
            </div>
            <template v-else-if="item">
                <button
                    @click="goBack"
                    class="text-gray-400 hover:text-white text-sm mb-6 transition"
                >
                    ← 返回
                </button>
                <button
                    v-if="auth.isAdmin"
                    @click="editItem"
                    class="text-gray-500 hover:text-indigo-400 text-sm mb-6 ml-4 transition"
                >
                    ✏️ 编辑
                </button>

                <div
                    class="bg-gray-900 rounded-2xl border border-gray-800 overflow-visible"
                >
                    <div
                        class="rounded-t-2xl overflow-hidden relative"
                        :class="[
                            heroAspectClass,
                            !activeVideoUrl &&
                            !readerUrl &&
                            !musicEmbedUrl &&
                            !musicPreviewActive
                                ? 'bg-linear-to-br from-gray-800 to-gray-950 flex items-center justify-center text-6xl'
                                : '',
                        ]"
                    >
                        <!-- Book reader: EPUB -->
                        <template v-if="isEpub">
                            <div
                                ref="bookReaderRef"
                                class="w-full h-full bg-white"
                                v-show="epubReady"
                            />
                            <div
                                v-if="!epubReady"
                                class="w-full h-full bg-linear-to-br from-amber-100 to-amber-50 flex flex-col items-center justify-center gap-3"
                            >
                                <span class="text-5xl">📖</span>
                                <span
                                    class="text-amber-800 text-sm font-medium"
                                    >{{ item.title }}</span
                                >
                                <button
                                    @click="startEpub"
                                    class="px-5 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-sm font-semibold transition-colors shadow-lg"
                                >
                                    开始阅读
                                </button>
                            </div>
                        </template>
                        <!-- Book reader: PDF -->
                        <template v-else-if="isPdf">
                            <embed
                                :src="readerUrl"
                                type="application/pdf"
                                class="w-full h-full"
                            />
                        </template>
                        <!-- Book reader: web link -->
                        <iframe
                            v-else-if="readerUrl"
                            :src="readerUrl"
                            class="w-full h-full"
                            frameborder="0"
                        />
                        <!-- Video player -->
                        <iframe
                            v-else-if="activeVideoUrl"
                            :src="activeVideoUrl"
                            class="w-full h-full"
                            frameborder="0"
                            allow="
                                accelerometer;
                                autoplay;
                                clipboard-write;
                                encrypted-media;
                                gyroscope;
                                picture-in-picture;
                            "
                            allowfullscreen
                        />
                        <!-- Apple Music embed -->
                        <iframe
                            v-else-if="musicEmbedUrl"
                            :src="musicEmbedUrl"
                            class="w-full h-full"
                            frameborder="0"
                            allow="autoplay *; encrypted-media *; fullscreen *"
                        />
                        <!-- Music album art + audio player -->
                        <div
                            v-else-if="musicPreviewActive"
                            class="w-full py-12 bg-linear-to-br from-fuchsia-950 via-gray-900 to-gray-950 flex items-center justify-center relative overflow-hidden"
                        >
                            <img
                                v-if="item.coverUrl"
                                :src="item.coverUrl"
                                class="absolute inset-0 w-full h-full object-cover opacity-30 blur-2xl scale-110"
                                alt=""
                            />
                            <div
                                class="relative z-10 flex flex-col items-center gap-5"
                            >
                                <div
                                    class="w-36 h-36 rounded-xl overflow-hidden shadow-2xl shadow-fuchsia-900/30 ring-1 ring-white/10"
                                >
                                    <img
                                        v-if="item.coverUrl"
                                        :src="item.coverUrl"
                                        class="w-full h-full object-cover"
                                        alt=""
                                    />
                                    <div
                                        v-else
                                        class="w-full h-full bg-fuchsia-900/50 flex items-center justify-center text-5xl"
                                    >
                                        🎵
                                    </div>
                                </div>
                                <div class="text-center">
                                    <p
                                        class="text-lg font-semibold text-white/90"
                                    >
                                        {{ item.title }}
                                    </p>
                                    <p class="text-sm text-fuchsia-300/80">
                                        {{ info.artist || "" }}
                                    </p>
                                </div>
                                <audio
                                    controls
                                    :src="info.preview_url"
                                    class="w-64 h-8"
                                    style="accent-color: #d946ef"
                                />
                            </div>
                        </div>
                        <!-- Empty state -->
                        <span v-else>{{ typeMeta.emoji }}</span>
                        <div
                            v-if="
                                videoSources.length > 1 &&
                                !readerUrl &&
                                !isMusic
                            "
                            class="absolute top-3 right-3 flex gap-1 z-10"
                        >
                            <button
                                v-for="src in videoSources"
                                :key="src.key"
                                @click="switchVideo(src.url)"
                                :class="
                                    activeVideoUrl === src.url
                                        ? 'bg-white/20 text-white'
                                        : 'bg-black/40 text-white/60 hover:bg-black/60'
                                "
                                class="text-[10px] px-2 py-1 rounded backdrop-blur-sm transition"
                            >
                                {{ src.key === "youtube" ? "YT" : "B站" }}
                            </button>
                        </div>
                    </div>

                    <div class="p-8">
                        <div class="flex items-center gap-3 mb-4">
                            <span
                                :class="typeMeta.color"
                                class="text-xs px-2 py-0.5 rounded"
                                >{{ typeMeta.label }}</span
                            >
                            <span class="text-xs text-gray-600">{{
                                item.source
                            }}</span>
                        </div>

                        <div class="flex items-start justify-between gap-4">
                            <h1 class="text-3xl font-bold mb-4">
                                {{ item.title }}
                            </h1>
                            <div
                                class="relative shrink-0"
                                @mouseenter="showDropdown"
                                @mouseleave="hideDropdown"
                            >
                                <button
                                    :disabled="savingStatus"
                                    :class="
                                        myStatus
                                            ? 'bg-blue-600'
                                            : 'bg-gray-800 hover:bg-gray-700'
                                    "
                                    class="text-sm px-4 py-2 rounded-lg transition flex items-center gap-2"
                                >
                                    {{ currentStatusLabel }} ▾
                                </button>
                                <div
                                    v-show="dropdownOpen"
                                    class="absolute right-0 top-full mt-0.5 bg-gray-800 border border-gray-700 rounded-lg py-1 w-32 z-10"
                                    @mouseenter="showDropdown"
                                    @mouseleave="hideDropdown"
                                >
                                    <button
                                        v-for="opt in statusOptions"
                                        :key="opt.key"
                                        @click="
                                            setStatus(opt.key);
                                            dropdownOpen = false;
                                        "
                                        :class="
                                            myStatus === opt.key
                                                ? 'text-blue-400'
                                                : 'text-gray-300 hover:text-white'
                                        "
                                        class="block w-full text-left px-3 py-1.5 text-sm transition"
                                    >
                                        {{ opt.label }}
                                    </button>
                                </div>
                            </div>
                        </div>

                        <p class="text-gray-300 text-base leading-relaxed mb-6">
                            {{ item.description }}
                        </p>

                        <!-- External link: all types -->
                        <div v-if="item.externalLink" class="flex gap-3 mb-6">
                            <a
                                v-if="isMusic"
                                :href="item.externalLink"
                                target="_blank"
                                class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-fuchsia-600 hover:bg-fuchsia-500 text-white font-semibold text-sm transition-colors shadow-lg shadow-fuchsia-900/20"
                            >
                                🎵 在 Apple Music 中打开
                            </a>
                            <a
                                v-else
                                :href="item.externalLink"
                                target="_blank"
                                class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl border border-gray-600 hover:border-gray-400 text-gray-300 hover:text-white text-sm transition-all"
                            >
                                {{ typeMeta.emoji }} 了解更多 →
                            </a>
                        </div>

                        <div
                            v-if="item.type==='game' && hasDemo"
                            class="flex gap-3 mb-6"
                        >
                            <a
                                v-if="hasDemo"
                                :href="info.demo_url"
                                target="_blank"
                                class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm transition-colors shadow-lg shadow-emerald-900/20"
                            >
                                🎮 免费试玩
                            </a>
                        </div>

                        <div
                            v-if="infoFields.length > 0"
                            class="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 bg-gray-800/50 rounded-lg"
                        >
                            <div
                                v-for="f in infoFields"
                                :key="f[0]"
                                class="text-center"
                            >
                                <div class="text-xs text-gray-500 mb-0.5">
                                    {{ f[0] }}
                                </div>
                                <div class="text-sm font-medium text-gray-200">
                                    {{ f[1] }}
                                </div>
                            </div>
                        </div>

                        <!-- Music: Audio preview -->
                        <div
                            v-if="item.type === 'music' && info.preview_url"
                            class="mt-4 p-4 bg-fuchsia-900/10 border border-fuchsia-500/10 rounded-lg"
                        >
                            <div class="flex items-center gap-3">
                                <span
                                    class="text-xs text-fuchsia-400 font-medium"
                                    >🎵 试听预览</span
                                >
                                <audio
                                    controls
                                    :src="info.preview_url"
                                    class="h-8 w-full max-w-md"
                                    style="accent-color: #d946ef"
                                />
                            </div>
                        </div>

                        <!-- Coffee: Flavor notes -->
                        <div
                            v-if="isCoffee && coffeeFlavors.length"
                            class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg"
                        >
                            <div class="flex flex-wrap items-center gap-2">
                                <span
                                    class="text-xs text-amber-400 font-medium mr-2"
                                    >☕ 风味</span
                                >
                                <span
                                    v-for="f in coffeeFlavors"
                                    :key="f"
                                    class="text-xs px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/20"
                                    >{{ f }}</span
                                >
                            </div>
                        </div>

                        <!-- Coffee: SCA Radar Chart -->
                        <div
                            v-if="hasScaScores"
                            class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg"
                        >
                            <span class="text-xs text-amber-400 font-medium">📊 SCA 品测雷达图</span>
                            <div ref="radarChartRef" class="w-full h-[280px] mt-2"></div>
                        </div>

                        <!-- Coffee: Brew Guide & Calculator -->
                        <div
                            v-if="isCoffee"
                            class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg"
                        >
                            <span class="text-xs text-amber-400 font-medium mb-3 block">☕ 冲煮参数计算器</span>
                            <div class="flex flex-wrap items-end gap-3 mb-3">
                                <div class="flex-1 min-w-[120px]">
                                    <label class="text-[10px] text-amber-500/70 block mb-1">咖啡粉量 (g)</label>
                                    <input v-model.number="brewDose" type="number" min="10" max="60" step="0.5"
                                        class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none focus:border-amber-400/50" />
                                </div>
                                <div class="flex-1 min-w-[120px]">
                                    <label class="text-[10px] text-amber-500/70 block mb-1">粉水比</label>
                                    <select v-model="brewRatio" class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none">
                                        <option value="1:15">1:15 (标准)</option>
                                        <option value="1:16">1:16 (偏淡)</option>
                                        <option value="1:14">1:14 (浓郁)</option>
                                        <option value="1:13">1:13 (极浓)</option>
                                    </select>
                                </div>
                                <div class="flex-1 min-w-[120px]">
                                    <label class="text-[10px] text-amber-500/70 block mb-1">水温</label>
                                    <select v-model="brewTemp" class="w-full bg-amber-900/30 border border-amber-500/20 rounded px-2.5 py-1.5 text-sm text-amber-200 focus:outline-none">
                                        <option value="88°C">88°C (深烘)</option>
                                        <option value="90°C">90°C (中深烘)</option>
                                        <option value="92°C">92°C (中烘/标准)</option>
                                        <option value="94°C">94°C (浅烘)</option>
                                    </select>
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-2 text-xs">
                                <div class="bg-amber-900/20 rounded p-2">
                                    <span class="text-amber-500/70">注水量</span>
                                    <span class="text-amber-200 font-semibold float-right">{{ Math.round(brewDose * brewRatioNum) }}g</span>
                                </div>
                                <div class="bg-amber-900/20 rounded p-2">
                                    <span class="text-amber-500/70">水温</span>
                                    <span class="text-amber-200 font-semibold float-right">{{ brewTemp }}</span>
                                </div>
                                <div class="bg-amber-900/20 rounded p-2">
                                    <span class="text-amber-500/70">建议时间</span>
                                    <span class="text-amber-200 font-semibold float-right">{{ brewTime }}</span>
                                </div>
                                <div class="bg-amber-900/20 rounded p-2">
                                    <span class="text-amber-500/70">粉水比</span>
                                    <span class="text-amber-200 font-semibold float-right">{{ brewRatio }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- Offline: Event detail card -->
                        <div
                            v-if="isOffline"
                            class="mt-4 p-4 bg-indigo-900/10 border border-indigo-500/10 rounded-lg"
                        >
                            <div class="flex items-center gap-2 mb-3">
                                <span
                                    class="text-xs text-indigo-400 font-medium"
                                    >📋 活动详情</span
                                >
                            </div>
                            <div class="space-y-2">
                                <p
                                    v-if="info.venue"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">📍</span>
                                    {{ info.venue }}
                                </p>
                                <p
                                    v-if="info.date"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">📅</span>
                                    {{ info.date }}
                                </p>
                                <p
                                    v-if="info.time"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">🕐</span>
                                    {{ info.time }}
                                </p>
                                <p
                                    v-if="info.price"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">💰</span>
                                    {{ info.price }}
                                </p>
                                <p
                                    v-if="info.capacity"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">👥</span>
                                    {{ info.capacity }}
                                </p>
                                <p
                                    v-if="info.difficulty"
                                    class="text-xs text-gray-300 flex items-center gap-2"
                                >
                                    <span class="text-indigo-400">⭐</span>
                                    {{ info.difficulty }}
                                </p>
                            </div>
                            <div
                                v-if="info.highlights"
                                class="flex flex-wrap gap-1.5 mt-3 pt-3 border-t border-indigo-500/10"
                            >
                                <span class="text-[10px] text-indigo-400"
                                    >✨</span
                                >
                                <span
                                    v-for="h in info.highlights
                                        .split(',')
                                        .map((s) => s.trim())
                                        .filter(Boolean)"
                                    :key="h"
                                    class="text-xs px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/20"
                                    >{{ h }}</span
                                >
                            </div>
                        </div>

                        <!-- Boardgame: Rulebook viewer -->
                        <div
                            v-if="isBoardgame && ruleHasContent"
                            class="mt-4 bg-amber-900/10 border border-amber-500/10 rounded-lg overflow-hidden"
                        >
                            <button
                                @click="showBoardgameRules = !showBoardgameRules; if(showBoardgameRules) rulePage = 0"
                                class="w-full flex items-center justify-between p-4 text-left hover:bg-amber-900/10 transition-colors"
                            >
                                <span class="text-xs text-amber-400 font-medium flex items-center gap-2">
                                    📖 规则书
                                    <span v-if="ruleImages.length" class="text-amber-600 font-normal">({{ ruleImages.length }} 页)</span>
                                    <span class="text-amber-600 font-normal">(点击展开)</span>
                                </span>
                                <svg
                                    :class="showBoardgameRules ? 'rotate-180' : ''"
                                    class="w-3.5 h-3.5 text-amber-500 transition-transform"
                                    fill="none" stroke="currentColor" viewBox="0 0 24 24"
                                >
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                                </svg>
                            </button>

                            <div v-show="showBoardgameRules" class="border-t border-amber-500/10">
                                <!-- Page viewer -->
                                <div v-if="ruleImages.length" class="px-4 py-4">
                                    <!-- Page display -->
                                    <div class="relative mx-auto max-w-2xl">
                                        <div class="rounded-lg overflow-hidden shadow-2xl shadow-amber-900/20 flex items-center justify-center min-h-[200px]"
                                            :style="{ background: 'linear-gradient(135deg, #1a1206, #2d1a05)' }"
                                        >
                                            <Transition :name="ruleFlipAnim === 'next' ? 'page-next' : 'page-prev'" mode="out-in">
                                                <img :key="rulePage" :src="ruleImages[rulePage]"
                                                    class="max-h-[60vh] max-w-full object-contain cursor-zoom-in"
                                                    @click="openLightbox(rulePage)"
                                                    :style="{ aspectRatio: 'auto' }" />
                                            </Transition>
                                        </div>

                                        <!-- Navigation arrows -->
                                        <button v-if="rulePage > 0" @click="rulePrev"
                                            class="absolute left-2 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full bg-black/30 hover:bg-amber-900/60 flex items-center justify-center text-amber-300/60 hover:text-amber-200 transition-all">
                                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
                                        </button>
                                        <button v-if="rulePage < ruleImages.length - 1" @click="ruleNext"
                                            class="absolute right-2 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full bg-black/30 hover:bg-amber-900/60 flex items-center justify-center text-amber-300/60 hover:text-amber-200 transition-all">
                                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                                        </button>

                                        <!-- Page counter -->
                                        <div class="text-center mt-2">
                                            <span class="text-xs text-amber-500 font-mono">第 {{ rulePage + 1 }} 页 / 共 {{ ruleImages.length }} 页</span>
                                        </div>
                                    </div>

                                    <!-- Thumbnail strip -->
                                    <div ref="ruleThumbs" class="rule-thumb-strip flex gap-2 mt-4 overflow-x-auto pb-2 justify-center">
                                        <img
                                            v-for="(src, i) in ruleImages" :key="i" :src="src"
                                            @click="ruleFlipAnim = i > rulePage ? 'next' : 'prev'; rulePage = i"
                                            :class="i === rulePage ? 'ring-2 ring-amber-400 opacity-100' : 'opacity-50 hover:opacity-80 ring-amber-400/0'"
                                            class="w-14 h-20 object-cover rounded cursor-pointer shrink-0 transition-all hover:ring-1"
                                        />
                                    </div>
                                </div>

                                <!-- Rule text -->
                                <div v-if="info.rule_text" class="px-4 pt-3 pb-4">
                                    <pre class="text-xs text-amber-200/80 whitespace-pre-wrap font-sans leading-relaxed overflow-auto max-h-64 bg-amber-900/20 rounded-lg p-3">{{ info.rule_text }}</pre>
                                </div>
                            </div>
                        </div>

                        <!-- Rule image lightbox -->
                        <Teleport to="body">
                            <div
                                v-if="lightboxPage != null"
                                @click.self="closeLightbox"
                                class="fixed inset-0 z-50 bg-black/95 flex items-center justify-center"
                            >
                                <button @click.stop="lightboxPrev"
                                    v-if="lightboxPage > 0"
                                    class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white/80 hover:text-white transition-all">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
                                </button>
                                <button @click.stop="lightboxNext"
                                    v-if="lightboxPage < ruleImages.length - 1"
                                    class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white/80 hover:text-white transition-all">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                                </button>
                                <img :src="lightboxSrc" class="max-w-[85vw] max-h-[88vh] object-contain rounded shadow-2xl" />
                                <div class="absolute bottom-6 text-white/50 text-sm font-mono">
                                    {{ lightboxPage + 1 }} / {{ ruleImages.length }}
                                </div>
                                <button @click.stop="closeLightbox" class="absolute top-4 right-4 text-white/60 hover:text-white text-2xl leading-none">✕</button>
                            </div>
                        </Teleport>

                        <div class="mt-6 text-xs text-gray-600">
                            更新于
                            {{
                                new Date(item.updatedAt).toLocaleDateString(
                                    "zh-CN",
                                )
                            }}
                        </div>
                    </div>
                </div>

                <section v-if="related.length > 0" class="mt-16">
                    <h2 class="text-xl font-semibold mb-6">相关推荐</h2>
                    <div
                        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
                    >
                        <AppCard v-for="r in related" :key="r.id" :item="r" />
                    </div>
                </section>

                <section v-if="item.id" class="mt-16 max-w-5xl mx-auto px-6">
                    <ReviewSection :item-id="item.id" />
                </section>
            </template>
            <div v-else class="text-center text-gray-500 py-20">
                <div class="text-4xl mb-3">📭</div>
                <p>内容未找到</p>
                <button
                    @click="goBack"
                    class="mt-4 text-sm text-blue-400 hover:text-blue-300 transition"
                >
                    ← 返回列表
                </button>
            </div>
        </main>
    

        <!-- Admin quick-edit dialog -->
        <div class="detail-edit-dialog">
            <AdminItemForm
                :visible="showEditDialog"
                :item="item"
                :all-tags="editTags"
                @update:visible="showEditDialog = $event"
                @saved="showEditDialog = false; fetchItem()"
            />
        </div>
    </div>
</template>

<style>
.page-next-enter-active,
.page-next-leave-active,
.page-prev-enter-active,
.page-prev-leave-active {
  transition: all 0.35s ease;
}
.page-next-enter-from {
  opacity: 0;
  transform: translateX(60px) rotateY(-10deg) scale(0.95);
}
.page-next-leave-to {
  opacity: 0;
  transform: translateX(-60px) rotateY(10deg) scale(0.95);
}
.page-prev-enter-from {
  opacity: 0;
  transform: translateX(-60px) rotateY(10deg) scale(0.95);
}
.page-prev-leave-to {
  opacity: 0;
  transform: translateX(60px) rotateY(-10deg) scale(0.95);
}
.rule-thumb-strip::-webkit-scrollbar {
  height: 4px;
}
.rule-thumb-strip::-webkit-scrollbar-track {
  background: transparent;
}
.rule-thumb-strip::-webkit-scrollbar-thumb {
  background: rgba(217, 119, 6, 0.3);
  border-radius: 4px;
}
.rule-thumb-strip::-webkit-scrollbar-thumb:hover {
  background: rgba(217, 119, 6, 0.5);
}
.detail-edit-dialog .el-dialog {
  --el-dialog-bg-color: #1f2937;
  --el-dialog-border-radius: 12px;
}
.detail-edit-dialog .el-dialog__header { border-bottom: 1px solid #374151; padding-bottom: 16px; }
.detail-edit-dialog .el-dialog__title { color: #f3f4f6; font-weight: 700; }
.detail-edit-dialog .el-input__wrapper {
  background-color: #374151 !important;
  box-shadow: 0 0 0 1px #4b5563 !important;
}
.detail-edit-dialog .el-input__inner { color: #f3f4f6; }
.detail-edit-dialog .el-input__inner::placeholder { color: #6b7280; }
.detail-edit-dialog .el-form-item__label { color: #d1d5db; font-weight: 600; }
.detail-edit-dialog .el-button--default {
  --el-button-bg-color: #374151; --el-button-border-color: #4b5563; --el-button-text-color: #d1d5db;
}
.detail-edit-dialog .el-button--default:hover {
  --el-button-bg-color: #4b5563; --el-button-border-color: #6b7280; --el-button-text-color: #f3f4f6;
}
.detail-edit-dialog .el-button--primary {
  --el-button-bg-color: #2563eb; --el-button-border-color: #3b82f6;
}
.detail-edit-dialog .el-textarea__inner {
  background-color: #374151; border-color: #4b5563; color: #f3f4f6;
}
.detail-edit-dialog .el-textarea__inner::placeholder { color: #6b7280; }
.detail-edit-dialog .el-select .el-input__wrapper {
  --el-fill-color-blank: #374151; --el-border-color: #4b5563;
}
.detail-edit-dialog .el-select .el-input__inner { color: #f3f4f6; }
.detail-edit-dialog .el-select .el-input__wrapper {
  --el-fill-color-blank: #374151;
  --el-border-color: #4b5563;
  background-color: #374151 !important;
}
.detail-edit-dialog .el-select .el-input__inner { color: #f3f4f6; }
.detail-edit-dialog .el-select .el-select__caret { color: #9ca3af; }
.detail-edit-dialog .el-select__placeholder { color: #6b7280 !important; }
.detail-edit-dialog .el-loading-mask { background-color: rgba(31, 41, 55, 0.8); }
.el-select-dropdown.is-multiple .el-select-dropdown__item.selected { color: #60a5fa; }
.admin-select-drop {
  --el-fill-color-blank: #1f2937;
  background-color: #1f2937 !important;
  border-color: #374151 !important;
}
.admin-select-drop .el-select-dropdown__item { color: #e5e7eb; }
.admin-select-drop .el-select-dropdown__item.is-hovering,
.admin-select-drop .el-select-dropdown__item:hover { background-color: #374151; }
.admin-select-drop .el-select-dropdown__item.selected { color: #60a5fa; font-weight: 600; }
</style>
