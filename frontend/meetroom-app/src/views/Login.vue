<script setup>
import { ref, onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
	EnvelopeIcon,
	LockClosedIcon,
	EyeIcon,
	EyeSlashIcon,
} from "@heroicons/vue/24/outline";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const email = ref("");
const password = ref("");
const rememberMe = ref(false);
const errorMessage = ref(null);
const successMessage = ref(null);
const passwordFieldType = ref("password");

// 1. ตรวจสอบ "Registered" (จากหน้า Register)
onMounted(() => {
	if (route.query.registered === "true") {
		successMessage.value = "สมัครสมาชิกเสร็จสิ้น! กรุณาเข้าสู่ระบบ";
		router.replace({ query: {} }); // เคลียร์ query param
		setTimeout(() => (successMessage.value = null), 4000);
	}

	// 2. Logic "Remember Me" (ดึงอีเมลที่จำไว้)
	const rememberedEmail = localStorage.getItem("rememberedEmail");
	if (rememberedEmail) {
		email.value = rememberedEmail;
		rememberMe.value = true;
	}
});

// 3. Logic "Show Password"
const togglePasswordVisibility = () => {
	passwordFieldType.value =
		passwordFieldType.value === "password" ? "text" : "password";
};

// 4. Logic "Login" (อัปเดตให้จำอีเมล)
const handleLogin = async () => {
	errorMessage.value = null;
	successMessage.value = null;
	try {
		await authStore.login(email.value, password.value);

		// 5. Logic "Remember Me" (บันทึก/ลบ อีเมล)
		if (rememberMe.value) {
			localStorage.setItem("rememberedEmail", email.value);
		} else {
			localStorage.removeItem("rememberedEmail");
		}

		// (authStore จะ redirect ไปหน้า Home เอง)
	} catch (error) {
		errorMessage.value = "อีเมลหรือรหัสผ่านไม่ถูกต้อง โปรดตรวจสอบอีกครั้ง";
		console.error(error);
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

		<form @submit.prevent="handleLogin" class="mt-8 space-y-6">
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
						:type="passwordFieldType"
						id="password"
						required
						class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						placeholder="enter your password"
					/>
					<LockClosedIcon
						class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
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

				<RouterLink to="#" class="font-medium text-blue-600 hover:underline">
					Forgot Password?
				</RouterLink>
			</div>

			<button
				type="submit"
				class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
			>
				Login
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
