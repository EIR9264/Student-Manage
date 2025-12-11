<template>
  <router-view></router-view>
</template>

<style>
/* 重置浏览器默认样式，解决“底部白边”问题 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* 覆盖可能存在的 Element Plus 或 Vite 默认白色边距 */
body {
  background-color: #f0f2f5;
  overflow-x: hidden;
}
</style>
