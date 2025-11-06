<script setup>
import { ref, computed } from "vue";
import { useAuthStore } from "../stores/authStore";
import { RouterLink } from "vue-router";
import {
	ArrowLeftIcon,
	LockClosedIcon,
	EyeIcon,
	EyeSlashIcon,
} from "@heroicons/vue/24/outline";
// 1. Import Validation
import { useFormValidation } from "../composables/useFormValidation";
import { Toaster, toast } from "vue-sonner";

const authStore = useAuthStore();
// 2. ใช้งาน Validation
const {
	validatePassword,
	validateRequired,
	validatePasswordMatch,
	checkPasswordStrength,
} = useFormValidation();

// --- States ---
const oldPassword = ref("");
const newPassword = ref("");
const confirmNewPassword = ref(""); // ⬅️ (เพิ่ม)
const isLoading = ref(false); // ⬅️ (เพิ่ม)
const errorMessage = ref(null); // (สำหรับ Server Error)

// (State สำหรับ Show/Hide Password)
const oldPassFieldType = ref("password");
const newPassFieldType = ref("password");
const confirmPassFieldType = ref("password");

// (State สำหรับ Touched)
const oldPasswordTouched = ref(false);
const newPasswordTouched = ref(false);
const confirmNewPasswordTouched = ref(false);

// --- Computed Errors ---
const oldPasswordError = computed(() =>
	validateRequired(oldPassword.value, "Old Password", oldPasswordTouched.value)
);
const newPasswordError = computed(() =>
	validatePassword(newPassword.value, newPasswordTouched.value)
);
const confirmNewPasswordError = computed(() =>
	validatePasswordMatch(
		newPassword.value,
		confirmNewPassword.value,
		confirmNewPasswordTouched.value
	)
);
const passwordStrength = computed(() => {
	if (!newPasswordTouched.value && newPassword.value.length === 0) {
		return { score: 0, label: "", color: "" };
	}
	return checkPasswordStrength(newPassword.value);
});

// --- Form Validity ---
const isFormValid = computed(() => {
	return (
		oldPasswordError.value === "" &&
		newPasswordError.value === "" &&
		confirmNewPasswordError.value === "" &&
		oldPassword.value.length > 0 // (เช็คเผื่อไว้)
	);
});

// --- Handlers ---
const handleChangePassword = async () => {
	errorMessage.value = null;
	// Mark all as touched
	oldPasswordTouched.value = true;
	newPasswordTouched.value = true;
	confirmNewPasswordTouched.value = true;

	if (!isFormValid.value) return;

	isLoading.value = true;
	try {
		await authStore.changePassword(oldPassword.value, newPassword.value);
		toast.success("Password changed successfully!");
		// Clear fields
		oldPassword.value = "";
		newPassword.value = "";
		confirmNewPassword.value = "";
		// Reset touched status
		oldPasswordTouched.value = false;
		newPasswordTouched.value = false;
		confirmNewPasswordTouched.value = false;
	} catch (error) {
		// (Server Error)
		errorMessage.value =
			error.response?.data?.message || "Failed to change password";
		toast.error(errorMessage.value);
	} finally {
		isLoading.value = false;
	}
};

// (Helpers สำหรับ Show/Hide Password)
const toggleOldPass = () => {
	oldPassFieldType.value =
		oldPassFieldType.value === "password" ? "text" : "password";
};
const toggleNewPass = () => {
	newPassFieldType.value =
		newPassFieldType.value === "password" ? "text" : "password";
};
const toggleConfirmPass = () => {
	confirmPassFieldType.value =
		confirmPassFieldType.value === "password" ? "text" : "password";
};
</script>

