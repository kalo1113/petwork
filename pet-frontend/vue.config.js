const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  // 基础构建配置（整合你两份配置的有效部分）
  publicPath: '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false,
  lintOnSave: process.env.NODE_ENV === 'development',

  // 开发服务器配置（保留你的代理逻辑，简化冗余配置）
  devServer: {
    port: 8081,
    open: true,
    client: {
      overlay: { warnings: true, errors: true },
      logging: 'verbose'
    },
    proxy: {
      '/pet': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        pathRewrite: { '^/pet': '/pet' }, 
        timeout: 10000, 
        secure: false 
      },
      '/pet-images': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        pathRewrite: { '^/pet-images': '/pet-images' }, 
        headers: { 'Cache-Control': 'max-age=86400' } 
      },
      '/user': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        pathRewrite: { '^/user': '/user' } 
      },
      '/insurance': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        pathRewrite: { '^/insurance': '/insurance' } 
      }
    }
  },

  // 编译链配置（整合图片处理 + 兼容修复）
  chainWebpack: config => {
    // 1. 图片资源处理（保留你的逻辑）
    config.module
      .rule('images')
      .test(/\.(png|jpe?g|gif|webp)(\?.*)?$/)
      .set('type', 'asset')
      .set('parser', {
        dataUrlCondition: { maxSize: 4096 }
      })
      .set('generator', {
        filename: 'static/images/[name].[hash:8].[ext]'
      })

    // 2. 自定义页面标题（保留你的逻辑）
    config.plugin('html').tap(args => {
      args[0].title = '宠物保障系统'
      return args
    })

    // 3. 排除无用的 .applescript 文件（解决 parse failed 错误）
    config.module
      .rule('ignore-applescript')
      .test(/\.applescript$/)
      .use('null-loader')
      .loader('null-loader')
      .end()

    // 4. 映射 polyfill 别名（关键：让 Webpack 找到浏览器端模块）
    config.resolve.alias
      .set('stream', path.resolve(__dirname, 'node_modules/stream-browserify'))
      .set('assert', path.resolve(__dirname, 'node_modules/assert'))
  },

  // Webpack 核心兼容配置（覆盖所有报错模块）
  configureWebpack: {
    resolve: {
      // 优先解析相对路径，解决 module 模块错误
      preferRelative: true,
      // 完整的 fallback 配置，覆盖所有缺失的 Node 模块
      fallback: {
        "fs": false,
        "path": false,
        "os": false,
        "net": false,
        "tls": false,
        "util": false,
        "url": false,
        "child_process": false,
        "readline": false,
        "dgram": false,
        "module": false,
        "stream": require.resolve("stream-browserify"), // 用 polyfill 替代
        "assert": require.resolve("assert"), // 用 polyfill 替代
        "crypto": false,
        "zlib": false,
        "http": false,
        "https": false
      }
    },
    // 忽略无影响的警告，避免编译阻断
    ignoreWarnings: [
      /Module not found: Error: Can't resolve 'dgram'/,
      /Module not found: Error: Can't resolve 'readline'/,
      /Module parse failed: Unexpected token/
    ]
  },

  // 样式配置（保留你的逻辑）
  css: {
    extract: process.env.NODE_ENV === 'production',
    sourceMap: false,
    loaderOptions: {}
  }
})