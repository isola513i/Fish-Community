<script setup>
import { ref } from 'vue';
import { useRoomStore } from '../../stores/roomStore';
import { RouterLink } from 'vue-router';
import { ArrowLeftIcon, PlusIcon, XMarkIcon } from '@heroicons/vue/24/outline';
import CapacityStepper from '../../components/CapacityStepper.vue';

const roomStore = useRoomStore();

const name = ref('');
const capacity = ref(10);
const location = ref('');
const errorMessage = ref(null);

const equipments = ref({}); // e.g., { TV: '1', Whiteboard: 'true' }
const newEquipKey = ref(''); // state สำหรับ input 'Key'
const newEquipValue = ref(''); // state สำหรับ input 'Value'

// ⭐️ 2. ฟังก์ชันสำหรับเพิ่ม/ลบ Equipment ⭐️
const addEquipment = () => {
  if (newEquipKey.value && newEquipValue.value) {
    equipments.value[newEquipKey.value] = newEquipValue.value;
    // เคลียร์ input
    newEquipKey.value = '';
    newEquipValue.value = '';
  }
};

const removeEquipment = (key) => {
  delete equipments.value[key];
};
// ------------------------------------

const handleSubmit = async () => {
  errorMessage.value = null;
  try {
    const roomData = {
      name: name.value,
      capacity: capacity.value,
      location: location.value,
      equipmentsJson: JSON.stringify(equipments.value),
    };
    await roomStore.createRoom(roomData);
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'สร้างห้องไม่สำเร็จ';
  }
};
</script>

<template>
  <div class="p-4 space-y-6">
    <div class="relative flex items-center justify-center">
      <RouterLink
        to="/admin/rooms"
        class="absolute left-0 p-2 rounded-full bg-soft-bg shadow-neumorphism hover:shadow-neumorphism-inset"
      >
        <ArrowLeftIcon class="w-6 h-6 text-gray-700" />
      </RouterLink>
      <h1 class="text-xl font-bold text-gray-900">Create New Room</h1>
    </div>

    <div
      v-if="errorMessage"
      class="p-3 text-sm text-red-700 bg-red-100 rounded-md"
    >
      {{ errorMessage }}
    </div>

    <div class="p-6 bg-soft-bg rounded-2xl shadow-neumorphism">
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label for="name" class="block text-sm font-medium text-gray-700"
            >Room Name</label
          >
          <input
            v-model="name"
            type="text"
            id="name"
            required
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div>
          <label
            class="block text-sm font-medium text-gray-700 text-center mb-2"
            >Capacity</label
          >
          <CapacityStepper v-model="capacity" />
        </div>

        <div>
          <label for="location" class="block text-sm font-medium text-gray-700"
            >Location (Optional)</label
          >
          <input
            v-model="location"
            type="text"
            id="location"
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700"
            >Equipments</label
          >

          <div v.if="Object.keys(equipments).length > 0" class="space-y-2 mt-2">
            <div
              v-for="(value, key) in equipments"
              :key="key"
              class="flex items-center justify-between p-2 text-sm bg-white rounded-md shadow-sm"
            >
              <span>
                <span class="font-semibold">{{ key }}</span
                >:
                <span>{{ value }}</span>
              </span>
              <button
                @click="removeEquipment(key)"
                type="button"
                class="text-red-500 hover:text-red-700"
              >
                <XMarkIcon class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="flex items-center gap-2 mt-2">
            <input
              v-model="newEquipKey"
              type="text"
              placeholder="e.g., TV"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-white"
            />
            <input
              v-model="newEquipValue"
              type="text"
              placeholder="e.g., true or 1"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-white"
            />
            <button
              @click="addEquipment"
              type="button"
              class="p-2 text-white bg-blue-600 rounded-md hover:bg-blue-700"
            >
              <PlusIcon class="w-5 h-5" />
            </button>
          </div>
        </div>
        <button
          type="submit"
          class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
        >
          Create Room
        </button>
      </form>
    </div>
  </div>
</template>
