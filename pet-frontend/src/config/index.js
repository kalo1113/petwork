// src/config/index.js
// 统一管理后端基础地址，只需改这里！
export const BASE_URL = 'http://localhost:8080'

// 可扩展其他全局配置（如头像映射路径、接口前缀等）
export const AVATAR_PATH = '/user-img/' // 头像映射路径（和后端一致）
export const PET_PHOTO_PATH = '/pet-img/' // 宠物图片映射路径（和后端WebMvcConfig一致）
export const PRODUCT_IMG_PATH = '/product-img/' // 产品图片路径（新增，和后端对应）
export const INSURANCE_IMG_PATH = '/insurance-img/' // 保险图片路径（核心新增，匹配后端）

/**
 * 通用图片路径拼接方法（推荐封装，避免重复代码）
 * @param {string} imgPath 后端返回的图片路径（如 /insurance-img/xxx.jpg）
 * @returns {string} 完整的图片访问URL
 */
export const getImageUrl = (imgPath) => {
  // 边界处理：如果没有图片路径，返回默认占位图（可选，根据你的需求调整）
  if (!imgPath) return `${BASE_URL}/images/default.png`
  // 如果已经是完整URL（比如CDN地址），直接返回
  if (imgPath.startsWith('http')) return imgPath
  // 核心：拼接基础地址 + 后端返回的原始路径（无需加额外前缀）
  return `${BASE_URL}${imgPath}`
}