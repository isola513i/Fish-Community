<script setup>
import { ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink } from "vue-router";
import {
	UserIcon,
	EnvelopeIcon,
	LockClosedIcon,
} from "@heroicons/vue/24/outline";

const authStore = useAuthStore();
const fullName = ref("");
const email = ref("");
const password = ref("");
const errorMessage = ref(null);

const handleRegister = async () => {
	errorMessage.value = null;
	try {
		await authStore.register(fullName.value, email.value, password.value);
		// authStore จะ redirect ไปหน้า Login เอง
	} catch (error) {
		errorMessage.value = error.response?.data?.message || "การลงทะเบียนล้มเหลว";
		console.error(error);
	}
};
</script>

<template>
	<div>
		<h1 class="text-3xl font-bold text-gray-900">Sign Up</h1>

		<div
			v-if="errorMessage"
			class="p-3 mt-4 text-sm text-red-700 bg-red-100 rounded-md"
		>
			{{ errorMessage }}
		</div>

		<form @submit.prevent="handleRegister" class="mt-8 space-y-6">
			<div>
				<label for="fullName" class="block text-sm font-medium text-gray-700"
					>Full Name</label
				>
				<div class="relative mt-1">
					<input
						v-model="fullName"
						type="text"
						id="fullName"
						required
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="Your Name"
					/>
					<UserIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
					/>
				</div>
			</div>

			<div>
				<label for="email" class="block text-sm font-medium text-gray-700"
					>Email</label
				>
				<div class="relative mt-1">
					<input
						v-model="email"
						type="email"
						id="email"
						required
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="demo@email.com"
					/>
					<EnvelopeIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
					/>
				</div>
			</div>

			<div>
				<label for="password" class="block text-sm font-medium text-gray-700"
					>Password</label
				>
				<div class="relative mt-1">
					<input
						v-model="password"
						type="password"
						id="password"
						required
						minlength="8"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="min 8 characters"
					/>
					<LockClosedIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
					/>
				</div>
			</div>

			<button
				type="submit"
				class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
			>
				Create Account
			</button>

			<p class="text-sm text-center text-gray-600">
				Already have an account?
				<RouterLink
					:to="{ name: 'Login' }"
					class="font-medium text-blue-600 hover:underline"
				>
					Sign in
				</RouterLink>
			</p>
		</form>
	</div>
</template>
