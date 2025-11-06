<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoomStore } from '../stores/roomStore';
import { useBookingStore } from '../stores/bookingStore';
import { storeToRefs } from 'pinia';
import {
  UsersIcon,
  MapPinIcon,
  ChevronDownIcon,
} from '@heroicons/vue/24/outline'; // 1. Import ChevronDownIcon

// Setup V-Calendar
import { Calendar } from 'v-calendar';
import 'v-calendar/style.css';

// Setup Stores
const roomStore = useRoomStore();
const bookingStore = useBookingStore();
const { allRooms, availableRooms, isLoading } = storeToRefs(roomStore);

// State สำหรับฟอร์ม
const selectedDate = ref(new Date());
const startTime = ref('09:00'); // 9:00 AM
const endTime = ref('10:00'); // 10:00 AM

// ⭐️ 2. สร้างฟังก์ชันสำหรับสร้างตัวเลือกเวลา ⭐️
const generateTimeSlots = () => {
  const slots = [];
  // สร้าง Slot เวลาทุก 30 นาที ตั้งแต่ 8:00 - 18:00
  for (let hour = 8; hour <= 18; hour++) {
    const h = String(hour).padStart(2, '0');
    slots.push({ text: `${h}:00`, value: `${h}:00` });
    if (hour < 18) {
      // ไม่เอา 18:30
      slots.push({ text: `${h}:30`, value: `${h}:30` });
    }
  }
  return slots;
};
const timeSlots = ref(generateTimeSlots());
// ------------------------------------------

// State สำหรับ Modal (เหมือนเดิม)
const showModal = ref(false);
const selectedRoom = ref(null);
const bookingTitle = ref('');
const bookingNotes = ref('');
const bookingError = ref(null);

// ดึงข้อมูลห้องทั้งหมดมาเตรียมไว้ (เหมือนเดิม)
onMounted(() => {
  roomStore.fetchAllRooms();
});

// แปลง Date + Time เป็น ISO String (UTC) (เหมือนเดิม)
const getISOString = (date, time) => {
  if (!date || !time) return null;
  const [hours, minutes] = time.split(':');
  const newDate = new Date(date);
  newDate.setHours(hours, minutes, 0, 0);
  return newDate.toISOString();
};

// Computed: สร้าง startAt และ endAt (เหมือนเดิม)
const isoStartAt = computed(() =>
  getISOString(selectedDate.value, startTime.value)
);
const isoEndAt = computed(() =>
  getISOString(selectedDate.value, endTime.value)
);

// (สำคัญ) เมื่อไหร่ก็ตามที่เวลาเปลี่ยน ให้ค้นหาห้องว่างใหม่ (เหมือนเดิม)
watch([isoStartAt, isoEndAt], () => {
  if (
    isoStartAt.value &&
    isoEndAt.value &&
    new Date(isoStartAt.value) < new Date(isoEndAt.value)
  ) {
    roomStore.fetchAvailableRooms(isoStartAt.value, isoEndAt.value);
  } else {
    availableRooms.value = [];
  }
});

// Computed: เช็คว่าห้องนี้ว่างหรือไม่ (เหมือนเดิม)
const isRoomAvailable = (roomId) => {
  return availableRooms.value.some((room) => room.id === roomId);
};

// Actions (เหมือนเดิม)
const openBookingModal = (room) => {
  if (!isRoomAvailable(room.id)) return;
  selectedRoom.value = room;
  bookingTitle.value = '';
  bookingNotes.value = '';
  bookingError.value = null;
  showModal.value = true;
};

const handleConfirmBooking = async () => {
  bookingError.value = null;
  try {
    const bookingData = {
      roomId: selectedRoom.value.id,
      title: bookingTitle.value,
      startAt: isoStartAt.value,
      endAt: isoEndAt.value,
      notes: bookingNotes.value,
    };
    await bookingStore.createBooking(bookingData);
    showModal.value = false;
  } catch (error) {
    bookingError.value = 'เกิดข้อผิดพลาด: ' + error.response?.data?.message;
  }
};
</script>

<template>
  <div class="p-4 space-y-6">
    <h1 class="text-3xl font-bold text-gray-900">Book Room</h1>

    <div class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism">
      <Calendar
        v-model="selectedDate"
        :min-date="new Date()"
        :attributes="[{ key: 'today', dot: true, dates: new Date() }]"
        is-required
        title-position="left"
        class="w-full border-none"
      />
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism-inset">
        <label for="startTime" class="block text-sm font-medium text-gray-700"
          >Start Time</label
        >
        <div class="relative mt-1">
          <select
            v-model="startTime"
            id="startTime"
            class="w-full px-3 py-2 pr-8 text-gray-900 bg-white border-none rounded-md shadow-inner appearance-none cursor-pointer focus:ring-2 focus:ring-blue-500"
          >
            <option
              v-for="slot in timeSlots"
              :key="slot.value"
              :value="slot.value"
            >
              {{ slot.text }}
            </option>
          </select>
          <ChevronDownIcon
            class="w-5 h-5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none"
          />
        </div>
      </div>

      <div class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism-inset">
        <label for="endTime" class="block text-sm font-medium text-gray-700"
          >End Time</label
        >
        <div class="relative mt-1">
          <select
            v-model="endTime"
            id="endTime"
            class="w-full px-3 py-2 pr-8 text-gray-900 bg-white border-none rounded-md shadow-inner appearance-none cursor-pointer focus:ring-2 focus:ring-blue-500"
          >
            <option
              v-for="slot in timeSlots"
              :key="slot.value"
              :value="slot.value"
            >
              {{ slot.text }}
            </option>
          </select>
          <ChevronDownIcon
            class="w-5 h-5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none"
          />
        </div>
      </div>
    </div>
    <div>
      <h2 class="text-xl font-bold text-gray-900 mb-4">Rooms</h2>

      <div v-if="isLoading" class="text-center">Loading...</div>

      <div
        v-if="
          !isoStartAt || !isoEndAt || new Date(isoStartAt) >= new Date(isoEndAt)
        "
        class="text-center text-gray-600"
      >
        กรุณาเลือกช่วงเวลาที่ถูกต้อง
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="room in allRooms"
          :key="room.id"
          @click="openBookingModal(room)"
          :class="[
            'p-4 rounded-2xl transition-all',
            isRoomAvailable(room.id)
              ? 'bg-soft-bg shadow-neumorphism cursor-pointer hover:shadow-neumorphism-inset'
              : 'bg-gray-200 opacity-50 shadow-inner',
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
          >
            (Booked)</span
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
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div
      class="w-full max-w-sm p-6 mx-4 bg-soft-bg rounded-2xl shadow-neumorphism"
    >
      <h2 class="text-xl font-semibold mb-4">Confirm Booking</h2>
      <p class="mb-2">
        Room: <span class="font-medium">{{ selectedRoom?.name }}</span>
      </p>
      <p class="text-sm">Time: {{ startTime }} - {{ endTime }}</p>

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
            class="w-full mt-1 border-gray-300 rounded-md shadow-sm bg-white"
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
            class="w-full mt-1 border-gray-300 rounded-md shadow-sm bg-white"
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
            class="w-full px-4 py-2 text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
          >
            Confirm
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
