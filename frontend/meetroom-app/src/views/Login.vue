<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { RouterLink, useRoute, useRouter } from 'vue-router';

const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const errorMessage = ref(null);
const successMessage = ref(null);

const route = useRoute();
const router = useRouter();

onMounted(() => {
  if (route.query.registered === 'true') {
    successMessage.value = 'สมัครสมาชิกเสร็จสิ้น';
    router.replace({ query: {} });

    setTimeout(() => {
      successMessage.value = null;
    }, 3000);
  }
});

const handleLogin = async () => {
  errorMessage.value = null;
  successMessage.value = null;
  try {
    await authStore.login(email.value, password.value);
  } catch (error) {
    errorMessage.value =
      'ขออภัย อีเมลหรือรหัสผ่านของคุณไม่ถูกต้อง โปรดตรวจสอบอีกครั้ง';
    console.error(error);
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
      <h1 class="text-3xl font-bold text-center text-gray-900">MeetRoom</h1>
      <div
        v-if="successMessage"
        class="p-3 text-sm text-green-700 bg-green-100 rounded-md"
      >
        {{ successMessage }}
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700"
            >อีเมล</label
          >
          <input
            v-model="email"
            type="email"
            id="email"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700"
            >รหัสผ่าน</label
          >
          <input
            v-model="password"
            type="password"
            id="password"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div v-if="errorMessage" class="text-sm text-red-600">
          {{ errorMessage }}
        </div>

        <button
          type="submit"
          class="w-full px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 cursor-pointer"
        >
          เข้าสู่ระบบ
        </button>
      </form>

      <p class="text-sm text-center text-gray-600">
        ไม่มีบัญชีใช่ไหม
        <RouterLink to="/register" class="font-medium text-blue-600">
          สมัครใช้งาน
        </RouterLink>
      </p>
    </div>
  </div>
</template>
