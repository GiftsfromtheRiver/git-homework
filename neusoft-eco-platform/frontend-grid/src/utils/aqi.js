/**
 * AQI 计算工具
 * 根据《环境空气质量指数(AQI)技术规定（试行）》HJ 633-2012
 */

// AQI 分级标准
const AQI_LEVELS = [
  { aqiMin: 0, aqiMax: 50, level: '优', color: '#00e400' },
  { aqiMin: 51, aqiMax: 100, level: '良', color: '#ffff00' },
  { aqiMin: 101, aqiMax: 150, level: '轻度污染', color: '#ff7e00' },
  { aqiMin: 151, aqiMax: 200, level: '中度污染', color: '#ff0000' },
  { aqiMin: 201, aqiMax: 300, level: '重度污染', color: '#99004c' },
  { aqiMin: 301, aqiMax: 500, level: '严重污染', color: '#7e0023' }
]

// 各污染物分指数对应的浓度限值 (μg/m³, CO为mg/m³)
const POLLUTANT_BREAKPOINTS = {
  pm25: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 35 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 35, cHigh: 75 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 75, cHigh: 115 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 115, cHigh: 150 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 150, cHigh: 250 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 250, cHigh: 350 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 350, cHigh: 500 }
  ],
  pm10: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 50 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 50, cHigh: 150 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 150, cHigh: 250 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 250, cHigh: 350 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 350, cHigh: 420 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 420, cHigh: 500 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 500, cHigh: 600 }
  ],
  so2: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 50 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 50, cHigh: 150 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 150, cHigh: 475 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 475, cHigh: 800 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 800, cHigh: 1600 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 1600, cHigh: 2100 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 2100, cHigh: 2620 }
  ],
  no2: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 40 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 40, cHigh: 80 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 80, cHigh: 180 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 180, cHigh: 280 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 280, cHigh: 565 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 565, cHigh: 750 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 750, cHigh: 940 }
  ],
  co: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 2 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 2, cHigh: 4 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 4, cHigh: 14 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 14, cHigh: 24 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 24, cHigh: 36 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 36, cHigh: 48 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 48, cHigh: 60 }
  ],
  o3: [
    { iaqiLow: 0, iaqiHigh: 50, cLow: 0, cHigh: 100 },
    { iaqiLow: 51, iaqiHigh: 100, cLow: 100, cHigh: 160 },
    { iaqiLow: 101, iaqiHigh: 150, cLow: 160, cHigh: 215 },
    { iaqiLow: 151, iaqiHigh: 200, cLow: 215, cHigh: 265 },
    { iaqiLow: 201, iaqiHigh: 300, cLow: 265, cHigh: 800 },
    { iaqiLow: 301, iaqiHigh: 400, cLow: 800, cHigh: 1000 },
    { iaqiLow: 401, iaqiHigh: 500, cLow: 1000, cHigh: 1200 }
  ]
}

// 污染物中文名
const POLLUTANT_NAMES = {
  pm25: 'PM2.5',
  pm10: 'PM10',
  so2: 'SO₂',
  no2: 'NO₂',
  co: 'CO',
  o3: 'O₃'
}

// 污染物单位
const POLLUTANT_UNITS = {
  pm25: 'μg/m³',
  pm10: 'μg/m³',
  so2: 'μg/m³',
  no2: 'μg/m³',
  co: 'mg/m³',
  o3: 'μg/m³'
}

/**
 * 计算单个污染物的 IAQI（分空气质量指数）
 * @param {Number} concentration - 浓度值
 * @param {String} pollutant - 污染物类型
 * @returns {Number} IAQI 值
 */
function calculateIAQI(concentration, pollutant) {
  if (concentration === null || concentration === undefined || isNaN(concentration)) {
    return null
  }
  
  const breakpoints = POLLUTANT_BREAKPOINTS[pollutant]
  if (!breakpoints) return null
  
  const c = parseFloat(concentration)
  
  // 找到对应的浓度区间
  for (let i = 0; i < breakpoints.length; i++) {
    const bp = breakpoints[i]
    if (c <= bp.cHigh) {
      // 线性插值计算 IAQI
      const iaqi = ((bp.iaqiHigh - bp.iaqiLow) / (bp.cHigh - bp.cLow)) * (c - bp.cLow) + bp.iaqiLow
      return Math.round(iaqi)
    }
  }
  
  // 超过最高限值
  return 500
}

/**
 * 计算 AQI 和首要污染物
 * @param {Object} data - 污染物浓度数据 { pm25, pm10, so2, no2, co, o3 }
 * @returns {Object} { aqi, primaryPollutant, levels: {各污染物的等级} }
 */
export function calculateAQI(data) {
  const pollutants = ['pm25', 'pm10', 'so2', 'no2', 'co', 'o3']
  const iaqiValues = {}
  let maxIAQI = 0
  let primaryPollutant = []
  
  // 计算各污染物的 IAQI
  pollutants.forEach(p => {
    if (data[p] !== null && data[p] !== undefined && data[p] !== '') {
      const iaqi = calculateIAQI(data[p], p)
      iaqiValues[p] = iaqi
      
      if (iaqi > maxIAQI) {
        maxIAQI = iaqi
        primaryPollutant = [p]
      } else if (iaqi === maxIAQI && iaqi > 0) {
        primaryPollutant.push(p)
      }
    }
  })
  
  // 计算各污染物等级
  const levels = {}
  pollutants.forEach(p => {
    if (iaqiValues[p] !== undefined) {
      levels[p] = getAQILevel(iaqiValues[p])
    }
  })
  
  return {
    aqi: maxIAQI,
    aqiLevel: getAQILevel(maxIAQI),
    primaryPollutant: primaryPollutant.map(p => POLLUTANT_NAMES[p]).join('、'),
    iaqiValues,
    levels
  }
}

/**
 * 根据 AQI 值获取等级信息
 * @param {Number} aqi - AQI 值
 * @returns {Object} { level, color, index }
 */
export function getAQILevel(aqi) {
  if (aqi === null || aqi === undefined || isNaN(aqi) || aqi < 0) {
    return { level: '未知', color: '#999', index: -1 }
  }
  
  for (let i = 0; i < AQI_LEVELS.length; i++) {
    if (aqi <= AQI_LEVELS[i].aqiMax) {
      return {
        level: AQI_LEVELS[i].level,
        color: AQI_LEVELS[i].color,
        index: i
      }
    }
  }
  
  return {
    level: '严重污染',
    color: '#7e0023',
    index: 5
  }
}

/**
 * 获取污染物中文名
 */
export function getPollutantName(key) {
  return POLLUTANT_NAMES[key] || key
}

/**
 * 获取污染物单位
 */
export function getPollutantUnit(key) {
  return POLLUTANT_UNITS[key] || ''
}

export default {
  calculateAQI,
  getAQILevel,
  getPollutantName,
  getPollutantUnit
}
