<script setup>
import { computed } from "vue";
import { Icon } from "@iconify/vue";

const iconMap = {
    game:      "game-icons:gamepad",
    movie:     "mdi:filmstrip-box-multiple",
    anime:     "mdi:animation-play",
    boardgame: "game-icons:dice-six-faces-four",
    model:     "mdi:puzzle",
    book:      "mdi:book-open-page-variant",
    music:     "mdi:music",
    digital:   "mdi:cellphone",
    coffee:    "mdi:coffee",
    offline:   "mdi:map-marker",
};

const props = defineProps({
    type: { type: String, required: true },
    size: { type: [String, Number], default: 20 },
});

const iconName = computed(() => iconMap[props.type] || "");
const iconSize = computed(() => {
    const s = typeof props.size === 'number' ? props.size : parseInt(props.size);
    return isNaN(s) ? 20 : s;
});
</script>

<template>
    <Icon v-if="iconName" :icon="iconName" :width="iconSize" :height="iconSize" color="#fff" class="inline-block align-middle" />
    <span v-else class="text-gray-500" :style="{ fontSize: iconSize + 'px' }">?</span>
</template>
