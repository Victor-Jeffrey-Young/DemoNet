<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFeatured, getHotItems } from '../../api/item'
import TypeIcon from '../TypeIcon.vue'

const router = useRouter()
const items = ref([])
const hotItems = ref([])

onMounted(async () => {
  try {
    const featured = await getFeatured({ type: 'boardgame' }) || []
    items.value = featured.length ? featured.slice(0, 8) : (await getHotItems({ type: 'boardgame', limit: 8 }) || [])
    // 试玩台：取有视频教学的热门桌游
    const hot = await getHotItems({ type: 'boardgame', limit: 12 }) || []
    hotItems.value = hot.filter(i => {
      try {
        const v = typeof i.infoJson === 'string' ? JSON.parse(i.infoJson) : (i.infoJson || {})
        return v.videos && (v.videos.youtube || v.videos.bilibili)
      } catch { return false }
    }).slice(0, 3)
  } catch {}
})

function goDetail(slug) { router.push({ name: 'Detail', params: { slug } }) }

function parseInfo(item) {
  try {
    return typeof item.infoJson === 'string' ? JSON.parse(item.infoJson) : (item.infoJson || {})
  } catch { return {} }
}

function weightStars(w) {
  return Math.round(parseFloat(w) || 0)
}
</script>

<template>
  <div v-if="items.length" class="storefront">
    <!-- ===== Storefront Header ===== -->
    <header class="storefront-header">
      <!-- Ambient lamp glows -->
      <div class="lamp-glow lamp-left" />
      <div class="lamp-glow lamp-right" />

      <div class="header-row">
        <div class="sign-board">
          <span class="sign-icon"><TypeIcon type="boardgame" :size="28" /></span>
          <div class="sign-text">
            <h1>桌游驿站</h1>
            <p>Board Game Tavern</p>
          </div>
        </div>
        <div class="open-sign">
          <span class="dot" />
          营业中 · OPEN
        </div>
      </div>

      <p class="header-subtitle">
        推开门，暖黄灯光洒在原木桌面上。货架上的盒装游戏整齐排列，绒布桌面正等着下一局开始——挑一款，坐下来试试？
      </p>
    </header>

    <!-- ===== Staff Picks Shelf ===== -->
    <section class="featured-section">
      <div class="section-label">
        <h2>店员推荐</h2>
        <span class="num">No.01</span>
        <div class="line" />
        <span class="num">Staff Picks</span>
      </div>

      <div class="display-shelf">
        <!-- Box row: mobile = horizontal scroll, desktop = grid -->
        <div class="box-row">
          <div
            v-for="(item, idx) in items.slice(0, 4)"
            :key="item.id"
            class="game-box"
            @click="goDetail(item.slug)"
          >
            <div class="box-3d">
              <div class="box-shine" />
              <div v-if="idx === 0" class="price-tag">HOT</div>
              <div v-else-if="idx === 2" class="price-tag">NEW</div>
              <div
                v-if="item.wideCoverUrl || item.coverUrl"
                class="box-cover-art"
                :style="{ backgroundImage: `url(${item.wideCoverUrl || item.coverUrl})` }"
              />
              <div v-else class="box-cover-fallback">
                <TypeIcon type="boardgame" :size="40" />
              </div>
              <div class="box-edge" />
            </div>
            <div class="box-info">
              <div class="title">{{ item.title }}</div>
              <div class="specs">
                <span v-if="parseInfo(item).players" class="spec-chip">👥 {{ parseInfo(item).players }}</span>
                <span v-if="parseInfo(item).playtime" class="spec-chip">⏱ {{ parseInfo(item).playtime }}</span>
                <span v-if="parseInfo(item).weight" class="spec-chip">
                  {{ '★'.repeat(weightStars(parseInfo(item).weight)) }}{{ '☆'.repeat(5 - weightStars(parseInfo(item).weight)) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <!-- Wood shelf board -->
        <div class="shelf-board" />
      </div>
    </section>

    <!-- ===== Demo Table ===== -->
    <section v-if="hotItems.length" class="demo-table-section">
      <div class="table-wrapper">
        <div class="felt-surface">
          <div class="table-header">
            <h3>🪑 试玩台 · 现在可开</h3>
            <span class="badge">{{ hotItems.length }} 款待开局</span>
          </div>
          <div class="demo-grid">
            <div
              v-for="item in hotItems"
              :key="item.id"
              class="demo-card"
              @click="goDetail(item.slug)"
            >
              <div class="mini-box">
                <div
                  v-if="item.coverUrl"
                  class="mini-cover"
                  :style="{ backgroundImage: `url(${item.coverUrl})` }"
                />
                <TypeIcon v-else type="boardgame" :size="24" />
              </div>
              <div class="demo-info">
                <h4>{{ item.title }}</h4>
                <p class="desc">{{ item.description }}</p>
                <div class="stats">
                  <span v-if="parseInfo(item).players">👥 {{ parseInfo(item).players }}</span>
                  <span v-if="parseInfo(item).playtime">⏱ {{ parseInfo(item).playtime }}</span>
                  <span>▶ 有教学</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== Atmosphere Bar ===== -->
    <div class="atmosphere-bar">
      <div class="atmo-card">
        <div class="ico">🕐</div>
        <h5>随时开局</h5>
        <p>7×24 线上试玩<br>规则教学视频随时看</p>
      </div>
      <div class="atmo-card">
        <div class="ico">📋</div>
        <h5>规则书库</h5>
        <p>每款游戏附带规则图解<br>不懂随时翻阅</p>
      </div>
      <div class="atmo-card">
        <div class="ico">🎯</div>
        <h5>难度分级</h5>
        <p>★ 到 ★★★★★ 标注<br>按水平挑选合适的局</p>
      </div>
    </div>
  </div>
  <div v-else />
</template>

<style scoped>
/* ===== Design Tokens ===== */
.storefront {
  --wood-dark: #2a1d0a;
  --wood-mid: #3d2b0f;
  --wood-light: #5c3f15;
  --amber-glow: #d4a056;
  --amber-bright: #f0c060;
  --paper: #e8dcc4;
  --paper-dark: #c4b489;
  --ink: #1a0f04;
  --shelf-shadow: rgba(0, 0, 0, 0.55);
  --brass: #b8860b;
  overflow-x: hidden;
}

/* ===== Wood Texture ===== */
.storefront {
  background-image:
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 2px,
      rgba(139, 105, 20, 0.04) 2px,
      rgba(139, 105, 20, 0.04) 3px
    ),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 80px,
      rgba(139, 105, 20, 0.06) 80px,
      rgba(139, 105, 20, 0.06) 82px
    ),
    linear-gradient(180deg, #1a1206 0%, #0f0b05 100%);
}

/* ===== Storefront Header ===== */
.storefront-header {
  position: relative;
  padding: 48px 6% 0;
  overflow: hidden;
}

.storefront-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      ellipse 60% 40% at 50% 0%,
      rgba(212, 160, 86, 0.12),
      transparent
    ),
    radial-gradient(
      ellipse 40% 30% at 80% 20%,
      rgba(184, 134, 11, 0.08),
      transparent
    );
  pointer-events: none;
}

