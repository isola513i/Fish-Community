<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { RouterLink } from 'vue-router';
import { ArrowLeftIcon } from '@heroicons/vue/24/outline';

const authStore = useAuthStore();

const oldPassword = ref('');
const newPassword = ref('');
const successMessage = ref(null);
const errorMessage = ref(null);

const handleChangePassword = async () => {
  clearMessages();
  try {
    await authStore.changePassword(oldPassword.value, newPassword.value);
    successMessage.value = 'เปลี่ยนรหัสผ่านสำเร็จ!';
    oldPassword.value = '';
    newPassword.value = '';
    // ตั้งเวลา 3 วินาที ให้ข้อความหายไป
    setTimeout(() => {
      successMessage.value = null;
    }, 3000);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || 'รหัสผ่านเก่าไม่ถูกต้อง';
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
      <h1 class="text-xl font-bold text-gray-900">Change Password</h1>
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
      <form @submit.prevent="handleChangePassword" class="space-y-4">
        <div>
          <label
            for="oldPassword"
            class="block text-sm font-medium text-gray-700"
            >Old Password</label
          >
          <input
            v-model="oldPassword"
            type="password"
            id="oldPassword"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <div>
          <label
            for="newPassword"
            class="block text-sm font-medium text-gray-700"
            >New Password</label
          >
          <input
            v-model="newPassword"
            type="password"
            id="newPassword"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <button
          type="submit"
          class="w-full px-4 py-3 font-medium text-white bg-orange-600 rounded-lg shadow-md hover:bg-orange-700"
        >
          Change Password
        </button>
      </form>
    </div>
  </div>
</template>
