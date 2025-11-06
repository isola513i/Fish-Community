<script setup>
import { ref, onMounted } from "vue";
import { useUserStore } from "../../stores/userStore";
import { RouterLink } from "vue-router";
import { ArrowLeftIcon } from "@heroicons/vue/24/outline";
import { Toaster, toast } from "vue-sonner";

const props = defineProps({
	id: {
		type: String,
		required: true,
	},
});

const userStore = useUserStore();
const isLoading = ref(true);

// Form State
const userEmail = ref("");
const userFullName = ref("");
const userRole = ref("MEMBER"); // Default
const userIsActive = ref(true); // Default

onMounted(async () => {
	const userId = Number(props.id);
	const userData = await userStore.fetchUser(userId);
	if (userData) {
		userEmail.value = userData.email;
		userFullName.value = userData.fullName;
		userRole.value = userData.role; // "ADMIN" or "MEMBER"
		userIsActive.value = userData.isActive;
	}
	isLoading.value = false;
});

const handleSubmit = async () => {
	try {
		const payload = {
			role: userRole.value, // "ADMIN" or "MEMBER"
			isActive: userIsActive.value,
		};

		// API: PUT /api/users/{id}
		await userStore.updateUser(Number(props.id), payload);
		toast.success("User updated successfully!");
		// store จะ redirect กลับไปเอง
	} catch (error) {
		toast.error(error.response?.data?.message || "Failed to update user");
	}
};
</script>

<template>
	<div class="p-4 space-y-6">
		<div class="relative flex items-center justify-center">
			<RouterLink
				to="/admin/users"
				class="absolute left-0 p-2 text-gray-700 hover:opacity-70"
			>
				<ArrowLeftIcon class="w-6 h-6 text-gray-700" />
			</RouterLink>
			<h1 class="text-xl font-bold text-gray-900">Edit User</h1>
		</div>

		<div v-if="isLoading" class="text-center">Loading user data...</div>

		<form
			v-else
			@submit.prevent="handleSubmit"
			class="p-6 bg-soft-bg rounded-2xl shadow-neumorphism space-y-4"
		>
			<div>
				<label class="block text-sm font-medium text-gray-500">Full Name</label>
				<input
					:value="userFullName"
					type="text"
					disabled
					class="w-full px-3 py-2 mt-1 bg-gray-200 border border-gray-300 rounded-md shadow-sm"
				/>
			</div>
			<div>
				<label class="block text-sm font-medium text-gray-500">Email</label>
				<input
					:value="userEmail"
					type="email"
					disabled
					class="w-full px-3 py-2 mt-1 bg-gray-200 border border-gray-300 rounded-md shadow-sm"
				/>
			</div>

			<div>
				<label for="role" class="block text-sm font-medium text-gray-700"
					>Role</label
				>
				<select
					v-model="userRole"
					id="role"
					class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
				>
					<option value="MEMBER">MEMBER</option>
					<option value="ADMIN">ADMIN</option>
				</select>
			</div>

			<div class="flex items-center justify-between">
				<label for="isActive" class="block text-sm font-medium text-gray-700"
					>Account Status</label
				>
				<button
					@click="userIsActive = !userIsActive"
					type="button"
					:class="[
						'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none',
						userIsActive ? 'bg-blue-600' : 'bg-gray-300',
					]"
				>
					<span
						:class="[
							'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
							userIsActive ? 'translate-x-5' : 'translate-x-0',
						]"
					></span>
				</button>
			</div>

			<button
				type="submit"
				class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
			>
				Update User
			</button>
		</form>
		<Toaster position="top-center" rich-colors />
	</div>
</template>
