<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { TYPE_META } from "../constants/types.js";

const props = defineProps({ item: { type: Object, required: true } });
const router = useRouter();

// Safe class maps for Tailwind JIT
const badgeMap = {
    emerald: 'bg-emerald-900/50 text-emerald-400',
    red:     'bg-red-900/50 text-red-400',
    violet:  'bg-purple-900/50 text-purple-400',
    amber:   'bg-amber-900/50 text-amber-400',
    sky:     'bg-blue-900/50 text-blue-400',
    fuchsia: 'bg-pink-900/50 text-pink-400',
    cyan:    'bg-cyan-900/50 text-cyan-400',
    orange:  'bg-orange-900/50 text-orange-400',
    indigo:  'bg-indigo-900/50 text-indigo-400',
};
const titleHoverMap = {
    emerald: 'group-hover:text-emerald-400',
    red:     'group-hover:text-red-400',
    violet:  'group-hover:text-violet-400',
    amber:   'group-hover:text-amber-400',
    sky:     'group-hover:text-blue-400',
    fuchsia: 'group-hover:text-fuchsia-400',
    cyan:    'group-hover:text-cyan-400',
    orange:  'group-hover:text-orange-400',
    indigo:  'group-hover:text-indigo-400',
};
const shadowMap = {
    emerald: 'hover:shadow-emerald-900/20',
    red:     'hover:shadow-red-900/20',
    violet:  'hover:shadow-violet-900/20',
    amber:   'hover:shadow-amber-900/20',
    sky:     'hover:shadow-blue-900/20',
    fuchsia: 'hover:shadow-fuchsia-900/20',
    cyan:    'hover:shadow-cyan-900/20',
    orange:  'hover:shadow-orange-900/20',
    indigo:  'hover:shadow-indigo-900/20',
};

const meta = computed(() => {
    const t = props.item.type;
    const m = TYPE_META[t];
    const accent = m?.accent || 'gray';
    return {
        label:  m?.label || t,
        emoji:  m?.emoji || '📦',
        badge:  badgeMap[accent] || 'bg-gray-800 text-gray-400',
        titleHover: titleHoverMap[accent] || 'group-hover:text-gray-400',
        shadow: shadowMap[accent] || '',
    };
});

function goDetail() {
    if (props.item?.slug)
        router.push({ name: "Detail", params: { slug: props.item.slug } });
}
</script>

<template>
    <div
        @click.stop="goDetail"
        class="bg-gray-900 rounded-xl overflow-hidden border border-gray-800 hover:border-gray-500 hover:-translate-y-1 hover:shadow-xl transition-all duration-300 cursor-pointer group"
        :class="meta.shadow"
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
                >{{ meta.emoji }}</span
            >
        </div>
        <div class="p-4">
            <span class="text-xs px-2 py-0.5 rounded" :class="meta.badge">
                {{ meta.label }}
            </span>
            <h3
                class="mt-2 font-semibold text-lg transition-colors truncate"
                :class="meta.titleHover"
            >
                {{ item.title }}
            </h3>
            <p class="text-gray-400 text-sm mt-1 line-clamp-2">
                {{ item.description }}
            </p>
        </div>
    </div>
</template>
