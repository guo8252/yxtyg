import { login as loginApi, getUserInfo } from '@/api/auth'

const state = {
  token: localStorage.getItem('yxtyg_token') || '',
  userInfo: JSON.parse(localStorage.getItem('yxtyg_user') || '{}')
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('yxtyg_token', token)
  },
  SET_USER_INFO(state, info) {
    state.userInfo = info
    localStorage.setItem('yxtyg_user', JSON.stringify(info))
  },
  CLEAR(state) {
    state.token = ''
    state.userInfo = {}
    localStorage.removeItem('yxtyg_token')
    localStorage.removeItem('yxtyg_user')
  }
}

const actions = {
  login({ commit }, { username, password }) {
    return loginApi({ username, password }).then(res => {
      const { token, user } = res.data
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', user)
      return res
    })
  },
  fetchUserInfo({ commit }) {
    return getUserInfo().then(res => {
      commit('SET_USER_INFO', res.data)
      return res
    })
  },
  logout({ commit }) {
    commit('CLEAR')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
