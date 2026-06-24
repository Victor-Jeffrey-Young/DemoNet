<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getHotItems, getRecommended } from "../api/item";
import AppCard from "../components/AppCard.vue";
import SearchBar from "../components/SearchBar.vue";
import { TYPE_META } from "../constants/types.js";

const router = useRouter();
const hotItems = ref([]);
const recommended = ref([]);
const hotByType = ref({});

const categories = Object.keys(TYPE_META).map(key => ({
    key,
    ...TYPE_META[key],
}));

const mainBanner = ref(null);
const bannerMeta = ref(null);

onMounted(async () => {
    try {
        const all = await getHotItems({ limit: 8 });
        hotItems.value = all || [];
        if (all && all.length > 0) {
            mainBanner.value = all[0];
            bannerMeta.value = TYPE_META[all[0].type] || null;
        }
    } catch (e) {
        console.error(e);
    }

    try {
        recommended.value = (await getRecommended({ limit: 4 })) || [];
    } catch (e) {
        /* */
    }

    for (const cat of ["game", "movie"]) {
        try {
            const data = await getHotItems({ type: cat, limit: 4 });
            hotByType.value[cat] = data || [];
        } catch (e) {
            /* */
        }
    }
});

function goList(type) {
    router.push(`/list/${type}`);
}

const accentColors = {
    emerald:  { bg: 'rgba(16,185,129,0.2)', border: 'rgba(16,185,129,0.35)' },
    red:      { bg: 'rgba(239,68,68,0.2)', border: 'rgba(239,68,68,0.35)' },
    violet:   { bg: 'rgba(139,92,246,0.2)', border: 'rgba(139,92,246,0.35)' },
    amber:    { bg: 'rgba(245,158,11,0.2)', border: 'rgba(245,158,11,0.35)' },
    sky:      { bg: 'rgba(14,165,233,0.2)', border: 'rgba(14,165,233,0.35)' },
    fuchsia:  { bg: 'rgba(217,70,239,0.2)', border: 'rgba(217,70,239,0.35)' },
    cyan:     { bg: 'rgba(6,182,212,0.2)', border: 'rgba(6,182,212,0.35)' },
    orange:   { bg: 'rgba(249,115,22,0.2)', border: 'rgba(249,115,22,0.35)' },
    indigo:   { bg: 'rgba(99,102,241,0.2)', border: 'rgba(99,102,241,0.35)' },
    gray:     { bg: 'rgba(156,163,175,0.2)', border: 'rgba(156,163,175,0.35)' },
};
function accentToBg(accent) { return accentColors[accent]?.bg || accentColors.gray.bg; }
function accentToBorder(accent) { return accentColors[accent]?.border || accentColors.gray.border; }
</script>

<template>
    <div class="min-h-screen bg-gray-950 text-white">
        <main class="max-w-7xl mx-auto px-6">
            <section class="py-12">
                <div class="flex flex-col items-center text-center gap-4 mb-10">
                    <h1 class="text-4xl font-bold tracking-tight">试玩派对</h1>
                    <p class="text-gray-400 text-lg max-w-lg">
                        聚合游戏Demo、电影预告、桌游教学等一切能让你"先试试"的内容
                    </p>
                    <div class="w-full max-w-md mt-4"><SearchBar /></div>
                </div>

                <div v-if="mainBanner" class="mb-12">
                    <div
                        class="relative min-h-[420px] rounded-2xl overflow-hidden cursor-pointer group"
                        @click="router.push(`/item/${mainBanner.slug}`)"
                    >
                        <!-- Background image -->
                        <div
                            v-if="mainBanner.wideCoverUrl || mainBanner.coverUrl"
                            class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-700"
                            :style="{ backgroundImage: `url(${mainBanner.wideCoverUrl || mainBanner.coverUrl})` }"
                        />
                        <!-- Fallback gradient -->
                        <div v-else class="absolute inset-0" :class="bannerMeta?.heroColor || 'from-gray-800 to-gray-900'" />
                        <!-- Overlay -->
                        <div class="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/40 to-transparent" />
                        <div class="absolute inset-0 bg-gradient-to-r from-gray-950/70 to-transparent" />
                        <!-- Content -->
                        <div class="relative z-10 h-full flex flex-col justify-end p-8 md:p-12">
                            <span
                                v-if="bannerMeta"
                                class="text-xs px-3 py-1 rounded-full inline-block w-fit mb-4 border text-white/90"
                                :style="{ 
                                    backgroundColor: accentToBg(bannerMeta.accent), 
                                    borderColor: accentToBorder(bannerMeta.accent) 
                                }"
                            >
                                {{ bannerMeta.emoji }} {{ bannerMeta.label }}
                            </span>
                            <h2 class="text-3xl md:text-4xl font-bold mb-3 text-white drop-shadow-lg">
                                {{ mainBanner.title }}
                            </h2>
                            <p class="text-gray-300 max-w-xl line-clamp-2 text-sm md:text-base leading-relaxed">
                                {{ mainBanner.description }}
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            <section v-if="recommended.length > 0" class="mb-16">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-semibold">🔥 最受欢迎</h2>
                    <router-link
                        to="/list"
                        class="text-blue-400 text-sm hover:underline"
                        >全部 →</router-link
                    >
                </div>
                <div
                    class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
                >
                    <AppCard
                        v-for="item in recommended"
                        :key="'rec-' + item.id"
                        :item="item"
                    />
                </div>
            </section>

            <section class="mb-16">
                <h2 class="text-xl font-semibold mb-6">分类探索</h2>
                <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
                    <div
                        v-for="cat in categories"
                        :key="cat.key"
                        @click="goList(cat.key)"
                        class="bg-gray-900 rounded-xl p-4 border border-gray-800 hover:border-gray-400 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/30 transition-all duration-200 cursor-pointer text-center group"
                    >
                        <div class="text-2xl mb-1.5 group-hover:scale-110 transition-transform duration-200">{{ cat.emoji }}</div>
                        <div class="font-semibold text-sm text-white">{{ cat.label }}</div>
                        <div class="text-gray-500 text-xs mt-1 hidden sm:block line-clamp-1">
                            {{ cat.desc }}
                        </div>
                    </div>
                </div>
            </section>

            <section v-if="hotItems.length > 0" class="mb-16">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-semibold">最新推荐</h2>
                    <router-link
                        to="/list"
                        class="text-blue-400 text-sm hover:underline"
                        >全部 →</router-link
                    >
                </div>
                <div
                    class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
                >
                    <AppCard
                        v-for="item in hotItems"
                        :key="item.id"
                        :item="item"
                    />
                </div>
            </section>

            <section v-if="hotByType.game?.length" class="mb-16">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-semibold">🎮 游戏精选</h2>
                    <router-link
                        to="/list/game"
                        class="text-blue-400 text-sm hover:underline"
                        >更多 →</router-link
                    >
                </div>
                <div
                    class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
                >
                    <AppCard
                        v-for="item in hotByType.game"
                        :key="item.id"
                        :item="item"
                    />
                </div>
            </section>

            <section v-if="hotByType.movie?.length" class="mb-16">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-semibold">🎬 电影精选</h2>
                    <router-link
                        to="/list/movie"
                        class="text-blue-400 text-sm hover:underline"
                        >更多 →</router-link
                    >
                </div>
                <div
                    class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
                >
                    <AppCard
                        v-for="item in hotByType.movie"
                        :key="item.id"
                        :item="item"
                    />
                </div>
            </section>
        </main>
    </div>
</template>
