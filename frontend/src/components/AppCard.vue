<script setup>
import { useRouter } from "vue-router";

const props = defineProps({ item: { type: Object, required: true } });
const router = useRouter();

const meta = {
    game: {
        label: "游戏",
        color: "bg-green-900/50 text-green-400",
        emoji: "🎮",
    },
    movie: { label: "电影", color: "bg-red-900/50 text-red-400", emoji: "🎬" },
    anime: {
        label: "动漫",
        color: "bg-purple-900/50 text-purple-400",
        emoji: "🎭",
    },
    boardgame: {
        label: "桌游",
        color: "bg-yellow-900/50 text-yellow-400",
        emoji: "🎲",
    },
    model: {
        label: "模型",
        color: "bg-blue-900/50 text-blue-400",
        emoji: "🧩",
    },
    book: {
        label: "书籍",
        color: "bg-amber-900/50 text-amber-400",
        emoji: "📖",
    },
    music: {
        label: "音乐",
        color: "bg-pink-900/50 text-pink-400",
        emoji: "🎵",
    },
    digital: {
        label: "数码",
        color: "bg-cyan-900/50 text-cyan-400",
        emoji: "📱",
    },
    coffee: {
        label: "咖啡",
        color: "bg-orange-900/50 text-orange-400",
        emoji: "☕",
    },
    offline: {
        label: "线下",
        color: "bg-indigo-900/50 text-indigo-400",
        emoji: "🏛️",
    },
};

function goDetail() {
    if (props.item?.slug)
        router.push({ name: "Detail", params: { slug: props.item.slug } });
}
</script>

<template>
    <div
        @click.stop="goDetail"
        class="bg-gray-900 rounded-xl overflow-hidden border border-gray-800 hover:border-gray-600 transition cursor-pointer group"
    >
        <div
            class="aspect-video bg-linear-to-br from-gray-800 to-gray-900 flex items-center justify-center text-4xl relative overflow-hidden"
        >
            <div
                v-if="item.wideCoverUrl || item.coverUrl"
                class="absolute inset-0 bg-cover bg-center group-hover:scale-105 transition-transform duration-700"
                :style="{
                    backgroundImage:
                        'url(' + (item.wideCoverUrl || item.coverUrl) + ')',
                }"
            />
            <div v-else class="absolute inset-0" />
            <div
                class="absolute inset-0 bg-black/20 group-hover:opacity-0 transition-opacity pointer-events-none"
            />
            <span
                v-if="!item.wideCoverUrl && !item.coverUrl"
                class="relative z-10"
                >{{ (meta[item.type] || {}).emoji || "📦" }}</span
            >
        </div>
        <div class="p-4">
            <span
                :class="
                    (meta[item.type] || {}).color || 'bg-gray-800 text-gray-400'
                "
                class="text-xs px-2 py-0.5 rounded"
            >
                {{ (meta[item.type] || {}).label || item.type }}
            </span>
            <h3
                class="mt-2 font-semibold text-lg group-hover:text-blue-400 transition-colors"
            >
                {{ item.title }}
            </h3>
            <p class="text-gray-400 text-sm mt-1 line-clamp-2">
                {{ item.description }}
            </p>
        </div>
    </div>
</template>
