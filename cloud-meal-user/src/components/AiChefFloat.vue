<script setup lang="ts">
function openChef(){uni.navigateTo({url:'/pages/ai-chef/index'})}
</script>
<template><view class="ai-float" @click="openChef"><view class="pulse"></view><text class="chef">👨‍🍳</text><text class="label">AI私厨</text></view></template>
<style scoped lang="scss">
.ai-float{position:fixed;right:24rpx;bottom:230rpx;width:116rpx;height:116rpx;border-radius:50%;background:linear-gradient(145deg,#174c3b,#0c2f24);box-shadow:0 14rpx 34rpx rgba(14,55,42,.35);z-index:25;display:flex;flex-direction:column;align-items:center;justify-content:center;border:4rpx solid rgba(229,189,90,.75)}.pulse{position:absolute;inset:-8rpx;border:3rpx solid rgba(229,189,90,.45);border-radius:50%;animation:pulse 2s infinite}.chef{font-size:42rpx;line-height:44rpx}.label{font-size:19rpx;color:#f7d477;font-weight:700;margin-top:4rpx}@keyframes pulse{0%{transform:scale(.92);opacity:.8}70%,100%{transform:scale(1.18);opacity:0}}
</style>
