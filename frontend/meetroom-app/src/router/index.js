import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/authStore";
import AppLayout from "../layouts/AppLayout.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import MyBooking from "../views/MyBooking.vue";
import BookRoom from "../views/BookRoom.vue";
import Profile from "../views/Profile.vue";
import AdminManageRooms from "../views/admin/ManageRooms.vue";

const routes = [
	// --- No need to Login ---
	{
		path: "/login",
		name: "Login",
		component: Login,
		meta: { requiresAuth: false },
	},
	{
		path: "/register",
		name: "Register",
		component: Register,
		meta: { requiresAuth: false },
	},

	// --- Must to Login  ---
	{
		path: "/",
		component: AppLayout,
		meta: { requiresAuth: true },
		children: [
			{
				path: "",
				name: "MyBookings",
				component: MyBooking,
			},
			{
				path: "book",
				name: "BookRoom",
				component: BookRoom,
			},
			{
				path: "profile",
				name: "Profile",
				component: Profile,
			},
			{
				path: "admin/rooms",
				name: "AdminManageRooms",
				component: AdminManageRooms,
				meta: { requiresAdmin: true },
			},
			// TODO: เพิ่มหน้าอื่นๆ ที่ต้อง Login ที่นี่
		],
	},
];

const router = createRouter({
	history: createWebHistory(),
	routes,
});

// Navigation Guard
router.beforeEach((to, from, next) => {
	const authStore = useAuthStore();
	const isLoggedIn = authStore.isLoggedIn;
	const isAdmin = authStore.isAdmin;

	if (to.meta.requiresAuth && !isLoggedIn) {
		authStore.returnUrl = to.fullPath;
		next({ name: "Login" });
	} else if (to.meta.requiresAdmin && !isAdmin) {
		next({ name: "MyBookings" });
	} else if (
		!to.meta.requiresAuth &&
		isLoggedIn &&
		(to.name === "Login" || to.name === "Register")
	) {
		next({ name: "MyBookings" });
	} else {
		next();
	}
});

export default router;
