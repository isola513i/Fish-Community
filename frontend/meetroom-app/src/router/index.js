import { createRouter, createWebHistory } from "vue-router";

const routes = [
	{
		path: "/",
		name: "Home",
		// component: Home,
		component: () => import("../views/MyBookings.vue"), // Lazy load
		meta: { requiresAuth: true }, // หน้านี้ต้อง Login ก่อน
	},
	{
		path: "/login",
		name: "Login",
		// component: Login
		component: () => import("../views/Login.vue"),
		meta: { requiresAuth: false },
	},
	{
		path: "/register",
		name: "Register",
		component: () => import("../views/Register.vue"),
		meta: { requiresAuth: false },
	},
	// TODO: เพิ่ม Routes สำหรับหน้าอื่นๆ ที่นี่
	// { path: '/book', name: 'BookRoom', ... }
	// { path: '/profile', name: 'Profile', ... }
	// { path: '/admin/rooms', name: 'AdminManageRooms', ... }
];

const router = createRouter({
	history: createWebHistory(),
	routes,
});

// Navigation Guard
router.beforeEach((to, from, next) => {
	const token = localStorage.getItem("authToken"); // (เราจะใช้ Pinia จัดการทีหลัง แต่นี่คือ Basic)

	if (to.meta.requiresAuth && !token) {
		next({ name: "Login" });
	} else {
		next();
	}
});

export default router;
