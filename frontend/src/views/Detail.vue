<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { Icon } from "@iconify/vue";
import { useRoute, useRouter } from "vue-router";
import { getItemBySlug, getHotItems } from "../api/item";
import { useAuthStore } from "../stores/auth";
import { saveUserItem, getUserItemStatus } from "../api/auth";
import AppCard from "../components/AppCard.vue";
import ReviewSection from "../components/ReviewSection.vue";
import AdminItemForm from "../components/admin/AdminItemForm.vue";
import TypeIcon from "../components/TypeIcon.vue";
import { Edit } from "@element-plus/icons-vue";
import { getMeta } from "../constants/types";

// Widgets
import MediaHero from "../components/detail/widgets/MediaHero.vue";
import ScreenshotGallery from "../components/detail/widgets/ScreenshotGallery.vue";

// Type Panels
import GameDetail from "../components/detail/types/GameDetail.vue";
import CoffeeDetail from "../components/detail/types/CoffeeDetail.vue";
import BoardgameDetail from "../components/detail/types/BoardgameDetail.vue";
import OfflineDetail from "../components/detail/types/OfflineDetail.vue";
import MusicDetail from "../components/detail/types/MusicDetail.vue";
import GenericDetail from "../components/detail/types/GenericDetail.vue";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const item = ref(null);
const related = ref([]);
const loading = ref(true);
const myStatus = ref(null);
const savingStatus = ref(false);
const dropdownOpen = ref(false);
let dropdownTimer = null;

const showEditDialog = ref(false);
const editTags = ref([]);

const typeComponentMap = {
  game: GameDetail,
  coffee: CoffeeDetail,
  boardgame: BoardgameDetail,
  offline: OfflineDetail,
  music: MusicDetail,
};

function showDropdown() {
  clearTimeout(dropdownTimer);
  dropdownOpen.value = true;
}
function hideDropdown() {
  dropdownTimer = setTimeout(() => {
    dropdownOpen.value = false;
  }, 150);
}

const statusOptions = [
  { key: "want_to_play", label: "有兴趣" },
  { key: "played", label: "已体验" },
  { key: "loved", label: "最爱" },
  { key: "dropped", label: "弃了" },
];

onMounted(() => fetchItem());
watch(
  () => route.params.slug,
  () => fetchItem(),
);

