<script setup>
import { ref, onMounted } from 'vue';
import { useRoomStore } from '../../stores/roomStore';
import { RouterLink } from 'vue-router';
import { ArrowLeftIcon, PlusIcon, XMarkIcon } from '@heroicons/vue/24/outline';
import CapacityStepper from '../../components/CapacityStepper.vue'; // ⬅️ 1. Import Stepper

// 2. รับ props id จาก router
const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

const roomStore = useRoomStore();
const isLoading = ref(true);

// State สำหรับฟอร์ม
const name = ref('');
const capacity = ref(1); // ⬅️ ใช้ Stepper
const location = ref('');
const equipments = ref({});
const isActive = ref(true);

const newEquipKey = ref('');
const newEquipValue = ref('');
const errorMessage = ref(null);

// 3. ดึงข้อมูลห้องเดิมมาใส่ฟอร์ม
onMounted(async () => {
  const roomData = await roomStore.fetchRoom(props.id);
  if (roomData) {
    name.value = roomData.name;
    capacity.value = roomData.capacity;
    location.value = roomData.location || '';
    // แปลง JSON string กลับเป็น object
    equipments.value = JSON.parse(roomData.equipmentsJson || '{}');
    isActive.value = roomData.isActive;
  }
  isLoading.value = false;
});

// (ฟังก์ชัน add/remove Equipment เหมือนเดิม)
const addEquipment = () => {
  if (newEquipKey.value && newEquipValue.value) {
    equipments.value[newEquipKey.value] = newEquipValue.value;
    newEquipKey.value = '';
    newEquipValue.value = '';
  }
};
const removeEquipment = (key) => {
  delete equipments.value[key];
};

// 4. Submit handler (เรียก updateRoom)
const handleSubmit = async () => {
  errorMessage.value = null;
  try {
    const roomData = {
      name: name.value,
      capacity: capacity.value,
      location: location.value,
      equipmentsJson: JSON.stringify(equipments.value),
      isActive: isActive.value, // ⬅️ ส่งค่า isActive
    };

    await roomStore.updateRoom(props.id, roomData);
    // store จะ redirect กลับไปเอง
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'อัปเดตห้องไม่สำเร็จ';
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
      <h1 class="text-xl font-bold text-gray-900">Edit Room</h1>
    </div>

    <div v-if="isLoading" class="text-center">Loading room data...</div>

    <div v-else class="p-6 bg-soft-bg rounded-2xl shadow-neumorphism">
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
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white"
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
            >Location</label
          >
          <input
            v-model="location"
            type="text"
            id="location"
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm bg-white"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700"
            >Equipments</label
          >
          <div v-if="Object.keys(equipments).length > 0" class="space-y-2 mt-2">
            <div
              v-for="(value, key) in equipments"
              :key="key"
              class="flex items-center justify-between p-2 text-sm bg-white rounded-md shadow-sm"
            >
              <span
                ><span class="font-semibold">{{ key }}</span
                >: <span>{{ value }}</span></span
              >
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

        <div class="flex items-center justify-between">
          <label for="isActive" class="block text-sm font-medium text-gray-700"
            >Room Status</label
          >
          <button
            @click="isActive = !isActive"
            type="button"
            :class="[
              'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none',
              isActive ? 'bg-blue-600' : 'bg-gray-300',
            ]"
          >
            <span
              :class="[
                'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
                isActive ? 'translate-x-5' : 'translate-x-0',
              ]"
            ></span>
          </button>
        </div>

        <button
          type="submit"
          class="w-full px-4 py-3 font-medium text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700"
        >
          Update Room
        </button>
      </form>
    </div>
  </div>
</template>
