import { createApp } from "vue";
import { createPinia } from "pinia";
import router from "./router";
import App from "./App.vue";
import "./style.css";

import VCalendar from "v-calendar";
import "v-calendar/style.css";
import "vue-sonner/style.css";
import { Toaster } from "vue-sonner";

import api from "./services/api";
import { useAuthStore } from "./stores/authStore";

const app = createApp(App);
app.component("Toaster", Toaster);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(VCalendar, {});

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
