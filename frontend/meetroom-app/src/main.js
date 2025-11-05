import { createApp } from "vue";
import { createPinia } from "pinia";
import router from "./router";
import App from "./App.vue";
import "./style.css";

import api from "./services/api";
import { useAuthStore } from "./stores/authStore";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

const authStore = useAuthStore();

api.interceptors.request.use(
	(config) => {
		const token = authStore.token;
		if (token) {
			config.headers["Authorization"] = `Bearer ${token}`;
		}
		return config;
	},
	(error) => {
		return Promise.reject(error);
	}
);

api.interceptors.response.use(
	(response) => {
		return response;
	},
	(error) => {
		if (
			error.response &&
			(error.response.status === 401 || error.response.status === 403)
		) {
			authStore.logout();
		}
		return Promise.reject(error);
	}
);

app.mount("#app");
