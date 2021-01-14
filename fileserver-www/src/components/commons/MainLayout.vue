<template>
  <div class="MainLayout zpa-column">
    <div class="MainHeader zpa-row">
      <slot name="header"></slot>
    </div>
    <div class="zpa-row">
      <div class="MainAside">
        <slot name="menu"></slot>
      </div>
      <div class="MainContent zpa-column">
        <slot name="content"></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MainLayout',
}
</script>

<style scoped lang="less">
.MainLayout {
  .MainHeader,
  .MainAside {
    flex: none;
  }
  .MainAside {
    background: linear-gradient(#3BB59C, #3D9AC6);
  }
  .MainContent {
    box-shadow: 0 4px 10px #9997 inset;
    background: #F5F6FA;
    overflow: hidden;
  }
}
</style>
