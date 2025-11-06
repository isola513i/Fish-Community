<script setup>
import { onMounted } from "vue";
import { useUserStore } from "../../stores/userStore";
import { storeToRefs } from "pinia";
import { RouterLink } from "vue-router";
import { PencilIcon, ArrowLeftIcon } from "@heroicons/vue/24/outline";

const userStore = useUserStore();
const { userList, isLoading } = storeToRefs(userStore);

onMounted(() => {
	userStore.fetchAllUsers();
});
</script>

<template>
	<div class="p-4 space-y-6 pb-20">
		<div class="relative flex items-center justify-center">
			<RouterLink
				to="/profile"
				class="absolute left-0 p-2 rounded-full bg-soft-bg shadow-neumorphism hover:shadow-neumorphism-inset"
			>
				<ArrowLeftIcon class="w-6 h-6 text-gray-700" />
			</RouterLink>
			<h1 class="text-xl font-bold text-gray-900">Manage Users</h1>
		</div>

		<div v-if="isLoading" class="text-center">Loading users...</div>

		<div v-if="!isLoading" class="space-y-4">
			<div
				v-for="user in userList"
				:key="user.id"
				class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism"
			>
				<div class="flex items-center justify-between">
					<div class="min-w-0">
						<h2 class="text-lg font-semibold text-gray-900 truncate">
							{{ user.fullName }}
						</h2>
						<p class="text-sm text-gray-600 truncate">{{ user.email }}</p>
						<p class="text-sm">
							Role: <span class="font-medium">{{ user.role }}</span> | Status:
							<span :class="user.isActive ? 'text-green-600' : 'text-red-600'">
								{{ user.isActive ? "Active" : "Inactive" }}
							</span>
						</p>
					</div>
					<RouterLink
						:to="{ name: 'AdminEditUser', params: { id: user.id } }"
						class="flex-shrink-0 p-2 ml-2 text-gray-600 rounded-lg hover:bg-white"
					>
						<PencilIcon class="w-5 h-5" />
					</RouterLink>
				</div>
			</div>
		</div>
	</div>
</template>
