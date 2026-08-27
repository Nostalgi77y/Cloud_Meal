export interface ApiResponse<T> { code: string; message: string; data: T }
export interface Category { id:string; name:string; type:number; sort:number }
export interface Dish { id:string; categoryId:string; name:string; price:number; image?:string; description?:string; stock:number }
export interface CartItem { id:string; dishId:string; dishName:string; image?:string; unitPrice:number; quantity:number }
export interface Address { id:string; consignee:string; phone:string; province?:string; city?:string; district?:string; detail:string; isDefault:number }
export interface Coupon { id:string; name:string; thresholdAmount:number; discountAmount:number; validUntil:string; status:number }
export interface UserCoupon { userCouponId:string; couponId:string; name:string; thresholdAmount:number; discountAmount:number; validUntil:string; status:string; usable:boolean }
export interface OrderDetail { id:string; name:string; unitPrice:number; quantity:number; amount:number }
export interface Order { id:string; orderNumber:string; status:string; payStatus:string; originalAmount:number; discountAmount:number; amount:number; consignee:string; address:string; createdTime:string; details:OrderDetail[] }
export interface PaymentCreate { mode:'MOCK'|'FREE'|'WECHAT'; status:'PAID'|'PENDING'; appId?:string; timeStamp?:string; nonceStr?:string; packageValue?:string; signType?:string; paySign?:string }
export interface AiIngredient { name:string; freshness:string; estimatedAmount:string; observation:string }
export interface AiRecipe { name:string; description:string; nutritionScore:number; difficultyScore:number; totalScore:number; cookingTime:string; difficulty:string; ingredients:string[]; steps:string[]; recommendation:string; imageUrl?:string; sourceUrl?:string }
export interface AiDishRecommendation { dishId:string; dishName:string; description?:string; price:number; image?:string; stock:number; matchScore:number; reason:string }
export interface AiChefResult { ingredients:AiIngredient[]; recipes:AiRecipe[]; safetyNote:string; dishRecommendations:AiDishRecommendation[] }
export interface AiConversation { id:string; title:string; createdAt:string; updatedAt:string }
