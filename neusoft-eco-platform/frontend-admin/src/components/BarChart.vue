<template>
  <div class="bar-chart-container">
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // X轴数据（省份名称数组）
  xData: {
    type: Array,
    default: () => []
  },
  // Y轴数据（数值数组）
  yData: {
    type: Array,
    default: () => []
  },
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 柱状图颜色
  barColor: {
    type: String,
    default: '#409EFF'
  },
  // Y轴名称
  yAxisName: {
    type: String,
    default: '次数'
  },
  // 是否显示图例
  showLegend: {
    type: Boolean,
    default: false
  },
  // 图例名称
  legendName: {
    type: String,
    default: ''
  }
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

function initChart() {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()

  // 监听窗口大小变化
  resizeObserver = new ResizeObserver(() => {
    chartInstance && chartInstance.resize()
  })
  resizeObserver.observe(chartRef.value)
}

function updateChart() {
  if (!chartInstance) return

  const option = {
    title: {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.xData,
      axisLabel: {
        interval: 0,
        rotate: 30,
        fontSize: 11,
        color: '#606266'
      },
      axisLine: {
        lineStyle: {
          color: '#DCDFE6'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: props.yAxisName,
      nameTextStyle: {
        color: '#909399',
        fontSize: 12
      },
      axisLabel: {
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: props.legendName || props.title,
        type: 'bar',
        data: props.yData,
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: props.barColor },
            { offset: 1, color: adjustColor(props.barColor, -30) }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: adjustColor(props.barColor, 20) },
              { offset: 1, color: props.barColor }
            ])
          }
        }
      }
    ]
  }

  if (props.showLegend) {
    option.legend = {
      show: true,
      top: '8%',
      right: '10%'
    }
  }

  chartInstance.setOption(option, true)
}

// 调整颜色明暗度
function adjustColor(color, amount) {
  const hex = color.replace('#', '')
  const num = parseInt(hex, 16)
  let r = (num >> 16) + amount
  let g = ((num >> 8) & 0x00FF) + amount
  let b = (num & 0x0000FF) + amount

  r = Math.max(0, Math.min(255, r))
  g = Math.max(0, Math.min(255, g))
  b = Math.max(0, Math.min(255, b))

  return '#' + ((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')
}

// 监听数据变化
watch(() => [props.xData, props.yData], () => {
  nextTick(() => updateChart())
}, { deep: true })

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})
</script>

<style scoped>
.bar-chart-container {
  width: 100%;
  height: 100%;
}
.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style>
