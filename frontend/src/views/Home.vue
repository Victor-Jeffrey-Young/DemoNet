<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getHotItems, getRecommended } from "../api/item";
import AppCard from "../components/AppCard.vue";
import SearchBar from "../components/SearchBar.vue";

const router = useRouter();
const hotItems = ref([]);
const recommended = ref([]);
const hotByType = ref({});

const categories = [
    { key: "game", label: "游戏", emoji: "🎮", desc: "试玩版 & 浏览器可玩" },
    { key: "movie", label: "电影", emoji: "🎬", desc: "预告片 & 导演访谈" },
    { key: "anime", label: "动漫", emoji: "🎭", desc: "先导PV & 前5分钟试看" },
    {
        key: "boardgame",
        label: "桌游",
        emoji: "🎲",
        desc: "规则教学 & 实况回放",
    },
    { key: "model", label: "模型", emoji: "🧩", desc: "开箱 & 360°展示" },
    { key: "book", label: "书籍", emoji: "📖", desc: "试读 & 书评" },
    { key: "music", label: "音乐", emoji: "🎵", desc: "30s试听 & 测评" },
    { key: "digital", label: "数码", emoji: "📱", desc: "开箱 & 评测" },
    { key: "coffee", label: "咖啡", emoji: "☕", desc: "风味 & 冲泡指南" },
    { key: "offline", label: "线下", emoji: "🏛️", desc: "展览 & 活动预览" },
];

const mainBanner = ref(null);

onMounted(async () => {
    try {
        const all = await getHotItems({ limit: 8 });
        hotItems.value = all || [];
        if (all && all.length > 0) mainBanner.value = all[0];
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
                        class="relative h-72 rounded-2xl overflow-hidden bg-linear-to-br from-gray-800 to-gray-900 flex items-center justify-center cursor-pointer"
                        @click="router.push(`/item/${mainBanner.slug}`)"
                    >
                        <div class="absolute inset-0 bg-black/30" />
                        <div class="relative z-10 text-center p-8">
                            <span
                                class="text-xs px-2 py-0.5 rounded bg-white/10 text-white/80 mb-3 inline-block"
                                >{{ mainBanner.type }}</span
                            >
                            <h2 class="text-3xl font-bold mb-2">
                                {{ mainBanner.title }}
                            </h2>
                            <p
                                class="text-gray-300 max-w-md mx-auto line-clamp-2"
                            >
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
                <div
                    class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4"
                >
                    <div
                        v-for="cat in categories"
                        :key="cat.key"
                        @click="goList(cat.key)"
                        class="bg-gray-900 rounded-xl p-5 border border-gray-800 hover:border-gray-500 hover:bg-gray-800 transition cursor-pointer text-center"
                    >
                        <div class="text-3xl mb-2">{{ cat.emoji }}</div>
                        <div class="font-semibold text-sm">{{ cat.label }}</div>
                        <div class="text-gray-500 text-xs mt-1 hidden sm:block">
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
