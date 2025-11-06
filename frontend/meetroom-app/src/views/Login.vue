<script setup>
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
	EnvelopeIcon,
	LockClosedIcon,
	EyeIcon,
	EyeSlashIcon,
} from "@heroicons/vue/24/outline";
import { useFormValidation } from "../composables/useFormValidation";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const { validateEmail, validateRequired } = useFormValidation();

// (State เดิม)
const email = ref("");
const password = ref("");
const rememberMe = ref(false);
const errorMessage = ref(null);
const successMessage = ref(null);
const passwordFieldType = ref("password");

// (Validation State เดิม)
const emailTouched = ref(false);
const passwordTouched = ref(false);

// ⬇️ 1. เพิ่ม State Loading ⬇️
const isLoading = ref(false);

// (Computed Errors เดิม)
const emailError = computed(() =>
	validateEmail(email.value, emailTouched.value)
);
const passwordError = computed(() =>
	validateRequired(password.value, "Password", passwordTouched.value)
);
const isFormValid = computed(() => {
	return emailError.value === "" && passwordError.value === "";
});

// (onMounted เหมือนเดิม)
onMounted(() => {
	if (route.query.registered === "true") {
		successMessage.value = "Registration successful!";
		router.replace({ query: {} });
		setTimeout(() => (successMessage.value = null), 4000);
	}
	const rememberedEmail = localStorage.getItem("rememberedEmail");
	if (rememberedEmail) {
		email.value = rememberedEmail;
		rememberMe.value = true;
	}
});

const togglePasswordVisibility = () => {
	passwordFieldType.value =
		passwordFieldType.value === "password" ? "text" : "password";
};

// ⬇️ 2. อัปเดต handleLogin ให้ใช้ isLoading ⬇️
const handleLogin = async () => {
	errorMessage.value = null;
	successMessage.value = null;
	emailTouched.value = true;
	passwordTouched.value = true;

	if (!isFormValid.value) return;

	isLoading.value = true; // ⬅️ เริ่มโหลด
	try {
		await authStore.login(email.value, password.value);

		if (rememberMe.value) {
			localStorage.setItem("rememberedEmail", email.value);
		} else {
			localStorage.removeItem("rememberedEmail");
		}
	} catch (error) {
		errorMessage.value = "Invalid email or password. Please try again";
		console.error(error);
	} finally {
		isLoading.value = false; // ⬅️ หยุดโหลด (ไม่ว่าจะสำเร็จหรือล้มเหลว)
	}
};
</script>

<template>
	<div>
		<h1 class="text-3xl font-bold text-gray-900">Sign In</h1>

		<div
			v-if="successMessage"
			class="p-3 mt-4 text-sm text-green-700 bg-green-100 rounded-md"
		>
			{{ successMessage }}
		</div>
		<div
			v-if="errorMessage"
			class="p-3 mt-4 text-sm text-red-700 bg-red-100 rounded-md"
		>
			{{ errorMessage }}
		</div>

		<form @submit.prevent="handleLogin" class="mt-8 space-y-6" novalidate>
			<div>
				<label for="email" class="block text-sm font-medium text-gray-700"
					>Email</label
				>
				<div class="relative mt-1">
					<input
						v-model="email"
						type="email"
						id="email"
						@blur="emailTouched.value = true"
						:class="{ 'border-red-500': emailError }"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="demo@email.com"
					/>
					<EnvelopeIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
						:class="[emailError ? 'text-red-500' : 'text-gray-400']"
					/>
				</div>
				<div v-if="emailError" class="text-red-600 text-sm mt-1">
					{{ emailError }}
				</div>
			</div>

			<div>
				<label for="password" class="block text-sm font-medium text-gray-700"
					>Password</label
				>
				<div class="relative mt-1">
					<input
						v-model="password"
						:type="passwordFieldType"
						id="password"
						@blur="passwordTouched.value = true"
						:class="{ 'border-red-500': passwordError }"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="enter your password"
					/>
					<LockClosedIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
						:class="[passwordError ? 'text-red-500' : 'text-gray-400']"
					/>
					<button
						type="button"
						@click="togglePasswordVisibility"
						class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
					>
						<EyeIcon v-if="passwordFieldType === 'password'" class="w-5 h-5" />
						<EyeSlashIcon v-else class="w-5 h-5" />
					</button>
				</div>
				<div v-if="passwordError" class="text-red-600 text-sm mt-1">
					{{ passwordError }}
				</div>
			</div>

			<div class="flex items-center justify-between text-sm">
				<div class="flex items-center">
					<input
						v-model="rememberMe"
						id="remember-me"
						type="checkbox"
						class="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
					/>
					<label for="remember-me" class="ml-2 block text-gray-700"
						>Remember Me</label
					>
				</div>
				<RouterLink
					:to="{ name: 'Welcome' }"
					class="font-medium text-blue-600 hover:underline"
				>
					Forgot Password?
				</RouterLink>
			</div>

			<button
				type="submit"
				:disabled="!isFormValid || isLoading"
				class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700 flex items-center justify-center"
				:class="{
					'disabled:bg-blue-300 disabled:cursor-not-allowed':
						!isFormValid || isLoading,
				}"
			>
				<svg
					v-if="isLoading"
					class="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
				>
					<circle
						class="opacity-25"
						cx="12"
						cy="12"
						r="10"
						stroke="currentColor"
						stroke-width="4"
					></circle>
					<path
						class="opacity-75"
						fill="currentColor"
						d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
					></path>
				</svg>
				<span>{{ isLoading ? "Logging in..." : "Login" }}</span>
			</button>

			<p class="text-sm text-center text-gray-600">
				Don't have an account?
				<RouterLink
					:to="{ name: 'Register' }"
					class="font-medium text-blue-600 hover:underline"
				>
					Sign up
				</RouterLink>
			</p>
		</form>
	</div>
</template>
