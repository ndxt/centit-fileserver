<template>
  <Dropdown class="user-drop-menu" transfer>
    <Icon class="app-icon" type="md-settings" :size="26" />
    <DropdownMenu slot="list">
      <DropdownItem>个人设置</DropdownItem>
      <DropdownItem>我的收藏</DropdownItem>
      <DropdownItem divided @click.native="logout">退出登录</DropdownItem>
    </DropdownMenu>
  </Dropdown>
</template>

<script>
import { logout } from '@centit/api-auth'

export default {
  name: 'UserDropMenu',

  methods: {
    logout () {
      if (window.$useCasLogin) {
        const ctx = window.$contextPath || ''
        window.location.href = ctx + 'api/admin/system/mainframe/logout'
        return
      }
      logout()
      this.$router.replace({
        name: 'Login',
      })
    },
  },
}
</script>

<style scoped>
.user-drop-menu {
  height: 28px;
  line-height: 28px;
  margin: 0 8px;
  color: #ccc;
}
</style>
