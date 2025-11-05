<script setup>
import { RouterLink } from "vue-router";
import { useAuthStore } from "../stores/authStore";
import {
	CalendarDaysIcon,
	BookmarkSquareIcon,
	UserIcon,
	Cog6ToothIcon,
} from "@heroicons/vue/24/outline";

const authStore = useAuthStore();
</script>

<template>
	<nav
		class="fixed bottom-0 left-0 right-0 w-full bg-white border-t border-gray-200 shadow-lg"
	>
		<div class="flex justify-around max-w-md mx-auto">
			<RouterLink
				to="/"
				class="flex flex-col items-center flex-1 p-3 text-gray-600 hover:text-blue-600"
				active-class="text-blue-600"
			>
				<BookmarkSquareIcon class="w-6 h-6" />
				<span class="text-xs">My Bookings</span>
			</RouterLink>

			<RouterLink
				to="/book"
				class="flex flex-col items-center flex-1 p-3 text-gray-600 hover:text-blue-600"
				active-class="text-blue-600"
			>
				<CalendarDaysIcon class="w-6 h-6" />
				<span class="text-xs">Book Room</span>
			</RouterLink>

			<RouterLink
				v-if="authStore.isAdmin"
				to="/admin/rooms"
				class="flex flex-col items-center flex-1 p-3 text-gray-600 hover:text-blue-600"
				active-class="text-blue-600"
			>
				<Cog6ToothIcon class="w-6 h-6" />
				<span class="text-xs">Admin</span>
			</RouterLink>

			<RouterLink
				to="/profile"
				class="flex flex-col items-center flex-1 p-3 text-gray-600 hover:text-blue-600"
				active-class="text-blue-600"
			>
				<UserIcon class="w-6 h-6" />
				<span class="text-xs">Profile</span>
			</RouterLink>
		</div>
	</nav>
</template>
