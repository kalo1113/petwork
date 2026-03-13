const { defineConfig } = require('@vue/cli-service')
const path = require('path')

// 仅保留端口动态配置，其余完全不变
const PORT = process.env.VUE_APP_PORT || 8081

module.exports = defineConfig({
  publicPath: '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false,
  lintOnSave: process.env.NODE_ENV === 'development',

  devServer: {
    port: PORT, // 动态端口（8081/8082）
    open: true, // 启动后自动打开浏览器
    client: {
      overlay: { warnings: true, errors: true },
      logging: 'verbose'
    },
    proxy: {
      '/pet': { target: 'http://localhost:8080', changeOrigin: true, pathRewrite: { '^/pet': '/pet' }, timeout: 10000, secure: false },
      '/pet-images': { target: 'http://localhost:8080', changeOrigin: true, pathRewrite: { '^/pet-images': '/pet-images' }, headers: { 'Cache-Control': 'max-age=86400' } },
      '/user': { target: 'http://localhost:8080', changeOrigin: true, pathRewrite: { '^/user': '/user' } },
      '/insurance': { target: 'http://localhost:8080', changeOrigin: true, pathRewrite: { '^/insurance': '/insurance' } },
      '/claim-img': { target: 'http://localhost:8080', changeOrigin: true, pathRewrite: { '^/claim-img': '/claim-img' }, timeout: 10000, secure: false },
    }
  },

  // 其余 chainWebpack/configureWebpack/css 配置完全不变（复制你原来的）
  chainWebpack: config => {
    config.module
      .rule('images')
      .test(/\.(png|jpe?g|gif|webp)(\?.*)?$/)
      .set('type', 'asset')
      .set('parser', { dataUrlCondition: { maxSize: 4096 } })
      .set('generator', { filename: 'static/images/[name].[hash:8].[ext]' })

    config.plugin('html').tap(args => {
      args[0].title = '宠物保障系统'
      return args
    })

    config.module
      .rule('ignore-applescript')
      .test(/\.applescript$/)
      .use('null-loader')
      .loader('null-loader')
      .end()

    config.resolve.alias
      .set('stream', path.resolve(__dirname, 'node_modules/stream-browserify'))
      .set('assert', path.resolve(__dirname, 'node_modules/assert'))
  },

  configureWebpack: {
    resolve: {
      preferRelative: true,
      fallback: {
        "fs": false, "path": false, "os": false, "net": false, "tls": false, "util": false, "url": false,
        "child_process": false, "readline": false, "dgram": false, "module": false,
        "stream": require.resolve("stream-browserify"), "assert": require.resolve("assert"),
        "crypto": false, "zlib": false, "http": false, "https": false
      }
    },
    ignoreWarnings: [/Module not found: Error: Can't resolve 'dgram'/, /Module not found: Error: Can't resolve 'readline'/, /Module parse failed: Unexpected token/]
  },

  css: {
    extract: process.env.NODE_ENV === 'production',
    sourceMap: false,
    loaderOptions: {}
  }
})