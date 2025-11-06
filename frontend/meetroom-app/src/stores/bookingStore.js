import { defineStore } from "pinia";
import { ref } from "vue";
import api from "../services/api";
import router from "../router";

export const useBookingStore = defineStore("booking", () => {
	const myBookings = ref([]);
	const isLoading = ref(false);

	async function fetchMyBookings() {
		isLoading.value = true;
		try {
			const response = await api.get("/bookings/my-bookings?size=100");
			myBookings.value = response.data.content;
		} catch (error) {
			console.error("Failed to fetch bookings:", error);
			myBookings.value = [];
		} finally {
			isLoading.value = false;
		}
	}

	async function cancelBooking(id) {
		try {
			await api.delete(`/bookings/${id}`);
			myBookings.value = myBookings.value.filter((b) => b.id !== id);
		} catch (error) {
			console.error("Failed to cancel booking:", error);
			throw error;
		}
	}

	async function createBooking(bookingData) {
		try {
			const response = await api.post("/bookings", bookingData);
			myBookings.value.unshift(response.data);

			router.push({ name: "MyBookings" });
		} catch (error) {
			console.error("Failed to create booking:", error);
			throw error;
		}
	}

	return {
		myBookings,
		isLoading,
		fetchMyBookings,
		cancelBooking,
		createBooking,
	};
});