.header-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.sign-board {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  padding: 12px 28px 12px 20px;
  background: linear-gradient(135deg, #3d2b0f, #2a1d0a);
  border: 2px solid var(--brass);
  border-radius: 4px;
  box-shadow:
    0 2px 0 #1a0f04,
    0 8px 24px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(212, 160, 86, 0.3);
  position: relative;
}

.sign-board::before,
.sign-board::after {
  content: '';
  position: absolute;
  top: 6px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brass);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
.sign-board::before {
  left: 8px;
}
.sign-board::after {
  right: 8px;
}

.sign-text h1 {
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  font-size: clamp(22px, 3vw, 32px);
  font-weight: 800;
  color: var(--amber-bright);
  letter-spacing: 0.05em;
  line-height: 1;
}

.sign-text p {
  font-size: clamp(11px, 0.8vw, 14px);
  color: var(--paper-dark);
  letter-spacing: 0.3em;
  text-transform: uppercase;
  margin-top: 4px;
  font-family: 'DM Mono', monospace;
}

.open-sign {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(26, 58, 40, 0.6);
  border: 1px solid rgba(76, 175, 80, 0.4);
  border-radius: 999px;
  font-size: clamp(12px, 0.9vw, 15px);
  color: #8bc34a;
  font-family: 'DM Mono', monospace;
}

.open-sign .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8bc34a;
  box-shadow: 0 0 8px #8bc34a;
  animation: bg-pulse 2s ease-in-out infinite;
}

