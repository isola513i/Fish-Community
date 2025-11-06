<script setup>
import { ref, onMounted, computed, watch, nextTick } from "vue";
import { useRoomStore } from "../stores/roomStore";
import { useBookingStore } from "../stores/bookingStore";
import { storeToRefs } from "pinia";
import { UsersIcon, MapPinIcon } from "@heroicons/vue/24/outline";
import { DatePicker } from "v-calendar";
import "v-calendar/style.css";
import { Toaster, toast } from "vue-sonner";
import "vue-sonner/style.css";

const isSubmitting = ref(false);
const OPEN_MIN = 8 * 60; // 08:00
const CLOSE_MIN = 22 * 60; // 22:00
const LATEST_START = CLOSE_MIN - 30;

const toMinutes = (t) => {
	const [h, m] = t.split(":").map(Number);
	return h * 60 + m;
};
const fromMinutes = (mins) => {
	const h = String(Math.floor(mins / 60)).padStart(2, "0");
	const m = String(mins % 60).padStart(2, "0");
	return `${h}:${m}`;
};

const effectiveMinStart = computed(() =>
	Math.min(minTodayStart.value, LATEST_START)
);

const dayClosed = computed(
	() => isToday.value && minTodayStart.value >= CLOSE_MIN
);

const ceilTo30 = (date) => {
	const d = new Date(date);
	const mins = d.getMinutes();
	const add = mins === 0 ? 0 : 30 - (mins % 30);
	d.setMinutes(mins + add, 0, 0);
	return d;
};

const selectedDate = ref(new Date());
const startTime = ref("08:00");
const endTime = ref("08:30");

const formattedDate = computed(() =>
	selectedDate.value?.toLocaleDateString("th-TH", {
		weekday: "short",
		day: "2-digit",
		month: "long",
		year: "numeric",
	})
);

// ====== “วันนี้” และ min slot ที่อนุญาต ======
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
	isToday.value ? effectiveMinStart.value : OPEN_MIN
);
const minStartStr = computed(() => fromMinutes(minStartMinutes.value));
const maxStartStr = computed(() => fromMinutes(LATEST_START));
const minEndStr = computed(() =>
	fromMinutes(Math.min(toMinutes(startTime.value) + 30, CLOSE_MIN))
);
const maxEndStr = computed(() => fromMinutes(CLOSE_MIN));

// ====== ISO + validation ======
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
	return new Date(isoStartAt.value) < new Date(isoEndAt.value);
});

// ====== Normalizers (snap 30 นาที + เคารพ min/max) ======
const onStartInput = (e) => {
	let m = toMinutes(e.target.value || "08:00");
	m = Math.max(m, minStartMinutes.value);
	m = Math.min(m, CLOSE_MIN - 30);
	m = Math.ceil(m / 30) * 30;
	startTime.value = fromMinutes(m);
	if (toMinutes(endTime.value) <= m) {
		endTime.value = fromMinutes(Math.min(m + 30, CLOSE_MIN));
	}
};

const onEndInput = (e) => {
	let m = toMinutes(e.target.value || "08:30");
	const min = Math.min(CLOSE_MIN, toMinutes(startTime.value) + 30);
	m = Math.max(m, min);
	m = Math.min(m, CLOSE_MIN);
	m = Math.ceil(m / 30) * 30;
	endTime.value = fromMinutes(m);
};

const lastActive = ref("end");
onMounted(() => {
	lastActive.value = "end";
});

const snapUp30 = (mins) => Math.ceil(mins / 30) * 30;
const snapDown30 = (mins) => Math.floor(mins / 30) * 30;
const onPlus = (delta) => {
	if (dayClosed.value) return;
	const s = toMinutes(startTime.value);
	const e = toMinutes(endTime.value);
	const base = Math.max(e, s + 30);
	let nextEnd = base + delta;
	nextEnd = Math.min(nextEnd, CLOSE_MIN);
	nextEnd = snapUp30(nextEnd);

	endTime.value = fromMinutes(nextEnd);
};

