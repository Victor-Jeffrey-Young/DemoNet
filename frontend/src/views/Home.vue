<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { getHotItems, getFeatured, getVisibleCategories } from "../api/item";
import { getScenes } from "../api/scene";
import AppCard from "../components/AppCard.vue";
import SearchBar from "../components/SearchBar.vue";
import TypeIcon from "../components/TypeIcon.vue";
import { ArrowLeft, ArrowRight, Search } from "@element-plus/icons-vue";
import { TYPE_META } from "../constants/types.js";

const router = useRouter();
const hotItems = ref([]);
const featured = ref([]);
const scenes = ref([]);

// Carousel state
const hotIdx = ref(0);
const featIdx = ref(0);
const sceneIdx = ref(0);
const cardWidth = ref(320); // default, updated on resize
const cardsPerPage = ref(1);
const GAP = 20; // gap-5

const HOT_TOTAL = 10; // how many trending cards to show
const FEAT_TOTAL = 6; // featured limit

const categories = ref([]);

const heroItem = ref(null);
const heroMeta = ref(null);

const cardBgClasses = [
    "bg-gray-800",
    "bg-blue-950",
    "bg-emerald-950",
    "bg-violet-950",
    "bg-amber-950",
    "bg-rose-950",
];

function calcDimensions() {
    cardWidth.value = window.innerWidth >= 768 ? 360 : 300;
    const leftPad = window.innerWidth * 0.05 + 24; // 5% + 1.5rem
    const availWidth = window.innerWidth - leftPad;
    const perPage = Math.max(1, Math.floor((availWidth + GAP) / (cardWidth.value + GAP)));
    cardsPerPage.value = perPage;
}

// Page-aware navigation
const hotMaxIdx = computed(() => Math.max(0, Math.min(HOT_TOTAL, hotItems.value.length) - cardsPerPage.value));
const featMaxIdx = computed(() => Math.max(0, featured.value.length - cardsPerPage.value));
const sceneMaxIdx = computed(() => Math.max(0, scenes.value.length - cardsPerPage.value));

function hotNext() { hotIdx.value = Math.min(hotIdx.value + 1, hotMaxIdx.value); }
function hotPrev() { hotIdx.value = Math.max(hotIdx.value - 1, 0); }
function featNext() { featIdx.value = Math.min(featIdx.value + 1, featMaxIdx.value); }
function featPrev() { featIdx.value = Math.max(featIdx.value - 1, 0); }
function sceneNext() { sceneIdx.value = Math.min(sceneIdx.value + 1, sceneMaxIdx.value); }
function scenePrev() { sceneIdx.value = Math.max(sceneIdx.value - 1, 0); }

const hotOffset = computed(() => -hotIdx.value * (cardWidth.value + GAP));
const featOffset = computed(() => -featIdx.value * (cardWidth.value + GAP));
const sceneOffset = computed(() => -sceneIdx.value * (cardWidth.value + GAP));

onMounted(async () => {
    calcDimensions();
    window.addEventListener("resize", calcDimensions);

    try {
        const all = await getHotItems({ limit: 12 });
        hotItems.value = all || [];
        if (all && all.length > 0) {
            heroItem.value = all[0];
            heroMeta.value = TYPE_META[all[0].type] || null;
        }
    } catch (e) {
        console.error(e);
    }

    try {
        featured.value = (await getFeatured({ limit: FEAT_TOTAL })) || [];
    } catch (e) {
        /* */
    }

    try {
        scenes.value = (await getScenes({ limit: 4 })) || [];
    } catch (e) {
        // 场景是渐进式入口；接口未就绪时不影响既有首页浏览。
    }

    try {
        const vis = await getVisibleCategories();
        categories.value = vis.filter(s => s.visible).map(s => ({ key: s.type, ...TYPE_META[s.type] }));
    } catch (e) { /* fallback to all */
        categories.value = Object.keys(TYPE_META).map(k => ({ key: k, ...TYPE_META[k] }));
    }
});

onUnmounted(() => {
    window.removeEventListener("resize", calcDimensions);
});

function goList(type) {
    router.push(`/list/${type}`);
}

function goItem(slug) {
    router.push(`/item/${slug}`);
}

function goScene(slug) {
    router.push(`/scene/${slug}`);
}



</script>

