<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request, resolveAssetUrl, upload } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { AiChefResult, AiConversation } from '@/types'

const user=useUserStore(),cart=useCartStore()
const ingredients=ref(''),preferences=ref('2人份，少油少盐，30分钟内'),imagePath=ref('')
const conversationId=ref(''),loading=ref(false),result=ref<AiChefResult>(),mode=ref<'order'|'cook'>('order')
const addingId=ref<string>(),stage=ref('等待你的食材或需求')

onLoad(async()=>{
  if(!user.isLoggedIn)await user.login()
  const conversation=await request<AiConversation>({url:'/user/ai/conversations',method:'POST'})
  conversationId.value=conversation.id
})

async function chooseImage(){
  const chosen=await uni.chooseImage({count:1,sizeType:['compressed'],sourceType:['album','camera']})
  const file=chosen.tempFiles[0]
  if(file.size>5*1024*1024){uni.showToast({title:'图片不能超过5MB',icon:'none'});return}
  imagePath.value=chosen.tempFilePaths[0]
}

async function analyze(){
  if(!ingredients.value.trim()&&!imagePath.value){uni.showToast({title:'请描述食材或上传照片',icon:'none'});return}
  if(loading.value)return
  loading.value=true;result.value=undefined;stage.value='正在识别食材并检索菜谱…'
  try{
    const data={conversationId:conversationId.value,ingredients:ingredients.value.trim(),preferences:preferences.value.trim()}
    result.value=imagePath.value
      ? await upload<AiChefResult>('/user/ai/analyze',imagePath.value,data)
      : await request<AiChefResult>({url:'/user/ai/analyze/text',method:'POST',data})
    stage.value=`已生成${result.value.recipes?.length||0}道菜谱和${result.value.dishRecommendations?.length||0}个在售推荐`
  }finally{loading.value=false}
}

async function addDish(dishId:string){
  addingId.value=dishId
  try{await cart.add(dishId);uni.showToast({title:'已加入购物车'})}finally{addingId.value=undefined}
}
</script>

