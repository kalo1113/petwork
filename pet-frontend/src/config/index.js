// src/config/index.js
// 统一管理后端基础地址，只需改这里！
export const BASE_URL = 'http://localhost:8080'

// 可扩展其他全局配置（如头像映射路径、接口前缀等）
export const API_PREFIX = '/api' // 可选，若接口有统一前缀（当前后端无/api前缀，暂时注释）
export const AVATAR_PATH = '/avatar/' // 头像映射路径（和后端一致）
export const PET_PHOTO_PATH = '/pet-images/' // 宠物图片映射路径（和后端WebMvcConfig一致）
