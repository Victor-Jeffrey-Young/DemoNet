<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminDashboard from '../components/admin/AdminDashboard.vue'
import AdminItemList from '../components/admin/AdminItemList.vue'
import AdminTagManager from '../components/admin/AdminTagManager.vue'
import AdminCarouselManager from '../components/admin/AdminCarouselManager.vue'
import AdminFetchPanel from '../components/admin/AdminFetchPanel.vue'

const auth = useAuthStore()
const route = useRoute()
const activeTab = ref('dashboard')

const dashboardRef = ref(null)
const itemListRef = ref(null)
const tagManagerRef = ref(null)
const carouselRef = ref(null)
const fetchPanelRef = ref(null)

function handleTabChange(tab) {
  switch (tab) {
    case 'dashboard': dashboardRef.value?.refresh(); break
    case 'items': itemListRef.value?.refresh(); break
    case 'tags': tagManagerRef.value?.refresh(); break
    case 'carousel': carouselRef.value?.refresh(); break
    case 'fetch': fetchPanelRef.value?.refresh(); break
  }
}

onMounted(() => {
  if (route.query.edit) {
    sessionStorage.setItem('adminEditId', route.query.edit)
    activeTab.value = 'items'
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <main class="max-w-6xl mx-auto px-6 py-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-800">
        <div>
          <h1 class="text-2xl font-bold text-white">管理后台</h1>
          <p class="text-xs text-gray-400 mt-1">内容 · 标签 · 轮播 · 抓取</p>
        </div>
        <div class="flex items-center gap-3 text-sm text-gray-300">
          <span>{{ auth.user?.username }}</span>
          <el-tag type="warning" size="small">ADMIN</el-tag>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="admin-tabs">
        <el-tab-pane label="仪表盘" name="dashboard">
          <AdminDashboard ref="dashboardRef" />
        </el-tab-pane>
        <el-tab-pane label="内容管理" name="items">
          <AdminItemList ref="itemListRef" />
        </el-tab-pane>
        <el-tab-pane label="标签管理" name="tags">
          <AdminTagManager ref="tagManagerRef" />
        </el-tab-pane>
        <el-tab-pane label="轮播管理" name="carousel">
          <AdminCarouselManager ref="carouselRef" />
        </el-tab-pane>
        <el-tab-pane label="数据抓取" name="fetch">
          <AdminFetchPanel ref="fetchPanelRef" />
        </el-tab-pane>
      </el-tabs>
    </main>
  </div>
</template>

<style>
/* ===== Safari subpixel rendering fix — prevent alternating-row blur ===== */
.admin-tabs {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.admin-tabs .el-table__body tr,
.admin-tabs .el-table__body td {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ===== Element Plus dark theme overrides ===== */
.admin-tabs .el-tabs__item {
  color: #9ca3af; font-weight: 500;
}
.admin-tabs .el-tabs__item.is-active {
  color: #f3f4f6; font-weight: 600;
}
.admin-tabs .el-tabs__active-bar { background-color: #3b82f6; }
.admin-tabs .el-tabs__nav-wrap::after { background-color: #374151; }

/* Table — global overrides for dark readability */
.admin-tabs .el-table {
  --el-table-bg-color: #1f2937;
  --el-table-tr-bg-color: #1f2937;
  --el-table-header-bg-color: #111827;
  --el-table-row-hover-bg-color: #374151;
  --el-table-text-color: #f3f4f6;
  --el-table-header-text-color: #d1d5db;
  --el-table-border-color: #4b5563;
  font-size: 14px;
}
.admin-tabs .el-table th.el-table__cell {
  background-color: #111827; color: #d1d5db; font-weight: 600; letter-spacing: 0.02em;
}

/* Sticky / fixed columns */
.admin-tabs .el-table .el-table__fixed-right {
  background-color: #1f2937; box-shadow: -2px 0 8px rgba(0,0,0,.5);
}

/* Dialog */
.admin-tabs .el-dialog {
  --el-dialog-bg-color: #1f2937;
  --el-dialog-title-font-size: 16px;
  --el-dialog-border-radius: 12px;
}
.admin-tabs .el-dialog__header { border-bottom: 1px solid #374151; padding-bottom: 16px; }
.admin-tabs .el-dialog__title { color: #f3f4f6; font-weight: 700; }

/* Input */
.admin-tabs .el-input__wrapper {
  background-color: #374151; box-shadow: 0 0 0 1px #4b5563 inset;
}
.admin-tabs .el-input__inner { color: #f3f4f6; }
.admin-tabs .el-input__inner::placeholder { color: #6b7280; }
.admin-tabs .el-textarea__inner { background-color: #374151; color: #f3f4f6; border-color: #4b5563; }
.admin-tabs .el-textarea__inner::placeholder { color: #6b7280; }
.admin-tabs .el-input.is-disabled .el-input__wrapper {
  background-color: #1f2937; box-shadow: 0 0 0 1px #374151 inset; opacity: .65;
}
.admin-tabs .el-input.is-disabled .el-input__inner { color: #9ca3af; }

/* Select trigger — override Element Plus white BG via CSS variables */
.admin-tabs .el-select {
  --el-fill-color-blank: #374151;
  --el-border-color: #4b5563;
  --el-text-color-regular: #e5e7eb;
  --el-text-color-placeholder: #6b7280;
  --el-select-border-color-hover: #6b7280;
}
.admin-tabs .el-select .el-input__wrapper {
  background-color: #374151 !important;
  box-shadow: 0 0 0 1px #4b5563 inset !important;
}
.admin-tabs .el-select .el-input.is-focus .el-input__wrapper {
  box-shadow: 0 0 0 1px #3b82f6 inset !important;
}
.admin-tabs .el-select .el-input__inner {
  color: #e5e7eb !important;
}
.admin-tabs .el-select .el-input .el-select__caret {
  color: #9ca3af;
}
.admin-tabs .el-select .el-select__placeholder {
  color: #6b7280;
  font-weight: 400;
}
.admin-tabs .el-select .el-select__selected-item {
  color: #e5e7eb;
  font-weight: 500;
}
.admin-tabs .el-select .el-input.is-focus .el-select__placeholder {
  color: #9ca3af;
}
/* Small select explicit height + text visibility */
.admin-tabs .el-input--small .el-input__wrapper { height: 32px; }
.admin-tabs .el-input--small .el-input__inner { line-height: 32px; color: #e5e7eb; }
.admin-tabs .el-select-dropdown {
  background-color: #1f2937; border-color: #4b5563;
}
.admin-tabs .el-select-dropdown__item { color: #e5e7eb; padding: 8px 16px; font-size: 13px; }
.admin-tabs .el-select-dropdown__item.is-hovering,
.admin-tabs .el-select-dropdown__item.hover,
.admin-tabs .el-select-dropdown__item:hover { background-color: #374151; color: #f3f4f6; }
.admin-tabs .el-select-dropdown__item.selected { color: #60a5fa; font-weight: 700; background-color: #1e3a5f; }

/* Tag */
.admin-tabs .el-tag { font-weight: 600; letter-spacing: 0.03em; }
.admin-tabs .el-tag--success { background-color: #065f46; border-color: #059669; color: #a7f3d0; }
.admin-tabs .el-tag--warning { background-color: #78350f; border-color: #d97706; color: #fde68a; }
.admin-tabs .el-tag--danger  { background-color: #7f1d1d; border-color: #dc2626; color: #fecaca; }
.admin-tabs .el-tag--info    { background-color: #1e3a5f; border-color: #3b82f6; color: #bfdbfe; }

/* Button */
.admin-tabs .el-button--default {
  --el-button-bg-color: #374151; --el-button-border-color: #4b5563; --el-button-text-color: #d1d5db;
}
.admin-tabs .el-button--default:hover {
  --el-button-bg-color: #4b5563; --el-button-border-color: #6b7280; --el-button-text-color: #f3f4f6;
}
.admin-tabs .el-button--primary {
  --el-button-bg-color: #2563eb; --el-button-border-color: #3b82f6;
}
.admin-tabs .el-button--danger {
  --el-button-bg-color: #dc2626; --el-button-border-color: #ef4444;
}
.admin-tabs .el-button--success {
  --el-button-bg-color: #059669; --el-button-border-color: #10b981;
}

/* Switch */
.admin-tabs .el-switch__label { color: #d1d5db; font-weight: 500; }

/* Pagination */
.admin-tabs .el-pagination {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: #d1d5db;
  --el-pagination-button-bg-color: #374151;
  --el-pagination-button-disabled-bg-color: #1f2937;
  --el-pagination-hover-color: #60a5fa;
}
.admin-tabs .el-pagination button { color: #d1d5db; }
.admin-tabs .el-pagination .el-pager li { color: #d1d5db; background-color: #374151; border-radius: 6px; margin: 0 2px; min-width: 32px; font-weight: 600; }
.admin-tabs .el-pagination .el-pager li.is-active { background-color: #2563eb; color: #fff; }
.admin-tabs .el-pagination .el-pager li:hover { color: #60a5fa; }
.admin-tabs .el-pagination .btn-prev, .admin-tabs .el-pagination .btn-next { background-color: #374151; border-radius: 6px; }
.admin-tabs .el-pagination .btn-prev:hover, .admin-tabs .el-pagination .btn-next:hover { color: #60a5fa; }

/* Popconfirm / Popover */
.admin-tabs .el-popconfirm { background-color: #1f2937; border-color: #4b5563; padding: 16px; }
.admin-tabs .el-popconfirm__main { color: #f3f4f6; }
.admin-tabs .el-popover { background-color: #1f2937; border-color: #4b5563; color: #e5e7eb; }

/* Form item label */
.admin-tabs .el-form-item__label { color: #d1d5db; font-weight: 600; }

/* Loading mask */
.admin-tabs .el-loading-mask { background-color: rgba(17, 24, 39, 0.7); }

/* Scrollbar */
.admin-tabs ::-webkit-scrollbar { width: 6px; }
.admin-tabs ::-webkit-scrollbar-track { background: #1f2937; }
.admin-tabs ::-webkit-scrollbar-thumb { background: #4b5563; border-radius: 3px; }
.admin-tabs ::-webkit-scrollbar-thumb:hover { background: #6b7280; }

/* ===== Global dark dropdown (for teleported selects outside .admin-tabs) ===== */
.admin-select-drop {
  background-color: #1f2937 !important;
  border: 1px solid #4b5563 !important;
  box-shadow: 0 4px 16px rgba(0,0,0,.5) !important;
}
.admin-select-drop .el-select-dropdown__item {
  color: #e5e7eb !important; padding: 8px 16px; font-size: 13px;
}
.admin-select-drop .el-select-dropdown__item.is-hovering,
.admin-select-drop .el-select-dropdown__item:hover {
  background-color: #374151 !important; color: #f3f4f6 !important;
}
.admin-select-drop .el-select-dropdown__item.selected {
  color: #60a5fa !important; font-weight: 700 !important; background-color: #1e3a5f !important;
}
.admin-select-drop .el-select-dropdown__empty {
  color: #6b7280 !important; padding: 12px;
}
.admin-select-drop .el-popper__arrow::before {
  background: #1f2937 !important; border-color: #4b5563 !important;
}
</style>
