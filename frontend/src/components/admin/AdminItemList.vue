<script setup>
import { ref, onMounted, watch } from "vue";
import {
    getAdminItems,
    getAdminItem,
    deleteItem,
    updateItemStatus,
    getAdminTags,
} from "../../api/admin";
import { ElMessage, ElMessageBox } from "element-plus";
import { TYPE_LIST, getMeta } from "../../constants/types";
import TypeIcon from "../../components/TypeIcon.vue";
import AdminItemForm from "./AdminItemForm.vue";
import { batchDeleteItems, batchUpdateStatus } from "../../api/admin";

const items = ref([]);
const total = ref(0);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(12);
const searchKeyword = ref("");
const filterType = ref("");
const filterStatus = ref(null);
const viewMode = ref("card");

const selectedIds = ref(new Set());
const selectAll = ref(false);

const formVisible = ref(false);
const editingItem = ref(null);
const allTags = ref([]);

async function loadItems() {
    loading.value = true;
    try {
        const params = { page: page.value, size: pageSize.value };
        if (searchKeyword.value) params.keyword = searchKeyword.value;
        if (filterType.value) params.type = filterType.value;
        if (filterStatus.value !== null && filterStatus.value !== "")
            params.status = filterStatus.value;
        const res = await getAdminItems(params);
        items.value = res.records || [];
        total.value = res.total || 0;
    } catch (e) {
        if (e.response?.status === 403)
            ElMessage.warning("请重新登录以获取管理员权限");
        console.error("Failed to load items:", e);
    } finally {
        loading.value = false;
    }
}

async function loadTags() {
    try {
        allTags.value = await getAdminTags();
    } catch (e) {}
}

function handleSearch() {
    page.value = 1;
    loadItems();
}
function handlePageChange() {
    loadItems();
}
function handleCreate() {
    editingItem.value = null;
    formVisible.value = true;
}
function handleEdit(item) {
    editingItem.value = { ...item };
    formVisible.value = true;
}

async function handleDelete(item) {
    try {
        await ElMessageBox.confirm(`确定删除「${item.title}」？`, "确认删除", {
            type: "warning",
        });
        await deleteItem(item.id);
        ElMessage.success("已删除");
        await loadItems();
    } catch (e) {
        if (e !== "cancel") ElMessage.error("删除失败");
    }
}

async function handleStatusChange(item) {
    const newStatus = item.status === 1 ? 0 : 1;
    try {
        await updateItemStatus(item.id, newStatus);
        item.status = newStatus;
        ElMessage.success(newStatus === 1 ? "已上线" : "已下架");
    } catch (e) {
        ElMessage.error("操作失败");
    }
}

function handleFormSaved() {
    formVisible.value = false;
    loadItems();
}

function formatStatus(status) {
    switch (status) {
        case 1:
            return { text: "上线", type: "success" };
        case 0:
            return { text: "待审核", type: "warning" };
        case -1:
            return { text: "已下架", type: "danger" };
        default:
            return { text: "未知", type: "info" };
    }
}

function formatDate(dateStr) {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleDateString("zh-CN");
}

let searchTimer = null;
watch(searchKeyword, () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(handleSearch, 400);
});

onMounted(async () => {
    loadItems();
    loadTags();
    await checkQuickEdit();
});