@keyframes bg-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.header-subtitle {
  margin-top: 24px;
  max-width: 600px;
  font-size: clamp(14px, 1.1vw, 18px);
  color: var(--paper-dark);
  line-height: 1.7;
  position: relative;
  z-index: 1;
}

/* ===== Ambient lamp glow ===== */
.lamp-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(212, 160, 86, 0.06),
    transparent 70%
  );
  pointer-events: none;
  filter: blur(20px);
  z-index: 0;
}

.lamp-left { top: -100px; left: 10%; }
.lamp-right { top: -50px; right: 15%; }

/* ===== Featured Section ===== */
.featured-section {
  padding: 40px 6%;
  position: relative;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.section-label .line {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(212, 160, 86, 0.4),
    transparent
  );
}

.section-label h2 {
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  font-size: clamp(18px, 1.5vw, 24px);
  font-weight: 600;
  color: var(--amber-glow);
  letter-spacing: 0.15em;
  white-space: nowrap;
}

.section-label .num {
  font-family: 'DM Mono', monospace;
  font-size: clamp(11px, 0.85vw, 14px);
  color: var(--paper-dark);
  opacity: 0.5;
}

/* ===== Display Shelf ===== */
.display-shelf {
  position: relative;
  padding: 0 0 8px;
}

.shelf-board {
  height: 14px;
  border-radius: 2px;
  margin-top: 12px;
  position: relative;
  z-index: 2;
  background:
    repeating-linear-gradient(
      90deg,
      rgba(0, 0, 0, 0.15) 0px,
      transparent 1px,
      transparent 120px,
      rgba(0, 0, 0, 0.2) 120px,
      transparent 121px
    ),
    linear-gradient(
      180deg,
      var(--wood-light) 0%,
      var(--wood-mid) 40%,
      var(--wood-dark) 100%
    );
  box-shadow:
    inset 0 1px 0 rgba(212, 160, 86, 0.2),
    inset 0 -2px 4px rgba(0, 0, 0, 0.4),
    0 4px 12px var(--shelf-shadow);
}

.box-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  align-items: end;
}

/* ===== Game Box ===== */
.game-box {
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.game-box:hover {
  transform: translateY(-8px);
}

.box-3d {
  aspect-ratio: 3 / 4;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
  background: #1a1206;
  border: 1px solid rgba(212, 160, 86, 0.2);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition:
    box-shadow 0.3s ease,
    border-color 0.3s ease;
}

.game-box:hover .box-3d {
  border-color: var(--amber-glow);
  box-shadow:
    0 12px 32px rgba(0, 0, 0, 0.6),
    0 0 24px rgba(212, 160, 86, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.box-cover-art {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.game-box:hover .box-cover-art {
  transform: scale(1.06);
}

.box-cover-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3d2b0f, #1a1206);
  color: rgba(212, 160, 86, 0.3);
}

.box-edge {
  position: absolute;
  top: 0;
  right: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(0, 0, 0, 0),
    rgba(0, 0, 0, 0.5)
  );
  z-index: 1;
}

.box-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.15),
    transparent
  );
  z-index: 2;
}

.price-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: var(--paper);
  color: var(--ink);
  font-family: 'DM Mono', monospace;
  font-size: clamp(10px, 0.8vw, 13px);
  font-weight: 700;
  padding: 3px 8px 4px;
  clip-path: polygon(0 0, 100% 0, 100% 65%, 82% 100%, 0 100%);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  z-index: 3;
}

.box-info {
  margin-top: 14px;
}