<template>
  <view class="chef-page">
    <view class="intro"><text class="badge">CLOUD MEAL AI</text><text class="title">今天怎么吃，交给AI私厨</text><text class="sub">拍食材、说偏好；既能教你做，也能从云膳直接点。</text></view>
    <view class="panel input-panel">
      <textarea v-model="ingredients" maxlength="2000" placeholder="例如：鸡胸肉、西兰花，想吃低脂高蛋白…" />
      <input v-model="preferences" maxlength="1000" placeholder="人数、口味、忌口、时间" />
      <view v-if="imagePath" class="preview"><image :src="imagePath" mode="aspectFill"/><text @click="imagePath=''">×</text></view>
      <view class="input-actions"><button class="photo" @click="chooseImage">📷 拍摄食材</button><button class="send" :loading="loading" :disabled="loading" @click="analyze">{{loading?'分析中':'开始分析'}}</button></view>
      <text class="stage">{{stage}}</text>
    </view>

    <template v-if="result">
      <view class="ingredients panel"><text class="section-title">识别到的食材</text><view class="chips"><text v-for="item in result.ingredients" :key="item.name">{{item.name}} · {{item.freshness}}</text></view></view>
      <view class="switch"><text :class="{active:mode==='order'}" @click="mode='order'">我想直接吃</text><text :class="{active:mode==='cook'}" @click="mode='cook'">我想自己做</text></view>
      <view v-if="mode==='order'" class="recommendations">
        <view v-if="!result.dishRecommendations?.length" class="panel empty">暂时没有匹配的在售菜品，可以查看AI做法。</view>
        <view v-for="dish in result.dishRecommendations" :key="dish.dishId" class="dish panel">
          <image v-if="dish.image" :src="resolveAssetUrl(dish.image)" mode="aspectFill"/><view v-else class="dish-placeholder">膳</view>
          <view class="dish-main"><view><text class="dish-name">{{dish.dishName}}</text><text class="score">匹配{{dish.matchScore}}%</text></view><text class="reason">{{dish.reason}}</text><view class="dish-bottom"><text>¥{{dish.price}}</text><button :loading="addingId===dish.dishId" @click="addDish(dish.dishId)">加入购物车</button></view></view>
        </view>
      </view>
      <view v-else class="recipes">
        <view v-for="recipe in result.recipes" :key="recipe.name" class="recipe panel">
          <view class="recipe-head"><text>{{recipe.name}}</text><strong>{{recipe.totalScore}}分</strong></view><text class="recipe-desc">{{recipe.description}}</text>
          <view class="metrics"><text>营养 {{recipe.nutritionScore}}</text><text>易做 {{recipe.difficultyScore}}</text><text>{{recipe.cookingTime}}</text></view>
          <view class="steps"><text class="step-title">准备：{{recipe.ingredients?.join('、')}}</text><text v-for="(step,index) in recipe.steps" :key="index">{{index+1}}. {{step}}</text></view>
        </view>
      </view>
      <view class="safety panel">食品安全提示：{{result.safetyNote}}</view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.chef-page{min-height:100vh;padding:28rpx 26rpx 70rpx;background:#f2f5f2}.intro{background:linear-gradient(145deg,#123c2f,#245e49);padding:40rpx 30rpx;border-radius:30rpx;color:#fff}.badge{font-size:19rpx;letter-spacing:5rpx;color:#edc866}.title,.sub{display:block}.title{font-size:40rpx;font-weight:800;margin:18rpx 0 12rpx}.sub{font-size:24rpx;color:#c5d8d0;line-height:1.6}.panel{background:#fff;border-radius:26rpx;padding:26rpx;margin-top:22rpx;box-shadow:0 8rpx 24rpx rgba(24,54,43,.05)}.input-panel textarea{width:100%;height:150rpx;box-sizing:border-box;background:#f5f7f5;border-radius:20rpx;padding:22rpx}.input-panel input{margin-top:18rpx;background:#f5f7f5;border-radius:18rpx;padding:20rpx}.input-actions{display:flex;gap:16rpx;margin-top:20rpx}.input-actions button{flex:1;margin:0;border-radius:22rpx;font-size:25rpx}.photo{background:#edf3ef;color:#174b39}.send{background:#123c2f;color:#fff}.stage{display:block;text-align:center;color:#84918b;font-size:21rpx;margin-top:18rpx}.preview{position:relative;width:180rpx;height:130rpx;margin-top:18rpx}.preview image{width:100%;height:100%;border-radius:18rpx}.preview text{position:absolute;right:-12rpx;top:-12rpx;width:38rpx;height:38rpx;border-radius:50%;background:#17211d;color:#fff;text-align:center}.section-title{font-size:28rpx;font-weight:700}.chips{display:flex;flex-wrap:wrap;gap:12rpx;margin-top:18rpx}.chips text{background:#edf6f0;color:#216044;border-radius:24rpx;padding:12rpx 18rpx;font-size:22rpx}.switch{display:flex;background:#e5ebe7;border-radius:22rpx;padding:8rpx;margin-top:24rpx}.switch text{flex:1;text-align:center;padding:18rpx;border-radius:17rpx;color:#68756f}.switch .active{background:#123c2f;color:#fff;font-weight:700}.dish{display:flex}.dish image,.dish-placeholder{width:150rpx;height:150rpx;border-radius:20rpx;flex-shrink:0}.dish-placeholder{background:#e4ece7;display:grid;place-items:center;color:#315c4c}.dish-main{flex:1;padding-left:20rpx;min-width:0}.dish-main>view:first-child{display:flex;justify-content:space-between}.dish-name{font-weight:700}.score{color:#d29b22;font-size:22rpx}.reason{display:block;color:#7d8983;font-size:22rpx;margin-top:14rpx}.dish-bottom{display:flex;align-items:center;justify-content:space-between;margin-top:18rpx;color:#d64d37;font-weight:700}.dish-bottom button{margin:0;background:#123c2f;color:#fff;font-size:22rpx;height:58rpx;line-height:58rpx;border-radius:30rpx;padding:0 24rpx}.recipe-head{display:flex;justify-content:space-between;font-size:31rpx;font-weight:700}.recipe-head strong{color:#d39d25}.recipe-desc{display:block;color:#75817b;line-height:1.6;margin:14rpx 0}.metrics{display:flex;gap:12rpx}.metrics text{font-size:21rpx;background:#f1f5f2;padding:9rpx 13rpx;border-radius:18rpx}.steps{margin-top:20rpx;border-top:1rpx solid #edf0ee;padding-top:16rpx}.steps text{display:block;font-size:24rpx;line-height:1.7;color:#47544e}.step-title{font-weight:700!important;color:#22332b!important;margin-bottom:8rpx}.safety{font-size:22rpx;color:#856c32;background:#fffaf0}.empty{text-align:center;color:#84918b}
</style>
