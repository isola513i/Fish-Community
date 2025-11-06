<script setup>
import { onMounted } from "vue";
import { useRoomStore } from "../../stores/roomStore";
import { storeToRefs } from "pinia";
import { RouterLink } from "vue-router";
import {
	PlusIcon,
	PencilIcon,
	ClockIcon,
	UserIcon,
} from "@heroicons/vue/24/outline"; // ⬅️ 1. Import Icon เพิ่ม

const roomStore = useRoomStore();
// ⭐️ 2. เปลี่ยนมาใช้ State ใหม่ ⭐️
const { adminRoomList, isLoading } = storeToRefs(roomStore);

onMounted(() => {
	// ⭐️ 3. เรียก Action ใหม่ ⭐️
	roomStore.fetchAdminRoomList();
});

// (Helper สำหรับจัดรูปแบบเวลา)
const formatTime = (isoString) => {
	if (!isoString) return "";
	const date = new Date(isoString);
	// ตรวจสอบว่าเป็นเวลาปัจจุบันหรือไม่ (เช่น จองไว้ตั้งแต่ 9:00 ตอนนี้ 9:30)
	if (date < new Date()) {
		return `Now until ${date.toLocaleTimeString("en-US", {
			hour: "2-digit",
			minute: "2-digit",
		})}`;
	}
	return date.toLocaleString("en-US", {
		dateStyle: "short",
		timeStyle: "short",
	});
};
</script>

<template>
	<div class="p-4 space-y-6">
		<h1 class="text-3xl font-bold text-gray-900">Manage Rooms</h1>

		<div v-if="isLoading" class="text-center">
			<SkeletonCard v-for="n in 3" :key="n" />
		</div>

		<div v-if="!isLoading" class="space-y-4">
			<div
				v-for="item in adminRoomList"
				:key="item.room.id"
				class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism"
			>
				<div class="flex items-center justify-between">
					<div>
						<h2
							class="text-lg font-semibold"
							:class="{
								'text-gray-900': item.room.isActive,
								'text-gray-400': !item.room.isActive,
							}"
						>
							{{ item.room.name }}
						</h2>
						<p
							class="text-sm"
							:class="{
								'text-gray-600': item.room.isActive,
								'text-gray-400': !item.room.isActive,
							}"
						>
							Capacity: {{ item.room.capacity }} | Status:
							<span
								:class="{
									'text-green-600': item.room.isActive,
									'text-red-600': !item.room.isActive,
								}"
							>
								{{ item.room.isActive ? "Active" : "Inactive" }}
							</span>
						</p>
					</div>
					<RouterLink
						:to="`/admin/rooms/edit/${item.room.id}`"
						class="p-2 text-gray-600 rounded-lg hover:bg-white shrink-0"
					>
						<PencilIcon class="w-5 h-5" />
					</RouterLink>
				</div>

				<div v-if="item.nextBooking" class="mt-4 pt-3 border-t border-gray-300">
					<p class="text-sm font-semibold text-blue-800">
						Booked: {{ item.nextBooking.title }}
					</p>
					<p class="flex items-center text-sm text-gray-700 mt-1">
						<UserIcon class="w-4 h-4 mr-2" />
						By: {{ item.nextBooking.user.fullName }}
					</p>
					<p class="flex items-center text-sm text-gray-700 mt-1">
						<ClockIcon class="w-4 h-4 mr-2" />
						When: {{ formatTime(item.nextBooking.startAt) }}
					</p>
				</div>

				<div
					v-else-if="item.room.isActive"
					class="mt-2 pt-2 text-sm text-green-700"
				>
					Available
				</div>
			</div>
		</div>
		<RouterLink
			to="/admin/rooms/create"
			class="fixed z-30 flex items-center justify-center w-14 h-14 p-4 text-white bg-blue-600 rounded-full shadow-lg bottom-20 right-4 hover:bg-blue-700"
		>
			<PlusIcon class="w-6 h-6" />
		</RouterLink>
	</div>
</template>
