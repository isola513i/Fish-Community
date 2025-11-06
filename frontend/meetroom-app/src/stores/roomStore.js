import { defineStore } from "pinia";
import { ref } from "vue";
import api from "../services/api";
import router from "../router";

export const useRoomStore = defineStore("room", () => {
	// === STATES ===
	const allRooms = ref([]); 
	const availableRooms = ref([]); 
	const adminRoomList = ref([]); 
	const isLoading = ref(false);

	// === ACTIONS ===
	async function fetchAllRooms() {
		isLoading.value = true;
		try {
			const response = await api.get("/rooms?size=100&sort=name");
			allRooms.value = response.data.content.filter((room) => room.isActive);
		} catch (error) {
			console.error("Failed to fetch all rooms:", error);
		} finally {
			isLoading.value = false;
		}
	}

	async function fetchAvailableRooms(startAt, endAt) {
		isLoading.value = true;
		availableRooms.value = [];
		try {
			const response = await api.get("/rooms/available", {
				params: { startAt, endAt },
			});
			availableRooms.value = response.data;
		} catch (error) {
			console.error("Failed to fetch available rooms:", error);
		} finally {
			isLoading.value = false;
		}
	}

	async function fetchAdminRoomList() {
		isLoading.value = true;
		try {
			const response = await api.get("/rooms/admin-list");
			adminRoomList.value = response.data;
		} catch (error) {
			console.error("Failed to fetch admin room list:", error);
			adminRoomList.value = [];
		} finally {
			isLoading.value = false;
		}
	}

	async function createRoom(roomData) {
		try {
			await api.post("/rooms", roomData);
			router.push({ name: "AdminManageRooms" });
		} catch (error) {
			console.error("Failed to create room:", error);
			throw error;
		}
	}

	async function fetchRoom(id) {
		try {
			const response = await api.get(`/rooms/${id}`);
			return response.data;
		} catch (error) {
			console.error("Failed to fetch room:", error);
			router.push({ name: "AdminManageRooms" });
		}
	}

	async function updateRoom(id, roomData) {
		try {
			const response = await api.put(`/rooms/${id}`, roomData);
			const updatedRoom = response.data;
			const numericId = parseInt(id, 10);

			const index = adminRoomList.value.findIndex(
				(item) => item.room.id === numericId
			);
			if (index !== -1) {
				adminRoomList.value[index].room = response.data;
			}
			const userIndex = allRooms.value.findIndex(
				(room) => room.id === numericId
			);
			if (userIndex !== -1) {
				allRooms.value[userIndex] = updatedRoom;
			} else {
				allRooms.value.push(updatedRoom);
			}

			router.push({ name: "AdminManageRooms" });
		} catch (error) {
			console.error("Failed to update room:", error);
			throw error;
		}
	}

	// === RETURN ===
	return {
		allRooms,
		availableRooms,
		adminRoomList,
		isLoading,
		fetchAllRooms,
		fetchAvailableRooms,
		fetchAdminRoomList,
		createRoom,
		fetchRoom,
		updateRoom, 
	};
});
