<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { getFeatured, getHotItems } from "../../api/item";
import { getMeta } from "../../constants/types";

// ── Geometry ──
const STEP = 20; // degrees between adjacent covers
const RADIUS = 650; // arc radius (px)
const VIS_RANGE = 5; // visible offset range

const router = useRouter();
const meta = getMeta("music");
const albums = ref([]);
const activeIdx = ref(0);
const N = computed(() => albums.value.length);

const viewportRef = ref(null);
const itemsRef = ref([]);
let prevDiffs = [];
let isFirstRender = true;

onMounted(async () => {
    try {
        const f = (await getFeatured({ type: "music" })) || [];
        albums.value = f.length
            ? f
            : (await getHotItems({ type: "music", limit: 8 })) || [];
    } catch {}
    await nextTick();
    if (N.value > 0) {
        activeIdx.value = Math.floor(N.value / 2);
        prevDiffs = new Array(N.value).fill(0);
        renderAll(false);
        isFirstRender = false;
        if (N.value > 1) startAutoPlay();
    }

    // 手动挂载非 passive 的滚轮监听
    if (viewportRef.value) {
        viewportRef.value.addEventListener("wheel", onWheel, {
            passive: false,
        });
    }
    // 挂载可见性监听
    document.addEventListener("visibilitychange", onVisibilityChange);
});

let autoTimer = null;
onUnmounted(() => {
    clearInterval(autoTimer);
    if (viewportRef.value) {
        viewportRef.value.removeEventListener("wheel", onWheel);
    }
    document.removeEventListener("visibilitychange", onVisibilityChange);
});

function startAutoPlay() {
    clearInterval(autoTimer);
    autoTimer = setInterval(
        () => navigateTo((activeIdx.value + 1) % N.value),
        4000,
    );
}
function resetAutoPlay() {
    if (N.value <= 1) return;
    clearInterval(autoTimer);
    autoTimer = setInterval(
        () => navigateTo((activeIdx.value + 1) % N.value),
        4000,
    );
}

// ── Arc wheel transform ──
function wheelTransform(offset) {
    const angle = (offset * STEP * Math.PI) / 180;
    const x = RADIUS * Math.sin(angle);
    const z = RADIUS * (Math.cos(angle) - 1);
    const ry = -offset * STEP;
    return `translate3d(${x.toFixed(1)}px, 0, ${z.toFixed(1)}px) rotateY(${ry.toFixed(1)}deg)`;
}

// ── Shortest-path diff (circular) ──
function shortestDiff(i) {
    let d = i - activeIdx.value;
    if (d > N.value / 2) d -= N.value;
    if (d < -N.value / 2) d += N.value;
    return d;
}

// ── Drag state ──
let startX = 0;
let dragOffsetPx = 0;
let dragging = false;
let hasMoved = false; // 状态锁：用于阻止拖拽释放时触发意外跳转

function gapPx() {
    return RADIUS * Math.sin((STEP * Math.PI) / 180);
}

// ── Render all ──
function renderAll(animate) {
    const items = itemsRef.value;
    if (!items || !items.length) return;

    items.forEach((el, i) => {
        if (!el) return;
        const diff = shortestDiff(i);
        const offset = diff + dragOffsetPx / gapPx();
        const absOff = Math.abs(offset);

        // 检测是否需要瞬移
        const didWrap =
            !isFirstRender && Math.abs(prevDiffs[i] - diff) > N.value / 2;

        if (animate) {
            el.classList.add("cf-anim");
        } else {
            el.classList.remove("cf-anim");
        }

        if (absOff > VIS_RANGE) {
            el.style.opacity = "0";
            el.style.pointerEvents = "none";
        } else {
            el.style.opacity = Math.max(
                0,
                1 - Math.pow(absOff / VIS_RANGE, 2.2),
            );
            el.style.pointerEvents =
                absOff <= VIS_RANGE * 0.7 ? "auto" : "none";
        }

        if (didWrap) {
            // 1. 瞬间关闭过渡动画
            el.style.transition = "none";
            // 2. 立即应用瞬移后的 3D 坐标（不使用 offsetHeight，完全不阻塞主线程）
            el.style.transform = wheelTransform(offset);
            el.style.zIndex = Math.floor(100 - absOff * 20);

            // 3. 异步恢复过渡动画，避开当前渲染帧
            setTimeout(() => {
                el.style.transition = "";
            }, 30);
        } else {
            // 正常卡片的 3D 变换
            el.style.transform = wheelTransform(offset);
            el.style.zIndex = Math.floor(100 - absOff * 20);
        }

        prevDiffs[i] = diff;
    });
}

// ── Navigate ──
function navigateTo(targetIdx) {
    let delta = targetIdx - activeIdx.value;
    if (delta > N.value / 2) delta -= N.value;
    if (delta < -N.value / 2) delta += N.value;
    activeIdx.value =
        (((activeIdx.value + delta) % N.value) + N.value) % N.value;
    renderAll(true);
}