async function checkQuickEdit() {
    const id = sessionStorage.getItem("adminEditId");
    if (!id) return;
    sessionStorage.removeItem("adminEditId");
    try {
        const item = await getAdminItem(Number(id));
        if (item) {
            editingItem.value = item;
            formVisible.value = true;
        }
    } catch (e) {
        /* item not found */
    }
}
// ── Batch selection ──
function toggleSelect(id) { const s = new Set(selectedIds.value); s.has(id) ? s.delete(id) : s.add(id); selectedIds.value = s; }
function toggleSelectAll() { selectAll.value = !selectAll.value; selectedIds.value = selectAll.value ? new Set(items.value.map(i => i.id)) : new Set(); }
async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确定删除 ${selectedIds.value.size} 条？`, '批量删除', { type: 'warning' });
    await batchDeleteItems([...selectedIds.value]); ElMessage.success('已删除'); selectedIds.value = new Set(); selectAll.value = false; loadItems();
  } catch (e) { if (e !== 'cancel') ElMessage.error('失败') }
}
async function batchStatus(status) {
  try {
    await batchUpdateStatus([...selectedIds.value], status); ElMessage.success(status === 1 ? '已上线' : '已下架'); selectedIds.value = new Set(); selectAll.value = false; loadItems();
  } catch (e) { ElMessage.error('失败') }
}
function handleSelectionChange(rows) { selectedIds.value = new Set(rows.map(r => r.id)); }
defineExpose({ refresh: loadItems });
</script>

<template>
    <div>
        <!-- Toolbar -->
        <div class="flex flex-wrap items-center gap-3 mb-4">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索..."
                clearable
                size="small"
                style="width: 160px"
            />
            <el-select
                v-model="filterType"
                placeholder="品类"
                clearable
                size="small"
                :teleported="false"
                popper-class="admin-select-drop"
                style="width: 110px"
                @change="handleSearch"
            >
                <el-option
                    v-for="t in TYPE_LIST"
                    :key="t"
                    :label="getMeta(t).label"
                    :value="t"
                />
            </el-select>
            <el-select
                v-model="filterStatus"
                placeholder="状态"
                clearable
                size="small"
                :teleported="false"
                popper-class="admin-select-drop"
                style="width: 100px"
                @change="handleSearch"
            >
                <el-option label="上线" :value="1" />
                <el-option label="待审核" :value="0" />
                <el-option label="已下架" :value="-1" />
            </el-select>
            <div class="flex-1" />
            <div class="flex items-center gap-1 bg-gray-800 rounded-lg p-0.5">
                <button
                    :class="[
                        'px-2.5 py-1.5 rounded text-sm transition',
                        viewMode === 'card'
                            ? 'bg-blue-600 text-white'
                            : 'text-gray-400 hover:text-white',
                    ]"
                    @click="viewMode = 'card'"
                >
                    卡片
                </button>
                <button
                    :class="[
                        'px-2.5 py-1.5 rounded text-sm transition',
                        viewMode === 'table'
                            ? 'bg-blue-600 text-white'
                            : 'text-gray-400 hover:text-white',
                    ]"
                    @click="viewMode = 'table'"
                >
                    列表
                </button>
            </div>
            <el-button type="primary" @click="handleCreate">新增内容</el-button>
        </div>

        <!-- Batch action bar -->
        <div v-if="selectedIds.size > 0" class="flex items-center gap-3 mb-4 p-2 bg-blue-900/30 rounded-lg border border-blue-800/50">
          <span class="text-sm text-blue-300">已选 {{ selectedIds.size }} 项</span>
          <el-button size="small" type="success" @click="batchStatus(1)">批量上线</el-button>
          <el-button size="small" type="warning" @click="batchStatus(0)">批量下架</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
          <el-button size="small" @click="selectedIds = new Set(); selectAll = false">取消</el-button>
        </div>

        <!-- Card View -->
        <div
            v-if="viewMode === 'card'"
            v-loading="loading"
            class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
        >
            <div
                v-for="item in items"
                :key="item.id"
                class="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden hover:border-gray-500 transition group"
            >
                <!-- Thumbnail -->
                <div class="aspect-2/1 bg-gray-900 relative overflow-hidden">
                    <img
                        v-if="item.wideCoverUrl || item.coverUrl"
                        :src="item.wideCoverUrl || item.coverUrl"
                        class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                    />
                    <div
                        v-else
                        class="w-full h-full flex items-center justify-center text-3xl opacity-40"
                    >
                        <TypeIcon :type="item.type" size="28" />
                    </div>
                    <!-- Type badge -->
                    <span
                        class="absolute top-2 left-2 px-2 py-0.5 rounded text-xs font-medium bg-black/60 text-white"
                    >
                        <TypeIcon :type="item.type" size="14" />
                        {{ getMeta(item.type).label }}
                    </span>
                    <!-- Status badge -->
                    <span
                        :class="[
                            'absolute top-2 right-2 px-2 py-0.5 rounded text-xs font-medium',
                            item.status === 1
                                ? 'bg-green-500/80 text-white'
                                : item.status === 0
                                  ? 'bg-amber-500/80 text-white'
                                  : 'bg-red-500/80 text-white',
                        ]"
                    >
                        {{ formatStatus(item.status).text }}
                    </span>
                </div>
                <!-- Info -->
                <div class="p-3">
                    <h4
                        class="text-sm font-medium text-white truncate mb-1"
                        :title="item.title"
                    >
                        {{ item.title }}
                    </h4>
                    <div
                        class="flex items-center gap-2 text-xs text-gray-300 mb-2"
                    >
                        <span>{{ formatDate(item.createdAt) }}</span>
                        <template v-if="item.source">
                            · {{ item.source }}</template
                        >
                    </div>
                    <p
                        v-if="item.description"
                        class="text-xs text-gray-300 line-clamp-2 mb-3"
                    >
                        {{ item.description }}
                    </p>
                    <!-- Actions -->
                    <div
                        class="flex items-center gap-2 pt-2 border-t border-gray-700/50"
                    >
                        <el-switch
                            :model-value="item.status === 1"
                            @change="handleStatusChange(item)"
                            size="small"
                        />
                        <div class="flex-1" />
                        <el-button
                            type="primary"
                            size="small"
                            text
                            @click="handleEdit(item)"
                            >编辑</el-button
                        >
                        <el-popconfirm
                            :title="`确定删除「${item.title}」？`"
                            @confirm="handleDelete(item)"
                        >
                            <template #reference>
                                <el-button type="danger" size="small" text
                                    >删除</el-button
                                >
                            </template>
                        </el-popconfirm>
                    </div>
                </div>
            </div>
            <!-- Empty -->
            <div
                v-if="items.length === 0"
                class="col-span-full text-center py-16 text-gray-300"
            >
                <div class="text-4xl mb-2">📭</div>
                <p>暂无内容</p>
            </div>
        </div>

        <!-- Table View -->
        <div v-else v-loading="loading">
            <el-table :data="items" style="width: 100%" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="40" />
                <el-table-column label="封面" width="90">
                    <template #default="{ row }">
                        <div
                            class="w-16 h-10 rounded overflow-hidden bg-gray-900 flex items-center justify-center"
                        >
                            <img
                                v-if="row.wideCoverUrl || row.coverUrl"
                                :src="row.wideCoverUrl || row.coverUrl"
                                class="w-full h-full object-cover"
                            />
                            <span v-else class="text-lg">{{
                                getMeta(row.type).emoji
                            }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="title"
                    label="标题"
                    min-width="160"
                    show-overflow-tooltip
                />
                <el-table-column label="品类" width="100">
                    <template #default="{ row }">
                        <el-tag size="small"
                            >{{ getMeta(row.type).emoji }}
                            {{ getMeta(row.type).label }}</el-tag
                        >
                    </template>
                </el-table-column>
                <el-table-column prop="source" label="来源" width="70" />
                <el-table-column label="状态" width="90">
                    <template #default="{ row }">
                        <el-tag
                            :type="formatStatus(row.status).type"
                            size="small"
                            >{{ formatStatus(row.status).text }}</el-tag
                        >
                    </template>
                </el-table-column>
                <el-table-column label="创建日期" width="110">
                    <template #default="{ row }">
                        <span class="text-xs text-gray-300">{{
                            formatDate(row.createdAt)
                        }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="上下架" width="80">
                    <template #default="{ row }">
                        <el-switch
                            :model-value="row.status === 1"
                            @change="handleStatusChange(row)"
                            size="small"
                        />
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            size="small"
                            text
                            @click="handleEdit(row)"
                            >编辑</el-button
                        >
                        <el-popconfirm
                            :title="`确定删除「${row.title}」？`"
                            @confirm="handleDelete(row)"
                        >
                            <template #reference>
                                <el-button type="danger" size="small" text
                                    >删除</el-button
                                >
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- Pagination -->
        <div class="flex justify-center mt-4">
            <el-pagination
                v-model:current-page="page"
                :page-size="pageSize"
                :total="total"
                layout="total, prev, pager, next"
                @current-change="handlePageChange"
            />
        </div>

        <!-- Edit Form Dialog -->
        <AdminItemForm
            v-model:visible="formVisible"
            :item="editingItem"
            :all-tags="allTags"
            @saved="handleFormSaved"
        />
    </div>
</template>
