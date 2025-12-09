module.exports = {
  devServer: {
    port: 8081, // 前端开发服务器端口
    proxy: {
      // 宠物接口代理（文字类请求，如列表、详情、新增等）
      '/pet': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/pet': '/pet'
        }
      },
      // 图片资源代理（宠物图片访问）
      '/pet-images': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/pet-images': '/pet-images'
        }
      },
      // 用户接口代理（登录/注册/用户信息）
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/user': '/user' // 路径无需重写，直接转发
        }
      }
    }
  }
}
