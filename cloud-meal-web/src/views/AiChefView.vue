<script setup lang="ts">
import { onBeforeUnmount,onMounted,ref } from 'vue'
import { ChefHat,Plus,Trash2,Eraser,ImagePlus,Send,ShoppingCart } from '@lucide/vue'
import { api,http,assetUrl } from '@/api/http'
import { useCartStore } from '@/stores/cart'
import type { AiConversation,AiResult,AiTurn } from '@/types'
interface LocalTurn{userText:string;result?:AiResult;createdAt:string}
const cart=useCartStore(),conversations=ref<AiConversation[]>([]),activeId=ref(''),turns=ref<LocalTurn[]>([])
const text=ref(''),preferences=ref('2人份，少油少盐，30分钟内'),file=ref<File>(),preview=ref(''),loading=ref(false),pageLoading=ref(true),error=ref('')
function parseTurn(turn:AiTurn):LocalTurn{try{return{userText:turn.userText,result:JSON.parse(turn.responseJson) as AiResult,createdAt:turn.createdAt}}catch{return{userText:turn.userText,createdAt:turn.createdAt}}}
async function loadConversations(){conversations.value=await api<AiConversation[]>({url:'/user/ai/conversations'})}
async function selectConversation(id:string){activeId.value=id;const data=await api<AiTurn[]>({url:`/user/ai/conversations/${id}/turns`});turns.value=data.map(parseTurn);error.value=''}
async function createConversation(){const created=await api<AiConversation>({url:'/user/ai/conversations',method:'POST'});await loadConversations();await selectConversation(created.id)}
async function removeConversation(id:string){if(!confirm('确定删除该 AI 会话及其全部记忆吗？'))return;await api<void>({url:`/user/ai/conversations/${id}`,method:'DELETE'});await loadConversations();if(conversations.value[0])await selectConversation(conversations.value[0].id);else await createConversation()}
async function clearMemory(){if(!activeId.value||!confirm('确定清空当前会话记忆吗？'))return;await api<void>({url:`/user/ai/conversations/${activeId.value}/memory`,method:'DELETE'});turns.value=[]}
function pick(event:Event){const selected=(event.target as HTMLInputElement).files?.[0];if(!selected)return;if(selected.size>5*1024*1024){error.value='图片不能超过5MB';return}file.value=selected;if(preview.value)URL.revokeObjectURL(preview.value);preview.value=URL.createObjectURL(selected)}
async function analyze(){
  if(!text.value.trim()&&!file.value){error.value='请描述食材或上传照片';return}
  if(!activeId.value)await createConversation()
  loading.value=true;error.value=''
  try{
    let result:AiResult
    if(file.value){const form=new FormData();form.append('conversationId',activeId.value);form.append('ingredients',text.value);form.append('preferences',preferences.value);form.append('image',file.value);const response=await http.post('/user/ai/analyze',form);result=response.data.data}
    else result=await api<AiResult>({url:'/user/ai/analyze/text',method:'POST',data:{conversationId:activeId.value,ingredients:text.value,preferences:preferences.value}})
    turns.value.push({userText:text.value||'上传了一张食材图片',result,createdAt:new Date().toISOString()});text.value='';file.value=undefined;if(preview.value)URL.revokeObjectURL(preview.value);preview.value='';await loadConversations()
  }catch(e){error.value=(e as Error).message}finally{loading.value=false}
}
async function addDish(id:string){await cart.add(id);alert('已加入购物车')}
onMounted(async()=>{try{await loadConversations();if(conversations.value[0])await selectConversation(conversations.value[0].id);else await createConversation()}catch(e){error.value=(e as Error).message}finally{pageLoading.value=false}})
onBeforeUnmount(()=>{if(preview.value)URL.revokeObjectURL(preview.value)})
</script>
<template><section class="chef-workspace"><aside class="chef-history"><header><div><ChefHat/><span><b>AI 私厨</b><small>只属于当前账号</small></span></div><button title="新建会话" @click="createConversation"><Plus/></button></header><div v-if="pageLoading" class="history-empty">读取会话中…</div><div v-else class="history-list"><button v-for="item in conversations" :key="item.id" :class="{active:item.id===activeId}" @click="selectConversation(item.id)"><span><b>{{item.title}}</b><small>{{item.updatedAt?.replace('T',' ').slice(0,16)}}</small></span><Trash2 :size="16" @click.stop="removeConversation(item.id)"/></button></div><footer><button @click="clearMemory"><Eraser :size="17"/>清空当前记忆</button></footer></aside><div class="chef-main"><header class="chef-title"><div><span>PERSONAL AI CHEF</span><h1>今天想怎么吃？</h1><p>同一会话会记住你的连续补充，历史严格绑定当前云膳 userId。</p></div></header><div class="conversation-stream"><div v-if="!turns.length" class="chef-welcome"><ChefHat :size="48"/><h2>开始一次新的私厨对话</h2><p>告诉我现有食材、人数、忌口和时间；也可以上传食材照片。</p></div><article v-for="(turn,index) in turns" :key="`${turn.createdAt}-${index}`" class="chef-turn"><div class="user-message">{{turn.userText}}</div><div v-if="turn.result" class="chef-answer"><div class="result-tabs-static"><b>AI 推荐</b><small>{{turn.createdAt.replace('T',' ').slice(0,16)}}</small></div><div class="chef-recommendations"><article v-for="dish in turn.result.dishRecommendations" :key="dish.dishId"><img v-if="dish.image" :src="assetUrl(dish.image)"><div><b>{{dish.dishName}} · {{dish.matchScore}}%</b><p>{{dish.reason}}</p><footer><strong>¥{{dish.price}}</strong><button @click="addDish(dish.dishId)"><ShoppingCart :size="15"/>加入购物车</button></footer></div></article></div><details v-for="recipe in turn.result.recipes" :key="recipe.name"><summary>{{recipe.name}} · {{recipe.totalScore}}分 · {{recipe.cookingTime}}</summary><p>{{recipe.description}}</p><ol><li v-for="step in recipe.steps" :key="step">{{step}}</li></ol></details><p class="safety">{{turn.result.safetyNote}}</p></div></article></div><div class="chef-composer"><p v-if="error" class="form-error">{{error}}</p><img v-if="preview" :src="preview" class="upload-preview"><textarea v-model="text" maxlength="2000" placeholder="例如：鸡胸肉、西兰花，想吃低脂高蛋白…"></textarea><input v-model="preferences" maxlength="1000" placeholder="人数、口味、忌口、时间"><div><label><ImagePlus :size="18"/>上传食材<input type="file" accept="image/*" @change="pick"></label><button :disabled="loading" @click="analyze"><Send :size="17"/>{{loading?'AI分析中…':'发送给私厨'}}</button></div></div></div></section></template>
