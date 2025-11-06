<script setup>
import { onMounted } from 'vue';
import { useBookingStore } from '../stores/bookingStore';
import { storeToRefs } from 'pinia';

const bookingStore = useBookingStore();
const { myBookings, isLoading } = storeToRefs(bookingStore);

onMounted(() => {
  bookingStore.fetchMyBookings();
});

const handleCancel = async (id) => {
  if (confirm('Are you sure you want to cancel this booking?')) {
    await bookingStore.cancelBooking(id);
  }
};

const formatTime = (isoString) => {
  if (!isoString) return '';
  const date = new Date(isoString);
  return date.toLocaleString('en-US', {
    dateStyle: 'short',
    timeStyle: 'short',
  });
};
</script>

<template>
  <div class="p-4 space-y-6">
    <h1 class="text-3xl font-bold text-gray-900">My Bookings</h1>

    <div v-if="isLoading" class="text-center">Loading...</div>

    <div
      v-if="!isLoading && myBookings.length === 0"
      class="p-6 text-center text-gray-600 bg-soft-bg rounded-2xl shadow-neumorphism"
    >
      You have no bookings.
    </div>

    <div vV-if="!isLoading" class="space-y-4">
      <div
        v-for="booking in myBookings"
        :key="booking.id"
        class="p-4 bg-soft-bg rounded-2xl shadow-neumorphism"
      >
        <h2 class="text-lg font-semibold text-blue-700">{{ booking.title }}</h2>
        <p class="text-sm text-gray-700">Room: {{ booking.room.name }}</p>
        <p class="text-sm text-gray-600">
          From: {{ formatTime(booking.startAt) }}
        </p>
        <p class="text-sm text-gray-600">To: {{ formatTime(booking.endAt) }}</p>
        <p
          v-if="booking.status === 'CANCELLED'"
          class="text-sm font-bold text-red-600"
        >
          CANCELLED
        </p>

        <button
          v-if="booking.status === 'CONFIRMED'"
          @click="handleCancel(booking.id)"
          class="w-full px-4 py-2 mt-4 font-medium text-red-600 bg-soft-bg rounded-lg shadow-neumorphism hover:shadow-neumorphism-inset"
        >
          Cancel Booking
        </button>
      </div>
    </div>
  </div>
</template>
