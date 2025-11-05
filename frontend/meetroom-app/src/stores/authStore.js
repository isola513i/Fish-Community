import { defineStore } from "pinia";
import { ref, computed } from "vue";
import api from "../services/api";
import router from "../router";

export const useAuthStore = defineStore("auth", () => {
	// === State ===
	const token = ref(localStorage.getItem("authToken") || null);
	const user = ref(JSON.parse(localStorage.getItem("authUser")) || null);

	// === Getters ===
	const isLoggedIn = computed(() => !!token.value);
	const isAdmin = computed(() => user.value?.role === "ADMIN");

	// === Actions ===
	function setAuthData(tokenResponse, userResponse) {
		token.value = tokenResponse.accessToken;
		user.value = userResponse;

		localStorage.setItem("authToken", tokenResponse.accessToken);
		localStorage.setItem("authUser", JSON.stringify(userResponse));

		api.defaults.headers.common[
			"Authorization"
		] = `Bearer ${tokenResponse.accessToken}`;
	}

	async function login(email, password) {
		try {
			const loginReq = { email, password };
			const tokenRes = await api.post("/auth/login", loginReq);

			api.defaults.headers.common[
				"Authorization"
			] = `Bearer ${tokenRes.data.accessToken}`;
			const userRes = await api.get("/users/me");

			setAuthData(tokenRes.data, userRes.data);

			router.push({ name: "MyBookings" });
		} catch (error) {
			console.error("Login failed:", error);
			// TODO: แสดง error message บน UI
			throw error;
		}
	}

	async function register(fullName, email, password) {
		try {
			const registerReq = { fullName, email, password, isAdmin: false };
			const tokenRes = await api.post("/auth/register", registerReq);

			api.defaults.headers.common[
				"Authorization"
			] = `Bearer ${tokenRes.data.accessToken}`;

			router.push({
				name: "Login",
				query: { registered: "true" },
			});
		} catch (error) {
			console.error("Register failed:", error);
			throw error;
		}
	}

	function logout() {
		token.value = null;
		user.value = null;
		localStorage.removeItem("authToken");
		localStorage.removeItem("authUser");
		delete api.defaults.headers.common["Authorization"];
		router.push({ name: "Login" });
	}

	async function updateProfile(newFullName) {
		try {
			const profileDto = {
				fullName: newFullName,
				timezone: user.value.timezone || "Asia/Bangkok",
			};

			const res = await api.put("/users/me", profileDto);

			user.value = res.data;
			localStorage.setItem("authUser", JSON.stringify(res.data));
		} catch (error) {
			console.error("Update profile failed:", error);
			throw error;
		}
	}

	async function changePassword(oldPass, newPass) {
		try {
			const passwordDto = {
				oldPassword: oldPass,
				newPassword: newPass,
			};

			await api.put("/users/me/change-password", passwordDto);
		} catch (error) {
			console.error("Change password failed:", error);
			throw error;
		}
	}

	return {
		token,
		user,
		isLoggedIn,
		isAdmin,
		login,
		register,
		logout,
		updateProfile,
		changePassword,
	};
});
