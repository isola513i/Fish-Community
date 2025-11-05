<script setup>
import { ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink } from "vue-router";

const authStore = useAuthStore();
const fullName = ref("");
const email = ref("");
const password = ref("");
const errorMessage = ref(null);

const handleRegister = async () => {
	errorMessage.value = null;
	try {
		await authStore.register(fullName.value, email.value, password.value);
		authStore.register();
	} catch (error) {
		errorMessage.value = error.response?.data?.message || "การลงทะเบียนล้มเหลว";
		console.error(error);
	}
};
</script>

<template>
	<div class="flex items-center justify-center min-h-screen bg-gray-100">
		<div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
			<h1 class="text-3xl font-bold text-center text-gray-900">สร้างบัญชี</h1>

			<form @submit.prevent="handleRegister" class="space-y-4">
				<div>
					<label for="fullName" class="block text-sm font-medium text-gray-700"
						>Full Name</label
					>
					<input
						v-model="fullName"
						type="text"
						id="fullName"
						required
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
					/>
				</div>
				<div>
					<label for="email" class="block text-sm font-medium text-gray-700"
						>Email</label
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
						>Password</label
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
					class="w-full px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
				>
					Register
				</button>
			</form>

			<p class="text-sm text-center text-gray-600">
				มีบัญชีผู้ใช้แล้ว?
				<RouterLink
					to="/login"
					class="font-medium text-blue-600 hover:underline"
				>
					เข้าสู่ระบบ
				</RouterLink>
			</p>
		</div>
	</div>
</template>
