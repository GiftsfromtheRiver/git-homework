import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入 Vant 样式
import 'vant/lib/index.css'

// 按需引入 Vant 组件
import {
  Button,
  Cell,
  CellGroup,
  Field,
  Form,
  NavBar,
  Tab,
  Tabs,
  List,
  PullRefresh,
  Tag,
  Icon,
  Empty,
  ActionSheet,
  Radio,
  RadioGroup,
  Dialog,
  Toast,
  Loading
} from 'vant'

const app = createApp(App)

// 注册 Vant 组件
app.use(Button)
app.use(Cell)
app.use(CellGroup)
app.use(Field)
app.use(Form)
app.use(NavBar)
app.use(Tab)
app.use(Tabs)
app.use(List)
app.use(PullRefresh)
app.use(Tag)
app.use(Icon)
app.use(Empty)
app.use(ActionSheet)
app.use(Radio)
app.use(RadioGroup)
app.use(Dialog)
app.use(Toast)
app.use(Loading)

app.use(router)
app.mount('#app')
