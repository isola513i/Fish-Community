import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/api';
import router from '../router';

export const useBookingStore = defineStore('booking', () => {
  const myBookings = ref([]);
  const allBookings = ref([]);
  const isLoading = ref(false);

  async function fetchMyBookings() {
    isLoading.value = true;
    try {
      const response = await api.get('/bookings/my-bookings?size=100');
      myBookings.value = response.data.content;
    } catch (error) {
      console.error('Failed to fetch bookings:', error);
      myBookings.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  async function getBookingById(id) {
    if (myBookings.value.length === 0) {
      await fetchMyBookings();
    }
    return myBookings.value.find((b) => b.id === id);
  }

  async function fetchAllBookings() {
    isLoading.value = true;
    try {
      // API: GET /api/bookings/all
      const response = await api.get(
        '/bookings/all?size=100&sort=startAt,desc'
      );
      allBookings.value = response.data.content;
    } catch (error) {
      console.error('Failed to fetch all bookings:', error);
      allBookings.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  async function updateBooking(id, bookingData) {
    try {
      const response = await api.put(`/bookings/${id}`, bookingData);
      const index = myBookings.value.findIndex((b) => b.id === id);
      if (index !== -1) {
        myBookings.value[index] = response.data;
      }

      router.push({ name: 'MyBookings' });
    } catch (error) {
      console.error('Failed to update booking:', error);
      throw error;
    }
  }

  async function cancelBooking(id) {
    const originalBookings = [...myBookings.value];
    myBookings.value = myBookings.value.filter((b) => b.id !== id);
    try {
      await api.delete(`/bookings/${id}`);
    } catch (error) {
      console.error('Failed to cancel booking, rolling back:', error);
      myBookings.value = originalBookings;
      toast.error('Failed to cancel booking. Please try again.');
      throw error;
    }
  }

  async function createBooking(bookingData) {
    try {
      const response = await api.post('/bookings', bookingData);
      myBookings.value.unshift(response.data);

      router.push({ name: 'MyBookings' });
    } catch (error) {
      console.error('Failed to create booking:', error);
      throw error;
    }
  }

  return {
    myBookings,
    isLoading,
    allBookings,
    fetchAllBookings,
    fetchMyBookings,
    cancelBooking,
    createBooking,
    getBookingById,
    updateBooking,
  };
});