.box-info .title {
  font-size: clamp(13px, 1.05vw, 17px);
  font-weight: 700;
  color: var(--paper);
  line-height: 1.3;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.box-info .specs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.spec-chip {
  font-family: 'DM Mono', monospace;
  font-size: clamp(10px, 0.8vw, 13px);
  color: var(--paper-dark);
  background: rgba(212, 160, 86, 0.1);
  border: 1px solid rgba(212, 160, 86, 0.15);
  padding: 2px 7px;
  border-radius: 3px;
}

/* ===== Demo Table ===== */
.demo-table-section {
  margin: 60px 6% 0;
  position: relative;
}

.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(212, 160, 86, 0.15);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.felt-surface {
  background:
    radial-gradient(
      ellipse at 50% 50%,
      rgba(26, 58, 40, 0.6),
      rgba(10, 25, 15, 0.9)
    ),
    repeating-conic-gradient(
      from 0deg at 50% 50%,
      rgba(255, 255, 255, 0.01) 0deg 2deg,
      transparent 2deg 4deg
    );
  padding: 32px 6% 40px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

.table-header h3 {
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  font-size: clamp(18px, 2.5vw, 24px);
  color: var(--amber-bright);
  letter-spacing: 0.08em;
}

.table-header .badge {
  font-family: 'DM Mono', monospace;
  font-size: clamp(11px, 0.85vw, 14px);
  color: #8bc34a;
  padding: 4px 12px;
  border: 1px solid rgba(139, 195, 74, 0.3);
  border-radius: 999px;
  background: rgba(26, 58, 40, 0.5);
  white-space: nowrap;
}

.demo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.demo-card {
  background: linear-gradient(
    135deg,
    rgba(45, 31, 10, 0.8),
    rgba(26, 15, 4, 0.9)
  );
  border: 1px solid rgba(212, 160, 86, 0.2);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 14px;
  cursor: pointer;
  transition:
    border-color 0.3s ease,
    transform 0.3s ease;
}

.demo-card:hover {
  border-color: var(--amber-glow);
  transform: translateY(-3px);
}

.mini-box {
  width: clamp(64px, 5.5vw, 80px);
  height: clamp(84px, 7vw, 106px);
  border-radius: 3px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #3d2b0f, #1a1206);
  border: 1px solid rgba(212, 160, 86, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.mini-cover {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
}

.demo-info {
  min-width: 0;
  flex: 1;
}

.demo-info h4 {
  font-size: clamp(14px, 1.1vw, 18px);
  font-weight: 700;
  color: var(--paper);
  margin-bottom: 4px;
}

.demo-info .desc {
  font-size: clamp(11px, 0.9vw, 14px);
  color: var(--paper-dark);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.demo-info .stats {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.demo-info .stats span {
  font-family: 'DM Mono', monospace;
  font-size: clamp(10px, 0.8vw, 13px);
  color: var(--amber-glow);
}

/* ===== Atmosphere Bar ===== */
.atmosphere-bar {
  margin: 48px 6% 60px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.atmo-card {
  text-align: center;
  padding: 24px 16px;
  border: 1px solid rgba(212, 160, 86, 0.12);
  border-radius: 8px;
  background: rgba(45, 31, 10, 0.3);
}

.atmo-card .ico {
  font-size: clamp(24px, 2vw, 32px);
  margin-bottom: 8px;
}

.atmo-card h5 {
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  font-size: clamp(13px, 1.05vw, 17px);
  color: var(--amber-glow);
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}

.atmo-card p {
  font-size: clamp(11px, 0.9vw, 14px);
  color: var(--paper-dark);
  line-height: 1.6;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .box-row {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
  .demo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .storefront-header {
    padding: 32px 5% 0;
  }
  .sign-board {
    padding: 10px 20px 10px 16px;
    gap: 10px;
  }
  .open-sign {
    display: none;
  }
  .header-subtitle {
    font-size: 13px;
    margin-top: 16px;
  }

  .featured-section {
    padding: 28px 5%;
  }

  .box-row {
    display: flex;
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    gap: 14px;
    padding-bottom: 8px;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }
  .box-row::-webkit-scrollbar {
    display: none;
  }

  .game-box {
    flex-shrink: 0;
    width: 150px;
    scroll-snap-align: center;
  }

  .lamp-glow {
    width: 200px;
    height: 200px;
  }

  .demo-table-section {
    margin: 40px 5% 0;
  }
  .felt-surface {
    padding: 20px 5% 28px;
  }
  .demo-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .atmosphere-bar {
    margin: 36px 5% 48px;
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
