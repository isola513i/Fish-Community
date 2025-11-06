<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useBookingStore } from "../stores/bookingStore";
import { useRouter } from "vue-router";
import { DatePicker } from "v-calendar";
import "v-calendar/style.css";
import { Toaster, toast } from "vue-sonner";
import "vue-sonner/style.css";
import { ArrowLeftIcon } from "@heroicons/vue/24/outline";

// === Setup ===
const props = defineProps({
	id: {
		type: String,
		required: true,
	},
});

const bookingStore = useBookingStore();
const router = useRouter();

// === State ===
const isLoading = ref(true);
const errorMessage = ref(null);
const booking = ref(null);

const isSaving = ref(false); // สำหรับปุ่ม Save
const isCancelling = ref(false); // สำหรับปุ่ม Cancel
const showCancelModal = ref(false); // State ควบคุม Modal

// Form State
const bookingTitle = ref("");
const bookingNotes = ref("");
const selectedDate = ref(new Date());
const startTime = ref("08:00");
const endTime = ref("08:30");

// === Time Logic (เหมือนเดิม) ===
const OPEN_MIN = 8 * 60; // 08:00
const CLOSE_MIN = 22 * 60; // 22:00
const LATEST_START = CLOSE_MIN - 30;

const toMinutes = (t) => {
	if (!t) return 0;
	const [h, m] = t.split(":").map(Number);
	return h * 60 + m;
};
const fromMinutes = (mins) => {
	const h = String(Math.floor(mins / 60)).padStart(2, "0");
	const m = String(mins % 60).padStart(2, "0");
	return `${h}:${m}`;
};

const ceilTo30 = (date) => {
	const d = new Date(date);
	const mins = d.getMinutes();
	const add = mins === 0 ? 0 : 30 - (mins % 30);
	d.setMinutes(mins + add, 0, 0);
	return d;
};

const isToday = computed(() => {
	const a = new Date(selectedDate.value);
	const now = new Date();
	return (
		a.getFullYear() === now.getFullYear() &&
		a.getMonth() === now.getMonth() &&
		a.getDate() === now.getDate()
	);
});

const minTodayStart = computed(() => {
	if (!isToday.value) return OPEN_MIN;
	const rounded = ceilTo30(new Date());
	const mins = rounded.getHours() * 60 + rounded.getMinutes();
	return Math.max(mins, OPEN_MIN);
});

const minStartMinutes = computed(() =>
	isToday.value ? Math.min(minTodayStart.value, LATEST_START) : OPEN_MIN
);

const minStartStr = computed(() => fromMinutes(minStartMinutes.value));
const maxStartStr = computed(() => fromMinutes(LATEST_START));
const minEndStr = computed(() =>
	fromMinutes(Math.min(toMinutes(startTime.value) + 30, CLOSE_MIN))
);
const maxEndStr = computed(() => fromMinutes(CLOSE_MIN));

const getISOString = (date, time) => {
	if (!date || !time) return null;
	const [hours, minutes] = time.split(":");
	const d = new Date(date);
	d.setHours(+hours, +minutes, 0, 0);
	return d.toISOString();
};

const isoStartAt = computed(() =>
	getISOString(selectedDate.value, startTime.value)
);
const isoEndAt = computed(() =>
	getISOString(selectedDate.value, endTime.value)
);

const isValidRange = computed(() => {
	if (!isoStartAt.value || !isoEndAt.value) return false;
	const start = new Date(isoStartAt.value);
	const end = new Date(isoEndAt.value);
	const durationMinutes = (end.getTime() - start.getTime()) / 60000;
	return durationMinutes >= 30;
});

// === Handlers ===

const handleCancel = () => {
	if (isSaving.value || isCancelling.value) return;
	showCancelModal.value = true;
};

