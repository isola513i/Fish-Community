<script setup>
import { onMounted, computed } from "vue";
import { useBookingStore } from "../stores/bookingStore";
import { storeToRefs } from "pinia";
import { RouterLink } from "vue-router";
import { CalendarDaysIcon, PencilIcon } from "@heroicons/vue/24/outline";
import SkeletonCard from "../components/SkeletonCard.vue"; // ⬅️ 1. Import Skeleton

const bookingStore = useBookingStore();
const { myBookings, isLoading } = storeToRefs(bookingStore);

onMounted(() => {
	bookingStore.fetchMyBookings();
});

// --- (Helpers และ Computed ... เหมือนเดิม) ---
const now = new Date();
const formatMonth = (isoString) => {
	if (!isoString) return "";
	return new Date(isoString).toLocaleString("en-US", { month: "short" });
};
const formatDay = (isoString) => {
	if (!isoString) return "";
	return new Date(isoString).getDate();
};
const formatTimeRange = (startIso, endIso) => {
	const options = { timeStyle: "short", hour12: true };
	const startTime = new Date(startIso).toLocaleTimeString("en-US", options);
	const endTime = new Date(endIso).toLocaleTimeString("en-US", options);
	return `${startTime} - ${endTime}`;
};
const upcomingBookings = computed(() =>
	myBookings.value
		.filter((b) => new Date(b.endAt) > now && b.status === "CONFIRMED")
		.sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
);
const pastBookings = computed(() =>
	myBookings.value
		.filter((b) => new Date(b.endAt) <= now || b.status === "CANCELLED")
		.sort((a, b) => new Date(b.startAt) - new Date(a.startAt))
);
</script>

<template>
	<div class="p-4 space-y-6 pb-20">
		<h1 class="text-3xl font-bold text-gray-900">My Bookings</h1>

		<div v-if="isLoading" class="pt-2 space-y-4">
			<SkeletonCard v-for="n in 3" :key="n" />
		</div>
		<div
			v-if="!isLoading && myBookings.length === 0"
			class="p-6 mt-10 text-center text-gray-600 bg-soft-bg rounded-2xl shadow-neumorphism"
		>
			<CalendarDaysIcon class="w-12 h-12 mx-auto text-gray-400" />
			<h3 class="mt-2 text-lg font-medium text-gray-800">No Bookings Yet</h3>
			<p class="mt-1 text-sm text-gray-500">
				You don't have any room bookings.
			</p>
			<div class="mt-6">
				<RouterLink
					to="/book"
					class="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
				>
					Book a Room
				</RouterLink>
			</div>
		</div>

		<div v-if="!isLoading && myBookings.length > 0" class="space-y-8">
			<div v-if="upcomingBookings.length > 0" class="space-y-4">
				<h2 class="text-xl font-semibold text-gray-800">Upcoming</h2>
				<div
					v-for="booking in upcomingBookings"
					:key="booking.id"
					class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism"
				>
					<div class="flex items-center gap-4">
						<div
							class="shrink-0 flex flex-col items-center justify-center w-16 h-16 bg-white rounded-lg shadow-sm"
						>
							<span class="text-xs font-semibold text-red-600 uppercase">{{
								formatMonth(booking.startAt)
							}}</span>
							<span class="text-2xl font-bold text-gray-800">{{
								formatDay(booking.startAt)
							}}</span>
						</div>
						<div class="grow min-w-0">
							<h2 class="text-lg font-semibold text-blue-700 truncate">
								{{ booking.title }}
							</h2>
							<p class="text-sm text-gray-700">Room: {{ booking.room.name }}</p>
							<p class="text-sm text-gray-600">
								{{ formatTimeRange(booking.startAt, booking.endAt) }}
							</p>
						</div>
						<RouterLink
							:to="{ name: 'EditBooking', params: { id: booking.id } }"
							class="shrink-0 p-2 text-gray-600 rounded-lg hover:bg-white"
						>
							<PencilIcon class="w-5 h-5" />
						</RouterLink>
					</div>
				</div>
			</div>

			<div v-if="pastBookings.length > 0" class="space-y-4">
				<h2 class="text-xl font-semibold text-gray-800">Past & Cancelled</h2>
				<div
					v-for="booking in pastBookings"
					:key="booking.id"
					class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism opacity-70"
				>
					<div class="flex items-center gap-4">
						<div
							class="shrink-0 flex flex-col items-center justify-center w-16 h-16 bg-white rounded-lg shadow-sm"
						>
							<span class="text-xs font-semibold text-gray-500 uppercase">{{
								formatMonth(booking.startAt)
							}}</span>
							<span class="text-2xl font-bold text-gray-700">{{
								formatDay(booking.startAt)
							}}</span>
						</div>
						<div class="grow min-w-0">
							<h2 class="text-lg font-semibold text-gray-600 truncate">
								{{ booking.title }}
							</h2>
							<p class="text-sm text-gray-600">Room: {{ booking.room.name }}</p>
							<p class="text-sm text-gray-500">
								{{ formatTimeRange(booking.startAt, booking.endAt) }}
							</p>
						</div>
					</div>
					<p
						v-if="booking.status === 'CANCELLED'"
						class="mt-3 text-sm font-bold text-red-600"
					>
						CANCELLED
					</p>
				</div>
			</div>
		</div>
	</div>
</template>
