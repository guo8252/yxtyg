<template>
  <div id="app">
    <el-container style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside width="200px" style="background-color: #304156" overflow-y="hidden">
        <div class="logo">
          <span>一线体验官</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >

          <!-- 智能分单 -->
          <el-submenu index="smart">
            <template slot="title">
              <i class="el-icon-magic-stick"></i>
              <span>智能分单</span>
            </template>
            <el-menu-item index="/smart-recommend">
              <i class="el-icon-s-custom"></i>
              <span>智能推荐</span>
            </el-menu-item>
            <el-menu-item index="/vector-search">
              <i class="el-icon-search"></i>
              <span>向量搜索</span>
            </el-menu-item>
          </el-submenu>

          <!-- 数据看板 -->
          <el-submenu index="dashboard">
            <template slot="title">
              <i class="el-icon-data-board"></i>
              <span>数据看板</span>
            </template>
            <el-menu-item index="/dashboard">
              <i class="el-icon-s-marketing"></i>
              <span>月度看板</span>
            </el-menu-item>
          </el-submenu>

          <!-- 数据管理 -->
          <el-submenu index="data">
            <template slot="title">
              <i class="el-icon-s-management"></i>
              <span>数据管理</span>
            </template>
            <el-menu-item index="/workorder">
              <i class="el-icon-document"></i>
              <span>需求提单</span>
            </el-menu-item>
            <el-menu-item index="/review">
              <i class="el-icon-edit-outline"></i>
              <span>评审统计</span>
            </el-menu-item>
            <el-menu-item index="/training">
              <i class="el-icon-s-cooperation"></i>
              <span>转培上报</span>
            </el-menu-item>
            <el-menu-item index="/experimenter">
              <i class="el-icon-user"></i>
              <span>体验官管理</span>
            </el-menu-item>
          </el-submenu>

          <!-- 需求管理 -->
          <el-menu-item index="/requirement" v-if="isDevOrAdmin">
            <i class="el-icon-document"></i>
            <span slot="title">需求管理</span>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-menu-item index="/user" v-if="isSysAdmin">
            <i class="el-icon-user-solid"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>

          <!-- 模型设置 -->
          <el-submenu index="model">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>模型设置</span>
            </template>
            <el-menu-item index="/model-config">
              <i class="el-icon-connection"></i>
              <span>大模型设置</span>
            </el-menu-item>
            <el-menu-item index="/embedding-config">
              <i class="el-icon-share"></i>
              <span>向量化模型</span>
            </el-menu-item>
            <el-menu-item index="/agent-config">
              <i class="el-icon-cpu"></i>
              <span>智能体参数</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header style="background-color: #fff; display: flex; align-items: center; justify-content: flex-end; box-shadow: 0 1px 4px rgba(0,0,0,.08); z-index: 1">
          <el-dropdown v-if="currentUser.username" @command="handleCommand">
            <span class="el-dropdown-link" style="cursor: pointer">
              {{ currentUser.realName || currentUser.username }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-header>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      const path = this.$route.path
      if (path.startsWith('/requirement/form')) {
        return '/requirement'
      }
      return path
    },
    currentUser() {
      return this.$store.state.user.userInfo
    },
    isSysAdmin() {
      return this.currentUser.role === 'SYS_ADMIN'
    },
    isDevOrAdmin() {
      const role = this.currentUser.role
      return role === 'DEV_ADMIN' || role === 'SYS_ADMIN' || role === 'PRODUCT_MANAGER'
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('user/logout')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.el-menu {
  border-right: none !important;
}

.el-menu-item {
  text-align: left;
}

.el-menu-item i {
  margin-right: 10px;
}

.el-submenu__title i {
  margin-right: 10px;
}

.el-submenu .el-menu-item {
  padding-left: 50px !important;
  min-width: auto;
}
</style>
