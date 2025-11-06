<script setup>
import { onMounted, computed, ref } from "vue"; // ⬅️ 1. เพิ่ม ref
import { useBookingStore } from "../../stores/bookingStore";
import { storeToRefs } from "pinia";
import { RouterLink } from "vue-router";
import {
	CalendarDaysIcon,
	ArrowLeftIcon,
	UserIcon,
} from "@heroicons/vue/24/outline";
import { Toaster, toast } from "vue-sonner";

const bookingStore = useBookingStore();
const { allBookings, isLoading } = storeToRefs(bookingStore);

// ⬇️ 2. เพิ่ม State สำหรับ Modal ⬇️
const isCancelling = ref(false);
const showCancelModal = ref(false);
const bookingToCancel = ref(null); // (เก็บ ID ที่จะลบ)

onMounted(() => {
	bookingStore.fetchAllBookings();
});

// ⬇️ 3. แก้ไข handleCancel ให้เปิด Modal ⬇️
const handleCancel = (booking) => {
	if (isCancelling.value) return;
	bookingToCancel.value = booking; // เก็บ Object ที่จะลบ
	showCancelModal.value = true;
};

// ⬇️ 4. เพิ่มฟังก์ชันยืนยัน (เหมือนใน EditBooking) ⬇️
const confirmCancelBooking = async () => {
	if (!bookingToCancel.value) return;

	isCancelling.value = true;
	try {
		await bookingStore.cancelBooking(bookingToCancel.value.id);
		toast.success("Booking cancelled by Admin");
		closeCancelModal();
	} catch (error) {
		toast.error("Failed to cancel booking");
		isCancelling.value = false; // คืนค่าปุ่มถ้า Error
	}
};

// ⬇️ 5. เพิ่มฟังก์ชันปิด Modal (เหมือนใน EditBooking) ⬇️
const closeCancelModal = () => {
	if (isCancelling.value) return;
	showCancelModal.value = false;
	bookingToCancel.value = null;
	isCancelling.value = false;
};

// --- Helpers (เหมือนเดิม) ---
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

// --- Computed (เหมือนเดิม) ---
const upcomingBookings = computed(() =>
	allBookings.value
		.filter((b) => new Date(b.endAt) > now && b.status === "CONFIRMED")
		.sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
);
const pastBookings = computed(() =>
	allBookings.value
		.filter((b) => new Date(b.endAt) <= now || b.status === "CANCELLED")
		.sort((a, b) => new Date(b.startAt) - new Date(a.startAt))
);
</script>

<template>
	<div class="p-4 space-y-6 pb-20">
		<div class="relative flex items-center justify-center">
			<RouterLink
				to="/profile"
				class="absolute left-0 p-2 text-gray-700 hover:opacity-70"
			>
				<ArrowLeftIcon class="w-6 h-6 text-gray-700" />
			</RouterLink>
			<h1 class="text-xl font-bold text-gray-900">All Bookings (Admin)</h1>
		</div>

		<div v-if="isLoading" class="pt-10 text-center text-gray-600">
			Loading...
		</div>
		<div
			v-if="!isLoading && allBookings.length === 0"
			class="p-6 mt-10 text-center text-gray-600 bg-soft-bg rounded-2xl shadow-neumorphism"
		>
			<CalendarDaysIcon class="w-12 h-12 mx-auto text-gray-400" />
			<h3 class="mt-2 text-lg font-medium text-gray-800">No Bookings Found</h3>
			<p class="mt-1 text-sm text-gray-500">
				There are no bookings in the system.
			</p>
		</div>

		<div v-if="!isLoading && allBookings.length > 0" class="space-y-8">
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
							<p class="flex items-center text-sm text-gray-600 mt-1">
								<UserIcon class="w-4 h-4 mr-1.5 shrink-0" />
								<span class="truncate">{{ booking.user.fullName }}</span>
							</p>
						</div>
					</div>

					<button
						@click="handleCancel(booking)"
						class="w-full px-4 py-2 mt-4 font-medium text-red-600 bg-soft-bg rounded-lg shadow-neumorphism hover:shadow-neumorphism-inset"
					>
						Cancel Booking
					</button>
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
							<p class="flex items-center text-sm text-gray-600 mt-1">
								<UserIcon class="w-4 h-4 mr-1.5 shrink-0" />
								<span class="truncate">{{ booking.user.fullName }}</span>
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
		<Toaster position="top-center" rich-colors />

		<div
			v-if="showCancelModal"
			class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 supports-[backdrop-filter]:backdrop-blur-[1.5px]"
		>
			<div
				class="w-full max-w-sm p-6 mx-4 rounded-2xl bg-white/95 ring-1 ring-black/5 shadow-xl"
			>
				<h2 class="text-xl font-semibold mb-2 text-gray-900">
					Cancel Booking?
				</h2>
				<p class="text-sm text-gray-600 mb-6">
					Are you sure you want to cancel this booking? This action cannot be
					undone.
				</p>

				<div
					v-if="bookingToCancel"
					class="mb-4 p-3 bg-soft-bg rounded-lg text-sm space-y-1"
				>
					<p class="font-medium text-gray-800 truncate">
						<strong>Title:</strong> {{ bookingToCancel.title }}
					</p>
					<p class="text-gray-700">
						<strong>Room:</strong> {{ bookingToCancel.room.name }}
					</p>
					<p class="text-gray-700">
						<strong>By:</strong> {{ bookingToCancel.user.fullName }}
					</p>
				</div>

				<div class="flex flex-col gap-3">
					<button
						@click="closeCancelModal"
						:disabled="isCancelling"
						class="w-full px-4 py-3 font-medium text-gray-700 bg-soft-bg rounded-lg shadow-neumorphism hover:shadow-neumorphism-inset"
					>
						Keep Booking
					</button>
					<button
						@click="confirmCancelBooking"
						:disabled="isCancelling"
						class="w-full px-4 py-3 font-medium text-white bg-red-600 rounded-lg shadow-md hover:bg-red-700 disabled:bg-red-300 flex items-center justify-center gap-2"
					>
						<svg
							v-if="isCancelling"
							class="animate-spin h-4 w-4"
							viewBox="0 0 24 24"
							fill="none"
						>
							<circle
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
								class="opacity-25"
							/>
							<path
								d="M4 12a8 8 0 018-8"
								stroke="currentColor"
								stroke-width="4"
								class="opacity-75"
							/>
						</svg>
						<span>{{ isCancelling ? "Cancelling..." : "Confirm Cancel" }}</span>
					</button>
				</div>
			</div>
		</div>
	</div>
</template>
