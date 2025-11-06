<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { storeToRefs } from 'pinia';
import { RouterLink } from 'vue-router';
import { ArrowLeftIcon } from '@heroicons/vue/24/outline';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

const editFullName = ref('');
const successMessage = ref(null);
const errorMessage = ref(null);

onMounted(() => {
  if (user.value) {
    editFullName.value = user.value.fullName;
  }
});

const handleUpdateProfile = async () => {
  clearMessages();
  try {
    await authStore.updateProfile(editFullName.value);
    successMessage.value = 'อัปเดตชื่อสำเร็จ!';
    setTimeout(() => {
      successMessage.value = null;
    }, 3000);
  } catch (error) {
    errorMessage.value = 'อัปเดตล้มเหลว';
  }
};

const clearMessages = () => {
  successMessage.value = null;
  errorMessage.value = null;
};
</script>

<template>
  <div class="p-4 space-y-6">
    <div class="relative flex items-center justify-center">
      <RouterLink
        to="/profile"
        class="absolute left-0 p-2 rounded-full bg-soft-bg shadow-neumorphism hover:shadow-neumorphism-inset"
      >
        <ArrowLeftIcon class="w-6 h-6 text-gray-700" />
      </RouterLink>
      <h1 class="text-xl font-bold text-gray-900">Edit Profile</h1>
    </div>

    <div
      v-if="successMessage"
      class="p-3 text-sm text-green-700 bg-green-100 rounded-md"
    >
      {{ successMessage }}
    </div>
    <div
      v-if="errorMessage"
      class="p-3 text-sm text-red-700 bg-red-100 rounded-md"
    >
      {{ errorMessage }}
    </div>

    <div class="p-6 bg-soft-bg rounded-2xl shadow-neumorphism">
      <form @submit.prevent="handleUpdateProfile" class="space-y-4">
        <div>
          <label for="email" class="block text-sm font-medium text-gray-500"
            >Email (แก้ไขไม่ได้)</label
          >
          <input
            :value="user?.email"
            type="email"
            id="email"
            disabled
            class="w-full px-3 py-2 mt-1 bg-gray-200 border border-gray-300 rounded-md shadow-sm"
          />
        </div>
        <div>
          <label for="fullName" class="block text-sm font-medium text-gray-700"
            >Full Name</label
          >
          <input
            v-model="editFullName"
            type="text"
            id="fullName"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <button
          type="submit"
          class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
        >
          Save Name
        </button>
      </form>
    </div>
  </div>
</template>
