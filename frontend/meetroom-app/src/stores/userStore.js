import { defineStore } from "pinia";
import { ref } from "vue";
import api from "../services/api";
import router from "../router";

export const useUserStore = defineStore("userAdmin", () => {
	// === STATE ===
	const userList = ref([]);
	const isLoading = ref(false);

	// === ACTIONS ===
	async function fetchAllUsers() {
		isLoading.value = true;
		try {
			// API: GET /api/users
			const response = await api.get("/users?size=200&sort=fullName");
			userList.value = response.data.content;
		} catch (error) {
			console.error("Failed to fetch users:", error);
			userList.value = [];
		} finally {
			isLoading.value = false;
		}
	}

	async function fetchUser(id) {
		try {
			// API: GET /api/users/{id}
			const response = await api.get(`/users/${id}`);
			return response.data;
		} catch (error) {
			console.error("Failed to fetch user:", error);
			router.push({ name: "AdminManageUsers" });
			return null;
		}
	}

	async function updateUser(id, userData) {
		try {
			const response = await api.put(`/users/${id}`, userData);
			const index = userList.value.findIndex((u) => u.id === id);
			if (index !== -1) {
				userList.value[index] = response.data;
			}

			router.push({ name: "AdminManageUsers" });
		} catch (error) {
			console.error("Failed to update user:", error);
			throw error;
		}
	}

	return {
		userList,
		isLoading,
		fetchAllUsers,
		fetchUser,
		updateUser,
	};
});