function prev() {
    navigateTo((activeIdx.value - 1 + N.value) % N.value);
}
function next() {
    navigateTo((activeIdx.value + 1) % N.value);
}

// ── Mouse drag ──
function onMouseDown(e) {
    if (N.value <= 1) return;
    dragging = true;
    startX = e.clientX;
    dragOffsetPx = 0;
    hasMoved = false;
    document.addEventListener("mousemove", onMouseMove);
    document.addEventListener("mouseup", onMouseUp);
}

function onMouseMove(e) {
    if (!dragging) return;

    // 1. 引入 0.8 的阻尼系数（Friction）
    // 增加滑动阻力，让手势感觉更有厚重质感、更沉稳
    const rawOffset = (startX - e.clientX) * 0.8;

    // 2. 核心限幅（Clamp）：限制最大拖拽位移为 1 张卡片的物理间距
    // 这样无论鼠标在屏幕上拖得多远，画面在视觉上最多也只会滑动一格，完美实现“拽一下，只动一张”
    const maxDrag = gapPx();
    dragOffsetPx = Math.max(-maxDrag, Math.min(maxDrag, rawOffset));

    if (Math.abs(dragOffsetPx) > 5) {
        hasMoved = true;
    }
    renderAll(false); // 实时重绘（无动画延迟，卡片紧贴指尖）
}

function onMouseUp() {
    if (!dragging) return;
    dragging = false;
    document.removeEventListener("mousemove", onMouseMove);
    document.removeEventListener("mouseup", onMouseUp);

    const g = gapPx();
    if (Math.abs(dragOffsetPx) > g * 0.3) {
        const delta = dragOffsetPx > 0 ? 1 : -1;
        dragOffsetPx = 0;
        navigateTo((activeIdx.value + delta + N.value) % N.value);
    } else {
        dragOffsetPx = 0;
        renderAll(true);
    }
    // 延迟重置滑动状态，确保 onClickCover 执行时能正确拦截
    setTimeout(() => {
        hasMoved = false;
    }, 50);
    resetAutoPlay();
}

// ── Scroll Wheel ──
let lastWheelTime = 0;
function onWheel(e) {
    e.preventDefault(); // 阻止默认的页面滚动行为
    const now = Date.now();
    if (now - lastWheelTime < 250) return; // 节流

    if (e.deltaX > 20 || e.deltaY > 20) {
        next();
        lastWheelTime = now;
    } else if (e.deltaX < -20 || e.deltaY < -20) {
        prev();
        lastWheelTime = now;
    }
    resetAutoPlay();
}

// ── Click side covers ──
function onClickCover(e, i) {
    if (hasMoved) return; // 如果是滑动释放，则在此处拦截，不触发跳转
    const diff = shortestDiff(i);
    const abs = Math.abs(diff);
    if (abs === 0) {
        goDetail(albums.value[i].slug);
    } else if (abs <= 2) {
        navigateTo(i);
        resetAutoPlay();
    }
}

// ── GPU layer restore ──
function onVisibilityChange() {
    if (document.visibilityState !== "visible" || !viewportRef.value) return;
    const scene = viewportRef.value.querySelector(".cf-scene");
    if (!scene) return;
    scene.style.display = "none";
    scene.offsetHeight; // force reflow
    scene.style.display = "";
    renderAll(false);
}

// ── Helpers ──
function goDetail(slug) {
    router.push({ name: "Detail", params: { slug } });
}
function parseInfo(item) {
    try {
        return JSON.parse(item.infoJson || "{}");
    } catch {
        return {};
    }
}
const currentAlbum = computed(() => albums.value[activeIdx.value] || {});
</script>

<template>
    <div v-if="N > 0" class="cf-root">
        <div ref="viewportRef" class="cf-viewport" @mousedown="onMouseDown">
            <div class="cf-scene">
                <div
                    v-for="(album, i) in albums"
                    :key="album.id || i"
                    :ref="
                        (el) => {
                            if (el) itemsRef[i] = el;
                        }
                    "
                    class="cf-item"
                    @click="onClickCover($event, i)"
                >
                    <div class="cf-cover">
                        <img
                            v-if="album.wideCoverUrl || album.coverUrl"
                            :src="album.wideCoverUrl || album.coverUrl"
                            class="cf-img"
                        />
                        <div
                            v-else
                            class="cf-img cf-fallback flex items-center justify-center text-4xl text-white/20"
                        >
                            🎵
                        </div>
                        <div class="cf-shine" />
                    </div>
                    <div class="cf-reflect">
                        <img
                            v-if="album.wideCoverUrl || album.coverUrl"
                            :src="album.wideCoverUrl || album.coverUrl"
                            class="cf-reflect-img"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- Controls + info overlaid at bottom -->
        <div class="cf-bottom">
            <div class="cf-now-playing">
                <div class="cf-now-title">{{ currentAlbum.title || "" }}</div>
                <div class="cf-now-artist">
                    {{ parseInfo(currentAlbum).artist || "" }}
                </div>
            </div>
            <div class="cf-controls">
                <button
                    @click="
                        prev();
                        resetAutoPlay();
                    "
                    class="cf-btn"
                >
                    <svg
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M15 19l-7-7 7-7"
                        />
                    </svg>
                </button>
                <div class="cf-dots">
                    <button
                        v-for="(_, i) in albums"
                        :key="i"
                        @click="
                            navigateTo(i);
                            resetAutoPlay();
                        "
                        :class="i === activeIdx ? 'cf-dot-active' : 'cf-dot'"
                    />
                </div>
                <button
                    @click="
                        next();
                        resetAutoPlay();
                    "
                    class="cf-btn"
                >
                    <svg
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M9 5l7 7-7 7"
                        />
                    </svg>
                </button>
            </div>
        </div>
    </div>
    <div v-else class="cf-root flex items-center justify-center">
        <span class="text-gray-600 text-sm">管理员尚未配置作品</span>
    </div>
