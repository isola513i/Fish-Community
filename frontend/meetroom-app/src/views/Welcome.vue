<script setup>
import { RouterLink } from "vue-router";
import AppLogo from "../assets/logo.png";
</script>

<template>
	<div
		class="fixed inset-0 z-50 flex flex-col justify-end p-6 pb-8 bg-white overflow-hidden"
		style="touch-action: none"
	>
		<div class="flex flex-col items-start max-w-2xl">
			<img :src="AppLogo" alt="MeetRoom Logo" class="h-32 w-auto " />
			<h1 class="text-5xl font-bold text-gray-800 tracking-tight mb-4">
				MeetRoom
			</h1>
			<p class="text-xl text-gray-600 mb-8 max-w-lg text-left">
				Your seamless meeting room booking experience.
			</p>

			<RouterLink
				:to="{ name: 'Login' }"
				class="w-full max-w-md px-10 py-4 text-lg font-medium text-white bg-gray-900 rounded-full shadow-lg hover:bg-gray-700 transition duration-300 ease-in-out text-center"
			>
				Get Started
			</RouterLink>
		</div>
	</div>
</template>
