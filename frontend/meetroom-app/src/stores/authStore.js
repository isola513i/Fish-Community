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

			router.push({ name: "Home" });
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
			const userRes = await api.get("/users/me");

			setAuthData(tokenRes.data, userRes.data);

			router.push({ name: "Home" });
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

	return { token, user, isLoggedIn, isAdmin, login, register, logout };
});
