<script setup>
import { useAuthStore } from '../stores/authStore';
import { storeToRefs } from 'pinia';
import { RouterLink } from 'vue-router';
import {
  ArrowRightOnRectangleIcon,
  UserCircleIcon,
  KeyIcon,
} from '@heroicons/vue/24/outline';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

const handleLogout = () => {
  authStore.logout();
};
</script>

<template>
  <div class="p-4 space-y-6">
    <h1 class="text-3xl font-bold text-gray-900">Profile</h1>

    <div class="p-6 rounded-2xl bg-soft-bg shadow-neumorphism">
      <img
        src="https://via.placeholder.com/100"
        alt="Profile Picture"
        class="w-24 h-24 mx-auto rounded-full border-4 border-soft-bg shadow-neumorphism-inset"
      />
      <div class="mt-4 text-center">
        <h2 class="text-xl font-semibold text-gray-800">
          {{ user?.fullName }}
        </h2>
        <p class="text-sm text-gray-600">{{ user?.email }}</p>
      </div>
    </div>

    <div class="p-4 space-y-2 bg-soft-bg rounded-2xl shadow-neumorphism">
      <RouterLink
        to="/profile/edit"
        class="flex items-center p-4 text-gray-700 rounded-lg hover:bg-white"
      >
        <UserCircleIcon class="w-6 h-6 mr-3" />
        <span>Edit Profile</span>
      </RouterLink>

      <RouterLink
        to="/profile/change-password"
        class="flex items-center p-4 text-gray-700 rounded-lg hover:bg-white"
      >
        <KeyIcon class="w-6 h-6 mr-3" />
        <span>Change Password</span>
      </RouterLink>
    </div>

    <div class="pt-4">
      <button
        @click="handleLogout"
        class="flex items-center justify-center w-full p-4 font-medium text-red-600 bg-soft-bg rounded-2xl shadow-neumorphism hover:shadow-neumorphism-inset"
      >
        <ArrowRightOnRectangleIcon class="w-5 h-5 mr-2" />
        Log out
      </button>
    </div>
  </div>
</template>