const onMinus = (delta) => {
	if (dayClosed.value) return;
	const s = toMinutes(startTime.value);
	const e = toMinutes(endTime.value);
	let nextEnd = e - delta;
	nextEnd = Math.max(nextEnd, s + 30);
	nextEnd = snapDown30(nextEnd);
	endTime.value = fromMinutes(nextEnd);
};
const canExtend = computed(() => toMinutes(endTime.value) + 30 <= CLOSE_MIN);
const canShrink = computed(
	() => toMinutes(endTime.value) - toMinutes(startTime.value) > 30
);

// ====== data stores ======
const roomStore = useRoomStore();
const bookingStore = useBookingStore();
const { allRooms, availableRooms, isLoading } = storeToRefs(roomStore);

onMounted(() => {
	roomStore.fetchAllRooms();
	if (isToday.value && toMinutes(startTime.value) < minStartMinutes.value) {
		startTime.value = fromMinutes(minStartMinutes.value);
		endTime.value = fromMinutes(
			Math.min(minStartMinutes.value + 30, CLOSE_MIN)
		);
	}

	if (isValidRange.value) {
		roomStore.fetchAvailableRooms(isoStartAt.value, isoEndAt.value);
	}
});

watch([isoStartAt, isoEndAt], () => {
	if (isValidRange.value) {
		roomStore.fetchAvailableRooms(isoStartAt.value, isoEndAt.value);
	} else {
		availableRooms.value = [];
	}
});

watch(selectedDate, () => {
	if (!isToday.value) {
		startTime.value = "08:00";
		endTime.value = "08:30";
	} else if (toMinutes(startTime.value) < minStartMinutes.value) {
		startTime.value = fromMinutes(minStartMinutes.value);
		endTime.value = fromMinutes(
			Math.min(minStartMinutes.value + 30, CLOSE_MIN)
		);
	}
});

