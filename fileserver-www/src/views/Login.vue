<template>
  <div class="Login">
    <LoginForm class="LoginForm" />
  </div>
</template>

<script>
  import LoginForm from '@centit/module-core/src/components/LoginForm'

  export default {
  name: 'Login',
  components: {
    LoginForm,
  },
}
</script>

<style scoped lang="less">
.Login {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url(../assets/login.png) center center;
  background-size: cover;
}
</style>