<template>
    <main class="flex-grow">
        <!-- ======= Store Header & Category Circles ======= -->
        <section class="max-w-[90%] mx-auto px-6 pt-20 pb-8">
            <div class="mb-14">
                <h1 class="text-6xl md:text-7xl lg:text-8xl font-black text-white leading-[1.05]">
                    试玩派对<span class="text-gray-600">.</span>
                </h1>
                <p class="text-xl md:text-2xl text-gray-500 mt-3 font-light">
                    聚合游戏·电影·动漫·桌游·模型·书籍·音乐·数码·咖啡·线下体验
                </p>
                <div class="mt-6 max-w-md">
                    <SearchBar />
                </div>
            </div>

            <!-- Category Circles -->
            <div class="overflow-x-auto hide-scrollbar -mx-6 px-6">
                <div class="flex gap-8 py-2">
                <button
                    v-for="cat in categories"
                    :key="cat.key"
                    @click="goList(cat.key)"
                    class="flex flex-col items-center gap-2.5 group min-w-[76px]"
                >
                    <div
                        class="w-16 h-16 md:w-20 md:h-20 rounded-full bg-gray-800 flex items-center justify-center group-hover:scale-110 group-hover:bg-gray-700 transition-all duration-300 ring-1 ring-gray-700/50 group-hover:ring-gray-500"
                    >
                        <TypeIcon :type="cat.key" :size="32" />
                    </div>
                    <span
                        class="text-[11px] md:text-xs font-semibold tracking-widest text-gray-500 group-hover:text-white transition-colors uppercase"
                    >
                        {{ cat.label }}
                    </span>
                </button>
            </div>
            </div>
        </section>

        <!-- ======= Hero Super Banner ======= -->
        <section v-if="heroItem" class="max-w-[90%] mx-auto px-6 pb-8">
            <div
                class="relative w-full h-[420px] md:h-[560px] lg:h-[620px] rounded-3xl overflow-hidden group cursor-pointer"
                @click="goItem(heroItem.slug)"
            >
                <div
                    v-if="heroItem.wideCoverUrl || heroItem.coverUrl"
                    class="absolute inset-0 bg-cover bg-center group-hover:scale-[1.03] transition-transform duration-700 ease-out"
                    :style="{
                        backgroundImage:
                            'url(' +
                            (heroItem.wideCoverUrl || heroItem.coverUrl) +
                            ')',
                    }"
                />
                <div v-else class="absolute inset-0 bg-gradient-to-br from-gray-800 to-gray-900" />
                <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/30 to-transparent" />
                <div class="absolute bottom-8 left-8 md:bottom-12 md:left-12 max-w-2xl">
                    <span
                        v-if="heroMeta"
                        class="text-[11px] uppercase font-bold tracking-[0.15em] text-blue-400 mb-3 block"
                    >
                        <TypeIcon :type="heroMeta.key || heroItem.type" size="18" />
                        {{ heroMeta.label }}
                    </span>
                    <h2 class="text-3xl md:text-4xl lg:text-5xl font-black text-white mb-3 drop-shadow-lg leading-tight">
                        {{ heroItem.title }}
                    </h2>
                    <p class="text-gray-300 max-w-xl line-clamp-2 text-sm md:text-base leading-relaxed">
                        {{ heroItem.description }}
                    </p>
                    <button class="mt-5 bg-white/10 backdrop-blur-sm text-white border border-white/20 px-6 py-2.5 rounded-full text-sm font-semibold hover:bg-white/20 transition-colors inline-flex items-center gap-2">
                        <el-icon :size="20" class="text-white"><Search /></el-icon>
                        探索详情
                    </button>
                </div>
            </div>
        </section>

        <!-- ======= Curated Scenarios ======= -->
        <section v-if="scenes.length > 1" class="py-8">
            <div class="flex justify-between items-end px-6 mb-6 max-w-[90%] mx-auto">
                <div>
                    <h2 class="text-2xl font-bold text-white">不知道做什么？从场景开始</h2>
                    <p class="text-gray-400 text-sm mt-1">先看时间、人数和条件，再决定要体验什么</p>
                </div>
            </div>

            <div class="relative">
                <button
                    v-if="sceneIdx > 0"
                    @click="scenePrev"
                    class="absolute left-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="上一个场景"
                >
                    <el-icon :size="24" class="text-white"><ArrowLeft /></el-icon>
                </button>

                <div class="overflow-hidden" :style="{ paddingLeft: 'calc(5% + 1.5rem)' }">
                    <div class="flex gap-5 transition-transform duration-500 ease-out will-change-transform" :style="{ transform: `translateX(${sceneOffset}px)` }">
                        <button
                            v-for="scene in scenes"
                            :key="scene.slug"
                            type="button"
                            @click="goScene(scene.slug)"
                            class="shrink-0 w-[300px] md:w-[360px] min-h-[220px] text-left rounded-3xl overflow-hidden border border-amber-900/50 bg-amber-950/30 hover:border-amber-500/60 hover:-translate-y-1 hover:shadow-2xl hover:shadow-black/30 transition-all duration-300 group relative"
                        >
                            <div v-if="scene.coverUrl" class="absolute inset-0 bg-cover bg-center opacity-30 group-hover:opacity-40 group-hover:scale-105 transition duration-500" :style="{ backgroundImage: 'url(' + scene.coverUrl + ')' }" />
                            <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/85 to-gray-900/40" />
                            <div class="relative z-10 flex h-full flex-col p-6">
                                <span class="text-[10px] uppercase font-bold tracking-widest text-amber-400">场景策展</span>
                                <h3 class="mt-2 text-xl font-bold text-white group-hover:text-amber-200 transition-colors">{{ scene.title }}</h3>
                                <p class="mt-2 text-sm leading-6 text-gray-300 line-clamp-3">{{ scene.description }}</p>
                                <span class="mt-auto pt-5 text-sm font-semibold text-amber-300">查看条件与候选 <span aria-hidden="true">→</span></span>
                            </div>
                        </button>
                    </div>
                </div>

                <button
                    v-if="sceneIdx < sceneMaxIdx"
                    @click="sceneNext"
                    class="absolute right-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="下一个场景"
                >
                    <el-icon :size="24" class="text-white"><ArrowRight /></el-icon>
                </button>
            </div>
        </section>

        <!-- ======= Trending Now Carousel ======= -->
        <section v-if="hotItems.length > 1" class="py-16">
            <div class="flex justify-between items-end px-6 mb-6 max-w-[90%] mx-auto">
                <div>
                    <h3 class="text-2xl font-bold text-white">热门趋势</h3>
                    <p class="text-gray-400 text-sm mt-1">本周最受关注的内容</p>
                </div>
            </div>

            <div class="relative">
                <!-- Left arrow -->
                <button
                    v-if="hotIdx > 0"
                    @click="hotPrev"
                    class="absolute left-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="上一个"
                >
                    <el-icon :size="24" class="text-white"><ArrowLeft /></el-icon>
                </button>

                <!-- Viewport -->
                <div class="overflow-hidden" :style="{ paddingLeft: 'calc(5% + 1.5rem)' }">
                    <div
                        class="flex gap-5 transition-transform duration-500 ease-out will-change-transform"
                        :style="{ transform: `translateX(${hotOffset}px)` }"
                    >
                        <div
                            v-for="(item, idx) in hotItems.slice(0, HOT_TOTAL)"
                            :key="'trend-' + item.id"
                            @click="goItem(item.slug)"
                            class="shrink-0 w-[300px] md:w-[360px] rounded-3xl overflow-hidden group cursor-pointer hover:shadow-2xl hover:shadow-black/30 transition-all duration-300 relative flex flex-col"
                            :class="cardBgClasses[idx % cardBgClasses.length]"
                        >
                            <div class="p-6 pb-2 relative z-10">
                                <span class="text-[10px] uppercase font-bold tracking-widest text-blue-400">
                                    {{ TYPE_META[item.type]?.label || item.type }}
                                </span>
                                <h4 class="text-xl font-bold text-white mt-1.5 group-hover:text-blue-300 transition-colors truncate">
                                    {{ item.title }}
                                </h4>
                                <p class="text-sm text-gray-400 mt-1 line-clamp-1">
                                    {{ item.description }}
                                </p>
                            </div>
                            <div class="h-[200px] w-full mt-auto relative overflow-hidden rounded-b-3xl">
                                <div
                                    v-if="item.wideCoverUrl || item.coverUrl"
                                    class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-500"
                                    :style="{ backgroundImage: 'url(' + (item.wideCoverUrl || item.coverUrl) + ')' }"
                                />
                                <div v-else class="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent flex items-center justify-center">
                                    <span class="text-4xl opacity-40">
                                <TypeIcon :type="item.type" size="40" />
                            </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right arrow -->
                <button
                    v-if="hotIdx < hotMaxIdx"
                    @click="hotNext"
                    class="absolute right-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="下一个"
                >
                    <el-icon :size="24" class="text-white"><ArrowRight /></el-icon>
                </button>
            </div>
        </section>

        <!-- ======= Featured Carousel ======= -->
        <section v-if="featured.length > 0" class="py-8">
            <div class="flex items-center justify-between px-6 mb-6 max-w-[90%] mx-auto">
                <div>
                    <h2 class="text-xl font-bold text-white">精选推荐</h2>
                    <p class="text-gray-400 text-sm mt-1">为你精选的内容</p>
                </div>
            </div>

            <div class="relative">
                <!-- Left arrow -->
                <button
                    v-if="featIdx > 0"
                    @click="featPrev"
                    class="absolute left-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="上一个"
                >
                    <el-icon :size="24" class="text-white"><ArrowLeft /></el-icon>
                </button>

                <!-- Viewport -->
                <div class="overflow-hidden py-1" :style="{ paddingLeft: 'calc(5% + 1.5rem)' }">
                    <div
                        class="flex gap-5 transition-transform duration-500 ease-out will-change-transform"
                        :style="{ transform: `translateX(${featOffset}px)` }"
                    >
                        <AppCard
                            v-for="item in featured"
                            :key="'feat-' + item.id"
                            :item="item"
                            class="shrink-0 w-[300px] md:w-[360px]"
                        />
                    </div>
                </div>

                <!-- Right arrow -->
                <button
                    v-if="featIdx < featMaxIdx"
                    @click="featNext"
                    class="absolute right-2 top-1/2 -translate-y-1/2 z-10 w-11 h-11 rounded-full bg-gray-900/90 backdrop-blur-sm border border-gray-600 flex items-center justify-center hover:bg-gray-700 transition-all shadow-lg hover:scale-105"
                    aria-label="下一个"
                >
                    <el-icon :size="24" class="text-white"><ArrowRight /></el-icon>
                </button>
            </div>
        </section>

    </main>
</template>
