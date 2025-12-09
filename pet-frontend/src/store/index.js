import { createRouter, createWebHashHistory } from 'vue-router'
import PetList from '../views/user/PetList.vue'
import PetAdd from '../views/user/PetAdd.vue'
import PetEdit from '../views/user/PetEdit.vue'
import ImageUpload from '../views/user/ImageUpload.vue'

const routes = [
  { path: '/', redirect: '/pet-list' },
  { path: '/pet-list', name: 'PetList', component: PetList },
  { path: '/pet-add', name: 'PetAdd', component: PetAdd },
  { path: '/pet-edit/:id', name: 'PetEdit', component: PetEdit },
  { path: '/image-upload', name: 'ImageUpload', component: ImageUpload }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
