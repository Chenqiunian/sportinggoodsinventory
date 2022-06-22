import Vue from './vue.js'
import App from './App.vue'
import SlideVerify from './index.js'
import Element from '../element-ui'
import '../element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false
Vue.use(SlideVerify)
Vue.use(Element)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  render: h => h(App)
})
