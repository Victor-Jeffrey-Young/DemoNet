<script setup>
import { computed } from "vue";
import {
    Trophy,
    Film,
    MagicStick,
    Goblet,
    Brush,
    Reading,
    Headset,
    Iphone,
    CoffeeCup,
    Compass,
} from "@element-plus/icons-vue";

const iconMap = {
    game: Trophy,
    movie: Film,
    anime: MagicStick,
    boardgame: Goblet,
    model: Brush,
    book: Reading,
    music: Headset,
    digital: Iphone,
    coffee: CoffeeCup,
    offline: Compass,
};

const props = defineProps({
    type: { type: String, required: true },
    size: { type: [String, Number], default: undefined },
});

const iconComponent = computed(() => iconMap[props.type]);
</script>

<template>
    <el-icon v-if="iconComponent" :size="size">
        <component :is="iconComponent" />
    </el-icon>
    <span v-else class="text-xs text-gray-500">?</span>
</template>