const confirmCancelBooking = async () => {
	isCancelling.value = true;
	errorMessage.value = null;
	try {
		await bookingStore.cancelBooking(Number(props.id));
		toast.success("Booking Cancelled");
		showCancelModal.value = false;
		router.push({ name: "MyBookings" });
	} catch (error) {
		errorMessage.value = "Failed to cancel booking.";
		toast.error(errorMessage.value);
	} finally {
		isCancelling.value = false;
	}
};

const closeCancelModal = () => {
	if (isCancelling.value) return;
	showCancelModal.value = false;
};

const handleSubmit = async () => {
	if (!isValidRange.value || isSaving.value || isCancelling.value) {
		if (!isValidRange.value) {
			toast.error("Booking must be at least 30 minutes.");
		}
		return;
	}

	errorMessage.value = null;
	isSaving.value = true;

	try {
		const payload = {
			title: bookingTitle.value,
			startAt: isoStartAt.value,
			endAt: isoEndAt.value,
			notes: bookingNotes.value,
		};

		await bookingStore.updateBooking(Number(props.id), payload);
		toast.success("Booking Updated!");
	} catch (error) {
		const msg = error?.response?.data?.message || "Failed to update booking";
		errorMessage.value = `Error: ${msg}`;
		toast.error(errorMessage.value);
	} finally {
		isSaving.value = false;
	}
};

// === Load Data (เหมือนเดิม) ===
onMounted(async () => {
	const bookingId = Number(props.id);
	const fetchedBooking = await bookingStore.getBookingById(bookingId);

	if (
		!fetchedBooking ||
		fetchedBooking.status === "CANCELLED" ||
		new Date(fetchedBooking.endAt) < new Date()
	) {
		errorMessage.value = "Booking not found or has already passed.";
		isLoading.value = false;
		setTimeout(() => router.push({ name: "MyBookings" }), 2000);
		return;
	}

	booking.value = fetchedBooking;
	bookingTitle.value = fetchedBooking.title;
	bookingNotes.value = fetchedBooking.notes || "";
	const start = new Date(fetchedBooking.startAt);
	const end = new Date(fetchedBooking.endAt);
	selectedDate.value = start;
	startTime.value = fromMinutes(start.getHours() * 60 + start.getMinutes());
	endTime.value = fromMinutes(end.getHours() * 60 + end.getMinutes());
	isLoading.value = false;
});

// === Time Watchers (เหมือนเดิม) ===
watch(selectedDate, () => {
	if (isToday.value && toMinutes(startTime.value) < minStartMinutes.value) {
		startTime.value = fromMinutes(minStartMinutes.value);
	}
});

watch(startTime, (newStart) => {
	const ms = toMinutes(newStart);
	if (isToday.value && ms < minStartMinutes.value) {
		startTime.value = fromMinutes(minStartMinutes.value);
	} else if (ms > LATEST_START) {
		startTime.value = fromMinutes(LATEST_START);
	}

	if (toMinutes(endTime.value) <= toMinutes(startTime.value)) {
		endTime.value = fromMinutes(
			Math.min(toMinutes(startTime.value) + 30, CLOSE_MIN)
		);
	}
});

watch(endTime, (newEnd) => {
	const me = toMinutes(newEnd);
	const minE = toMinutes(startTime.value) + 30;
	if (me < minE) endTime.value = fromMinutes(Math.min(minE, CLOSE_MIN));
	if (me > CLOSE_MIN) endTime.value = fromMinutes(CLOSE_MIN);
});
</script>