</template>

<style scoped>
.cf-root {
    position: relative;
    overflow: hidden;
    width: 100%;
    height: calc(100vh - 4rem);
    background: linear-gradient(180deg, #0a0a18 0%, #050510 100%);
    display: flex;
    flex-direction: column;
    padding-top: 20px;
}
.cf-viewport {
    flex: 1;
    position: relative;
    width: 100%;
    overflow: hidden;
    touch-action: pan-y;
    user-select: none;
}
.cf-scene {
    position: absolute;
    inset: 0;
    perspective: 900px;
    perspective-origin: 50% 45%;
    transform-style: preserve-3d;
}

.cf-item {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 360px;
    height: 360px;
    margin-left: -180px;
    margin-top: -200px;
    transform-origin: 50% 50%;
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    will-change: transform, opacity;
    cursor: pointer;
}
.cf-item.cf-anim {
    /* 缩短至 0.45s 级别，使用更符合物理规律的贝塞尔曲线 */
    transition:
        transform 0.45s cubic-bezier(0.25, 0.8, 0.25, 1),
        opacity 0.45s ease;
}

.cf-cover {
    position: absolute;
    inset: 0;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 16px 60px rgba(0, 0, 0, 0.6);
}
.cf-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 10px;
}
.cf-fallback {
    background: linear-gradient(140deg, #3b0764, #9d174d);
}
.cf-shine {
    position: absolute;
    inset: 0;
    pointer-events: none;
    border-radius: 10px;
    background: linear-gradient(
        135deg,
        rgba(255, 255, 255, 0.12) 0%,
        rgba(255, 255, 255, 0.02) 35%,
        transparent 55%,
        rgba(0, 0, 0, 0.1) 100%
    );
}

.cf-reflect {
    position: absolute;
    left: 0;
    /* 1. 还原旧版 below 15px 的间距设计 */
    top: calc(100% + 15px);
    width: 100%;
    height: 120px;
    transform: scaleY(-1);
    opacity: 0.35;
    pointer-events: none;
    border-radius: 0 0 10px 10px;
    overflow: hidden;
    -webkit-mask-image: linear-gradient(
        to top,
        #000 0%,
        rgba(0, 0, 0, 0.3) 70%,
        transparent 100%
    );
    mask-image: linear-gradient(
        to top,
        #000 0%,
        rgba(0, 0, 0, 0.3) 70%,
        transparent 100%
    );
}

.cf-reflect-img {
    width: 100%;
    height: 360px;
    object-fit: cover;
    position: absolute;
    /* 2. 关键：将 top: 0 改为 bottom: 0 */
    /* 这能确保容器截取并倒置的是专辑最底部的画面，从而使倒影内容完全物理对称 */
    bottom: 0;
}

.cf-bottom {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px 0 24px;
}
.cf-now-playing {
    position: absolute;
    left: 24px;
    text-align: left;
    max-width: 280px;
}
.cf-now-title {
    color: rgba(255, 255, 255, 0.9);
    font-size: 16px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.cf-now-artist {
    color: rgba(255, 255, 255, 0.45);
    font-size: 13px;
    margin-top: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.cf-controls {
    display: flex;
    align-items: center;
    gap: 18px;
}
.cf-btn {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.15);
    background: rgba(255, 255, 255, 0.04);
    color: rgba(255, 255, 255, 0.65);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition:
        background 0.2s,
        border-color 0.2s,
        transform 0.15s;
}
.cf-btn:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.35);
    transform: scale(1.06);
}
.cf-btn:active {
    transform: scale(0.94);
}
.cf-dots {
    display: flex;
    gap: 6px;
}
.cf-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    transition: all 0.3s;
    cursor: pointer;
    border: none;
}
.cf-dot:hover {
    background: rgba(255, 255, 255, 0.4);
}
.cf-dot-active {
    width: 22px;
    height: 6px;
    border-radius: 6px;
    background: #c084fc;
    transition: all 0.3s;
    cursor: pointer;
    border: none;
}
</style>