<template>
	<div class="p-4 space-y-6">
		<div class="relative flex items-center justify-center">
			<RouterLink
				to="/profile"
				class="absolute left-0 p-2 text-gray-700 hover:opacity-70"
			>
				<ArrowLeftIcon class="w-6 h-6 text-gray-700" />
			</RouterLink>
			<h1 class="text-xl font-bold text-gray-900">Change Password</h1>
		</div>

		<div class="p-6 bg-white rounded-2xl shadow-lg">
			<form @submit.prevent="handleChangePassword" class="space-y-5" novalidate>
				<div>
					<label
						for="oldPassword"
						class="block text-sm font-medium text-gray-700"
						>Old Password</label
					>
					<div class="relative mt-1">
						<input
							v-model="oldPassword"
							:type="oldPassFieldType"
							id="oldPassword"
							@blur="oldPasswordTouched.value = true"
							:class="{ 'border-red-500': oldPasswordError }"
							class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						/>
						<LockClosedIcon
							class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
							:class="[oldPasswordError ? 'text-red-500' : 'text-gray-400']"
						/>
						<button
							type="button"
							@click="toggleOldPass"
							class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500"
						>
							<EyeIcon v-if="oldPassFieldType === 'password'" class="w-5 h-5" />
							<EyeSlashIcon v-else class="w-5 h-5" />
						</button>
					</div>
					<div v-if="oldPasswordError" class="text-red-600 text-sm mt-1">
						{{ oldPasswordError }}
					</div>
				</div>

				<div>
					<label
						for="newPassword"
						class="block text-sm font-medium text-gray-700"
						>New Password</label
					>
					<div class="relative mt-1">
						<input
							v-model="newPassword"
							:type="newPassFieldType"
							id="newPassword"
							@blur="newPasswordTouched.value = true"
							:class="{ 'border-red-500': newPasswordError }"
							class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						/>
						<LockClosedIcon
							class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
							:class="[newPasswordError ? 'text-red-500' : 'text-gray-400']"
						/>
						<button
							type="button"
							@click="toggleNewPass"
							class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500"
						>
							<EyeIcon v-if="newPassFieldType === 'password'" class="w-5 h-5" />
							<EyeSlashIcon v-else class="w-5 h-5" />
						</button>
					</div>
					<div v-if="newPasswordTouched || newPassword.length > 0" class="mt-2">
						<div class="w-full bg-gray-200 rounded-full h-1.5">
							<div
								class="h-1.5 rounded-full transition-all duration-300"
								:class="passwordStrength.color"
								:style="{ width: (passwordStrength.score / 5) * 100 + '%' }"
							></div>
						</div>
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
					<div v-if="newPasswordError" class="text-red-600 text-sm mt-1">
						{{ newPasswordError }}
					</div>
				</div>

				<div>
					<label
						for="confirmNewPassword"
						class="block text-sm font-medium text-gray-700"
						>Confirm New Password</label
					>
					<div class="relative mt-1">
						<input
							v-model="confirmNewPassword"
							:type="confirmPassFieldType"
							id="confirmNewPassword"
							@blur="confirmNewPasswordTouched.value = true"
							:class="{ 'border-red-500': confirmNewPasswordError }"
							class="w-full px-3 py-3 pl-10 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
						/>
						<LockClosedIcon
							class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
							:class="[
								confirmNewPasswordError ? 'text-red-500' : 'text-gray-400',
							]"
						/>
						<button
							type="button"
							@click="toggleConfirmPass"
							class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500"
						>
							<EyeIcon
								v-if="confirmPassFieldType === 'password'"
								class="w-5 h-5"
							/>
							<EyeSlashIcon v-else class="w-5 h-5" />
						</button>
					</div>
					<div v-if="confirmNewPasswordError" class="text-red-600 text-sm mt-1">
						{{ confirmNewPasswordError }}
					</div>
				</div>

				<button
					type="submit"
					:disabled="!isFormValid || isLoading"
					class="w-full px-4 py-3 font-medium text-white bg-orange-600 rounded-lg shadow-md hover:bg-orange-700 flex items-center justify-center"
					:class="{
						'disabled:bg-orange-300 disabled:cursor-not-allowed':
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
					<span>{{ isLoading ? "Changing..." : "Change Password" }}</span>
				</button>
			</form>
		</div>
	</div>
	<Toaster position="top-center" rich-colors />
</template>