async function fetchItem() {
  loading.value = true;
  related.value = [];
  myStatus.value = null;
  try {
    item.value = await getItemBySlug(route.params.slug);
    if (!item.value) {
      router.replace("/");
      return;
    }
    const d = await getHotItems({ type: item.value.type, limit: 4 });
    related.value = (d || []).filter((r) => r.id !== item.value.id);
    if (auth.isLoggedIn) {
      try {
        const ui = await getUserItemStatus(item.value.id);
        if (ui) myStatus.value = ui.status;
      } catch {}
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

const typeMeta = computed(() => getMeta(item.value?.type));

const badgeAccentMap = {
  emerald: "text-emerald-400 bg-emerald-900/50",
  red: "text-red-400 bg-red-900/50",
  violet: "text-purple-400 bg-purple-900/50",
  amber: "text-amber-400 bg-amber-900/50",
  sky: "text-blue-400 bg-blue-900/50",
  fuchsia: "text-pink-400 bg-pink-900/50",
  cyan: "text-cyan-400 bg-cyan-900/50",
  orange: "text-orange-400 bg-orange-900/50",
  indigo: "text-indigo-400 bg-indigo-900/50",
};
const typeBadge = computed(
  () => badgeAccentMap[typeMeta.value?.accent] || "text-gray-400 bg-gray-800",
);

const info = computed(() => {
  try {
    return JSON.parse(item.value?.infoJson || "{}");
  } catch {
    return {};
  }
});

const currentStatusLabel = computed(
  () => statusOptions.find((s) => s.key === myStatus.value)?.label || "收藏",
);

function goBack() {
  router.push(item.value?.type ? `/list/${item.value.type}` : "/");
}
function editItem() {
  showEditDialog.value = true;
  loadEditTags();
}

async function loadEditTags() {
  try {
    const { getAdminTags } = await import("../api/admin");
    editTags.value = await getAdminTags();
  } catch {}
}

const infoFields = computed(() => {
  const skip = [
    "demo_url",
    "benchmark_url",
    "videos",
    "dlc",
    "features",
    "languages",
    "min_requirements",
    "rec_requirements",
    "flavor_notes",
    "flavor",
    "total_cup_points",
    "aroma",
    "aftertaste",
    "acidity",
    "body",
    "balance",
    "uniformity",
    "clean_cup",
    "sweetness",
    "rule_images",
    "rule_text",
    "preview_url",
    "reader_url",
    "screenshots",
    "free",
    "is_free",
    "is_dlc",
    "demo_available",
    // 已由专属组件渲染或属技术元数据，不在属性网格中显示
    "trailer",
    "websites",
    "similar_games",
    "game_mode",
    "rating_count",
    // 标题别名类，不适合放属性网格
    "synonyms",
    "english_title",
    "romaji_title",
    "name_jp",
    // SCA 杯测师评分，已在雷达图展示
    "cupper_points",
  ];
  if (item.value?.type === "offline") {
    skip.push(
      "venue",
      "date",
      "time",
      "price",
      "capacity",
      "difficulty",
      "highlights",
    );
  }
  const result = Object.entries(info.value)
    .filter(
      ([k, v]) =>
        !skip.includes(k) &&
        v !== null &&
        v !== "" &&
        typeof v !== "object" &&
        typeof v !== "boolean",
    )
    .map(([k, v]) => {
      const labels = {
        // 通用
        price: "价格",
        year: "年份",
        genre: "类型",
        category: "分类",
        // 游戏
        developer: "开发商",
        publisher: "出版商",
        platform: "平台",
        release_date: "发售日",
        theme: "主题",
        rating: "评分",
        // 电影
        director: "导演",
        duration: "时长",
        // 动漫
        studio: "工作室",
        episodes: "集数",
        seasons: "季数",
        network: "播出平台",
        score: "评分",
        // 模型
        grade: "等级",
        scale: "比例",
        series: "系列",
        material: "材质",
        // 书籍
        pages: "页数",
        author: "作者",
        isbn: "ISBN",
        // 音乐
        artist: "艺术家",
        album: "专辑",
        tracks: "曲目数",
        // 桌游
        players: "玩家数",
        play_time: "游玩时间",
        playtime: "游玩时长",
        weight: "重量",
        // 咖啡
        origin: "产地",
        region: "产区",
        farm: "庄园",
        producer: "生产者",
        roast: "烘焙度",
        roast_level: "烘焙度",
        process: "处理法",
        processing_method: "处理法",
        variety: "品种",
        altitude: "海拔",
        harvest: "采收期",
        // 线下活动
        venue: "地点",
        date: "日期",
        time: "时间",
        capacity: "容量",
        difficulty: "难度",
        event_type: "活动类型",
        // 数码
        brand: "品牌",
      };
      if (k === "price" && String(v).toLowerCase() === "free") {
        return ["价格", "免费 (Free)"];
      }
      return [labels[k] || k.replace(/_/g, " "), v];
    });

  if (!result.some((f) => f[0] === "价格")) {
    if (info.value.free) {
      result.push(["价格", "免费 (Free)"]);
    } else if (info.value.demo_available) {
      result.push(["价格", "Demo 阶段暂不收费"]);
    }
  }

  return result;
});

const screenshots = computed(() => {
  const raw = info.value.screenshots;
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === "string")
    return raw
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);
  return [];
});

// For video switching (if parent wants to do it)
const mediaHeroRef = ref(null);

async function setStatus(newStatus) {
  if (!auth.isLoggedIn) {
    router.push({ name: "Login", query: { redirect: route.fullPath } });
    return;
  }
  if (newStatus === myStatus.value) return;
  savingStatus.value = true;
  try {
    await saveUserItem(item.value.id, newStatus);
    myStatus.value = newStatus;
  } catch {
  } finally {
    savingStatus.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="w-full max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 py-6 sm:py-10">
      <div v-if="loading" class="text-center text-gray-500 py-20">
        加载中...
      </div>
      <template v-else-if="item">
        <div class="flex items-center gap-3 mb-4 sm:mb-6">
          <button
            @click="goBack"
            class="text-gray-400 hover:text-white text-sm transition shrink-0"
          >
            ← 返回
          </button>

          <!-- Video switcher -->
          <div
            v-if="
              mediaHeroRef?.videoSources?.length > 1 &&
              !mediaHeroRef?.readerUrl &&
              !mediaHeroRef?.isMusic
            "
            class="flex items-center gap-1 overflow-x-auto scrollbar-hide"
          >
            <button
              v-for="src in mediaHeroRef.videoSources"
              :key="src.key"
              @click="mediaHeroRef.activeVideoUrl = src.url"
              :class="
                mediaHeroRef.activeVideoUrl === src.url
                  ? 'bg-white/15 text-white ring-1 ring-white/20'
                  : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white'
              "
              class="text-[11px] px-2.5 py-1 rounded-md transition-all whitespace-nowrap"
            >
              {{
                src.key === "youtube"
                  ? "YT"
                  : src.key === "bilibili"
                    ? "B站"
                    : src.key === "steam"
                      ? "Steam"
                      : src.key
              }}
            </button>
          </div>

          <div class="flex-1" />
          <button
            v-if="auth.isAdmin"
            @click="editItem"
            class="text-gray-500 hover:text-indigo-400 text-sm transition inline-flex items-center gap-1 shrink-0"
          >
            <el-icon :size="14"><Edit /></el-icon> 编辑
          </button>
        </div>

        <div
          class="bg-gray-900 rounded-xl sm:rounded-2xl border border-gray-800 overflow-visible"
        >
          <MediaHero ref="mediaHeroRef" :item="item" :info="info" />

          <!-- Screenshot Gallery -->
          <ScreenshotGallery :screenshots="screenshots" />

          <div class="p-4 sm:p-8">
            <div class="flex items-center gap-3 mb-4">
              <span :class="typeBadge" class="text-xs px-2 py-0.5 rounded">{{
                typeMeta.label
              }}</span>
              <span class="text-xs text-gray-600">{{ item.source }}</span>
            </div>

            <div class="flex items-start justify-between gap-2 sm:gap-4">
              <h1
                class="text-2xl sm:text-3xl font-bold mb-4 min-w-0 break-words"
              >
                {{ item.title }}
              </h1>
              <div
                class="relative shrink-0"
                @mouseenter="showDropdown"
                @mouseleave="hideDropdown"
              >
                <button
                  :disabled="savingStatus"
                  :class="
                    myStatus ? 'bg-blue-600' : 'bg-gray-800 hover:bg-gray-700'
                  "
                  class="text-sm px-4 py-2 rounded-lg transition flex items-center gap-2"
                >
                  {{ currentStatusLabel }} ▾
                </button>
                <div
                  v-show="dropdownOpen"
                  class="absolute right-0 top-full mt-0.5 bg-gray-800 border border-gray-700 rounded-lg py-1 w-32 z-10"
                  @mouseenter="showDropdown"
                  @mouseleave="hideDropdown"
                >
                  <button
                    v-for="opt in statusOptions"
                    :key="opt.key"
                    @click="
                      setStatus(opt.key);
                      dropdownOpen = false;
                    "
                    :class="
                      myStatus === opt.key
                        ? 'text-blue-400'
                        : 'text-gray-300 hover:text-white'
                    "
                    class="block w-full text-left px-3 py-1.5 text-sm transition"
                  >
                    {{ opt.label }}
                  </button>
                </div>
              </div>
            </div>

            <p class="text-gray-300 text-base leading-relaxed mb-6">
              {{ item.description }}
            </p>

            <!-- Dynamic Type Component -->
            <component
              :is="typeComponentMap[item.type] || GenericDetail"
              :item="item"
              :info="info"
            />

            <div
              v-if="infoFields.length > 0"
              class="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 bg-gray-800/50 rounded-lg mt-6"
            >
              <div v-for="f in infoFields" :key="f[0]" class="text-center">
                <div class="text-xs text-gray-500 mb-0.5">{{ f[0] }}</div>
                <div class="text-sm font-medium text-gray-200">{{ f[1] }}</div>
              </div>
            </div>

            <div class="mt-6 text-xs text-gray-600">
              更新于 {{ new Date(item.updatedAt).toLocaleDateString("zh-CN") }}
            </div>
          </div>
        </div>

        <section v-if="item.id" class="mt-16">
          <ReviewSection :item-id="item.id" />
        </section>

        <section
          v-if="related.length > 0"
          class="mt-16 border-t border-gray-800/60 pt-16"
        >
          <h2 class="text-xl font-semibold mb-6">相关推荐</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <AppCard v-for="r in related" :key="r.id" :item="r" />
          </div>
        </section>
      </template>
      <div v-else class="text-center text-gray-500 py-20">
        <div class="text-4xl mb-3">📭</div>
        <p>内容未找到</p>
        <button
          @click="goBack"
          class="mt-4 text-sm text-blue-400 hover:text-blue-300 transition"
        >
          ← 返回列表
        </button>
      </div>
    </main>

    <!-- Admin quick-edit dialog -->
    <div class="detail-edit-dialog">
      <AdminItemForm
        :visible="showEditDialog"
        :item="item"
        :all-tags="editTags"
        @update:visible="showEditDialog = $event"
        @saved="
          showEditDialog = false;
          fetchItem();
        "
      />
    </div>
  </div>
</template>
