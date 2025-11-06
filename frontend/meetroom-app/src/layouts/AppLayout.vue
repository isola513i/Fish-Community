<script setup>
import { RouterView } from 'vue-router';
import BottomNav from '../components/BottomNav.vue';
</script>

<template>
  <div class="flex flex-col h-scree">
    <main class="grow overflow-y-auto pb-16">
      <RouterView />
    </main>

    <BottomNav />
  </div>
</template>
