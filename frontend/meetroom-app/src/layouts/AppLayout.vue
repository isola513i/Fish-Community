<script setup>
import { RouterView } from 'vue-router';
import BottomNav from '../components/BottomNav.vue';
import { Toaster } from 'vue-sonner';
</script>

<template>
  <div class="flex flex-col h-scree">
    <main class="grow overflow-y-auto pb-16">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <BottomNav />
    <Toaster position="top-center" rich-colors />
  </div>
</template>