<template>
	<div class="p-4 space-y-6 pb-20">
		<div class="relative flex items-center justify-center">
			<RouterLink
				:to="{ name: 'MyBookings' }"
				class="absolute left-0 p-2 text-gray-700 hover:opacity-70"
			>
				<ArrowLeftIcon class="w-6 h-6 text-gray-700" />
			</RouterLink>
			<h1 class="text-xl font-bold text-gray-900">Edit Booking</h1>
		</div>

		<div v-if="isLoading" class="text-center">Loading booking...</div>
		<form
			v-if="!isLoading && booking"
			@submit.prevent="handleSubmit"
			class="space-y-6"
		>
			<div class="p-4 bg-white rounded-xl ring-1 ring-gray-200">
				<h2 class="text-lg font-semibold text-blue-700">
					Room: {{ booking.room.name }}
				</h2>
				<p class="text-sm text-gray-600">
					Capacity: {{ booking.room.capacity }}
				</p>
			</div>

			<div class="p-6 bg-soft-bg rounded-2xl shadow-neumorphism space-y-4">
				<div>
					<label for="title" class="block text-sm font-medium text-gray-700"
						>Title</label
					>
					<input
						v-model="bookingTitle"
						type="text"
						id="title"
						required
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
					/>
				</div>
				<div>
					<label for="notes" class="block text-sm font-medium text-gray-700"
						>Notes (Optional)</label
					>
					<textarea
						v-model="bookingNotes"
						id="notes"
						rows="3"
						class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
					></textarea>
				</div>
			</div>

			<div
				class="p-4 bg-white rounded-2xl shadow-sm ring-1 ring-black/5 space-y-3"
			>
				<div class="flex justify-center">
					<DatePicker
						v-model="selectedDate"
						:min-date="new Date()"
						is-inline
						is-required
						title-position="left"
						class="w-full"
					/>
				</div>
			</div>

			<fieldset class="rounded-xl bg-white ring-1 ring-gray-200 p-3">
				<legend class="px-1 text-xs font-medium text-gray-500">Time</legend>
				<div class="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-2 sm:gap-3">
					<div>
						<span class="text-xs text-gray-600">Start</span>
						<input
							id="startTime"
							type="time"
							v-model="startTime"
							:min="minStartStr"
							:max="maxStartStr"
							step="1800"
							class="h-10 w-full rounded-lg border border-gray-200 bg-white pl-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300"
						/>
					</div>

					<div>
						<span class="text-xs text-gray-600">End</span>
						<input
							id="endTime"
							type="time"
							v-model="endTime"
							:min="minEndStr"
							:max="maxEndStr"
							step="1800"
							class="h-10 w-full rounded-lg border border-gray-200 bg-white pl-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300"
						/>
					</div>
				</div>

				<div
					v-if="!isValidRange && startTime && endTime"
					class="mt-2 text-sm text-red-600"
				>
					Duration must be at least 30 minutes.
				</div>
			</fieldset>

			<button
				type="submit"
				:disabled="!isValidRange || isSaving || isCancelling"
				class="w-full px-4 py-3 font-medium text-white rounded-lg shadow-md flex items-center justify-center gap-2"
				:class="
					!isValidRange || isSaving || isCancelling
						? 'bg-blue-300 cursor-not-allowed'
						: 'bg-blue-600 hover:bg-blue-700'
				"
			>
				<svg
					v-if="isSaving"
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
				<span>{{ isSaving ? "Saving..." : "Save Changes" }}</span>
			</button>

			<button
				type="button"
				@click="handleCancel"
				:disabled="isSaving || isCancelling"
				class="w-full py-3 text-sm font-medium text-red-600 text-center rounded-lg hover:bg-red-50 disabled:opacity-50"
			>
				Cancel Booking
			</button>
		</form>
	</div>

	<Toaster position="top-center" rich-colors />

	<div
		v-if="showCancelModal"
		class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 supports-backdrop-filter:backdrop-blur-[1.5px]"
	>
		<div
			class="w-full max-w-sm p-6 mx-4 rounded-2xl bg-white/95 ring-1 ring-black/5 shadow-xl"
		>
			<h2 class="text-xl font-semibold mb-2 text-gray-900">Cancel Booking?</h2>
			<p class="text-sm text-gray-600 mb-6">
				Are you sure you want to cancel this booking? This action cannot be
				undone.
			</p>

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
</template>

<style scoped>
:deep(.vc-container) {
	width: 100%;
}
</style>
