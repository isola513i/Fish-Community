import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/authStore";
import AppLayout from "../layouts/AppLayout.vue";
import AuthLayout from "../layouts/AuthLayout.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import MyBookings from "../views/MyBookings.vue";
import BookRoom from "../views/BookRoom.vue";
import Profile from "../views/Profile.vue";
import EditProfile from "../views/EditProfile.vue";
import ChangePassword from "../views/ChangePassword.vue";
import CreateRoom from "../views/admin/CreateRoom.vue";
import ManageRooms from "../views/admin/ManageRooms.vue";
import EditRoom from "../views/admin/EditRoom.vue";
import EditBooking from "../views/EditBooking.vue";
import AdminManageUsers from "../views/admin/AdminManageUsers.vue";
import AdminEditUser from "../views/admin/AdminEditUser.vue";
import AllBookings from "../views/admin/AllBookings.vue";
import Welcome from "../views/Welcome.vue";

const routes = [
	{
		path: "/auth",
		component: AuthLayout,
		meta: { requiresAuth: false },
		children: [
			{
				path: "welcome",
				name: "Welcome",
				component: Welcome,
			},
			{
				path: "login",
				name: "Login",
				component: Login,
			},
			{
				path: "register",
				name: "Register",
				component: Register,
			},
		],
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
				component: MyBookings,
			},
			{
				path: "book",
				name: "BookRoom",
				component: BookRoom,
			},
			{
				path: "bookings/edit/:id",
				name: "EditBooking",
				component: EditBooking,
				meta: { requiresAuth: true },
				props: true,
			},
			{
				path: "profile",
				name: "Profile",
				component: Profile,
			},
			{
				path: "profile/edit",
				name: "EditProfile",
				component: EditProfile,
			},
			{
				path: "profile/change-password",
				name: "ChangePassword",
				component: ChangePassword,
			},
			{
				path: "admin/rooms",
				name: "AdminManageRooms",
				component: ManageRooms,
				meta: { requiresAdmin: true },
			},
			{
				path: "admin/rooms/create",
				name: "AdminCreateRoom",
				component: CreateRoom,
				meta: { requiresAuth: true, requiresAdmin: true },
			},
			{
				path: "admin/rooms/edit/:id",
				name: "AdminEditRoom",
				component: EditRoom,
				meta: { requiresAuth: true, requiresAdmin: true },
				props: true,
			},
			{
				path: "admin/users",
				name: "AdminManageUsers",
				component: AdminManageUsers,
				meta: { requiresAuth: true, requiresAdmin: true },
			},
			{
				path: "admin/users/edit/:id",
				name: "AdminEditUser",
				component: AdminEditUser,
				meta: { requiresAuth: true, requiresAdmin: true },
				props: true,
			},
			{
				path: "admin/bookings",
				name: "AdminAllBookings",
				component: AllBookings,
				meta: { requiresAuth: true, requiresAdmin: true },
			},
		],
	},
];

const router = createRouter({
	history: createWebHistory(),
	routes,
});

// Navigation Guard ⬇️
router.beforeEach((to, from, next) => {
	const authStore = useAuthStore();
	const isLoggedIn = authStore.isLoggedIn;
	const isAdmin = authStore.isAdmin;

	if (to.meta.requiresAuth && !isLoggedIn) {
		authStore.returnUrl = to.fullPath;
		next({ name: "Welcome" });
	} else if (to.meta.requiresAdmin && !isAdmin) {
		next({ name: "MyBookings" });
	} else if (
		!to.meta.requiresAuth &&
		isLoggedIn &&
		to.path.startsWith("/auth")
	) {
		next({ name: "MyBookings" });
	} else {
		next();
	}
});

export default router;