watch(startTime, (newStart) => {
	const ms = toMinutes(newStart);
	if (ms < minStartMinutes.value)
		startTime.value = fromMinutes(minStartMinutes.value);
	if (ms > LATEST_START) startTime.value = fromMinutes(LATEST_START);
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

const isRoomAvailable = (roomId) =>
	availableRooms.value.some((room) => room.id === roomId);

const showModal = ref(false);
const selectedRoom = ref(null);
const bookingTitle = ref("");
const bookingNotes = ref("");
const bookingError = ref(null);

const openBookingModal = (room) => {
	if (!isRoomAvailable(room.id)) return;
	selectedRoom.value = room;
	bookingTitle.value = "";
	bookingNotes.value = "";
	bookingError.value = null;
	showModal.value = true;
};

const handleConfirmBooking = async () => {
	bookingError.value = null;
	isSubmitting.value = true;
	try {
		const payload = {
			roomId: selectedRoom.value.id,
			title: bookingTitle.value,
			startAt: isoStartAt.value,
			endAt: isoEndAt.value,
			notes: bookingNotes.value,
		};
		await bookingStore.createBooking(payload);

		toast.success("จองสำเร็จ 🎉");
		showModal.value = false;

		await roomStore.fetchAvailableRooms(isoStartAt.value, isoEndAt.value);
		await nextTick();
		document.getElementById(`room-${selectedRoom.value.id}`)?.scrollIntoView({
			behavior: "smooth",
			block: "center",
		});
	} catch (error) {
		const msg =
			error?.response?.data?.message ??
			error?.response?.data?.error ??
			error?.message ??
			"Unknown error";
		bookingError.value = "เกิดข้อผิดพลาด: " + msg;
		toast.error(bookingError.value);
		console.error("createBooking failed:", error);
	} finally {
		isSubmitting.value = false;
	}
};
</script>

<template>
	<div class="p-4 space-y-6">
		<h1 class="text-3xl font-bold text-gray-900">Book Room</h1>

		<!-- Calendar -->
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
			<div class="flex flex-wrap items-center justify-between gap-2">
				<div
					class="px-3 py-1 rounded-full bg-gray-50 ring-1 ring-gray-200 text-sm"
				>
					วันที่ที่เลือก: <span class="font-semibold">{{ formattedDate }}</span>
				</div>
			</div>
		</div>

		<fieldset class="rounded-xl bg-white ring-1 ring-gray-200 p-3">
			<legend class="px-1 text-xs font-medium text-gray-500">Time</legend>

			<div class="flex items-center justify-between">
				<span v-if="dayClosed" class="text-xs text-red-600"
					>หมดเวลาจองสำหรับวันนี้แล้ว</span
				>
				<span v-else class="text-[11px] text-gray-500"
					>เปิดจอง 08:00 – 22:00</span
				>
			</div>

			<div class="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-2 sm:gap-3">
				<!-- Start -->
				<div class="flex flex-col gap-1">
					<span class="text-xs text-gray-600">Start</span>
					<div class="relative">
						<input
							id="startTime"
							type="time"
							inputmode="numeric"
							v-model="startTime"
							:min="minStartStr"
							:max="maxStartStr"
							step="1800"
							:disabled="dayClosed"
							@focus="lastActive = 'start'"
							@change="onStartInput"
							class="h-10 w-full rounded-lg border border-gray-200 bg-white pl-3 pr-8 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300"
						/>
						<!-- ⏰ แสดงเฉพาะตอนหมดเวลา -->
						<svg
							v-if="dayClosed"
							class="pointer-events-none absolute right-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
							viewBox="0 0 24 24"
							fill="none"
						>
							<path
								d="M12 6v6l4 2"
								stroke="currentColor"
								stroke-width="1.5"
								stroke-linecap="round"
							/>
							<circle
								cx="12"
								cy="12"
								r="9"
								stroke="currentColor"
								stroke-width="1.5"
							/>
						</svg>
					</div>
				</div>

				<!-- End -->
				<div class="flex flex-col gap-1">
					<span class="text-xs text-gray-600">End</span>
					<div class="relative">
						<input
							id="endTime"
							type="time"
							inputmode="numeric"
							v-model="endTime"
							:min="minEndStr"
							:max="maxEndStr"
							step="1800"
							:disabled="dayClosed"
							@focus="lastActive = 'end'"
							@change="onEndInput"
							class="h-10 w-full rounded-lg border border-gray-200 bg-white pl-3 pr-8 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300"
						/>
						<svg
							v-if="dayClosed"
							class="pointer-events-none absolute right-2 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
							viewBox="0 0 24 24"
							fill="none"
						>
							<path
								d="M12 6v6l4 2"
								stroke="currentColor"
								stroke-width="1.5"
								stroke-linecap="round"
							/>
							<circle
								cx="12"
								cy="12"
								r="9"
								stroke="currentColor"
								stroke-width="1.5"
							/>
						</svg>
					</div>
				</div>
			</div>

			<!-- Chips: Minimal -->
			<div class="mt-2 flex justify-end gap-1.5">
				<button
					type="button"
					@click="onMinus(30)"
					:disabled="dayClosed || !canShrink"
					class="rounded-full border border-gray-200 px-3 py-1 text-[12px] disabled:opacity-50 disabled:cursor-not-allowed text-gray-600 hover:bg-gray-50"
				>
					–30m
				</button>

				<button
					type="button"
					@click="onPlus(30)"
					:disabled="dayClosed || !canExtend"
					class="rounded-full border border-gray-200 px-3 py-1 text-[12px] disabled:opacity-50 disabled:cursor-not-allowed text-gray-600 hover:bg-gray-50"
				>
					+30m
				</button>
			</div>
		</fieldset>

		<!-- Warning -->
		<div v-if="!isValidRange" class="mt-2 text-sm text-red-600">
			กรุณาเลือกช่วงเวลาให้ End > Start อย่างน้อย 30 นาที
		</div>

		<div>
			<h2 class="text-xl font-bold text-gray-900 mb-4">Rooms</h2>

			<div v-if="isLoading" class="text-center text-gray-600">
				กำลังค้นหาห้องว่าง…
			</div>
			<div v-else-if="!isValidRange" class="text-center text-gray-600">
				กรุณาเลือกช่วงเวลาที่ถูกต้อง
			</div>
			<div
				v-else-if="availableRooms.length === 0"
				class="text-center text-gray-600"
			>
				ไม่มีห้องว่างสำหรับ {{ startTime }}–{{ endTime }} ({{ formattedDate }})
			</div>

			<div v-else class="space-y-4">
				<div
					v-for="room in allRooms"
					:key="room.id"
					:id="`room-${room.id}`"
					@click="
						isRoomAvailable(room.id) && isValidRange
							? openBookingModal(room)
							: null
					"
					:class="[
						'p-4 rounded-2xl transition-all ring-1',
						isRoomAvailable(room.id) && isValidRange
							? 'bg-white ring-gray-200 hover:ring-blue-300 cursor-pointer'
							: 'bg-gray-100 ring-gray-200 opacity-60 cursor-not-allowed',
					]"
				>
					<h2
						class="text-lg font-semibold"
						:class="[
							isRoomAvailable(room.id) ? 'text-blue-700' : 'text-gray-600',
						]"
					>
						{{ room.name }}
					</h2>
					<span
						v-if="!isRoomAvailable(room.id)"
						class="text-sm font-bold text-red-600"
						>(Booked)</span
					>

					<div class="flex items-center mt-2 text-sm text-gray-600">
						<UsersIcon class="w-4 h-4 mr-2" />
						<span>Capacity: {{ room.capacity }}</span>
					</div>
					<div
						v-if="room.location"
						class="flex items-center mt-1 text-sm text-gray-600"
					>
						<MapPinIcon class="w-4 h-4 mr-2" />
						<span>{{ room.location }}</span>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div
		v-if="showModal"
		class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 supports-[backdrop-filter]:backdrop-blur-[1.5px]"
	>
		<div
			class="w-full max-w-sm p-6 mx-4 rounded-2xl bg-white/95 ring-1 ring-black/5 shadow-xl"
		>
			<h2 class="text-xl font-semibold mb-4">Confirm Booking</h2>
			<div class="mb-3 space-y-1 text-sm">
				<div>
					Room: <span class="font-medium">{{ selectedRoom?.name }}</span>
				</div>
				<div>
					Date: <span class="font-medium">{{ formattedDate }}</span>
				</div>
				<div>
					Time: <span class="font-medium">{{ startTime }} - {{ endTime }}</span>
				</div>
				<div class="text-gray-600">
					Duration:
					{{ ((toMinutes(endTime) - toMinutes(startTime)) / 60).toFixed(1) }}
					ชั่วโมง
				</div>
			</div>

			<form @submit.prevent="handleConfirmBooking" class="space-y-4 mt-4">
				<div>
					<label for="title" class="block text-sm font-medium text-gray-700"
						>Title</label
					>
					<input
						v-model="bookingTitle"
						type="text"
						id="title"
						required
						class="w-full mt-1 px-3 py-2 rounded-md bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
					/>
				</div>
				<div>
					<label for="notes" class="block text-sm font-medium text-gray-700"
						>Notes (Optional)</label
					>
					<textarea
						v-model="bookingNotes"
						id="notes"
						rows="2"
						class="w-full mt-1 px-3 py-2 rounded-md bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
					></textarea>
				</div>

				<div v-if="bookingError" class="text-sm text-red-600">
					{{ bookingError }}
				</div>

				<div class="flex gap-4">
					<button
						type="button"
						@click="showModal = false"
						class="w-full px-4 py-2 text-gray-700 bg-soft-bg rounded-lg shadow-neumorphism hover:shadow-neumorphism-inset"
					>
						Cancel
					</button>
					<button
						type="submit"
						:disabled="!isValidRange || !bookingTitle || isSubmitting"
						class="w-full px-4 py-2 text-white rounded-lg shadow-md flex items-center justify-center gap-2"
						:class="
							!isValidRange || !bookingTitle || isSubmitting
								? 'bg-blue-300 cursor-not-allowed'
								: 'bg-blue-600 hover:bg-blue-700'
						"
						title="เลือกช่วงเวลาให้ถูกต้องและกรอก Title ก่อนยืนยัน"
					>
						<svg
							v-if="isSubmitting"
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
						<span>{{ isSubmitting ? "Booking…" : "Confirm" }}</span>
					</button>
				</div>
			</form>
		</div>
		<Toaster position="top-center" rich-colors />
	</div>
</template>
<!-- ใน SFC เดียวกัน เพิ่ม style ท้ายไฟล์ -->
<style scoped>
/* บังคับให้ container ของ v-calendar กว้างเต็ม */
:deep(.vc-container) {
	width: 100%;
}
</style>
