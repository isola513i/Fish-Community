<script setup>
import { ref, onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";
import { storeToRefs } from "pinia";
import { ArrowRightOnRectangleIcon } from "@heroicons/vue/24/outline";

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

const editFullName = ref("");
const oldPassword = ref("");
const newPassword = ref("");
const successMessage = ref(null);
const errorMessage = ref(null);

onMounted(() => {
	if (user.value) {
		editFullName.value = user.value.fullName;
	}
});

const handleLogout = () => {
	authStore.logout();
};

const handleUpdateProfile = async () => {
	clearMessages();
	try {
		await authStore.updateProfile(editFullName.value);
		successMessage.value = "อัปเดตชื่อสำเร็จ";
		setTimeout(() => {
			successMessage.value = null;
		}, 3000);
	} catch (error) {
		errorMessage.value = "อัปเดตล้มเหลว";
	}
};

const handleChangePassword = async () => {
	clearMessages();
	try {
		await authStore.changePassword(oldPassword.value, newPassword.value);
		successMessage.value = "เปลี่ยนรหัสผ่านสำเร็จ";
		oldPassword.value = "";
		newPassword.value = "";
	} catch (error) {
		errorMessage.value =
			error.response?.data?.message || "รหัสผ่านเก่าไม่ถูกต้อง";
	}
};

const clearMessages = () => {
	successMessage.value = null;
	errorMessage.value = null;
};
</script>

<template>
	<div class="p-4 space-y-6">
		<h1 class="text-3xl font-bold text-gray-900">Profile</h1>

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

		<div class="p-4 bg-white rounded-lg shadow">
			<div class="space-y-2">
				<div>
					<label class="text-sm font-medium text-gray-500">Email</label>
					<p class="text-gray-900">{{ user?.email }}</p>
				</div>
				<div>
					<label class="text-sm font-medium text-gray-500">Role</label>
					<p class="text-gray-900 capitalize">{{ user?.role.toLowerCase() }}</p>
				</div>
			</div>
		</div>

		<div class="p-4 bg-white rounded-lg shadow">
			<form @submit.prevent="handleUpdateProfile" class="space-y-4">
				<h2 class="text-lg font-semibold">แก้ไขชื่อ</h2>
				<div>
					<label for="fullName" class="block text-sm font-medium text-gray-700"
						>Full Name</label
					>
					<input
						v-model="editFullName"
						type="text"
						id="fullName"
						required
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
					/>
				</div>
				<button
					type="submit"
					class="w-full px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
				>
					Save Name
				</button>
			</form>
		</div>

		<div class="p-4 bg-white rounded-lg shadow">
			<form @submit.prevent="handleChangePassword" class="space-y-4">
				<h2 class="text-lg font-semibold">เปลี่ยนรหัสผ่าน</h2>
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
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
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
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
					/>
				</div>
				<button
					type="submit"
					class="w-full px-4 py-2 font-medium text-white bg-orange-600 rounded-md hover:bg-orange-700"
				>
					Change Password
				</button>
			</form>
		</div>

		<div class="pt-4">
			<button
				@click="handleLogout"
				class="flex items-center justify-center w-full px-4 py-2 font-medium text-red-700 bg-red-100 rounded-md hover:bg-red-200"
			>
				<ArrowRightOnRectangleIcon class="w-5 h-5 mr-2" />
				Logout
			</button>
		</div>
	</div>
</template>
