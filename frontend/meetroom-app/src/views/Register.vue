<script setup>
import { ref, computed } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink } from "vue-router";
import {
	UserIcon,
	EnvelopeIcon,
	LockClosedIcon,
} from "@heroicons/vue/24/outline";
// 1. ⬇️ Import ฟังก์ชันใหม่
import { useFormValidation } from "../composables/useFormValidation";

const authStore = useAuthStore();
// 2. ⬇️ ใช้งานฟังก์ชันใหม่
const {
	validateEmail,
	validatePassword,
	validateRequired,
	checkPasswordStrength,
} = useFormValidation();

// (State)
const fullName = ref("");
const email = ref("");
const password = ref("");
const errorMessage = ref(null);
const isLoading = ref(false);

// (Validation State)
const fullNameTouched = ref(false);
const emailTouched = ref(false);
const passwordTouched = ref(false);

// (Computed Errors)
const fullNameError = computed(() =>
	validateRequired(fullName.value, "Full Name", fullNameTouched.value)
);
const emailError = computed(() =>
	validateEmail(email.value, emailTouched.value)
);
const passwordError = computed(() =>
	// (ใช้ validatePassword ที่เข้มงวดขึ้น)
	validatePassword(password.value, passwordTouched.value, 8)
);
const isFormValid = computed(() => {
	return (
		fullNameError.value === "" &&
		emailError.value === "" &&
		passwordError.value === ""
	);
});

// 3. ⬇️ NEW: Computed property สำหรับ Strength Meter ⬇️
const passwordStrength = computed(() => {
	// แสดงผลเมื่อผู้ใช้เริ่มพิมพ์ (หรือ blur)
	if (!passwordTouched.value && password.value.length === 0) {
		return { score: 0, label: "", color: "" };
	}
	return checkPasswordStrength(password.value);
});
// --------------------------------------------------

// (handleRegister เหมือนเดิม)
const handleRegister = async () => {
	errorMessage.value = null;
	fullNameTouched.value = true;
	emailTouched.value = true;
	passwordTouched.value = true;

	if (!isFormValid.value) return;

	isLoading.value = true;
	try {
		await authStore.register(fullName.value, email.value, password.value);
	} catch (error) {
		errorMessage.value = error.response?.data?.message || "การลงทะเบียนล้มเหลว";
		console.error(error);
	} finally {
		isLoading.value = false;
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

		<form @submit.prevent="handleRegister" class="mt-8 space-y-6" novalidate>
			<div>
				<label for="fullName" class="block text-sm font-medium text-gray-700"
					>Full Name</label
				>
				<div class="relative mt-1">
					<input
						v-model="fullName"
						type="text"
						id="fullName"
						@blur="fullNameTouched.value = true"
						:class="{ 'border-red-500': fullNameError }"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="e.g., John Doe"
					/>
					<UserIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
						:class="[fullNameError ? 'text-red-500' : 'text-gray-400']"
					/>
				</div>
				<div v-if="fullNameError" class="text-red-600 text-sm mt-1">
					{{ fullNameError }}
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
						@blur="emailTouched.value = true"
						:class="{ 'border-red-500': emailError }"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="e.g., john.doe@mail.com"
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
						type="password"
						id="password"
						@blur="passwordTouched.value = true"
						:class="{ 'border-red-500': passwordError && passwordTouched }"
						minlength="8"
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="Enter your password"
					/>
					<LockClosedIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
						:class="[
							passwordError && passwordTouched
								? 'text-red-500'
								: 'text-gray-400',
						]"
					/>
				</div>

				<div v-if="passwordTouched || password.length > 0" class="mt-2">
					<div class="w-full bg-gray-200 rounded-full h-1.5">
						<div
							class="h-1.5 rounded-full transition-all duration-300"
							:class="passwordStrength.color"
							:style="{ width: (passwordStrength.score / 5) * 100 + '%' }"
						></div>
					</div>
					<div class="flex justify-between items-center mt-1">
						<span
							class="text-xs font-medium"
							:class="[
								passwordStrength.score <= 2
									? 'text-red-500'
									: passwordStrength.score <= 4
									? 'text-yellow-600'
									: 'text-green-600',
							]"
						>
							{{ passwordStrength.label }}
						</span>
					</div>
				</div>

				<div
					v-if="passwordError && passwordTouched"
					class="text-red-600 text-sm mt-1"
				>
					{{ passwordError }}
				</div>
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
				<span>{{ isLoading ? "Creating Account..." : "Create Account" }}</span>
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
