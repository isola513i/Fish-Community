<script setup>
const props = defineProps({
  modelValue: {
    type: Number,
    required: true,
  },
});
const emit = defineEmits(['update:modelValue']);

const increment = () => {
  emit('update:modelValue', props.modelValue + 1);
};

const decrement = () => {
  if (props.modelValue > 1) {
    emit('update:modelValue', props.modelValue - 1);
  }
};
</script>

<template>
  <div class="flex items-center justify-center gap-4">
    <button
      @click="decrement"
      type="button"
      class="flex items-center justify-center w-12 h-12 text-blue-600 rounded-full bg-soft-bg shadow-neumorphism hover:shadow-neumorphism-inset"
      :disabled="modelValue <= 1"
    >
      <span class="text-3xl font-light">-</span>
    </button>

    <span class="text-3xl font-bold text-gray-900 w-16 text-center">
      {{ modelValue }}
    </span>

    <button
      @click="increment"
      type="button"
      class="flex items-center justify-center w-12 h-12 text-blue-600 rounded-full bg-soft-bg shadow-neumorphism hover:shadow-neumorphism-inset"
    >
      <span class="text-3xl font-light">+</span>
    </button>
  </div>
</template>
