// vue.config.js 完整配置
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  // 基础路径（部署时根据实际域名调整）
  publicPath: '/',
  // 输出目录
  outputDir: 'dist',
  // 静态资源目录
  assetsDir: 'static',
  // 关闭生产环境sourcemap
  productionSourceMap: false,

  // ========== 开发服务器配置 ==========
  devServer: {
    port: 8081, // 前端端口
    open: true, // 启动后自动打开浏览器
    overlay: {
      // 编译错误/警告显示在页面上
      warnings: false,
      errors: true
    },
    proxy: {
      // 宠物业务接口代理
      '/pet': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true, // 开启跨域
        pathRewrite: { '^/pet': '/pet' }, // 路径无需重写
        // 超时配置
        timeout: 10000,
        // 支持https
        secure: false
      },
      // 宠物图片资源代理
      '/pet-images': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/pet-images': '/pet-images' },
        // 图片请求支持缓存
        headers: {
          'Cache-Control': 'max-age=86400'
        }
      },
      // 用户相关接口代理
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/user': '/user' }
      },
      // 保险相关接口代理（补充，若有）
      '/insurance': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/insurance': '/insurance' }
      }
    }
  },

  // ========== 编译配置（解决Element Plus/Vuetify 兼容） ==========
  chainWebpack: config => {
    // 1. 忽略Element Plus组件的Vue解析（关键！）
    config.module
      .rule('vue')
      .use('vue-loader')
      .tap(options => {
        options.compilerOptions = {
          ...(options.compilerOptions || {}),
          // 声明el-前缀为自定义元素，交给Element Plus处理
          isCustomElement: tag => tag.startsWith('el-')
        }
        return options
      })

    // 2. 优化图片加载（适配宠物图片上传/预览）
    config.module
      .rule('images')
      .test(/\.(png|jpe?g|gif|webp)(\?.*)?$/)
      .use('url-loader')
      .loader('url-loader')
      .tap(options => {
        options.limit = 4096 // 小于4kb的图片转base64
        options.fallback = {
          loader: 'file-loader',
          options: {
            name: 'static/images/[name].[hash:8].[ext]'
          }
        }
        return options
      })

    // 3. 提升Element Plus样式优先级
    config.plugin('html').tap(args => {
      args[0].title = '宠物保障系统' // 自定义页面标题
      return args
    })
  },

  // ========== 样式配置 ==========
  css: {
    // 开启CSS模块化
    requireModuleExtension: true,
    // 提取CSS（生产环境）
    extract: process.env.NODE_ENV === 'production',
    // CSS sourceMap
    sourceMap: false,
    // 预处理器配置（若使用less/sass）
    loaderOptions: {
      css: {
        // 给css-loader传递参数
        modules: {
          localIdentName: '[name]-[hash]'
        },
        localsConvention: 'camelCaseOnly'
      }
    }
  }
})
