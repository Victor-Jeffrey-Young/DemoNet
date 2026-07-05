<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import TypeIcon from '../TypeIcon.vue'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
function go() { router.push({ name: 'Detail', params: { slug: props.item.slug } }) }

const info = computed(() => {
  try { return typeof props.item.infoJson === 'string' ? JSON.parse(props.item.infoJson) : (props.item.infoJson || {}) } catch { return {} }
})
const players = computed(() => info.value.players || '')
const playtime = computed(() => info.value.playtime || '')
const weight = computed(() => parseFloat(info.value.weight) || 0)
const weightStars = computed(() => Math.round(weight.value))

const hasVideo = computed(() => {
  const v = info.value.videos
  return !!(v && (v.bilibili || v.youtube))
})

function watchVideo() {
  const v = info.value.videos
  const url = v?.bilibili || v?.youtube
  if (url) window.open(url, '_blank')
}
</script>

<template>
  <div class="game-box-card" @click="go">
    <div class="shelf-display">
      <!-- Left: 3D game box -->
      <div class="box-area">
        <div class="box-3d">
          <div class="box-shine" />
          <div
            v-if="item.wideCoverUrl || item.coverUrl"
            class="box-cover"
            :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }"
          />
          <div v-else class="box-fallback">
            <TypeIcon type="boardgame" :size="36" />
          </div>
          <!-- Weight badge -->
          <div v-if="weight" class="weight-badge">
            <span
              v-for="i in 5"
              :key="i"
              class="weight-star"
              :class="{ active: i <= weightStars }"
            >★</span>
          </div>
          <div class="box-edge" />
        </div>
        <!-- Wood shelf under box -->
        <div class="mini-shelf" />
      </div>

      <!-- Right: Info -->
      <div class="info-area">
        <div class="tag-row">
          <span class="cat-tag">🎲 TABLETOP</span>
          <span v-if="hasVideo" class="video-tag" @click.stop="watchVideo">▶ 教学</span>
        </div>

        <h3 class="title">{{ item.title }}</h3>

        <div class="specs">
          <span v-if="players" class="spec">👥 {{ players }}</span>
          <span v-if="playtime" class="spec">⏱ {{ playtime }}</span>
          <span v-if="weight" class="spec">{{ '★'.repeat(weightStars) }}{{ '☆'.repeat(5 - weightStars) }}</span>
        </div>

        <p class="desc">{{ item.description }}</p>

        <div class="action">
          <span class="link">查看游戏详情 →</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.game-box-card {
  position: relative;
  cursor: pointer;
}

.shelf-display {
  display: flex;
  gap: 18px;
  padding: 16px;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(30, 21, 8, 0.6), rgba(20, 14, 5, 0.8));
  border: 1px solid rgba(212, 160, 86, 0.15);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  height: 100%;
}

.game-box-card:hover .shelf-display {
  border-color: rgba(212, 160, 86, 0.35);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

/* ===== 3D Box ===== */
.box-area {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.box-3d {
  width: clamp(96px, 8vw, 120px);
  aspect-ratio: 3 / 4;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
  background: #1a1206;
  border: 1px solid rgba(212, 160, 86, 0.2);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.game-box-card:hover .box-3d {
  border-color: rgba(212, 160, 86, 0.5);
  box-shadow:
    0 12px 24px rgba(0, 0, 0, 0.5),
    0 0 16px rgba(212, 160, 86, 0.1);
}

.box-cover {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.game-box-card:hover .box-cover {
  transform: scale(1.05);
}

.box-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3d2b0f, #1a1206);
  color: rgba(212, 160, 86, 0.3);
}

.box-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  z-index: 2;
}

.box-edge {
  position: absolute;
  top: 0;
  right: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.5));
  z-index: 1;
}

.weight-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  display: flex;
  gap: 1px;
  z-index: 3;
}

.weight-star {
  font-size: clamp(8px, 0.7vw, 11px);
  color: rgba(60, 40, 10, 0.8);
}

.weight-star.active {
  color: #f0c060;
  text-shadow: 0 0 2px rgba(240, 192, 96, 0.5);
}

.mini-shelf {
  height: 6px;
  border-radius: 2px;
  margin-top: -1px;
  background: linear-gradient(180deg, #5c3f15 0%, #3d2b0f 50%, #2a1d0a 100%);
  box-shadow:
    inset 0 1px 0 rgba(212, 160, 86, 0.15),
    0 3px 8px rgba(0, 0, 0, 0.4);
}

/* ===== Info Area ===== */
.info-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.tag-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.cat-tag {
  font-family: 'DM Mono', monospace;
  font-size: clamp(9px, 0.75vw, 12px);
  font-weight: 700;
  color: #d4a056;
  letter-spacing: 0.1em;
  background: rgba(212, 160, 86, 0.1);
  border: 1px solid rgba(212, 160, 86, 0.15);
  padding: 2px 6px;
  border-radius: 3px;
}

.video-tag {
  font-family: 'DM Mono', monospace;
  font-size: clamp(9px, 0.75vw, 12px);
  color: #8bc34a;
  background: rgba(26, 58, 40, 0.5);
  border: 1px solid rgba(139, 195, 74, 0.2);
  padding: 2px 6px;
  border-radius: 3px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.video-tag:hover {
  background: rgba(26, 58, 40, 0.8);
}

.title {
  font-size: clamp(15px, 1.2vw, 19px);
  font-weight: 700;
  color: #e8dcc4;
  line-height: 1.3;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s ease;
}

.game-box-card:hover .title {
  color: #f0c060;
}

.specs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.spec {
  font-family: 'DM Mono', monospace;
  font-size: clamp(10px, 0.8vw, 13px);
  color: #c4b489;
  background: rgba(212, 160, 86, 0.08);
  border: 1px solid rgba(212, 160, 86, 0.12);
  padding: 2px 7px;
  border-radius: 3px;
}

.desc {
  font-size: clamp(12px, 0.95vw, 15px);
  color: rgba(196, 180, 137, 0.6);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.action {
  margin-top: 10px;
}

.link {
  font-size: clamp(11px, 0.85vw, 14px);
  color: rgba(212, 160, 86, 0.5);
  transition: color 0.2s ease;
}

.game-box-card:hover .link {
  color: #d4a056;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .shelf-display {
    gap: 12px;
    padding: 12px;
  }
  .box-3d {
    width: 72px;
  }
  .title {
    font-size: 14px;
  }
  .desc {
    -webkit-line-clamp: 1;
  }
}
</style>
