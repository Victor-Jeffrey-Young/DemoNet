<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  info: {
    type: Object,
    required: true
  }
})

const radarChartRef = ref(null)
let radarChartInstance = null

const scaDimensions = ['aroma','flavor','aftertaste','acidity','body','balance','uniformity','clean_cup','sweetness']
const scaLabels = ['香气','风味','余韵','酸质','醇厚度','平衡感','一致性','干净度','甜度']

async function renderRadarChart() {
  if (!radarChartRef.value) return
  try {
    const el = radarChartRef.value
    const echarts = await import('echarts/dist/echarts.esm.js')
    if (radarChartInstance) radarChartInstance.dispose()
    radarChartInstance = echarts.init(el)
    const values = scaDimensions.map(d => Number(props.info[d]) || 0)
    radarChartInstance.setOption({
      backgroundColor: '#1a120a',
      title: { text: 'SCA 评分', left: 'center', textStyle: { color: '#fbbf24', fontSize: 14 } },
      radar: {
        indicator: scaDimensions.map((d, i) => ({ name: scaLabels[i], max: 10 })),
        center: ['50%', '55%'],
        radius: '60%',
        axisName: { fontSize: 9, color: '#fbbf24' },
        splitArea: { areaStyle: { color: ['rgba(251,191,36,0.04)', 'rgba(251,191,36,0.1)'] } },
        axisLine: { lineStyle: { color: 'rgba(251,191,36,0.3)' } },
        splitLine: { lineStyle: { color: 'rgba(251,191,36,0.15)' } }
      },
      series: [{
        type: 'radar',
        data: [{ value: values, name: 'SCA', areaStyle: { color: 'rgba(251,191,36,0.35)' } }],
        lineStyle: { color: '#fbbf24', width: 2.5 },
        itemStyle: { color: '#fbbf24', borderColor: '#fff', borderWidth: 1 },
        symbol: 'circle',
        symbolSize: 5,
        label: { show: true, formatter: '{c}', color: '#fbbf24', fontSize: 10, distance: 8 }
      }]
    })
    setTimeout(() => { if (radarChartInstance) radarChartInstance.resize() }, 100)
  } catch(e) {
    console.error('radar:', e)
  }
}

watch(() => props.info, () => {
  let attempts = 0;
  const tryRender = () => {
    if (radarChartRef.value) {
      renderRadarChart()
    } else if (attempts++ < 20) {
      setTimeout(tryRender, 100)
    }
  };
  tryRender()
}, { immediate: true })

onUnmounted(() => {
  if (radarChartInstance) {
    radarChartInstance.dispose()
    radarChartInstance = null
  }
})
</script>

<template>
  <div class="mt-4 p-4 bg-amber-900/10 border border-amber-500/10 rounded-lg">
    <span class="text-xs text-amber-400 font-medium">📊 SCA 品测雷达图</span>
    <div ref="radarChartRef" class="w-full h-[280px] mt-2"></div>
  </div>
</template>
