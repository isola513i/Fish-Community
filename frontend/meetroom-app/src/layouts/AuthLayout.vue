<script setup>
import { RouterView } from "vue-router";
</script>

<template>
	<div class="flex flex-col min-h-screen bg-white">
		<div class="h-64 w-full bg-black"></div>

		<div
			class="flex-grow w-full max-w-md mx-auto bg-white -mt-16 rounded-t-3xl shadow-2xl"
		>
			<div class="p-8 pt-10">
				<RouterView />
			</div>
		</div>
	</div>
</template>
